import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import { Button } from "@/components/ui/button.tsx";
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  useSidebar,
} from "@/components/ui/sidebar.tsx";
import useChatContext from "@/hooks/useChatContext.ts";
import useChats from "@/hooks/useChats.ts";
import { cn } from "@/lib/utils.ts";
import PathConstants from "@/routing/pathConstants.ts";
import { useRouterState } from "@tanstack/react-router";
import { Loader2, Plus } from "lucide-react";
import { useCallback } from "react";
import { useTranslation } from "react-i18next";

function ChatHistorySidebar() {
  const { t } = useTranslation();
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  const isChatRoute = PathConstants.SPACE_CHAT.startsWith("/spaces/")
    ? currentPath.match(/^\/spaces\/[^/]+\/chat$/)
    : false;

  const { selectedChatId, setSelectedChatId } = useChatContext();
  const { chats, isLoading, createChat, deleteChat, isCreating } = useChats();

  const handleCreateChat = useCallback(async () => {
    const chat = await createChat();
    setSelectedChatId(chat.id);
  }, [createChat, setSelectedChatId]);

  const handleDeleteChat = useCallback(
    async (chatId: string) => {
      await deleteChat(chatId);
      if (selectedChatId === chatId) {
        setSelectedChatId(null);
      }
    },
    [deleteChat, selectedChatId, setSelectedChatId],
  );

  if (!isChatRoute || !open) {
    return null;
  }

  return (
    <SidebarGroup className="flex min-h-0 flex-1 flex-col p-2">
      <SidebarGroupLabel>{t("chat.history")}</SidebarGroupLabel>
      <SidebarGroupContent className="flex shrink-0 flex-col gap-2">
        <Button
          className="w-full justify-start gap-2"
          variant="outline"
          onClick={() => void handleCreateChat()}
          disabled={isCreating}
        >
          {isCreating ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Plus className="h-4 w-4" />
          )}
          {t("chat.new")}
        </Button>
      </SidebarGroupContent>
      <SidebarGroupContent className="min-h-0 flex-1 overflow-y-auto">
        <div className="space-y-1">
          {chats.map((chat) => (
            <div
              key={chat.id}
              className={cn(
                "group flex items-center gap-1 rounded-none px-2 py-2 text-sm cursor-pointer hover:bg-muted",
                selectedChatId === chat.id && "bg-muted",
              )}
            >
              <button
                type="button"
                className="flex-1 text-left truncate"
                onClick={() => setSelectedChatId(chat.id)}
              >
                {chat.title}
              </button>
              <DestructiveActionButton
                title={t("chat.delete.title")}
                description={t("chat.delete.description")}
                onConfirm={() => void handleDeleteChat(chat.id)}
              />
            </div>
          ))}
          {!isLoading && chats.length === 0 && (
            <p className="px-2 py-4 text-sm text-muted-foreground">{t("chat.empty.list")}</p>
          )}
        </div>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

export default ChatHistorySidebar;
