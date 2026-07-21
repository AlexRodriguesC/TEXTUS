import { createContext } from "react";

export type PendingChatMessage = {
  chatId: string;
  message: string;
};

export type ChatContextValue = {
  selectedChatId: string | null;
  setSelectedChatId: (chatId: string | null) => void;
  pendingMessage: PendingChatMessage | null;
  setPendingMessage: (message: PendingChatMessage | null) => void;
  consumePendingMessage: (chatId: string) => string | null;
};

export const ChatContext = createContext<ChatContextValue | null>(null);
