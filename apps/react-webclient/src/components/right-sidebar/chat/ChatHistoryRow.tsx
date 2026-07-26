import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import { Button } from "@/components/ui/button.tsx";
import { cn } from "@/lib/utils.ts";
import { useTranslation } from "react-i18next";

interface ChatHistoryRowProps {
  chatId: string;
  title: string;
  isSelected: boolean;
  onSelect: () => void;
  onDelete: () => void;
}

function ChatHistoryRow({
  title,
  isSelected,
  onSelect,
  onDelete,
}: ChatHistoryRowProps) {
  const { t } = useTranslation();

  return (
    <div
      className={cn(
        "group/chat-row flex items-center gap-1 rounded-none text-sm cursor-pointer hover:bg-muted",
        isSelected && "bg-muted",
      )}
    >
      <Button
        variant="secondary"
        className="flex-1 justify-start text-left truncate bg-transparent hover:bg-transparent px-1 normal-case font-normal"
        onClick={onSelect}
      >
        {title}
      </Button>
      <span className="flex items-center justify-center h-7 w-7 shrink-0 opacity-0 transition-opacity group-hover/chat-row:opacity-100">
        <DestructiveActionButton
          title={t("chat.delete.title")}
          description={t("chat.delete.description")}
          onConfirm={onDelete}
        />
      </span>
    </div>
  );
}

export default ChatHistoryRow;
