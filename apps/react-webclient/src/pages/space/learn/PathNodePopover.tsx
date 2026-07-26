import { Button } from "@/components/ui/button.tsx";
import useLesson from "@/hooks/useLesson.tsx";
import RetryLessonButton from "@/components/RetryLessonButton.tsx";
import ReviewTopicsButton from "@/components/ReviewTopicsButton.tsx";
import LessonAttemptsIndicator from "@/components/LessonAttemptsIndicator.tsx";
import { cn } from "@/lib/utils.ts";
import { useTranslation } from "react-i18next";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

interface PathNodePopoverProps {
  lessonId: number;
  className?: string;
}

function PathNodePopover({ lessonId, className }: PathNodePopoverProps) {
  const { lesson, isLoading, deleteExercises, isDeletingExercises } = useLesson({
    lessonId: lessonId,
  });
  const { t } = useTranslation();
  const { strSpaceId } = useSpaceId();
  const { path } = useLearningPath();
  const chapterId =
    path?.chapters
      .flatMap((chapter) => chapter.lessons)
      .findIndex((lesson) => lesson.id === lessonId) ?? 0;

  const content = (() => {
    if (!lesson || isLoading) {
      return (
        <p className={"loading-dots"}>{t("learning-path.popover.loading")}</p>
      );
    }

    if (lesson.isMastered()) {
        return (
            <div className={cn("flex flex-col gap-2", className)}>
                <ReviewTopicsButton
                    onCancel={() => {}}
                    strSpaceId={strSpaceId}
                    chapterId={chapterId}
                />
            </div>
        );
    }

    if (lesson.attemptsLeft === 0) {
        return (
            <div className={cn("flex flex-col gap-2", className)}>
                <RetryLessonButton
                  onRetry={() => void deleteExercises()}
                  isDeleting={isDeletingExercises}
                />
                <ReviewTopicsButton
                    onCancel={() => {}}
                    strSpaceId={strSpaceId}
                    chapterId={chapterId}
                />
            </div>
        );
    }

    return (
      <div className={cn("flex flex-col gap-2 w-full", className)}>
        <div className={"flex flex-row md:flex-col gap-2 w-full"}>
          <Link
            className="order-1 md:order-2 w-full"
            to={PathConstants.LESSON}
            params={{
              spaceId: strSpaceId!,
              lessonId: lesson.id.toString() ?? "",
              chapterId: chapterId.toString() ?? "",
            }}
          >
            <Button className={"w-full"}>{t("button.continue")}</Button>
          </Link>
          <LessonAttemptsIndicator
            className="order-2 md:order-1 place-content-center place-items-center w-full"
            maxAttempts={lesson.maxAttempts}
            attemptsLeft={lesson.attemptsLeft}
          />
        </div>
        <ReviewTopicsButton
          className={"order-3 w-full"}
          onCancel={() => {}}
          strSpaceId={strSpaceId}
          chapterId={chapterId}
        />
      </div>
    );
  })();

  return <div className={"flex flex-col items-center w-full"}>{content}</div>;
}

export default PathNodePopover;
