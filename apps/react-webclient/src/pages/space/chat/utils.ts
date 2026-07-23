import type ChatMessage from "@/classes/ChatMessage.ts";

export function makeMessageKey(
  role: ChatMessage["role"],
  content: string,
): string {
  return `${role}\0${content}`;
}

export function historyIncludesUserMessage(
  messages: ChatMessage[],
  content: string,
): boolean {
  return messages.some(
    (message) => message.role === "user" && message.content === content,
  );
}
