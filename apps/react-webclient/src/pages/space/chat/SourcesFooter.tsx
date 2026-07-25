import type ChatMessage from "@/classes/ChatMessage.ts";
import {MessageFooter} from "@/components/ui/message.tsx";
import {HintOverlay} from "@/components/HintOverlay.tsx";
import SourceEntry from "@/pages/space/chat/SourceEntry.tsx";
import {useTranslation} from "react-i18next";

function SourcesFooter({message}: {message: ChatMessage }) {
    const {t} = useTranslation();

    if (!message || !message.topics || message.topics.length === 0) return null;

    const content = (
        <div className="flex flex-col gap-1">
            {message.topics.map((topicId) => (
                <SourceEntry key={topicId} topicId={topicId} />
            ))}
        </div>
    );

    return (
        <MessageFooter className="user-select-none">
            <HintOverlay
                variant="panel"
                className="min-w-56 p-0"
                trigger={
                    <div className="select-none cursor-pointer">
                        {t("chat.sources.basedOn", { count: message.topics.length })}
                    </div>
                }
                content={content}
            />
        </MessageFooter>
    );
}

export default SourcesFooter;
