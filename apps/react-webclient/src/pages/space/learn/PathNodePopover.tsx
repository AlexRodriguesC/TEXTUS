import {Button} from "@/components/ui/button.tsx";
import useLesson from "@/hooks/useLesson.tsx";
import RetryLessonButton from "@/components/RetryLessonButton.tsx";
import ReviewTopicsButton from "@/components/ReviewTopicsButton.tsx";
import LessonAttemptsIndicator from "@/components/LessonAttemptsIndicator.tsx";
import {cn} from "@/lib/utils.ts";
import {useTranslation} from "react-i18next";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useLearningPath from "@/hooks/useLearningPath.ts";
import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

interface PathNodePopoverProps {
    lessonId: number;
    className?: string;
}

function PathNodePopover({lessonId, className}: PathNodePopoverProps) {
    const {lesson, isLoading} = useLesson({lessonId: lessonId});
    const {t} = useTranslation();
    const {strSpaceId} = useSpaceId();
    const {path} = useLearningPath();
    const chapterId = path?.chapters.flatMap(chapter => chapter.lessons).findIndex(lesson => lesson.id === lessonId) ?? 0;

    const content = (() => {
        if (!lesson || isLoading) {
            return (
                <p className={"loading-dots"}>
                    {t("learning-path.popover.loading")}
                </p>
            )
        }

        if (lesson.isMastered()) {
            return (
                <>

                </>
            )
        }

        if (lesson.attemptsLeft === 0) {
            return (
                <div className={cn("flex flex-col gap-2 w-fit", className)}>
                    <RetryLessonButton lesson={lesson}/>
                    <ReviewTopicsButton onCancel={() => {}} strSpaceId={strSpaceId} chapterId={chapterId}/>
                </div>
            )
        }

        return (
            <div className={cn("flex flex-col gap-2", className)}>
                <div className={"flex flex-row md:flex-col gap-2"}>
                    <Link className="order-1 md:order-2" to={PathConstants.LESSON} params={{spaceId: strSpaceId!, lessonId: lesson.id.toString() ?? "", chapterId: chapterId.toString() ?? ""}}>
                        <Button className={"w-full"}>{t("button.continue")}</Button>
                    </Link>
                    <LessonAttemptsIndicator
                        className="order-2 md:order-1 place-self-center"
                        maxAttempts={lesson.maxAttempts}
                        attemptsLeft={lesson.attemptsLeft}
                    />
                </div>
                <ReviewTopicsButton className={"order-3"} onCancel={() => {}} strSpaceId={strSpaceId} chapterId={chapterId}/>
            </div>
        )
    })();

    return (
        <div className={"flex flex-col items-center w-fit"}>
            {content}
        </div>
    )
}

export default PathNodePopover
