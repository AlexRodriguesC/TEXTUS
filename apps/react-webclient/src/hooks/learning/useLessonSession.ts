import { useState } from "react";
import type { Exercise } from "@/classes/Exercise.ts";
import useSubmitSolution from "@/hooks/useSubmitSolution.tsx";
import { evaluateExerciseAnswer } from "@/pages/space/learn/evaluateExerciseAnswer.ts";
import { failureToast } from "@/lib/toasts.tsx";
import { useTranslation } from "react-i18next";

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
  const { t } = useTranslation();
  const [answer, setAnswer] = useState<unknown | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [wasCorrect, setWasCorrect] = useState<boolean | null>(null);
  const [persistSucceeded, setPersistSucceeded] = useState(false);
  const { isPending: isAttemptLoading, submitSolution } = useSubmitSolution({
    spaceId,
    lessonId,
  });

  const persistAttempt = async (response: unknown): Promise<void> => {
    if (!nextExercise) return;
    try {
      const result = await submitSolution(
        nextExercise.id,
        nextExercise.type,
        response,
      );
      if (result) {
        setPersistSucceeded(true);
        return;
      }
      failureToast(t("exercise.submit.error"));
    } catch {
      failureToast(t("exercise.submit.error"));
    }
  };

  const handleNext = () => {
    if (!persistSucceeded) return;
    void resetNextExercise();
  };

  const handleSkip = async () => {
    if (!nextExercise || isAttemptLoading || submitted) return;
    setWasCorrect(false);
    setSubmitted(true);
    await persistAttempt(null);
  };

  const handleConfirm = async () => {
    if (!nextExercise || answer === null || isAttemptLoading || submitted)
      return;
    setWasCorrect(evaluateExerciseAnswer(nextExercise, answer));
    setSubmitted(true);
    await persistAttempt(answer);
  };

  return {
    answer,
    setAnswer,
    submitted,
    wasCorrect,
    isAttemptLoading,
    persistSucceeded,
    handleNext,
    handleSkip,
    handleConfirm,
  };
}

export default useLessonSession;
