export type ChatMessageRole = "user" | "assistant" | "system";

class ChatMessage {
  role: ChatMessageRole;
  content: string;
  timestamp: string;
  topics: number[];

  constructor(role: ChatMessageRole, content: string, timestamp: string, topics: number[]) {
    this.role = role;
    this.content = content;
    this.timestamp = timestamp;
    this.topics = topics;
  }
}

export default ChatMessage;
