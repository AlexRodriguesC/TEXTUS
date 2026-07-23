import { Bubble, BubbleContent } from "@/components/ui/bubble.tsx";
import {
  Message,
  MessageAvatar,
  MessageContent,
} from "@/components/ui/message.tsx";
import { MARKDOWN_CLASSES } from "@/pages/space/chat/constants.ts";
import MessageAvatarIcon from "@/pages/space/chat/MessageAvatarIcon.tsx";
import ThinkingMarker from "@/pages/space/chat/ThinkingMarker.tsx";
import type { DisplayMessage } from "@/pages/space/chat/types.ts";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

// Single user or assistant message row with markdown content in the chat list.
function ChatMessageRow({
  message,
  isStreamingPlaceholder,
}: {
  message: DisplayMessage;
  isStreamingPlaceholder: boolean;
}) {
  if (isStreamingPlaceholder && !message.content) {
    return <ThinkingMarker />;
  }

  const isUser = message.role === "user";

  return (
    <Message align={isUser ? "end" : "start"}>
      <MessageAvatar>
        <MessageAvatarIcon role={message.role} />
      </MessageAvatar>
      <MessageContent>
        <Bubble variant={isUser ? "default" : "muted"}>
          <BubbleContent className={MARKDOWN_CLASSES}>
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {message.content}
            </ReactMarkdown>
          </BubbleContent>
        </Bubble>
      </MessageContent>
    </Message>
  );
}

export default ChatMessageRow;
