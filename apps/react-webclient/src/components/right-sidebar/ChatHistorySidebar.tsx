import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
} from "@/components/ui/sidebar.tsx";
import useChatContext from "@/hooks/context/useChatContext.ts";
import useChats from "@/hooks/useChats.ts";
import useChatRouteActive from "@/hooks/useChatRouteActive.ts";
import CreateChatDialog from "@/components/right-sidebar/chat/CreateChatDialog.tsx";
import ChatHistoryList from "@/components/right-sidebar/chat/ChatHistoryList.tsx";
import { useCallback } from "react";
import { useTranslation } from "react-i18next";

function ChatHistorySidebar() {
  const { t } = useTranslation();
  const isActive = useChatRouteActive();
  const { selectedChatId, setSelectedChatId } = useChatContext();
  const { chats, isLoading, createChat, deleteChat, isCreating } = useChats();

  const handleCreateChat = useCallback(
    async (title: string) => {
      const chat = await createChat(title);
      setSelectedChatId(chat.id);
    },
    [createChat, setSelectedChatId],
  );

  const handleDeleteChat = useCallback(
    async (chatId: string) => {
      await deleteChat(chatId);
      if (selectedChatId === chatId) {
        setSelectedChatId(null);
      }
    },
    [deleteChat, selectedChatId, setSelectedChatId],
  );

  if (!isActive) {
    return null;
  }

  return (
    <SidebarGroup className="flex min-h-0 flex-1 flex-col p-2">
      <SidebarGroupLabel className="flex items-center justify-between w-full px-4 pl-1 py-3">
        {t("chat.history")}
        <CreateChatDialog
          isCreating={isCreating}
          onCreate={(title) => void handleCreateChat(title)}
        />
      </SidebarGroupLabel>
      <SidebarGroupContent className="min-h-0 flex-1 overflow-y-auto pr-4">
        <ChatHistoryList
          chats={chats}
          isLoading={isLoading}
          selectedChatId={selectedChatId}
          onSelect={setSelectedChatId}
          onDelete={(chatId) => void handleDeleteChat(chatId)}
        />
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

export default ChatHistorySidebar;
