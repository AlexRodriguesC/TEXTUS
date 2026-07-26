import SpaceLearnEmpty from "@/pages/space/learn/SpaceLearnEmpty.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import { ScrollArea } from "@/components/ui/scroll-area.tsx";
import ChapterHeader from "@/pages/space/learn/ChapterHeader.tsx";
import ChapterPath from "@/pages/space/learn/ChapterPath.tsx";
import { useRef } from "react";
import { Separator } from "@/components/ui/separator.tsx";
import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import LearningPathJobStatus from "@/classes/LearningPathJobStatus.ts";
import { cn } from "@/lib/utils.ts";
import SpaceLearnSkeleton from "@/pages/space/learn/SpaceLearnSkeleton.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import { ArrowUp } from "lucide-react";
import useElementSize from "@/hooks/useElementSize.ts";
import useActiveChapter from "@/hooks/learning/useActiveChapter.ts";

function SpaceLearn() {
  const { path, status, isPathLoading } = useLearningPath();
  const { t } = useTranslation();
  const viewportRef = useRef<HTMLDivElement>(null);
  const headerRef = useRef<HTMLDivElement>(null);
  const headerHeight = useElementSize(headerRef);
  const viewportHeight = useElementSize(viewportRef, [
    isPathLoading,
    path?.chapters.length,
  ]);
  const activeChapter = useActiveChapter(path, viewportRef, headerHeight);

  if (
    path === undefined ||
    status === LearningPathJobStatus.NOT_CREATED ||
    path.chapters.length === 0
  ) {
    return <SpaceLearnEmpty />;
  }

  const current = activeChapter ?? path.chapters[0];

  return (
    <CenteredPage>
      <div className="flex h-full w-full flex-col min-h-0">
        <ScrollArea className="h-full w-full min-h-0" viewportRef={viewportRef}>
          <div
            ref={headerRef}
            className="sticky top-0 z-20 w-full bg-background"
          >
            <ChapterHeader key={current.index} chapter={current} />
          </div>

          <div className="flex h-full flex-col items-center pt-4">
            {isPathLoading ? (
              <SpaceLearnSkeleton />
            ) : (
              path.chapters.map((chapter, i) => (
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
              ))
            )}

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
