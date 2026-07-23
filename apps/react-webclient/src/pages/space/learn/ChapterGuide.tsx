import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import useTopics from "@/hooks/useTopics.tsx";
import { useEffect, useMemo } from "react";
import useChapterId from "@/hooks/useChapterId.ts";
import { useNavigate } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { ScrollArea } from "@/components/ui/scroll-area.tsx";
import ChapterGuideBreadcrumbs from "@/pages/space/learn/ChapterGuideBreadcrumbs.tsx";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import { useTranslation } from "react-i18next";

function ChapterGuide() {
  const { t: tr } = useTranslation();
  const { chapterId } = useChapterId();
  const { spaceId } = useSpaceId();
  const { path } = useLearningPath();
  const { topics } = useTopics();
  const navigate = useNavigate();

  // Resolve chapter data
  const chapter = path?.chapters?.find((c) => c.index === chapterId);
  const chapterTopics = chapter?.lessons.flatMap((n) => n.topics) ?? [];

  // Filter topics client-side
  const tops = useMemo(
    () => topics.filter((t) => chapterTopics.includes(t.id)),
    [topics],
  );

  useEffect(() => {
    if (spaceId === undefined) {
      navigate({ to: PathConstants.NOT_FOUND });
    }
  }, [spaceId, navigate]);

  useEffect(() => {
    if (spaceId !== undefined && !chapter) {
      navigate({
        to: PathConstants.SPACE_LEARN,
        params: { spaceId: String(spaceId) },
      });
    }
  }, [spaceId, chapter, navigate]);

  if (!spaceId || !chapter) {
    return (
      <div className="flex items-center justify-center h-full">
        <p>Loading chapter...</p>
      </div>
    );
  }

  return (
    <div className="flex h-full w-full justify-center overflow-hidden">
      <ScrollArea className="h-full w-full max-w-[90%] lg:max-w-[40%]">
        <div className="flex flex-col gap-6 px-4 py-6">
          <ChapterGuideBreadcrumbs chapter={chapter} />
          <div className="flex flex-col gap-4">
            {tops.map((t) => (
              <div
                key={t.id}
                className="border p-4 hover:bg-muted/50 transition-colors"
              >
                <div className={"flex justify-end text-foreground/40"}>
                  <InformationQuestionMark
                    tooltip={tr("chapter.guide.location", {
                      page: t.page,
                      documentName: t.sourceFileName,
                    })}
                  />
                </div>
                <h2 className="font-semibold">{t.title}</h2>
                <p className="mt-1 text-sm text-muted-foreground">
                  {t.summary}
                </p>
              </div>
            ))}
          </div>
        </div>
      </ScrollArea>
    </div>
  );
}

export default ChapterGuide;
