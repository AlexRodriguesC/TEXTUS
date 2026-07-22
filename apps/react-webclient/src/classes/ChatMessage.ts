export type ChatMessageRole = "user" | "assistant" | "system";

class ChatMessage {
  role: ChatMessageRole;
  content: string;
  timestamp: string;

  constructor(role: ChatMessageRole, content: string, timestamp: string) {
    this.role = role;
    this.content = content;
    this.timestamp = timestamp;
  }
}

export default ChatMessage;
