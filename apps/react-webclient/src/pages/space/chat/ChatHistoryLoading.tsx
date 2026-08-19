import { MessageScrollerItem } from "@/components/ui/message-scroller.tsx";
import ChatHistorySkeleton from "@/pages/space/chat/ChatHistorySkeleton.tsx";

function ChatHistoryLoading() {
  return (
    <MessageScrollerItem messageId="history-loading">
      <ChatHistorySkeleton />
    </MessageScrollerItem>
  );
}

export default ChatHistoryLoading;
