import ChatHistoryRow from "@/components/right-sidebar/chat/ChatHistoryRow.tsx";
import { useTranslation } from "react-i18next";

interface ChatListItem {
  id: string;
  title: string;
}

interface ChatHistoryListProps {
  chats: ChatListItem[];
  isLoading: boolean;
  selectedChatId: string | null;
  onSelect: (chatId: string) => void;
  onDelete: (chatId: string) => void;
}

function ChatHistoryList({
  chats,
  isLoading,
  selectedChatId,
  onSelect,
  onDelete,
}: ChatHistoryListProps) {
  const { t } = useTranslation();

  return (
    <div className="space-y-1">
      {chats.map((chat) => (
        <ChatHistoryRow
          key={chat.id}
          chatId={chat.id}
          title={chat.title}
          isSelected={selectedChatId === chat.id}
          onSelect={() => onSelect(chat.id)}
          onDelete={() => onDelete(chat.id)}
        />
      ))}
      {!isLoading && chats.length === 0 && (
        <p className="px-2 py-4 text-sm text-muted-foreground">
          {t("chat.empty.list")}
        </p>
      )}
    </div>
  );
}

export default ChatHistoryList;
