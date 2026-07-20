import { Button } from "@/components/ui/button.tsx";
import { Skeleton } from "@/components/ui/skeleton.tsx";
import { useTranslation } from "react-i18next";
import useLessonId from "@/hooks/useLessonId.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useLesson from "@/hooks/useLesson.tsx";
import {ArrowLeft, ArrowRight} from "lucide-react";
import { Link } from "@tanstack/react-router";
import useLearningPath from "@/hooks/useLearningPath.ts";
import PathConstants from "@/routing/pathConstants.ts";
import ReviewTopicsButton from "@/components/ReviewTopicsButton.tsx";
import RetryLessonButton from "@/components/RetryLessonButton.tsx";
import {useQueryClient} from "@tanstack/react-query";
import {getGetLearningPathQueryKey} from "@/client/generated/paths/paths.ts";

interface LessonFooterProps {
    onCancel: () => void;
    onConfirm: () => void;
    onNext?: () => void;
    onSkip?: () => void;
    confirmationEnabled?: boolean;
    submitted: boolean;
}

function LessonFooter({
                          onCancel,
                          onConfirm,
                          onNext,
                          confirmationEnabled = false,
                          submitted,
                      }: LessonFooterProps) {
    const { t } = useTranslation();
    const { lessonId } = useLessonId();
    const { spaceId, strSpaceId } = useSpaceId();
    const queryClient = useQueryClient();
    const { lesson, exerciseLoading } = useLesson({ lessonId: lessonId });
    const { path } = useLearningPath();

    const chapterId = path?.chapters.flatMap(chapter => chapter.lessons).findIndex(lesson => lesson.id === lessonId) ?? 0;

    const flatLessons = path?.chapters.flatMap((chapter) => chapter.lessons) ?? [];
    const currentIndex = flatLessons.findIndex((l) => l.id === lessonId);
    const nextLesson = currentIndex >= 0 && currentIndex < flatLessons.length - 1
        ? flatLessons[currentIndex + 1]
        : null;
    const nextChapter = nextLesson
        ? path?.chapters.find((c) => c.lessons.some((l) => l.id === nextLesson.id))
        : null;

    if (!lesson) {
        return (
            <div className={"flex flex-row items-center justify-between w-full"}>
                <Skeleton className={"h-9 w-24"} />
                <Skeleton className={"h-9 w-28"} />
            </div>
        );
    }

    const content = (() => {
        if (lesson.isMastered()) {
            return <>
                <ReviewTopicsButton strSpaceId={strSpaceId!} chapterId={chapterId} onCancel={onCancel} />

                <Link to={PathConstants.LESSON} params={{spaceId: strSpaceId!, lessonId: nextLesson?.id.toString() ?? "", chapterId: nextChapter?.index.toString() ?? ""}}>
                    <Button className={"flex flex-row items-center"} onClick={() => queryClient.invalidateQueries({ queryKey: getGetLearningPathQueryKey(spaceId)})}>
                        <ArrowRight size={16} />
                        {t("button.next")}
                    </Button>
                </Link>
            </>
        }

        if (lesson.isCheckpoint() && lesson.topics.length == 0) {
            return <>
                <Link to={PathConstants.SPACE_LEARN} params={{spaceId: strSpaceId}}>
                    <Button className={"flex flex-row items-center"} disabled={exerciseLoading} onClick={onCancel} variant={"outline"}>
                        <ArrowLeft size={16} />
                        {t("button.back")}
                    </Button>
                </Link>

                <Link to={PathConstants.LESSON} params={{spaceId: strSpaceId!, lessonId: nextLesson?.id.toString() ?? "", chapterId: nextChapter?.index.toString() ?? ""}}>
                    <Button className={"flex flex-row items-center"}>
                        <ArrowRight size={16} />
                        {t("button.next")}
                    </Button>
                </Link>
            </>
        }

        if (lesson.attemptsLeft === 0) {
            return (
                <>
                    <ReviewTopicsButton strSpaceId={strSpaceId!} chapterId={chapterId} onCancel={onCancel} />
                    <RetryLessonButton lesson={lesson}/>
                </>
            )
        }

        return (
            <>
                <Button disabled={exerciseLoading} onClick={onCancel} variant={"outline"}>
                    {t("button.skip")}
                </Button>
                {
                    submitted ? (
                        <Button disabled={exerciseLoading} onClick={onNext}>
                            {t("button.next")}
                        </Button>
                    ) : (
                        <Button disabled={!confirmationEnabled} onClick={onConfirm}>
                            {t("button.submit")}
                        </Button>
                    )
                }
            </>
        )
    })();

    return (
        <div className={"flex flex-row items-center justify-between w-full"}>
            {content}
        </div>
    );
}

export default LessonFooter;