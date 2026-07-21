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
import {Plus} from "lucide-react";
import {useCallback, useState} from "react";
import { useTranslation } from "react-i18next";
import {Spinner} from "@/components/ui/spinner.tsx";
import {Dialog, DialogClose, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog.tsx";
import {Input} from "@/components/ui/input.tsx";
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
    const [title, setTitle] = useState("");

  const handleCreateChat = useCallback(async (title?: string) => {
    const chat = await createChat(title);
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
      <SidebarGroupLabel className="flex items-center justify-between w-full px-4 pl-1 py-3">
          {t("chat.history")}
          <Dialog>
              <DialogTrigger asChild>
                  <Button
                      className={"w-fit p-0"}
                      variant="ghost"
                      disabled={isCreating}
                  >
                      {isCreating ? (
                          <Spinner className="h-4 w-4" />
                      ) : (
                          <span className="flex items-center justify-center rounded-full bg-background/60 h-7 w-7 shrink-0">
                    <Plus className="h-4 w-4" />
                </span>
                      )}
                  </Button>
              </DialogTrigger>
              <DialogContent>
                  <DialogHeader>
                      <DialogTitle>{t("chat.create.title")}</DialogTitle>
                  </DialogHeader>
                  <Input
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      placeholder={t("chat.create.placeholder")}
                  />
                  <DialogFooter className="flex flex-col w-full md:flex-row md:justify-between">
                      <DialogClose asChild>
                          <Button variant="ghost">{t("chat.cancel")}</Button>
                      </DialogClose>
                      <DialogClose asChild>
                          <Button
                              disabled={isCreating || !title.trim()}
                              onClick={() => void handleCreateChat(title)}
                          >
                              {t("chat.create.confirm")}
                          </Button>
                      </DialogClose>
                  </DialogFooter>
              </DialogContent>
          </Dialog>
      </SidebarGroupLabel>
        <SidebarGroupContent className="min-h-0 flex-1 overflow-y-auto pr-4">
            <div className="space-y-1">
                {chats.map((chat) => (
                    <div
                        key={chat.id}
                        className={cn(
                            "group flex items-center gap-1 rounded-none text-sm cursor-pointer hover:bg-muted",
                            selectedChatId === chat.id && "bg-muted",
                        )}
                    >
                        <Button
                            variant="secondary"
                            className="flex-1 justify-start text-left truncate bg-transparent hover:bg-transparent px-1"
                            onClick={() => setSelectedChatId(chat.id)}
                        >
                            {chat.title}
                        </Button>
                        <span className="flex items-center justify-center h-7 w-7 shrink-0">
          <DestructiveActionButton
              title={t("chat.delete.title")}
              description={t("chat.delete.description")}
              onConfirm={() => void handleDeleteChat(chat.id)}
          />
        </span>
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
