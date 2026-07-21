import { type ReactNode, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ChatContext, type PendingChatMessage } from "@/context/ChatContext.tsx";
import useChats from "@/hooks/useChats.ts";

export function ChatProvider({ children }: { children: ReactNode }) {
  const { chats, isLoading, createChat, isCreating } = useChats();
  const [preferredChatId, setPreferredChatId] = useState<string | null>(null);
  const [pendingMessage, setPendingMessageState] = useState<PendingChatMessage | null>(null);
  const pendingRef = useRef<PendingChatMessage | null>(null);

  const setPendingMessage = useCallback((message: PendingChatMessage | null) => {
    pendingRef.current = message;
    setPendingMessageState(message);
  }, []);

  const consumePendingMessage = useCallback((chatId: string): string | null => {
    const pending = pendingRef.current;
    if (!pending || pending.chatId !== chatId) {
      return null;
    }
    pendingRef.current = null;
    setPendingMessageState(null);
    return pending.message;
  }, []);

  const selectedChatId = useMemo(() => {
    if (preferredChatId) {
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
    () => ({
      selectedChatId,
      setSelectedChatId: setPreferredChatId,
      pendingMessage,
      setPendingMessage,
      consumePendingMessage,
    }),
    [selectedChatId, pendingMessage, setPendingMessage, consumePendingMessage],
  );

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}
