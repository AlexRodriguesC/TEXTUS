import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import type { Exercise } from "@/classes/Exercise.ts";
import ExerciseHelpButton from "@/pages/space/learn/ExerciseHelpButton.tsx";

interface ExerciseFooterProps {
  submitted: boolean;
  exerciseLoading: boolean;
  skipEnabled: boolean;
  confirmationEnabled: boolean;
  wasCorrect?: boolean | null;
  answer?: unknown | null;
  nextExercise?: Exercise;
  onConfirm: () => void;
  onSkip: () => void;
  onNext?: () => void;
}

function ExerciseFooter({
  submitted,
  exerciseLoading,
  skipEnabled,
  confirmationEnabled,
  wasCorrect,
  answer,
  nextExercise,
  onConfirm,
  onSkip,
  onNext,
}: ExerciseFooterProps) {
  const { t } = useTranslation();

  return (
    <>
      {!submitted && (
        <Button
          disabled={exerciseLoading || !skipEnabled}
          onClick={onSkip}
          variant={"outline"}
        >
          {t("button.skip")}
        </Button>
      )}
      {nextExercise && submitted && !wasCorrect && (
        <ExerciseHelpButton
          exercise={nextExercise}
          userAnswer={answer}
          topicId={nextExercise.topicId}
        />
      )}
      {submitted ? (
        <Button
          disabled={exerciseLoading}
          onClick={onNext}
          className="ml-auto"
        >
          {t("button.next")}
        </Button>
      ) : (
        <Button
          disabled={!confirmationEnabled}
          onClick={onConfirm}
          className="ml-auto"
        >
          {t("button.submit")}
        </Button>
      )}
    </>
  );
}

export default ExerciseFooter;
