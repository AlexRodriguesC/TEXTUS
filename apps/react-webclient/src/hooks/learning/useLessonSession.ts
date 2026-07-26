import { useState } from "react";
import type { Exercise } from "@/classes/Exercise.ts";
import useSubmitSolution from "@/hooks/useSubmitSolution.tsx";

interface UseLessonSessionOptions {
  spaceId: number | undefined;
  lessonId: number | undefined;
  nextExercise: Exercise | undefined;
  resetNextExercise: () => Promise<void> | void;
}

function useLessonSession({
  spaceId,
  lessonId,
  nextExercise,
  resetNextExercise,
}: UseLessonSessionOptions) {
  const [answer, setAnswer] = useState<unknown | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [wasCorrect, setWasCorrect] = useState<boolean | null>(null);
  const { isPending: isAttemptLoading, submitSolution } = useSubmitSolution({
    spaceId,
    lessonId,
  });

  const handleNext = () => {
    void resetNextExercise()?.then(() => {
      setAnswer(null);
      setSubmitted(false);
      setWasCorrect(null);
    });
  };

  const handleSkip = async () => {
    if (!nextExercise || isAttemptLoading) return;
    const result = await submitSolution(
      nextExercise.id,
      nextExercise.type,
      null,
    );
    setWasCorrect(result?.correct ?? false);
    setSubmitted(true);
  };

  const handleConfirm = async () => {
    if (!nextExercise || answer === null || isAttemptLoading) return;
    const result = await submitSolution(
      nextExercise.id,
      nextExercise.type,
      answer,
    );
    setWasCorrect(result?.correct ?? false);
    setSubmitted(true);
  };

  return {
    answer,
    setAnswer,
    submitted,
    wasCorrect,
    isAttemptLoading,
    handleNext,
    handleSkip,
    handleConfirm,
  };
}

export default useLessonSession;
