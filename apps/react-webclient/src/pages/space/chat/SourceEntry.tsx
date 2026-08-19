import useTopics from "@/hooks/useTopics.tsx";
import {useTranslation} from "react-i18next";
import {ArrowUpRight} from "lucide-react";
import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";

function SourceEntry({topicId}: {topicId: number}) {
    const {t} = useTranslation();
    const {strSpaceId} = useSpaceId();
    const {topics} = useTopics();
    const topic = topics.find(t => t.id === topicId);
    if (!topic) return null;

    return (
        <Link
            to={PathConstants.SPACE_TOPICS}
            params={{ spaceId: strSpaceId }}
            search={{ q: topic.title }}
            className="flex w-full items-center justify-between gap-2 border px-3 py-2 text-sm text-foreground hover:bg-muted/50 transition-colors"
        >
            <span className="truncate">
                {t("chat.source.based-on", { title: topic.title })}
            </span>
            <ArrowUpRight className="size-4 shrink-0 text-muted-foreground" />
        </Link>
    );
}

export default SourceEntry;
