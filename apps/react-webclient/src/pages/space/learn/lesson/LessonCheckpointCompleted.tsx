import { useTranslation } from "react-i18next";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";
import LessonOutcomePanel from "@/components/LessonOutcomePanel.tsx";

function LessonCheckpointCompleted() {
  const { t } = useTranslation();
  const { lessonId } = useLessonId();
  const { lesson } = useLesson({ lessonId });
  const hadAnyExercises = lesson?.topics.length ?? [].length > 0;

  return (
    <LessonOutcomePanel
      className="md:flex-row"
      title={t("exercise.completed.title")}
      description={
        hadAnyExercises
          ? t("exercise.checkpoint.description.exercises")
          : t("exercise.checkpoint.description.no-exercises")
      }
    >
      <div className="flex items-center justify-center size-28 aspect-square">
        <img src={"/trophy.webp"} alt={"Party"} />
      </div>
    </LessonOutcomePanel>
  );
}

export default LessonCheckpointCompleted;
