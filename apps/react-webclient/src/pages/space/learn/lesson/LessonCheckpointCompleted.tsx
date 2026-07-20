import {useTranslation} from "react-i18next";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";

function LessonCheckpointCompleted() {
    const {t} = useTranslation();
    const {lessonId} = useLessonId();
    const {lesson} = useLesson({lessonId});
    const hadAnyExercises = lesson?.topics.length ?? [].length > 0;

    return (
        <div className={"flex flex-col md:flex-row items-center justify-center gap-6 text-center max-w-md mx-auto py-12"}>
            <div className="flex items-center justify-center size-28 aspect-square">
                <img src={"/trophy.webp"} alt={"Party"}/>
            </div>

            <div className="flex flex-col gap-2">
                <h2 className="text-lg font-semibold">{t("exercise.completed.title")}</h2>
                <p className="text-sm text-muted-foreground">
                    {hadAnyExercises ? t("exercise.checkpoint.description.exercises") : t("exercise.checkpoint.description.no-exercises")}
                </p>
            </div>
        </div>
    )
}

export default LessonCheckpointCompleted
