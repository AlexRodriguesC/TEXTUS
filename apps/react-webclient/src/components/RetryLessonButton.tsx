import {Spinner} from "@/components/ui/spinner.tsx";
import {RotateCcw} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {useTranslation} from "react-i18next";
import type {Lesson} from "@/hooks/useLesson.tsx";

function RetryLessonButton({lesson}: {lesson: Lesson}) {
    const { t } = useTranslation();

    return (
        <Button disabled={lesson?.isDeleting} onClick={() => lesson?.deleteExercises()}>
            {lesson?.isDeleting ? <Spinner/> : <RotateCcw size={16} />}
            {t("button.retry")}
        </Button>
    )
}

export default RetryLessonButton;
