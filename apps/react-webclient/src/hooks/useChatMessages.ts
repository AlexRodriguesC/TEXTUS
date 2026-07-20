import { getChatHistory } from "@/client/chatApi.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useQuery } from "@tanstack/react-query";

export const getChatMessagesQueryKey = (spaceId: number, chatId: string | null) =>
  ["chatMessages", spaceId, chatId] as const;

function useChatMessages(chatId: string | null) {
  const { spaceId } = useSpaceId();

  const query = useQuery({
    queryKey: getChatMessagesQueryKey(spaceId, chatId),
    queryFn: () => getChatHistory(spaceId, chatId!),
    enabled: chatId != null,
  });

  return {
    messages: query.data?.messages ?? [],
    isLoading: query.isLoading,
    refetch: query.refetch,
  };
}

export default useChatMessages;
