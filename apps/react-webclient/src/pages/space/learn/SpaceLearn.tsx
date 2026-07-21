import SpaceLearnEmpty from "@/pages/space/learn/SpaceLearnEmpty.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import { ScrollArea } from "@/components/ui/scroll-area.tsx";
import ChapterHeader from "@/pages/space/learn/ChapterHeader.tsx";
import { useEffect, useRef, useState } from "react";
import { Separator } from "@/components/ui/separator.tsx";
import useLearningPath from "@/hooks/useLearningPath.ts";
import {
  type ChapterResponseDTO,
  type LessonResponseDTO,
} from "@/client/model";
import PathNode from "@/pages/space/learn/PathNodeType.tsx";
import { cn } from "@/lib/utils.ts";
import LoadingScreen from "@/components/LoadingScreen.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import { ArrowUp } from "lucide-react";

const OFFSETS = [0, 60, 0, -60];

function getPrerequisiteLesson(
  chapters: ChapterResponseDTO[],
  chapterIndex: number,
  lesson: LessonResponseDTO,
): LessonResponseDTO | null {
  if (lesson.index > 0) {
    const chapter = chapters.find((c) => c.index === chapterIndex);
    return chapter?.lessons.find((l) => l.index === lesson.index - 1) ?? null;
  }

  return null;
}

function ChapterPath({
  chapter,
  chapters,
}: {
  chapter: ChapterResponseDTO;
  chapters: ChapterResponseDTO[];
}) {
  return (
    <div
      className="flex flex-col items-center gap-2 pb-8"
      data-chapter-index={chapter.index}
    >
      <div className="flex flex-col items-center gap-6 pt-4">
        {chapter.lessons.map((lesson: LessonResponseDTO, i: number) => {
          const prerequisite = getPrerequisiteLesson(
            chapters,
            chapter.index,
            lesson,
          );
          const disabled = prerequisite !== null && !prerequisite.completed;

          return (
            <PathNode
              key={lesson.id}
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
  const { path, status, isPathLoading } = useLearningPath();
  const { t } = useTranslation();
  const [activeChapter, setActiveChapter] = useState<ChapterResponseDTO | null>(
    null,
  );
  const [headerHeight, setHeaderHeight] = useState(0);
  const viewportRef = useRef<HTMLDivElement>(null);
  const [viewportHeight, setViewportHeight] = useState(0);
  const headerRef = useRef<HTMLDivElement>(null);
  const intersectingRef = useRef<Set<number>>(new Set());

  useEffect(() => {
    if (path?.chapters.length) {
      setActiveChapter((prev: ChapterResponseDTO | null) => prev ?? path.chapters[0]);
    }
  }, [path]);

  useEffect(() => {
    if (!headerRef.current) return;
    const ro = new ResizeObserver(([entry]) => {
      setHeaderHeight(entry.contentRect.height);
    });
    ro.observe(headerRef.current);
    return () => ro.disconnect();
  }, []);

  useEffect(() => {
    if (!viewportRef.current) return;
    const ro = new ResizeObserver(([entry]) => {
      setViewportHeight(entry.contentRect.height);
    });
    ro.observe(viewportRef.current);
    return () => ro.disconnect();
  }, [isPathLoading, path?.chapters.length]);

  useEffect(() => {
    if (!path?.chapters.length || !viewportRef.current || headerHeight === 0) {
      return;
    }

    const elements = document.querySelectorAll<HTMLDivElement>(
      "[data-chapter-index]",
    );
    intersectingRef.current = new Set();

    // Collapse the root into a thin band just below the sticky header.
    // Bottom margin uses a percentage (not px) so it can never collapse
    // the root's area to zero/negative, regardless of rounding.
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const index = Number(entry.target.getAttribute("data-chapter-index"));
          if (entry.isIntersecting) {
            intersectingRef.current.add(index);
          } else {
            intersectingRef.current.delete(index);
          }
        });

        if (intersectingRef.current.size === 0) return;

        const bestIndex = Math.min(...intersectingRef.current);
        const chapter = path.chapters.find((c) => c.index === bestIndex);
        if (chapter) setActiveChapter(chapter);
      },
      {
        root: viewportRef.current,
        rootMargin: `-${headerHeight}px 0px -80% 0px`,
        threshold: 0,
      },
    );

    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, [path, headerHeight]);

  if (status !== "NOT_CREATED" && isPathLoading) {
    return <LoadingScreen />;
  }

  if (
    path === undefined ||
    status === "NOT_CREATED" ||
    path.chapters.length === 0
  ) {
    return <SpaceLearnEmpty />;
  }

  const current = activeChapter ?? path.chapters[0];

  return (
    <CenteredPage>
      <div className="h-full w-full max-w-[90%] lg:max-w-[40%] flex flex-col min-h-0">
        <ScrollArea className="h-full w-full min-h-0" viewportRef={viewportRef}>
          <div
            ref={headerRef}
            className="sticky top-0 z-20 w-full bg-background"
          >
            <ChapterHeader key={current.index} chapter={current} />
          </div>

          <div className="flex h-full flex-col items-center pt-4">
            {path.chapters.map((chapter, i) => (
              <div key={chapter.index} className={cn("flex flex-col")}>
                <ChapterPath chapter={chapter} chapters={path.chapters} />
                {i < path.chapters.length - 1 && (
                  <div
                    className="flex items-center gap-3 my-4"
                    data-chapter-index={chapter.index}
                  >
                    <Separator className="flex-1 bg-border/50" />
                    <span className="text-muted-foreground text-xs whitespace-wrap">
                      {chapter.description}
                    </span>
                    <Separator className="flex-1 bg-border/50" />
                  </div>
                )}
              </div>
            ))}

            <div
              className="flex w-full flex-col items-center justify-center gap-4"
              style={{ minHeight: Math.max(viewportHeight - headerHeight, 0) }}
            >
              <div className="opacity-60">
                <ResponsiveLottie src={"/animations/not_found.lottie"} />
              </div>

              <p className="text-muted-foreground text-sm opacity-60">
                {t("learning-path.end")}
              </p>

              <Button
                variant="outline"
                size="sm"
                onClick={() =>
                  viewportRef.current?.scrollTo({ top: 0, behavior: "smooth" })
                }
              >
                <div className={"flex flex-row gap-2 items-center"}>
                  <ArrowUp />
                  {t("button.back-to-top")}
                </div>
              </Button>
            </div>
          </div>
        </ScrollArea>
      </div>
    </CenteredPage>
  );
}

export default SpaceLearn;
