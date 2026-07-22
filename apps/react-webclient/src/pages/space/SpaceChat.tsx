import ChatInput from "@/components/ChatInput.tsx";
import useChatContext from "@/hooks/useChatContext.ts";
import useChatMessages from "@/hooks/useChatMessages.ts";
import useChatStream from "@/hooks/useChatStream.ts";
import useChats from "@/hooks/useChats.ts";
import useModels from "@/hooks/useModels.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type ChatMessage from "@/classes/ChatMessage.ts";
import ChatMessageClass from "@/classes/ChatMessage.ts";
import { Avatar, AvatarFallback } from "@/components/ui/avatar.tsx";
import { Bubble, BubbleContent } from "@/components/ui/bubble.tsx";
import { Marker, MarkerContent, MarkerIcon } from "@/components/ui/marker.tsx";
import {
  Message,
  MessageAvatar,
  MessageContent,
} from "@/components/ui/message.tsx";
import {
  MessageScroller,
  MessageScrollerButton,
  MessageScrollerContent,
  MessageScrollerItem,
  MessageScrollerProvider,
  MessageScrollerViewport,
} from "@/components/ui/message-scroller.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import LoadingScreen from "@/components/LoadingScreen.tsx";
import { Bot, Loader2, UserRound } from "lucide-react";
import { useCallback, useLayoutEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

type DisplayMessage = ChatMessage & { id: string };

type ActiveTurn = {
  userId: string;
  assistantId: string;
  userContent: string;
};

type SpaceChatConversationProps = {
  chatId: string;
  chatTitle?: string;
  models: { value: string; label: string }[];
  defaultModel?: string;
  sendMessage: ReturnType<typeof useChatStream>["sendMessage"];
  isStreaming: boolean;
  spaceId: number;
  invalidateChats: () => void;
};

const MARKDOWN_CLASSES =
  "prose prose-sm dark:prose-invert prose-p:my-1 prose-pre:my-2 rounded-xl";

function makeMessageKey(role: ChatMessage["role"], content: string): string {
  return `${role}\0${content}`;
}

function historyIncludesUserMessage(
  messages: ChatMessage[],
  content: string,
): boolean {
  return messages.some(
    (message) => message.role === "user" && message.content === content,
  );
}

function MessageAvatarIcon({ role }: { role: ChatMessage["role"] }) {
  return (
    <Avatar size="sm">
      <AvatarFallback>
        {role === "user" ? <UserRound /> : <Bot />}
      </AvatarFallback>
    </Avatar>
  );
}

function ThinkingMarker() {
  return (
    <Marker role="status" className="shimmer">
      <MarkerIcon>
        <Spinner />
      </MarkerIcon>
      <MarkerContent className="shimmer loading-dots"/>
    </Marker>
  );
}

function ChatMessageRow({
  message,
  isStreamingPlaceholder,
}: {
  message: DisplayMessage;
  isStreamingPlaceholder: boolean;
}) {
  if (isStreamingPlaceholder && !message.content) {
    return <ThinkingMarker />;
  }

  const isUser = message.role === "user";

  return (
    <Message align={isUser ? "end" : "start"}>
      <MessageAvatar>
        <MessageAvatarIcon role={message.role} />
      </MessageAvatar>
      <MessageContent>
        <Bubble variant={isUser ? "default" : "muted"}>
          <BubbleContent className={MARKDOWN_CLASSES}>
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {message.content}
            </ReactMarkdown>
          </BubbleContent>
        </Bubble>
      </MessageContent>
    </Message>
  );
}

function SpaceChatConversation({
  chatId,
  chatTitle,
  models,
  defaultModel,
  sendMessage,
  isStreaming,
  invalidateChats,
}: SpaceChatConversationProps) {
  const { t } = useTranslation();
  const { consumePendingMessage } = useChatContext();
  const [draftMessages, setDraftMessages] = useState<DisplayMessage[]>([]);
  const [activeTurn, setActiveTurn] = useState<ActiveTurn | null>(null);
  const [clientIdMap, setClientIdMap] = useState<Map<string, string>>(
    () => new Map(),
  );
  const submittingRef = useRef(false);

  const {
    messages: historyMessages,
    refetch,
    isLoading: isHistoryLoading,
  } = useChatMessages(chatId);

  const displayedMessages = useMemo(() => {
    const historyKeys = new Set(
      historyMessages.map((message) =>
        makeMessageKey(message.role, message.content),
      ),
    );

    const history = historyMessages.map((message, index) => ({
      ...message,
      id:
        clientIdMap.get(makeMessageKey(message.role, message.content)) ??
        `${message.timestamp}-${index}`,
    }));

    // Drop drafts that history already covers. Keep an in-flight empty
    // assistant placeholder even though history has no matching key yet.
    const pendingDrafts = draftMessages.filter((draft) => {
      if (!draft.content && draft.role === "assistant") {
        return true;
      }
      return !historyKeys.has(makeMessageKey(draft.role, draft.content));
    });

    return [...history, ...pendingDrafts];
  }, [clientIdMap, draftMessages, historyMessages]);

  const showHistoryLoading =
    isHistoryLoading && displayedMessages.length === 0;
  const showEmptyState =
    displayedMessages.length === 0 && !isHistoryLoading && !isStreaming;

  const handleSubmit = useCallback(
    async (message: string, model?: string) => {
      if (isStreaming || submittingRef.current) {
        return;
      }

      submittingRef.current = true;

      const userId = crypto.randomUUID();
      const assistantId = crypto.randomUUID();
      const userKey = makeMessageKey("user", message);

      setActiveTurn({ userId, assistantId, userContent: message });
      setClientIdMap((current) => {
        const next = new Map(current);
        next.set(userKey, userId);
        return next;
      });

      const userMessage: DisplayMessage = Object.assign(
        new ChatMessageClass("user", message, new Date().toISOString()),
        { id: userId },
      );
      const assistantPlaceholder: DisplayMessage = Object.assign(
        new ChatMessageClass("assistant", "", new Date().toISOString()),
        { id: assistantId },
      );

      setDraftMessages((current) => [
        ...current,
        userMessage,
        assistantPlaceholder,
      ]);

      try {
        await sendMessage(chatId, message, model, (token) => {
          setDraftMessages((current) =>
            current.map((draft) =>
              draft.id === assistantId
                ? Object.assign(
                    new ChatMessageClass(
                      "assistant",
                      draft.content + token,
                      draft.timestamp,
                    ),
                    { id: assistantId },
                  )
                : draft,
            ),
          );
        });

        const result = await refetch();
        invalidateChats();

        const refreshedMessages = result.data ?? [];
        if (historyIncludesUserMessage(refreshedMessages, message)) {
          const userIndex = refreshedMessages.findLastIndex(
            (entry) => entry.role === "user" && entry.content === message,
          );
          const assistantMessage =
            userIndex >= 0
              ? refreshedMessages
                  .slice(userIndex + 1)
                  .find((entry) => entry.role === "assistant")
              : undefined;

          setClientIdMap((current) => {
            const next = new Map(current);
            next.set(userKey, userId);
            if (assistantMessage) {
              next.set(
                makeMessageKey("assistant", assistantMessage.content),
                assistantId,
              );
            }
            return next;
          });

          // Clear this turn's drafts by id so the same MessageScrollerItem
          // keys survive into history (via clientIdMap), with no remount.
          setDraftMessages((current) =>
            current.filter(
              (draft) => draft.id !== userId && draft.id !== assistantId,
            ),
          );
          setActiveTurn(null);
        }
      } catch {
        setDraftMessages((current) =>
          current.filter(
            (draft) => !(draft.id === assistantId && !draft.content),
          ),
        );
        setActiveTurn(null);
      } finally {
        submittingRef.current = false;
      }
    },
    [chatId, isStreaming, invalidateChats, refetch, sendMessage],
  );

  useLayoutEffect(() => {
    if (isStreaming) {
      return;
    }

    const message = consumePendingMessage(chatId);
    if (message) {
      void handleSubmit(message);
    }
  }, [chatId, consumePendingMessage, handleSubmit, isStreaming]);

  return (
    <div className="flex flex-col flex-1 min-h-0 items-center bg-transparent bg-none">
      {/*
        Stick to the live edge. Turn-anchoring (scrollAnchor + last-anchor)
        intentionally jumps the latest user message to the top of the viewport;
        that is the abrupt "scroll up" users hit on send / stream end.
      */}
      <MessageScrollerProvider autoScroll defaultScrollPosition="end">
        <div className="relative flex flex-1 min-h-0 w-full max-w-3xl">
          <MessageScroller>
            <MessageScrollerViewport>
              <MessageScrollerContent
                aria-busy={isStreaming}
                className="gap-4 pt-3"
              >
                {showHistoryLoading && (
                  <MessageScrollerItem messageId="history-loading">
                    <div className="flex flex-1 items-center justify-center py-12 text-muted-foreground">
                      <Loader2 className="h-4 w-4 animate-spin" />
                    </div>
                  </MessageScrollerItem>
                )}
                {showEmptyState && (
                  <MessageScrollerItem messageId="empty">
                    <div className="flex flex-1 items-center justify-center py-12 text-sm text-muted-foreground text-center px-6">
                      {t("chat.empty.messages", {
                        title: chatTitle ?? t("chat.new"),
                      })}
                    </div>
                  </MessageScrollerItem>
                )}
                {displayedMessages.map((message) => (
                  <MessageScrollerItem
                    key={message.id}
                    messageId={message.id}
                  >
                    <ChatMessageRow
                      message={message}
                      isStreamingPlaceholder={
                        activeTurn?.assistantId === message.id && isStreaming
                      }
                    />
                  </MessageScrollerItem>
                ))}
              </MessageScrollerContent>
            </MessageScrollerViewport>
            <MessageScrollerButton />
          </MessageScroller>
        </div>
      </MessageScrollerProvider>
      <div className="w-full flex items-center justify-center relative">
        <div className="flex p-3 w-full shrink-0 place-content-center">
          <ChatInput
            onSubmit={(message, model) => void handleSubmit(message, model)}
            models={models}
            defaultModel={defaultModel}
            disabled={isStreaming}
          />
        </div>
      </div>
    </div>
  );
}

function SpaceChat() {
  const { t } = useTranslation();
  const { spaceId } = useSpaceId();
  const { chats, isLoading: isChatsLoading, invalidateChats } = useChats();
  const { models, defaultModel, isLoading: isModelsLoading } = useModels();
  const { sendMessage, isStreaming } = useChatStream();
  const { selectedChatId } = useChatContext();

  const isInitialLoading = isChatsLoading || isModelsLoading;
  const selectedChat = chats.find((chat) => chat.id === selectedChatId);

  return (
    <div className="flex w-full h-full overflow-hidden">
      <div className="flex flex-col flex-1 min-h-0 items-center overflow-hidden">
        <div className="w-full h-full md:max-w-[80%] lg:max-w-[60%] flex flex-col min-h-0 overflow-hidden">
          {isInitialLoading ? (
            <div className="flex flex-1 items-center justify-center text-muted-foreground gap-2">
              <LoadingScreen />
            </div>
          ) : selectedChatId ? (
            <SpaceChatConversation
              key={selectedChatId}
              chatId={selectedChatId}
              chatTitle={selectedChat?.title}
              models={models}
              defaultModel={defaultModel}
              sendMessage={sendMessage}
              isStreaming={isStreaming}
              spaceId={spaceId}
              invalidateChats={invalidateChats}
            />
          ) : (
            <>
              <div className="flex flex-1 min-h-0 items-center justify-center text-sm text-muted-foreground text-center px-6">
                {t("chat.empty.messages", { title: t("chat.new") })}
              </div>
              <div className="flex p-3 w-full shrink-0 place-content-center">
                <ChatInput
                  onSubmit={() => undefined}
                  models={models}
                  defaultModel={defaultModel}
                  disabled
                />
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default SpaceChat;
