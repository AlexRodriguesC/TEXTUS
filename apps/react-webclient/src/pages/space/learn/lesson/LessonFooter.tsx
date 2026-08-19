import useLessonId from "@/hooks/useLessonId.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonNavigation from "@/hooks/learning/useLessonNavigation.ts";
import LessonFooterSkeleton from "@/pages/space/learn/lesson/LessonFooterSkeleton.tsx";
import MasteredFooter from "@/pages/space/learn/lesson/footer/MasteredFooter.tsx";
import CheckpointFooter from "@/pages/space/learn/lesson/footer/CheckpointFooter.tsx";
import ExhaustedFooter from "@/pages/space/learn/lesson/footer/ExhaustedFooter.tsx";
import ExerciseFooter from "@/pages/space/learn/lesson/footer/ExerciseFooter.tsx";

interface LessonFooterProps {
  onConfirm: () => void;
  onSkip: () => void;
  onNext?: () => void;
  confirmationEnabled?: boolean;
  skipEnabled?: boolean;
  wasCorrect?: boolean | null;
  answer?: unknown | null;
  submitted: boolean;
}

function LessonFooter({
  onConfirm,
  onSkip,
  onNext,
  confirmationEnabled = false,
  skipEnabled = true,
  wasCorrect,
  answer,
  submitted,
}: LessonFooterProps) {
  const { lessonId } = useLessonId();
  const { strSpaceId } = useSpaceId();
  const {
    lesson,
    exerciseLoading,
    nextExercise,
    deleteExercises,
    isDeletingExercises,
    invalidateLearningPath,
    resetNextExercise,
  } = useLesson({ lessonId });
  const navigation = useLessonNavigation();

  if (!lesson || !strSpaceId) {
    return <LessonFooterSkeleton className="w-full" />;
  }

  const content = (() => {
    if (lesson.isMastered()) {
      return (
        <MasteredFooter
          strSpaceId={strSpaceId}
          navigation={navigation}
          onInvalidatePath={() => invalidateLearningPath()}
        />
      );
    }

    if (lesson.isCheckpoint() && lesson.topics.length === 0) {
      return (
        <CheckpointFooter
          strSpaceId={strSpaceId}
          navigation={navigation}
          exerciseLoading={exerciseLoading}
        />
      );
    }

    if (lesson.attemptsLeft === 0) {
      return (
        <ExhaustedFooter
          strSpaceId={strSpaceId}
          chapterId={navigation.chapterId}
          onRetry={() => void deleteExercises()}
          isDeleting={isDeletingExercises}
        />
      );
    }

    return (
      <ExerciseFooter
        submitted={submitted}
        exerciseLoading={exerciseLoading}
        skipEnabled={skipEnabled}
        confirmationEnabled={confirmationEnabled}
        wasCorrect={wasCorrect}
        answer={answer}
        nextExercise={nextExercise}
        onConfirm={onConfirm}
        onSkip={onSkip}
        onNext={onNext}
        onAdvanceExercise={resetNextExercise}
      />
    );
  })();

  return (
    <div className={"flex flex-row items-center justify-between w-full"}>
      {content}
    </div>
  );
}

export default LessonFooter;
