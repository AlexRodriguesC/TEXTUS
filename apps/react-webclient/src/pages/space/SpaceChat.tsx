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
import { Loader2 } from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import LoadingScreen from "@/components/LoadingScreen.tsx";

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
  const [draftMessages, setDraftMessages] = useState<DisplayMessage[]>([]);
  const [streamingAssistant, setStreamingAssistant] = useState("");

  const { messages: historyMessages, refetch } = useChatMessages(chatId);

  const displayedMessages = useMemo(() => {
    const history = historyMessages.map((message, index) => ({
      ...message,
      id: `${message.timestamp}-${index}`,
    }));
    return [...history, ...draftMessages];
  }, [draftMessages, historyMessages]);

  const messagesContainerRef = useCallback(
    (el: HTMLDivElement | null) => {
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    },
    [],
  );

  const handleSubmit = useCallback(
    async (message: string, model?: string) => {
      if (isStreaming) {
        return;
      }

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
        await refetch();
        await queryClient.invalidateQueries({ queryKey: getListChatsQueryKey(spaceId) });
      } finally {
        setDraftMessages([]);
        setStreamingAssistant("");
      }
    },
    [chatId, isStreaming, queryClient, refetch, sendMessage, spaceId],
  );

  return (
    <>
      <div
        ref={messagesContainerRef}
        className="flex-1 min-h-0 pt-3 overflow-y-auto overscroll-behavior-contain flex flex-col"
      >
        {displayedMessages.length === 0 && !streamingAssistant && (
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
              className={`max-w-[85%] rounded-xl px-4 py-2 text-sm whitespace-pre-wrap wrap-break-word ${
                msg.role === "user"
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted"
              }`}
            >
              {msg.content}
            </div>
          </div>
        ))}
        {streamingAssistant && (
          <div className="flex justify-start">
            <div className="max-w-[85%] rounded-none px-4 py-2 text-sm whitespace-pre-wrap wrap-break-word bg-muted">
              {streamingAssistant}
            </div>
          </div>
        )}
        {isStreaming && !streamingAssistant && (
          <div className="flex justify-start">
            <div className="rounded-none px-4 py-2 bg-muted">
              <Loader2 className="h-4 w-4 animate-spin" />
            </div>
          </div>
        )}
      </div>

      <div className="flex p-3 w-full shrink-0 place-content-center">
        <ChatInput
          onSubmit={(message, model) => void handleSubmit(message, model)}
          models={models}
          defaultModel={defaultModel}
          disabled={isStreaming}
        />
      </div>
    </>
  );
}

function SpaceChat() {
  const { t } = useTranslation();
  const { spaceId } = useSpaceId();
  const { chats, isLoading: isChatsLoading } = useChats();
  const { models, defaultModel, isLoading: isModelsLoading } = useModels();
  const { sendMessage, isStreaming } = useChatStream();
  const { selectedChatId } = useChatContext();

  const { isLoading: isHistoryLoading } = useChatMessages(selectedChatId);

  const isLoading = isChatsLoading || isHistoryLoading || isModelsLoading;
  const selectedChat = chats.find((chat) => chat.id === selectedChatId);

  return (
    <div className="flex w-full h-full overflow-hidden">
      <div className="flex flex-col flex-1 min-h-0 items-center overflow-hidden">
        <div className="w-full h-full md:max-w-[80%] lg:max-w-[60%] flex flex-col min-h-0 overflow-hidden">
          {isLoading ? (
            <div className="flex flex-1 items-center justify-center text-muted-foreground gap-2">
              <LoadingScreen/>
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
