import { createContext } from "react";

export type PendingChatMessage = {
  chatId: string;
  message: string;
  topicIds?: number[];
};

export type ConsumedPendingMessage = {
  message: string;
  topicIds?: number[];
};

export type ChatContextValue = {
  selectedChatId: string | null;
  setSelectedChatId: (chatId: string | null) => void;
  pendingMessage: PendingChatMessage | null;
  setPendingMessage: (message: PendingChatMessage | null) => void;
  consumePendingMessage: (chatId: string) => ConsumedPendingMessage | null;
};

export const ChatContext = createContext<ChatContextValue | null>(null);
