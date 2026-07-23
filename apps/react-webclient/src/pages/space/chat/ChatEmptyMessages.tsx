import { MessageScrollerItem } from "@/components/ui/message-scroller.tsx";
import { useTranslation } from "react-i18next";

// Empty-state text shown when a conversation has no messages yet.
function ChatEmptyMessages({
  title,
  asScrollerItem = false,
}: {
  title: string;
  asScrollerItem?: boolean;
}) {
  const { t } = useTranslation();
  const text = t("chat.empty.messages", { title });

  if (asScrollerItem) {
    return (
      <MessageScrollerItem messageId="empty">
        <div className="flex flex-1 items-center justify-center py-12 text-sm text-muted-foreground text-center px-6">
          {text}
        </div>
      </MessageScrollerItem>
    );
  }

  return (
    <div className="flex flex-1 min-h-0 items-center justify-center text-sm text-muted-foreground text-center px-6">
      {text}
    </div>
  );
}

export default ChatEmptyMessages;
