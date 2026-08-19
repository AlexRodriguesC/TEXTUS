import { streamChatMessage } from "@/client/chatApi.ts";
import { createStreamReveal } from "@/hooks/streamReveal.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useCallback, useRef, useState } from "react";

function useChatStream() {
  const { spaceId } = useSpaceId();
  const [isStreaming, setIsStreaming] = useState(false);
  const abortRef = useRef<AbortController | null>(null);
  const revealRef = useRef<ReturnType<typeof createStreamReveal> | null>(null);

  const sendMessage = useCallback(
    async (
      chatId: string,
      message: string,
      model: string | undefined,
      onToken: (token: string) => void,
    ) => {
      abortRef.current?.abort();
      revealRef.current?.cancel();

      const controller = new AbortController();
      abortRef.current = controller;
      const reveal = createStreamReveal(onToken);
      revealRef.current = reveal;
      setIsStreaming(true);

      try {
        await streamChatMessage(
          spaceId,
          chatId,
          { message, model },
          (token) => reveal.push(token),
          controller.signal,
        );
        await reveal.done();
      } catch (error) {
        reveal.cancel();
        throw error;
      } finally {
        if (revealRef.current === reveal) {
          revealRef.current = null;
        }
        setIsStreaming(false);
        abortRef.current = null;
      }
    },
    [spaceId],
  );

  const cancel = useCallback(() => {
    abortRef.current?.abort();
    revealRef.current?.cancel();
    revealRef.current = null;
    setIsStreaming(false);
  }, []);

  return {
    sendMessage,
    cancel,
    isStreaming,
  };
}

export default useChatStream;
