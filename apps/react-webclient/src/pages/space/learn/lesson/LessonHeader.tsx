import useSpaceId from "@/hooks/useSpaceId.ts";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";
import { Link } from "@tanstack/react-router";
import { ArrowLeft } from "lucide-react";
import { Progress } from "@/components/ui/progress.tsx";
import { Button } from "@/components/ui/button.tsx";
import LessonAttemptsIndicator from "@/components/LessonAttemptsIndicator.tsx";
import LessonHeaderSkeleton from "@/pages/space/learn/lesson/LessonHeaderSkeleton.tsx";

function LessonHeader() {
  const { lessonId } = useLessonId();
  const { strSpaceId } = useSpaceId();
  const { lesson, isLoading } = useLesson({ lessonId: lessonId });

  if (isLoading || !lesson) {
    return <LessonHeaderSkeleton className="flex items-center gap-3 w-full" />;
  }

  const content = (() => {
    if (lesson.isMastered()) {
      return (
        <Progress
          value={100}
          className="flex-1 **:data-[slot=progress-track]:h-3 **:data-[slot=progress-indicator]:bg-green-500"
        />
      );
    }

    if (lesson.attemptsLeft > 0) {
      return (
        <Progress
          value={Math.min(0.95, Math.max(0.1, lesson.mastery)) * 100}
          className="flex-1 **:data-[slot=progress-track]:h-3"
        />
      );
    }

    if (lesson.attemptsLeft === 0) {
      return (
        <Progress
          value={100}
          className="flex-1 **:data-[slot=progress-track]:h-3 grayscale"
        />
      );
    }

    return (
      <Progress
        value={Math.min(0.95, Math.max(0.1, lesson.mastery)) * 100}
        className="flex-1 **:data-[slot=progress-track]:h-3"
      />
    );
  })();

  return (
    <>
      <Link
        to="/spaces/$spaceId/learn"
        params={{ spaceId: strSpaceId! }}
        className="inline-flex items-center justify-center"
      >
        <Button variant="ghost" size="icon">
          <ArrowLeft className="h-5 w-5" />
        </Button>
      </Link>

      {content}

      <LessonAttemptsIndicator
        maxAttempts={lesson.maxAttempts}
        attemptsLeft={lesson.attemptsLeft}
      />
    </>
  );
}

export default LessonHeader;
