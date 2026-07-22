import { customInstance } from "@/client/mutator.ts";

type ApiResponse<T> = { data: T; status: number; headers: unknown };

type ChatTransport = {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
};

type ChatsTransport = {
  chats: ChatTransport[];
};

type ChatMessageTransport = {
  role: "user" | "assistant" | "system";
  content: string;
  timestamp: string;
};

type ChatHistoryTransport = {
  messages: ChatMessageTransport[];
};

type ModelsTransport = {
  models: { id: string; name: string }[];
};

type SendMessageTransport = {
  message: string;
  model?: string;
};

export async function listChats(spaceId: number): Promise<ChatTransport[]> {
  const response = await customInstance<ApiResponse<ChatsTransport>>(
    `/api/v1/spaces/${spaceId}/chats`,
  );
  return response.data.chats;
}

export async function createChat(
  spaceId: number,
  title?: string,
): Promise<ChatTransport> {
  const response = await customInstance<ApiResponse<ChatTransport>>(
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
): Promise<ChatHistoryTransport> {
  const response = await customInstance<ApiResponse<ChatHistoryTransport>>(
    `/api/v1/spaces/${spaceId}/chats/${chatId}`,
  );
  return response.data;
}

export async function deleteChat(
  spaceId: number,
  chatId: string,
): Promise<void> {
  await customInstance(`/api/v1/spaces/${spaceId}/chats/${chatId}`, {
    method: "DELETE",
  });
}

export async function listModels(): Promise<ModelsTransport> {
  const response =
    await customInstance<ApiResponse<ModelsTransport>>("/api/v1/models");
  return response.data;
}

export async function streamChatMessage(
  spaceId: number,
  chatId: string,
  request: SendMessageTransport,
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
    buffer = await processSseBuffer(buffer, onToken);
  }

  await processSseBuffer(buffer, onToken, true);
}

function nextFrame(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => resolve());
  });
}

async function processSseBuffer(
  buffer: string,
  onToken: (token: string) => void,
  flush = false,
): Promise<string> {
  const normalized = buffer.replace(/\r\n/g, "\n");
  const events = normalized.split("\n\n");
  const remainder = flush ? "" : (events.pop() ?? "");

  const tokens: string[] = [];
  for (const event of events) {
    const dataLines = event
      .split("\n")
      .filter((line) => line.startsWith("data:"))
      .map((line) => line.slice(5).trimStart());

    if (dataLines.length > 0) {
      tokens.push(dataLines.join("\n"));
      continue;
    }

    if (event.trim()) {
      tokens.push(event);
    }
  }

  for (let index = 0; index < tokens.length; index++) {
    onToken(tokens[index]);
    if (index < tokens.length - 1) {
      await nextFrame();
    }
  }

  return remainder;
}
