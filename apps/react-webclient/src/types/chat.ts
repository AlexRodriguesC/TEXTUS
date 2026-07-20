export interface ChatDTO {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatsResponseDTO {
  chats: ChatDTO[];
}

export interface ChatMessageDTO {
  role: "user" | "assistant" | "system";
  content: string;
  timestamp: string;
}

export interface ChatHistoryResponseDTO {
  messages: ChatMessageDTO[];
}

export interface ModelDTO {
  id: string;
  name: string;
}

export interface ModelsResponseDTO {
  models: ModelDTO[];
}

export interface SendMessageRequest {
  message: string;
  model?: string;
}
