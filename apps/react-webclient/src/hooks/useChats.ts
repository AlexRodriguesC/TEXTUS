import {
  createChat,
  deleteChat,
  listChats,
} from "@/client/chatApi.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type { ChatDTO } from "@/types/chat.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback } from "react";

export const getChatsQueryKey = (spaceId: number) => ["chats", spaceId] as const;

function useChats() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();

  const { data: chats = [], isLoading } = useQuery({
    queryKey: getChatsQueryKey(spaceId),
    queryFn: () => listChats(spaceId),
  });

  const invalidate = useCallback(() => {
    queryClient.invalidateQueries({ queryKey: getChatsQueryKey(spaceId) });
  }, [queryClient, spaceId]);

  const createMutation = useMutation({
    mutationFn: (title?: string) => createChat(spaceId, title),
    onSuccess: (chat: ChatDTO) => {
      queryClient.setQueryData<ChatDTO[]>(getChatsQueryKey(spaceId), (current) => [
        chat,
        ...(current ?? []),
      ]);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (chatId: string) => deleteChat(spaceId, chatId),
    onSuccess: () => invalidate(),
  });

  return {
    chats,
    isLoading,
    createChat: (title?: string) => createMutation.mutateAsync(title),
    isCreating: createMutation.isPending,
    deleteChat: (chatId: string) => deleteMutation.mutateAsync(chatId),
    isDeleting: deleteMutation.isPending,
    invalidate,
  };
}

export default useChats;
