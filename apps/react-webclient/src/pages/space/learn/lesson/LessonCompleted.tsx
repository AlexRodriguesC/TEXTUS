import { useTranslation } from "react-i18next";
import LessonOutcomePanel from "@/components/LessonOutcomePanel.tsx";

function LessonCompleted() {
  const { t } = useTranslation();

  return (
    <LessonOutcomePanel
      className="md:flex-row"
      title={t("exercise.completed.title")}
      description={t("exercise.completed.description")}
    >
      <div className="flex items-center justify-center size-28 aspect-square">
        <img src={"/party.webp"} alt={"Party"} />
      </div>
    </LessonOutcomePanel>
  );
}

export default LessonCompleted;
