import { MessageScrollerItem } from "@/components/ui/message-scroller.tsx";
import { Loader2 } from "lucide-react";

// Centered spinner shown in the message list while chat history is loading.
function ChatHistoryLoading() {
  return (
    <MessageScrollerItem messageId="history-loading">
      <div className="flex flex-1 items-center justify-center py-12 text-muted-foreground">
        <Loader2 className="h-4 w-4 animate-spin" />
      </div>
    </MessageScrollerItem>
  );
}

export default ChatHistoryLoading;
