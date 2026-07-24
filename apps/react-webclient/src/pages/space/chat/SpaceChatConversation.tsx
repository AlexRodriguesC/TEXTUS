import ChatEmptyMessages from "@/pages/space/chat/ChatEmptyMessages.tsx";
import ChatHistoryLoading from "@/pages/space/chat/ChatHistoryLoading.tsx";
import ChatInputFooter from "@/pages/space/chat/ChatInputFooter.tsx";
import ChatMessageRow from "@/pages/space/chat/ChatMessageRow.tsx";
import type {
  ActiveTurn,
  DisplayMessage,
  SpaceChatConversationProps,
} from "@/pages/space/chat/types.ts";
import {
  historyIncludesUserMessage,
  makeMessageKey,
} from "@/pages/space/chat/utils.ts";
import ChatMessageClass from "@/classes/ChatMessage.ts";
import useChatContext from "@/hooks/context/useChatContext.ts";
import useChatMessages from "@/hooks/useChatMessages.ts";
import {
  MessageScroller,
  MessageScrollerButton,
  MessageScrollerContent,
  MessageScrollerItem,
  MessageScrollerProvider,
  MessageScrollerViewport,
} from "@/components/ui/message-scroller.tsx";
import { useCallback, useLayoutEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

// Active conversation view with message list, streaming drafts, and chat input.
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
      <MessageScrollerProvider autoScroll defaultScrollPosition="end">
        <div className="relative flex flex-1 min-h-0 w-full max-w-3xl">
          <MessageScroller>
            <MessageScrollerViewport>
              <MessageScrollerContent
                aria-busy={isStreaming}
                className="gap-4 pt-3"
              >
                {showHistoryLoading && <ChatHistoryLoading />}
                {showEmptyState && (
                  <ChatEmptyMessages
                    title={chatTitle ?? t("chat.new")}
                    asScrollerItem
                    onSuggestionClick={(message) => void handleSubmit(message)}
                    disabled={isStreaming}
                  />
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
      <ChatInputFooter
        onSubmit={(message, model) => void handleSubmit(message, model)}
        models={models}
        defaultModel={defaultModel}
        disabled={isStreaming}
      />
    </div>
  );
}

export default SpaceChatConversation;
