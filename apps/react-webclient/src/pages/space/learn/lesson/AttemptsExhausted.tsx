import {BatteryLow} from "lucide-react";
import {useTranslation} from "react-i18next";

export default function AttemptsExhausted() {
  const {t} = useTranslation();

  return (
      <div className={"flex flex-col items-center justify-center gap-6 text-center max-w-md mx-auto py-12"}>
        <div className="flex items-center justify-center rounded-full bg-secondary size-16">
          <BatteryLow size={28} className="text-muted-foreground" />
        </div>

        <div className="flex flex-col gap-2">
          <h2 className="text-lg font-semibold">{t("exercise.exhausted.title")}</h2>
          <p className="text-sm text-muted-foreground">
            {t("exercise.exhausted.description")}
          </p>
        </div>
      </div>
  );
}
