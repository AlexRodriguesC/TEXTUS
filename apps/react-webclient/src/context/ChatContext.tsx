import { createContext } from "react";

export type ChatContextValue = {
  selectedChatId: string | null;
  setSelectedChatId: (chatId: string | null) => void;
};

export const ChatContext = createContext<ChatContextValue | null>(null);
