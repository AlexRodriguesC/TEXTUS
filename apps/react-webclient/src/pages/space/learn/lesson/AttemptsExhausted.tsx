import { BatteryLow } from "lucide-react";
import { useTranslation } from "react-i18next";
import LessonOutcomePanel from "@/components/LessonOutcomePanel.tsx";

export default function AttemptsExhausted() {
  const { t } = useTranslation();

  return (
    <LessonOutcomePanel
      title={t("exercise.exhausted.title")}
      description={t("exercise.exhausted.description")}
    >
      <div className="flex items-center justify-center rounded-full bg-secondary size-16">
        <BatteryLow size={28} className="text-muted-foreground" />
      </div>
    </LessonOutcomePanel>
  );
}
