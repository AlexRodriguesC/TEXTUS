import SpaceLearnEmpty from "@/pages/space/learn/SpaceLearnEmpty.tsx";
import useLearningPath, {type Chapter} from "@/hooks/useLearningPath.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import ChapterHeader from "@/pages/space/learn/ChapterHeader.tsx";
import {Star, Trophy} from "lucide-react";
import {useEffect, useRef, useState} from "react";
import {Separator} from "@/components/ui/separator.tsx";

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
    const icon = node.type === "trophy" ? <img className={"w-fit h-fit"} src={"/checkpoint.png"}/> : node.type === "chest" ? "🎁" : <img src={"/lesson.png"}/>;
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
        <div className={"flex flex-col items-center gap-2 pb-8"} data-chapter-index={chapter.index}>
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
    const [activeChapter, setActiveChapter] = useState<Chapter | null>(null);
    const chaptersRef = useRef<HTMLDivElement[]>([]);
    const scrollContainerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!path?.chapters.length) return;

        // Collect chapter container elements
        const elements = document.querySelectorAll<HTMLDivElement>("[data-chapter-index]");
        chaptersRef.current = Array.from(elements);

        const observer = new IntersectionObserver(
            (entries) => {
                // Find the chapter with the largest intersection ratio
                let maxRatio = 0;
                let bestEntry: IntersectionObserverEntry | null = null;

                entries.forEach((entry) => {
                    if (entry.intersectionRatio > maxRatio) {
                        maxRatio = entry.intersectionRatio;
                        bestEntry = entry;
                    }
                });

                if (bestEntry && maxRatio > 0.3) {
                    const index = Number(bestEntry.target.getAttribute("data-chapter-index"));
                    const chapter = path.chapters.find((c) => c.index === index);
                    if (chapter) setActiveChapter(chapter);
                }
            },
            { root: scrollContainerRef.current, threshold: [0.3, 0.6] }
        );

        chaptersRef.current.forEach((el) => observer.observe(el));

        return () => observer.disconnect();
    }, [path]);

    if (path === undefined || status === "NOT_CREATED") {
        return <SpaceLearnEmpty />;
    }

    return (
        <CenteredPage>
            <div className="h-full w-full max-w-[90%] lg:max-w-[40%] flex flex-col items-center justify-center gap-16">
                <div className={"sticky top-0 z-20 w-full"}>
                    {activeChapter ? (
                        <ChapterHeader chapter={activeChapter} />
                    ) : (
                        <ChapterHeader chapter={path.chapters[0]} />
                    )}
                </div>

                <ScrollArea
                    ref={scrollContainerRef}
                    className="h-full w-full"
                >
                    <div className={"flex h-full flex-col items-center"}>
                        {path.chapters.map((chapter, i) => (
                            <div key={chapter.index} className="flex flex-col">
                                <ChapterPath chapter={chapter} sectionIndex={i + 1} />
                                {i < path.chapters.length - 1 && (
                                    <div className="flex items-center gap-3 my-4">
                                        <Separator className="flex-1 bg-border/50" />
                                        <span className="text-muted-foreground text-xs whitespace-wrap">{chapter.description}</span>
                                        <Separator className="flex-1 bg-border/50" />
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </ScrollArea>
            </div>
        </CenteredPage>
    );
}

export default SpaceLearn;