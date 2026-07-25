import type ChatMessage from "@/classes/ChatMessage.ts";
import {MessageFooter} from "@/components/ui/message.tsx";
import {HintOverlay} from "@/components/HintOverlay.tsx";
import SourceEntry from "@/pages/space/chat/SourceEntry.tsx";
import {useTranslation} from "react-i18next";

function SourcesFooter({message}: {message: ChatMessage }) {
    const {t} = useTranslation();

    if (!message || !message.topics || message.topics.length === 0) return null;

    const content = (
        <div className={"flex flex-col gap-2"}>
            {message.topics.map(t => <SourceEntry key={t} topicId={t}/>)}
        </div>
    )

    return (
        <MessageFooter className="user-select-none">
            <HintOverlay trigger={
                <div className={"select-none"}>
                    {t("chat.sources.basedOn", {count: message.topics.length})}
                </div>
            } content={content}/>
        </MessageFooter>
    )
}

export default SourcesFooter;
