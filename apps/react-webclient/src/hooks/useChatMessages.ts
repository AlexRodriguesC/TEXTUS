import { getChatHistory } from "@/client/chatApi.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useQuery } from "@tanstack/react-query";
import ChatMessage from "@/classes/ChatMessage.ts";
import { mapChatMessageFromCustom } from "@/hooks/dtoMappers.ts";

export const getChatMessagesQueryKey = (
  spaceId: number,
  chatId: string | null,
) => ["chatMessages", spaceId, chatId] as const;

function useChatMessages(chatId: string | null) {
  const { spaceId } = useSpaceId();

  const query = useQuery({
    queryKey: getChatMessagesQueryKey(spaceId, chatId),
    queryFn: async () => {
      const response = await getChatHistory(spaceId, chatId!);
      return response.messages.map(mapChatMessageFromCustom);
    },
    enabled: chatId != null && spaceId !== undefined,
  });

  const messages: ChatMessage[] = query.data ?? [];

  return {
    messages,
    isLoading: query.isLoading,
    refetch: query.refetch,
  };
}

export default useChatMessages;
