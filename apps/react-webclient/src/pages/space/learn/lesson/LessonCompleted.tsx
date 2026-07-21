import { useTranslation } from "react-i18next";

function LessonCompleted() {
  const { t } = useTranslation();

  return (
    <div
      className={
        "flex flex-col md:flex-row items-center justify-center gap-6 text-center max-w-md mx-auto py-12"
      }
    >
      <div className="flex items-center justify-center size-28 aspect-square">
        <img src={"/party.webp"} alt={"Party"} />
      </div>

      <div className="flex flex-col gap-2">
        <h2 className="text-lg font-semibold">
          {t("exercise.completed.title")}
        </h2>
        <p className="text-sm text-muted-foreground">
          {t("exercise.completed.description")}
        </p>
      </div>
    </div>
  );
}

export default LessonCompleted;
