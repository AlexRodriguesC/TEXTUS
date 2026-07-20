import { streamChatMessage } from "@/client/chatApi.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useCallback, useRef, useState } from "react";

function useChatStream() {
  const { spaceId } = useSpaceId();
  const [isStreaming, setIsStreaming] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const sendMessage = useCallback(
    async (
      chatId: string,
      message: string,
      model: string | undefined,
      onToken: (token: string) => void,
    ) => {
      abortRef.current?.abort();
      const controller = new AbortController();
      abortRef.current = controller;
      setIsStreaming(true);

      try {
        await streamChatMessage(
          spaceId,
          chatId,
          { message, model },
          onToken,
          controller.signal,
        );
      } finally {
        setIsStreaming(false);
        abortRef.current = null;
      }
    },
    [spaceId],
  );

  const cancel = useCallback(() => {
    abortRef.current?.abort();
    setIsStreaming(false);
  }, []);

  return {
    sendMessage,
    cancel,
    isStreaming,
  };
}

export default useChatStream;
