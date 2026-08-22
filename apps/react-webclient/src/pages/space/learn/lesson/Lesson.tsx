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
import type { Exercise } from "@/classes/Exercise.ts";
import type LessonModel from "@/classes/Lesson.ts";

function Lesson() {
  const { lessonId } = useLessonId();
  return <LessonView key={lessonId} />;
}

interface LessonExerciseSessionProps {
  spaceId: number | undefined;
  lessonId: number | undefined;
  nextExercise: Exercise | undefined;
  exerciseLoading: boolean;
  resetNextExercise: () => Promise<void> | void;
}

function LessonExerciseSession({
  spaceId,
  lessonId,
  nextExercise,
  exerciseLoading,
  resetNextExercise,
}: LessonExerciseSessionProps) {
  const {
    answer,
    setAnswer,
    submitted,
    wasCorrect,
    isAttemptLoading,
    persistSucceeded,
    handleNext,
    handleSkip,
    handleConfirm,
  } = useLessonSession({
    spaceId,
    lessonId,
    nextExercise,
    resetNextExercise,
  });

  return (
    <>
      <div className="flex-1 min-h-0 overflow-y-auto w-full place-content-center">
        {exerciseLoading ? (
          <LessonQuestionSkeleton />
        ) : (
          <ExerciseRenderer
            exercise={nextExercise}
            submitted={submitted}
            onAnswer={setAnswer}
          />
        )}
      </div>
      <Separator />
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
          persistPending={!persistSucceeded}
        />
      </div>
    </>
  );
}

function LessonOutcome({ lesson }: { lesson: LessonModel }) {
  const content = (() => {
    if (lesson.attemptsLeft === 0) {
      return <AttemptsExhausted />;
    }

    if (lesson.isMastered()) {
      return <LessonCompleted />;
    }

    return <LessonCheckpointCompleted />;
  })();

  return (
    <>
      <div className="flex-1 min-h-0 overflow-y-auto w-full place-content-center">
        {content}
      </div>
      <Separator />
      <div className="sticky bottom-4 pt-4 flex items-center gap-3 w-full">
        <LessonFooter
          submitted={false}
          onConfirm={() => undefined}
          onSkip={() => undefined}
        />
      </div>
    </>
  );
}

function LessonView() {
  const { lessonId } = useLessonId();
  const { spaceId } = useSpaceId();
  const { isLoading, nextExercise, exerciseLoading, lesson, resetNextExercise } =
    useLesson({ lessonId });

  if (isLoading || !lesson) {
    return <LessonPageSkeleton />;
  }

  const isOutcome =
    lesson.attemptsLeft === 0 ||
    lesson.isMastered() ||
    (lesson.isCheckpoint() && lesson.topics.length === 0);

  return (
    <CenteredPage>
      <div className="flex flex-col items-center gap-4 h-full w-full">
        <div className="sticky top-4 flex items-center gap-3 w-full">
          <LessonHeader />
        </div>
        {isOutcome ? (
          <LessonOutcome lesson={lesson} />
        ) : (
          <LessonExerciseSession
            key={nextExercise?.id ?? "pending"}
            spaceId={spaceId}
            lessonId={lessonId}
            nextExercise={nextExercise}
            exerciseLoading={exerciseLoading}
            resetNextExercise={resetNextExercise}
          />
        )}
      </div>
    </CenteredPage>
  );
}

export default Lesson;
