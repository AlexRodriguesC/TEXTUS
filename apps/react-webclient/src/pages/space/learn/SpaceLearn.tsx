import SpaceLearnEmpty from "@/pages/space/learn/SpaceLearnEmpty.tsx";
import useLearningPath, {type Chapter} from "@/hooks/useLearningPath.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import ChapterHeader from "@/pages/space/learn/ChapterHeader.tsx";
import {Star, Trophy} from "lucide-react";

function getStepCount(topicCount: number): number {
    if (topicCount <= 3) return 3;
    if (topicCount <= 6) return 5;
    return 8;
}

interface PathNode {
    topics: number[];
    type: "star" | "chest" | "trophy";
}

function distributeTopics(topics: number[], stepCount: number): number[][] {
    const groups: number[][] = Array.from({ length: stepCount }, () => []);
    topics.forEach((topicId, i) => {
        groups[i % stepCount].push(topicId);
    });
    return groups;
}

function buildNodes(topics: number[], stepCount: number): PathNode[] {
    const groups = distributeTopics(topics, stepCount);
    return groups.map((g, i) => ({
        topics: g,
        type: i === groups.length - 1 ? "trophy" : (i + 1) % 3 === 0 ? "chest" : "star",
    }));
}

const OFFSETS = [0, 60, 0, -60];

function PathStep({ node, offset }: { node: PathNode; offset: number }) {
    const icon = node.type === "trophy" ? <Trophy color={"yellow"}/> : node.type === "chest" ? "🎁" : <Star color={"yellow"}/>;
    return (
        <div className={"flex justify-center"} style={{ transform: `translateX(${offset}px)` }}>
            <div className={"flex h-16 w-16 items-center justify-center rounded-full bg-gray-700 text-2xl text-gray-400"}>
                {icon}
            </div>
        </div>
    );
}

function ChapterPath({ chapter}: { chapter: Chapter; sectionIndex: number }) {
    const stepCount = getStepCount(chapter.topics.length);
    const nodes = buildNodes(chapter.topics, stepCount);

    return (
        <div className={"flex flex-col gap-2 pb-8"}>
            <ChapterHeader chapter={chapter} />
            <div className={"flex flex-col items-center gap-6 pt-4"}>
                {nodes.map((node, i) => (
                    <PathStep key={i} node={node} offset={OFFSETS[i % OFFSETS.length]} />
                ))}
            </div>
        </div>
    );
}

function SpaceLearn() {
    const { path, status } = useLearningPath();

    if (path === undefined || status === "NOT_CREATED") {
        return (
            <SpaceLearnEmpty/>
        )
    }

    return (
        <CenteredPage>
            <ScrollArea className="h-full w-full max-w-[90%] lg:max-w-[40%] flex items-center justify-center">
                <div className={"flex h-full flex-col gap-4 px-4"}>
                    {path.chapters.map((chapter, i) => (
                        <ChapterPath key={chapter.index} chapter={chapter} sectionIndex={i + 1} />
                    ))}
                </div>
            </ScrollArea>
        </CenteredPage>
    )
}

export default SpaceLearn;