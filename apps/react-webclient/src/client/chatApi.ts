import { customInstance } from "@/client/mutator.ts";
import type {
  ChatDTO,
  ChatHistoryResponseDTO,
  ChatsResponseDTO,
  ModelsResponseDTO,
  SendMessageRequest,
} from "@/types/chat.ts";

type ApiResponse<T> = { data: T; status: number; headers: unknown };

export async function listChats(spaceId: number): Promise<ChatDTO[]> {
  const response = await customInstance<ApiResponse<ChatsResponseDTO>>(
    `/api/v1/spaces/${spaceId}/chats`,
  );
  return response.data.chats;
}

export async function createChat(spaceId: number, title?: string): Promise<ChatDTO> {
  const response = await customInstance<ApiResponse<ChatDTO>>(
    `/api/v1/spaces/${spaceId}/chats`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: title ? JSON.stringify({ title }) : undefined,
    },
  );
  return response.data;
}

export async function getChatHistory(
  spaceId: number,
  chatId: string,
): Promise<ChatHistoryResponseDTO> {
  const response = await customInstance<ApiResponse<ChatHistoryResponseDTO>>(
    `/api/v1/spaces/${spaceId}/chats/${chatId}`,
  );
  return response.data;
}

export async function deleteChat(spaceId: number, chatId: string): Promise<void> {
  await customInstance(`/api/v1/spaces/${spaceId}/chats/${chatId}`, {
    method: "DELETE",
  });
}

export async function listModels(): Promise<ModelsResponseDTO> {
  const response = await customInstance<ApiResponse<ModelsResponseDTO>>(
    "/api/v1/models",
  );
  return response.data;
}

export async function streamChatMessage(
  spaceId: number,
  chatId: string,
  request: SendMessageRequest,
  onToken: (token: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const token = localStorage.getItem("TOKEN");
  const baseUrl = import.meta.env.VITE_API_BASE_URL as string;

  const response = await fetch(
    `${baseUrl}/api/v1/spaces/${spaceId}/chats/${chatId}/messages`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "text/event-stream",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify(request),
      signal,
    },
  );

  if (!response.ok) {
    throw new Error(`Stream request failed with status ${response.status}`);
  }

  const reader = response.body?.getReader();
  if (!reader) {
    throw new Error("Streaming is not supported in this browser");
  }

  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    buffer = processSseBuffer(buffer, onToken);
  }

  processSseBuffer(buffer, onToken, true);
}

function processSseBuffer(
  buffer: string,
  onToken: (token: string) => void,
  flush = false,
): string {
  const events = buffer.split("\n\n");
  const remainder = flush ? "" : (events.pop() ?? "");

  for (const event of events) {
    const dataLines = event
      .split("\n")
      .filter((line) => line.startsWith("data:"))
      .map((line) => line.slice(5).trimStart());

    if (dataLines.length > 0) {
      onToken(dataLines.join("\n"));
      continue;
    }

    if (event.trim()) {
      onToken(event);
    }
  }

  return remainder;
}
