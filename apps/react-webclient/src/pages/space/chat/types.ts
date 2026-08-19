import type ChatMessage from "@/classes/ChatMessage.ts";
import useChatStream from "@/hooks/useChatStream.ts";

export type DisplayMessage = ChatMessage & { id: string };

export type ActiveTurn = {
  userId: string;
  assistantId: string;
  userContent: string;
};

export type SpaceChatConversationProps = {
  chatId: string;
  chatTitle?: string;
  models: { value: string; label: string }[];
  defaultModel?: string;
  sendMessage: ReturnType<typeof useChatStream>["sendMessage"];
  isStreaming: boolean;
  invalidateChats: () => void;
};
