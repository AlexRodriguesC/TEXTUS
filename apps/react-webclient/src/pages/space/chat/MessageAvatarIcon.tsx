import type ChatMessage from "@/classes/ChatMessage.ts";
import { Avatar, AvatarFallback } from "@/components/ui/avatar.tsx";
import { Bot, UserRound } from "lucide-react";

// Avatar icon shown next to each message bubble in the conversation list.
function MessageAvatarIcon({ role }: { role: ChatMessage["role"] }) {
  return (
    <Avatar>
      <AvatarFallback>
        {role === "user" ? <UserRound /> : <Bot />}
      </AvatarFallback>
    </Avatar>
  );
}

export default MessageAvatarIcon;
