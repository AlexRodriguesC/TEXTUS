import { Bubble, BubbleContent, BubbleGroup } from "@/components/ui/bubble.tsx";
import { MessageScrollerItem } from "@/components/ui/message-scroller.tsx";
import type Topic from "@/classes/Topic.ts";
import useTopics from "@/hooks/useTopics.tsx";
import {
  buildTopicSuggestion,
  pickSuggestedTopics,
} from "@/pages/space/chat/chatTopicSuggestions.ts";
import { useRef } from "react";
import { useTranslation } from "react-i18next";

function ChatEmptyMessages({
  title,
  asScrollerItem = false,
  onSuggestionClick,
  disabled = false,
}: {
  title: string;
  asScrollerItem?: boolean;
  onSuggestionClick?: (message: string) => void;
  disabled?: boolean;
}) {
  const { t } = useTranslation();
  const { topics } = useTopics();
  const suggestedTopicsRef = useRef<Topic[] | null>(null);

  if (suggestedTopicsRef.current === null && topics.length > 0) {
    suggestedTopicsRef.current = pickSuggestedTopics(topics);
  }

  const suggestedTopics = suggestedTopicsRef.current ?? [];
  const text = t("chat.empty.messages", { title });
  const showSuggestions =
    onSuggestionClick !== undefined && suggestedTopics.length > 0;

  const content = (
    <div className="flex flex-1 flex-col items-center gap-4 py-12 text-center px-6 w-full">
      <p className="text-sm text-muted-foreground">{text}</p>
      {showSuggestions && (
        <BubbleGroup className="flex flex-col w-full">
          {suggestedTopics.map((topic, index) => {
            const message = buildTopicSuggestion(t, topic, index);
            return (
              <Bubble key={topic.id} variant="tinted" align={index % 2 === 0 ? "start" : "end"}>
                <BubbleContent asChild>
                  <button
                    type="button"
                    disabled={disabled}
                    onClick={() => onSuggestionClick(message)}
                  >
                    {message}
                  </button>
                </BubbleContent>
              </Bubble>
            );
          })}
        </BubbleGroup>
      )}
    </div>
  );

  if (asScrollerItem) {
    return <MessageScrollerItem messageId="empty">{content}</MessageScrollerItem>;
  }

  return (
    <div className="flex flex-1 min-h-0 items-center justify-center">{content}</div>
  );
}

export default ChatEmptyMessages;
