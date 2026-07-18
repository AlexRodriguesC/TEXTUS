import Question from "@/pages/space/learn/Question.tsx";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import LessonHeader from "@/pages/space/learn/lesson/LessonHeader.tsx";
import LessonFooter from "@/pages/space/learn/lesson/LessonFooter.tsx";
import {Separator} from "@/components/ui/separator.tsx";
import {useMemo, useState} from "react";
import List from "@/pages/space/learn/List.tsx";
import Group from "@/pages/space/learn/Group.tsx";
import Pair from "@/pages/space/learn/Pair.tsx";
import useSubmitSolution from "@/hooks/useSubmitSolution.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import {useQueryClient} from "@tanstack/react-query";
import {getGetLessonQueryKey} from "@/client/generated/lessons/lessons.ts";

function Lesson() {
    const { lessonId } = useLessonId();
    const { spaceId } = useSpaceId();
    const { isLoading, nextExercise, exerciseLoading } = useLesson({lessonId: lessonId});
    const [answer, setAnswer] = useState<any | null>(null);
    const {isPending:isAttemptLoading, submitSolution} = useSubmitSolution({spaceId, lessonId});

    const exercise = useMemo(() => {
        if (isLoading || exerciseLoading || !nextExercise) {
            return <div>loading</div>
        }

        if (nextExercise.type === "LIST") {
            return <List list={nextExercise}  onAnswer={setAnswer}></List>
        } else if (nextExercise.type === "QUESTION") {
            return <Question question={nextExercise} onAnswer={setAnswer}></Question>
        } else if (nextExercise.type === "GROUP") {
            return <Group group={nextExercise}  onAnswer={setAnswer}></Group>
        } else if (nextExercise.type === "PAIR") {
            return <Pair pair={nextExercise}  onAnswer={setAnswer}></Pair>
        }
        return <div>Invalid exercise type</div>
    }, [nextExercise, exerciseLoading, isLoading])

    const handleConfirm = () => {
        if (!nextExercise || answer === null) return;
        submitSolution(nextExercise.id, nextExercise.type, answer);
    };

    if (isLoading) {
        return <div>Loading...</div>;
    }

    return (
        <CenteredPage>
            <div className="flex flex-col items-center gap-4 h-full w-[90%] md:w-3/5">
                <div className="sticky top-4 flex items-center gap-3 w-full">
                    <LessonHeader />
                </div>
                <div className="flex-1 min-h-0 overflow-y-auto w-full place-content-center">
                    {exerciseLoading ? (
                        <div className="flex flex-col items-center gap-4">Loading...</div>
                    ) : (
                        exercise
                    )}
                </div>
                <Separator></Separator>
                <div className="sticky bottom-4 pt-4 flex items-center gap-3 w-full">
                    <LessonFooter
                        onCancel={() => setAnswer(null)}
                        onConfirm={handleConfirm}
                        confirmationEnabled={answer !== null && !isAttemptLoading}
                    />
                </div>
            </div>
        </CenteredPage>
    );
}

export default Lesson;
