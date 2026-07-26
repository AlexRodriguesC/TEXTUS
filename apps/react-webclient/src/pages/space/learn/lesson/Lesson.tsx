import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import LessonHeader from "@/pages/space/learn/lesson/LessonHeader.tsx";
import LessonFooter from "@/pages/space/learn/lesson/LessonFooter.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import AttemptsExhausted from "@/pages/space/learn/lesson/AttemptsExhausted.tsx";
import LessonCompleted from "@/pages/space/learn/lesson/LessonCompleted.tsx";
import LessonCheckpointCompleted from "@/pages/space/learn/lesson/LessonCheckpointCompleted.tsx";
import LessonPageSkeleton from "@/pages/space/learn/lesson/LessonPageSkeleton.tsx";
import LessonQuestionSkeleton from "@/pages/space/learn/LessonQuestionSkeleton.tsx";
import ExerciseRenderer from "@/pages/space/learn/ExerciseRenderer.tsx";
import useLessonSession from "@/hooks/learning/useLessonSession.ts";

function Lesson() {
  const { lessonId } = useLessonId();
  const { spaceId } = useSpaceId();
  const { isLoading, nextExercise, exerciseLoading, lesson, resetNextExercise } =
    useLesson({ lessonId });
  const {
    answer,
    setAnswer,
    submitted,
    wasCorrect,
    isAttemptLoading,
    handleNext,
    handleSkip,
    handleConfirm,
  } = useLessonSession({
    spaceId,
    lessonId,
    nextExercise,
    resetNextExercise,
  });

  if (isLoading) {
    return <LessonPageSkeleton />;
  }

  const content = (() => {
    if (exerciseLoading) {
      return <LessonQuestionSkeleton />;
    }

    if (lesson!.attemptsLeft === 0) {
      return <AttemptsExhausted />;
    }

    if (lesson!.isMastered()) {
      return <LessonCompleted />;
    }

    if (lesson!.isCheckpoint() && lesson?.topics.length === 0) {
      return <LessonCheckpointCompleted />;
    }

    return (
      <ExerciseRenderer
        exercise={nextExercise}
        submitted={submitted}
        onAnswer={setAnswer}
      />
    );
  })();

  return (
    <CenteredPage>
      <div className="flex flex-col items-center gap-4 h-full w-[90%] md:w-3/5">
        <div className="sticky top-4 flex items-center gap-3 w-full">
          <LessonHeader />
        </div>
        <div className="flex-1 min-h-0 overflow-y-auto w-full place-content-center">
          {content}
        </div>
        <Separator></Separator>
        <div className="sticky bottom-4 pt-4 flex items-center gap-3 w-full">
          <LessonFooter
            submitted={submitted}
            onConfirm={() => void handleConfirm()}
            onSkip={() => void handleSkip()}
            onNext={handleNext}
            confirmationEnabled={answer !== null && !isAttemptLoading}
            wasCorrect={wasCorrect}
            answer={answer}
            skipEnabled={!isAttemptLoading}
          />
        </div>
      </div>
    </CenteredPage>
  );
}

export default Lesson;
