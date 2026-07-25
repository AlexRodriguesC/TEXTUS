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
        <div className={"flex flex-row items-center w-full gap-2"}>
            <p>{t("chat.source.based-on", {title: topic.title})}</p>
            <Link
                to={PathConstants.SPACE_TOPICS}
                params={{ spaceId: strSpaceId }}
                search={{ q: topic.title }}
            >
                <ArrowUpRight/>
            </Link>
        </div>
    )
}

export default SourceEntry;
