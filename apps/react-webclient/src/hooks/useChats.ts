import {
  getListChatsQueryKey,
  useCreateChat,
  useDeleteChat,
  useListChats,
} from "@/client/generated/chats/chats.ts";
import type { ChatsResponseDTO, ErrorResponseDTO } from "@/client/model";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useMemo } from "react";
import Chat from "@/classes/Chat.ts";
import { mapChat } from "@/hooks/dtoMappers.ts";

const ignored = -1;

function useChats() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();

  const { data, isLoading } = useListChats(spaceId ?? ignored, {
    query: { enabled: spaceId !== undefined },
  });

  const chats: Chat[] = useMemo(() => {
    if (!data || data.status !== 200 || !isChatsResponse(data.data)) return [];
    return data.data.chats.map(mapChat);
  }, [data]);

  const invalidate = useCallback(() => {
    if (spaceId === undefined) return;
    queryClient.invalidateQueries({ queryKey: getListChatsQueryKey(spaceId) });
  }, [queryClient, spaceId]);

  const { mutateAsync: createChatMutation, isPending: isCreating } =
    useCreateChat({
      mutation: {
        onSuccess: async () => {
          invalidate();
        },
      },
    });

  const { mutateAsync: deleteChatMutation, isPending: isDeleting } =
    useDeleteChat({
      mutation: {
        onSuccess: async () => {
          invalidate();
        },
      },
    });

  const createChat = useCallback(
    async (title?: string) => {
      if (spaceId === undefined) {
        throw new Error("spaceId is required to create a chat");
      }
      const result = await createChatMutation({
        spaceId,
        data: title ? { title } : undefined,
      });
      if (result.status !== 201) {
        throw new Error("Failed to create chat");
      }
      return mapChat(result.data);
    },
    [createChatMutation, spaceId],
  );

  const deleteChat = useCallback(
    (chatId: string) => {
      if (spaceId === undefined) {
        throw new Error("spaceId is required to delete a chat");
      }
      return deleteChatMutation({ spaceId, chatId });
    },
    [deleteChatMutation, spaceId],
  );

  return {
    chats,
    isLoading,
    createChat,
    isCreating,
    deleteChat,
    isDeleting,
    invalidate,
    invalidateChats: invalidate,
  };
}

function isChatsResponse(
  data: ChatsResponseDTO | ErrorResponseDTO,
): data is ChatsResponseDTO {
  return "chats" in data;
}

export default useChats;
