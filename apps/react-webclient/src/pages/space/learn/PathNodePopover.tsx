import {Button} from "@/components/ui/button.tsx";
import useLesson from "@/hooks/useLesson.tsx";
import RetryLessonButton from "@/components/RetryLessonButton.tsx";
import ReviewTopicsButton from "@/components/ReviewTopicsButton.tsx";
import LessonAttemptsIndicator from "@/components/LessonAttemptsIndicator.tsx";
import {cn} from "@/lib/utils.ts";
import {useTranslation} from "react-i18next";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useLearningPath from "@/hooks/useLearningPath.ts";

interface PathNodePopoverProps {
    lessonId: number;
    className?: string;
}

function PathNodePopover({lessonId, className}: PathNodePopoverProps) {
    const {isLoading, lesson} = useLesson({lessonId: lessonId});
    const {t} = useTranslation();
    const {strSpaceId} = useSpaceId();
    const {path} = useLearningPath();
    const chapterId = path?.chapters.flatMap(chapter => chapter.lessons).findIndex(lesson => lesson.id === lessonId) ?? 0;

    const content = (() => {
        if (isLoading) {
            return <div>Loading...</div>;
        }

        if (lesson!.isMastered()) {
            return (
                <>

                </>
            )
        }

        if (lesson!.attemptsLeft === 0) {
            return (
                <>
                    <RetryLessonButton lesson={lesson!}/>
                    <ReviewTopicsButton onCancel={() => {}} strSpaceId={strSpaceId} chapterId={chapterId}/>
                </>
            )
        }

        return (
            <div className={cn("flex flex-col gap-2", className)}>
                <div className={"flex flex-row md:flex-col gap-2"}>
                    <Button className="order-1 md:order-2">{t("button.continue")}</Button>
                    <LessonAttemptsIndicator
                        className="order-2 md:order-1 place-self-center"
                        maxAttempts={lesson!.maxAttempts}
                        attemptsLeft={lesson!.attemptsLeft}
                    />
                </div>
                <ReviewTopicsButton className={"order-3"} onCancel={() => {}} strSpaceId={strSpaceId} chapterId={chapterId}/>
            </div>
        )
    })();

    return (
        <div className={"flex flex-col items-center"}>
            {content}
        </div>
    )
}

export default PathNodePopover
