import ChatEmptyMessages from "@/pages/space/chat/ChatEmptyMessages.tsx";
import ChatHistoryLoading from "@/pages/space/chat/ChatHistoryLoading.tsx";
import ChatInputFooter from "@/pages/space/chat/ChatInputFooter.tsx";
import ChatMessageRow from "@/pages/space/chat/ChatMessageRow.tsx";
import type { SpaceChatConversationProps } from "@/pages/space/chat/types.ts";
import useChatConversation from "@/hooks/useChatConversation.ts";
import {
  MessageScroller,
  MessageScrollerButton,
  MessageScrollerContent,
  MessageScrollerItem,
  MessageScrollerProvider,
  MessageScrollerViewport,
} from "@/components/ui/message-scroller.tsx";
import { useTranslation } from "react-i18next";

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
  const {
    displayedMessages,
    activeTurn,
    showHistoryLoading,
    showEmptyState,
    handleSubmit,
  } = useChatConversation({
    chatId,
    sendMessage,
    isStreaming,
    invalidateChats,
  });

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
                    onSuggestionClick={({ message, topicIds }) =>
                      void handleSubmit(message, undefined, topicIds)
                    }
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
