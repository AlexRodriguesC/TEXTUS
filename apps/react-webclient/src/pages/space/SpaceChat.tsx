import ChatInput from "@/components/ChatInput.tsx";
import useChatContext from "@/hooks/useChatContext.ts";
import useChatMessages from "@/hooks/useChatMessages.ts";
import useChatStream from "@/hooks/useChatStream.ts";
import { getListChatsQueryKey } from "@/client/generated/chats/chats.ts";
import useChats from "@/hooks/useChats.ts";
import useModels from "@/hooks/useModels.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type { ChatMessageDTO } from "@/types/chat.ts";
import { useQueryClient } from "@tanstack/react-query";
import {ArrowDown, ArrowUp, Loader2} from "lucide-react";
import { useCallback, useLayoutEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import LoadingScreen from "@/components/LoadingScreen.tsx";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {Button} from "@/components/ui/button.tsx";

type DisplayMessage = ChatMessageDTO & { id: string };

type SpaceChatConversationProps = {
  chatId: string;
  chatTitle?: string;
  models: { value: string; label: string }[];
  defaultModel?: string;
  sendMessage: ReturnType<typeof useChatStream>["sendMessage"];
  isStreaming: boolean;
  spaceId: number;
};

function historyIncludesUserMessage(
  messages: ChatMessageDTO[],
  content: string,
): boolean {
  return messages.some(
    (message) => message.role === "user" && message.content === content,
  );
}

function SpaceChatConversation({
  chatId,
  chatTitle,
  models,
  defaultModel,
  sendMessage,
  isStreaming,
  spaceId,
}: SpaceChatConversationProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { consumePendingMessage } = useChatContext();
  const [draftMessages, setDraftMessages] = useState<DisplayMessage[]>([]);
  const [streamingAssistant, setStreamingAssistant] = useState("");
  const submittingRef = useRef(false);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [isAtBottom, setIsAtBottom] = useState(true);

  const {
    messages: historyMessages,
    refetch,
    isLoading: isHistoryLoading,
  } = useChatMessages(chatId);

  const displayedMessages = useMemo(() => {
    const history = historyMessages.map((message, index) => ({
      ...message,
      id: `${message.timestamp}-${index}`,
    }));
    return [...history, ...draftMessages];
  }, [draftMessages, historyMessages]);

  const messagesContainerRef = useCallback((el: HTMLDivElement | null) => {
    containerRef.current = el;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }, []);

  const handleScrollDown = useCallback(() => {
    containerRef.current?.scrollTo({
      top: containerRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, []);

  const handleScrollUp = useCallback(() => {
    containerRef.current?.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }, [])

  const handleScroll = useCallback(() => {
    const el = containerRef.current;
    if (!el) return;
    const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    setIsAtBottom(distanceFromBottom < 10);
  }, []);

  useLayoutEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
    handleScroll();
  }, [displayedMessages.length, streamingAssistant, handleScroll]);

  const handleSubmit = useCallback(
    async (message: string, model?: string) => {
      if (isStreaming || submittingRef.current) {
        return;
      }

      submittingRef.current = true;

      const userMessage: DisplayMessage = {
        id: `draft-user-${Date.now()}`,
        role: "user",
        content: message,
        timestamp: new Date().toISOString(),
      };

      setDraftMessages((current) => [...current, userMessage]);
      setStreamingAssistant("");

      try {
        await sendMessage(chatId, message, model, (token) => {
          setStreamingAssistant((current) => current + token);
        });
        const result = await refetch();
        await queryClient.invalidateQueries({
          queryKey: getListChatsQueryKey(spaceId),
        });

        const refreshedMessages = result.data?.messages ?? [];
        if (historyIncludesUserMessage(refreshedMessages, message)) {
          setDraftMessages([]);
          setStreamingAssistant("");
        }
      } finally {
        submittingRef.current = false;
      }
    },
    [chatId, isStreaming, queryClient, refetch, sendMessage, spaceId],
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
      <div
          ref={messagesContainerRef}
          onScroll={handleScroll}
          className="flex-1 min-h-0 w-full max-w-3xl pt-3 overflow-y-auto overscroll-behavior-contain flex flex-col gap-4 bg-transparent bg-none"
      >
        {isHistoryLoading &&
          displayedMessages.length === 0 &&
          !streamingAssistant && (
            <div className="flex flex-1 items-center justify-center text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
            </div>
          )}
        {displayedMessages.length === 0 &&
          !streamingAssistant &&
          !isHistoryLoading && (
            <div className="flex flex-1 items-center justify-center text-sm text-muted-foreground text-center px-6">
              {t("chat.empty.messages", { title: chatTitle ?? t("chat.new") })}
            </div>
          )}
        {displayedMessages.map((msg) => (
          <div
            key={msg.id}
            className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            <div
              className={`max-w-[85%] rounded-xl px-4 py-2 text-sm wrap-break-word prose prose-sm dark:prose-invert prose-p:my-1 prose-pre:my-2 ${
                msg.role === "user"
                  ? "bg-primary/90 text-primary-foreground"
                  : "bg-secondary/40 text-secondary-foreground"
              }`}
            >
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {msg.content}
              </ReactMarkdown>
            </div>
          </div>
        ))}
        {streamingAssistant && (
          <div className="flex justify-start">
            <div className="max-w-[85%] rounded-none px-4 py-2 text-sm wrap-break-word prose prose-sm dark:prose-invert prose-p:my-1 prose-pre:my-2">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {streamingAssistant}
              </ReactMarkdown>
            </div>
          </div>
        )}
        {isStreaming && !streamingAssistant && (
          <div className="flex justify-start">
            <div className="rounded-none px-4 py-2 bg-muted loading-dots">
            </div>
          </div>
        )}
      </div>
      <div className="w-full flex items-center justify-center relative">
        <Button
            variant="outline"
            className="absolute bottom-34 left-1/2 -translate-x-1/2 rounded-full px-3 py-1 w-fit bg-muted/80 hover:bg-muted/90"
            onClick={isAtBottom ? handleScrollUp : handleScrollDown}
        >
          {isAtBottom ? <ArrowUp /> : <ArrowDown />}
        </Button>
        <div className="flex p-3 w-full max-w-3xl shrink-0 place-content-center">
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
  const { chats, isLoading: isChatsLoading } = useChats();
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
