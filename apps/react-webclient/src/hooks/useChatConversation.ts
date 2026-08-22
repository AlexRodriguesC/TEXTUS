import ChatMessageClass from "@/classes/ChatMessage.ts";
import type {
  ActiveTurn,
  DisplayMessage,
  SpaceChatConversationProps,
} from "@/pages/space/chat/types.ts";
import { mergeDisplayedMessages } from "@/pages/space/chat/messageMerge.ts";
import {
  historyIncludesUserMessage,
  makeMessageKey,
} from "@/pages/space/chat/utils.ts";
import useChatContext from "@/hooks/context/useChatContext.ts";
import useChatMessages from "@/hooks/useChatMessages.ts";
import { useCallback, useLayoutEffect, useMemo, useRef, useState } from "react";

type UseChatConversationParams = Pick<
  SpaceChatConversationProps,
  "chatId" | "sendMessage" | "isStreaming" | "invalidateChats"
>;

function useChatConversation({
  chatId,
  sendMessage,
  isStreaming,
  invalidateChats,
}: UseChatConversationParams) {
  const { consumePendingMessage } = useChatContext();
  const [draftMessages, setDraftMessages] = useState<DisplayMessage[]>([]);
  const [activeTurn, setActiveTurn] = useState<ActiveTurn | null>(null);
  const [clientIdMap, setClientIdMap] = useState<Map<string, string>>(
    () => new Map(),
  );
  const submittingRef = useRef(false);

  const {
    messages: historyMessages,
    refetch,
    isLoading: isHistoryLoading,
  } = useChatMessages(chatId);

  const displayedMessages = useMemo(
    () => mergeDisplayedMessages(historyMessages, draftMessages, clientIdMap),
    [clientIdMap, draftMessages, historyMessages],
  );

  const showHistoryLoading =
    isHistoryLoading && displayedMessages.length === 0;
  const showEmptyState =
    displayedMessages.length === 0 && !isHistoryLoading && !isStreaming;

  const handleSubmit = useCallback(
    async (message: string, model?: string, topicIds?: number[]) => {
      if (isStreaming || submittingRef.current) {
        return;
      }

      submittingRef.current = true;

      const userId = crypto.randomUUID();
      const assistantId = crypto.randomUUID();
      const userKey = makeMessageKey("user", message);

      setActiveTurn({ userId, assistantId, userContent: message });
      setClientIdMap((current) => {
        const next = new Map(current);
        next.set(userKey, userId);
        return next;
      });

      const userMessage: DisplayMessage = Object.assign(
        new ChatMessageClass("user", message, new Date().toISOString(), []),
        { id: userId },
      );
      const assistantPlaceholder: DisplayMessage = Object.assign(
        new ChatMessageClass("assistant", "", new Date().toISOString(), []),
        { id: assistantId },
      );

      setDraftMessages((current) => [
        ...current,
        userMessage,
        assistantPlaceholder,
      ]);

      try {
        await sendMessage(
          chatId,
          message,
          model,
          (token) => {
            setDraftMessages((current) =>
              current.map((draft) =>
                draft.id === assistantId
                  ? Object.assign(
                      new ChatMessageClass(
                        "assistant",
                        draft.content + token,
                        draft.timestamp,
                        draft.topics,
                      ),
                      { id: assistantId },
                    )
                  : draft,
              ),
            );
          },
          topicIds,
        );

        const result = await refetch();
        invalidateChats();

        const refreshedMessages = result.data ?? [];
        if (historyIncludesUserMessage(refreshedMessages, message)) {
          const userIndex = refreshedMessages.findLastIndex(
            (entry) => entry.role === "user" && entry.content === message,
          );
          const assistantMessage =
            userIndex >= 0
              ? refreshedMessages
                  .slice(userIndex + 1)
                  .find((entry) => entry.role === "assistant")
              : undefined;

          setClientIdMap((current) => {
            const next = new Map(current);
            next.set(userKey, userId);
            if (assistantMessage) {
              next.set(
                makeMessageKey("assistant", assistantMessage.content),
                assistantId,
              );
            }
            return next;
          });

          setDraftMessages((current) =>
            current.filter(
              (draft) => draft.id !== userId && draft.id !== assistantId,
            ),
          );
          setActiveTurn(null);
        }
      } catch {
        setDraftMessages((current) =>
          current.filter(
            (draft) => !(draft.id === assistantId && !draft.content),
          ),
        );
        setActiveTurn(null);
      } finally {
        submittingRef.current = false;
      }
    },
    [chatId, isStreaming, invalidateChats, refetch, sendMessage],
  );

  useLayoutEffect(() => {
    if (isStreaming) {
      return;
    }

    const pending = consumePendingMessage(chatId);
    if (pending) {
      void handleSubmit(pending.message, undefined, pending.topicIds);
    }
  }, [chatId, consumePendingMessage, handleSubmit, isStreaming]);

  return {
    displayedMessages,
    activeTurn,
    showHistoryLoading,
    showEmptyState,
    handleSubmit,
  };
}

export default useChatConversation;
