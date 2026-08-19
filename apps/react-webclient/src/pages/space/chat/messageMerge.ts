import type ChatMessage from "@/classes/ChatMessage.ts";
import type { DisplayMessage } from "@/pages/space/chat/types.ts";
import { makeMessageKey } from "@/pages/space/chat/utils.ts";

export function mergeDisplayedMessages(
  historyMessages: ChatMessage[],
  draftMessages: DisplayMessage[],
  clientIdMap: Map<string, string>,
): DisplayMessage[] {
  const historyKeys = new Set(
    historyMessages.map((message) =>
      makeMessageKey(message.role, message.content),
    ),
  );

  const history = historyMessages.map((message, index) => ({
    ...message,
    id:
      clientIdMap.get(makeMessageKey(message.role, message.content)) ??
      `${message.timestamp}-${index}`,
  }));

  const pendingDrafts = draftMessages.filter((draft) => {
    if (!draft.content && draft.role === "assistant") {
      return true;
    }
    return !historyKeys.has(makeMessageKey(draft.role, draft.content));
  });

  return [...history, ...pendingDrafts];
}
