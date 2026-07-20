import { type ReactNode, useEffect, useMemo, useState } from "react";
import { ChatContext } from "@/context/ChatContext.tsx";
import useChats from "@/hooks/useChats.ts";

export function ChatProvider({ children }: { children: ReactNode }) {
  const { chats, isLoading, createChat, isCreating } = useChats();
  const [preferredChatId, setPreferredChatId] = useState<string | null>(null);

  const selectedChatId = useMemo(() => {
    if (preferredChatId && chats.some((chat) => chat.id === preferredChatId)) {
      return preferredChatId;
    }
    return chats[0]?.id ?? null;
  }, [chats, preferredChatId]);

  useEffect(() => {
    if (isLoading || isCreating || chats.length > 0) {
      return;
    }
    void createChat().then((chat) => setPreferredChatId(chat.id));
  }, [chats.length, createChat, isCreating, isLoading]);

  const value = useMemo(
    () => ({ selectedChatId, setSelectedChatId: setPreferredChatId }),
    [selectedChatId],
  );

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}
