import SpaceLearnEmpty from "@/pages/space/learn/SpaceLearnEmpty.tsx";
import useLearningPath, {type Chapter} from "@/hooks/useLearningPath.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import ChapterHeader from "@/pages/space/learn/ChapterHeader.tsx";
import {useEffect, useRef, useState} from "react";
import {Separator} from "@/components/ui/separator.tsx";
import PathNode, {type PathNodeType} from "@/pages/space/learn/PathNodeType.tsx";

function getStepCount(topicCount: number): number {
    if (topicCount <= 3) return 3;
    if (topicCount <= 6) return 5;
    return 8;
}

function distributeTopics(topics: number[], stepCount: number): number[][] {
    const groups: number[][] = Array.from({ length: stepCount }, () => []);
    topics.forEach((topicId, i) => {
        groups[i % stepCount].push(topicId);
    });
    return groups;
}

function buildNodes(topics: number[], stepCount: number): PathNodeType[] {
    const groups = distributeTopics(topics, stepCount);
    return groups.map((g, i) => ({
        topics: g,
        type: i === groups.length - 1 ? "trophy" : "star",
    }));
}

const OFFSETS = [0, 60, 0, -60];

function ChapterPath({ chapter}: { chapter: Chapter; sectionIndex: number }) {
    const stepCount = getStepCount(chapter.topics.length);
    const nodes = buildNodes(chapter.topics, stepCount);

    return (
        <div className={"flex flex-col items-center gap-2 pb-8"} data-chapter-index={chapter.index}>
            <div className={"flex flex-col items-center gap-6 pt-4"}>
                {nodes.map((node, i) => (
                    <PathNode key={i} node={node} offset={OFFSETS[i % OFFSETS.length]} />
                ))}
            </div>
        </div>
    );
}

function SpaceLearn() {
    const { path, status } = useLearningPath();
    const [activeChapter, setActiveChapter] = useState<Chapter | null>(null);
    const chaptersRef = useRef<HTMLDivElement[]>([]);
    const viewportRef = useRef<HTMLDivElement>(null);
    const ratiosRef = useRef<Map<number, number>>(new Map());

    useEffect(() => {
        if (!path?.chapters.length || !viewportRef.current) return;

        const elements = document.querySelectorAll<HTMLDivElement>("[data-chapter-index]");
        chaptersRef.current = Array.from(elements);
        ratiosRef.current = new Map();

        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    const index = Number(entry.target.getAttribute("data-chapter-index"));
                    ratiosRef.current.set(index, entry.intersectionRatio);
                });

                let bestIndex: number | null = null;
                let maxRatio = 0;
                ratiosRef.current.forEach((ratio, index) => {
                    if (ratio > maxRatio) {
                        maxRatio = ratio;
                        bestIndex = index;
                    }
                });

                if (bestIndex !== null && maxRatio > 0.3) {
                    const chapter = path.chapters.find((c) => c.index === bestIndex);
                    if (chapter) setActiveChapter(chapter);
                }
            },
            { root: viewportRef.current, threshold: [0, 0.3, 0.6, 1] }
        );

        chaptersRef.current.forEach((el) => observer.observe(el));

        return () => observer.disconnect();
    }, [path]);

    if (path === undefined || status === "NOT_CREATED" || path.chapters.length === 0) {
        return <SpaceLearnEmpty />;
    }

    return (
        <CenteredPage>
            <div className="h-full w-full max-w-[90%] lg:max-w-[40%] flex flex-col items-center gap-16 min-h-0">
                <div className="sticky top-0 z-20 w-full">
                    {activeChapter ? (
                        <ChapterHeader chapter={activeChapter} />
                    ) : (
                        <ChapterHeader chapter={path.chapters[0]} />
                    )}
                </div>

                <ScrollArea className="h-full w-full min-h-0" viewportRef={viewportRef}>
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