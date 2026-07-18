import SpaceLearnEmpty from "@/pages/space/learn/SpaceLearnEmpty.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import ChapterHeader from "@/pages/space/learn/ChapterHeader.tsx";
import {useEffect, useRef, useState} from "react";
import {Separator} from "@/components/ui/separator.tsx";
import useLearningPath from "@/hooks/useLearningPath.ts";
import {useNavigate} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import {type ChapterResponseDTO, type LessonResponseDTO} from "@/client/model";
import PathNode from "@/pages/space/learn/PathNodeType.tsx";
import {cn} from "@/lib/utils.ts";

const OFFSETS = [0, 60, 0, -60];

function getPrerequisiteLesson(
    chapters: ChapterResponseDTO[],
    chapterIndex: number,
    lesson: LessonResponseDTO
): LessonResponseDTO | null {
    if (lesson.index > 0) {
        const chapter = chapters.find((c) => c.index === chapterIndex);
        return chapter?.lessons.find((l) => l.index === lesson.index - 1) ?? null;
    }

    const previousChapter = chapters.find((c) => c.index === chapterIndex - 1);
    if (!previousChapter || previousChapter.lessons.length === 0) {
        return null;
    }

    return previousChapter.lessons.reduce((max, l) => (l.index > max.index ? l : max));
}

function ChapterPath({ chapter, chapters }: { chapter: ChapterResponseDTO; chapters: ChapterResponseDTO[]; sectionIndex: number }) {
    const navigate = useNavigate();
    const { strSpaceId } = useSpaceId();

    return (
        <div className={"flex flex-col items-center gap-2 pb-8"} data-chapter-index={chapter.index}>
            <div className={"flex flex-col items-center gap-6 pt-4"}>
                {chapter.lessons.map((lesson, i) => {
                    const prerequisite = getPrerequisiteLesson(chapters, chapter.index, lesson);
                    const disabled = prerequisite !== null && !prerequisite.completed;

                    return (
                        <PathNode
                            key={lesson.id}
                            onClick={() => {
                                if (disabled) return;
                                navigate({
                                    to: PathConstants.LESSON,
                                    params: {
                                        spaceId: strSpaceId!,
                                        chapterId: chapter.index.toString(),
                                        lessonId: lesson.id.toString()
                                    }
                                });
                            }}
                            node={lesson}
                            offset={OFFSETS[i % OFFSETS.length]}
                            disabled={disabled}
                        />
                    );
                })}
            </div>
        </div>
    );
}

function SpaceLearn() {
    const { path, status } = useLearningPath();
    const [activeChapter, setActiveChapter] = useState<ChapterResponseDTO | null>(null);
    const chaptersRef = useRef<HTMLDivElement[]>([]);
    const viewportRef = useRef<HTMLDivElement>(null);
    const ratiosRef = useRef<Map<number, number>>(new Map());

    const completedCount = path?.chapters.flatMap((c) => c.lessons).filter((l) => l.completed).length ?? 0;

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
                            <div key={chapter.index} className={cn("flex flex-col")}>
                                <ChapterPath chapter={chapter} chapters={path.chapters} sectionIndex={i + 1} />
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