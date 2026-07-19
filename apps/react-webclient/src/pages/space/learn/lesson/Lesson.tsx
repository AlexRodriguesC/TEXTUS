import Question from "@/pages/space/learn/Question.tsx";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import LessonHeader from "@/pages/space/learn/lesson/LessonHeader.tsx";
import LessonFooter from "@/pages/space/learn/lesson/LessonFooter.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { useMemo, useState } from "react";
import List from "@/pages/space/learn/List.tsx";
import Group from "@/pages/space/learn/Group.tsx";
import Pair from "@/pages/space/learn/Pair.tsx";
import useSubmitSolution from "@/hooks/useSubmitSolution.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import AttemptsExhausted from "@/pages/space/learn/lesson/AttemptsExhausted.tsx";
import {useQueryClient} from "@tanstack/react-query";
import {getGetNextExerciseQueryKey} from "@/client/generated/lessons/lessons.ts";
import LessonCompleted from "@/pages/space/learn/lesson/LessonCompleted.tsx";

function Lesson() {
  const queryClient = useQueryClient();
  const { lessonId } = useLessonId();
  const { spaceId } = useSpaceId();
  const { isLoading, nextExercise, exerciseLoading, lesson } = useLesson({
    lessonId: lessonId,
  });
  const [answer, setAnswer] = useState<any | null>(null);
  const [isCorrect, setIsCorrect] = useState<boolean | null>(null);
  const [submitted, setSubmitted] = useState<boolean>(false);
  const { isPending: isAttemptLoading, submitSolution } = useSubmitSolution({
    spaceId,
    lessonId,
  });

  const handleAnswer = (answer: any, isCorrect: boolean | null) => {
    setAnswer(answer);
    setIsCorrect(isCorrect);
  }

  const handleNext = () => {
    queryClient.invalidateQueries({queryKey: getGetNextExerciseQueryKey(spaceId!, lessonId!)})
  }

  const exercise = useMemo(() => {
    if (isLoading || exerciseLoading || !nextExercise) {
      return <div>loading</div>;
    }

    if (nextExercise.type === "LIST") {
      return <List list={nextExercise} onAnswer={setAnswer}></List>;
    } else if (nextExercise.type === "QUESTION") {
      return <Question question={nextExercise} submitted={submitted} onAnswer={handleAnswer}></Question>;
    } else if (nextExercise.type === "GROUP") {
      return <Group group={nextExercise} onAnswer={setAnswer}></Group>;
    } else if (nextExercise.type === "PAIR") {
      return <Pair pair={nextExercise} onAnswer={setAnswer}></Pair>;
    }
    return <div>Invalid exercise type</div>;
  }, [nextExercise, exerciseLoading, isLoading, isAttemptLoading]);

  const handleConfirm = () => {
    if (!nextExercise || answer === null) return;
    submitSolution(nextExercise.id, nextExercise.type, answer);
    setSubmitted(true);
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  const content = (() => {
    if (lesson!.attemptsLeft === 0) {
      return <AttemptsExhausted />;
    }
    if (lesson!.isMastered()) {
      return <LessonCompleted />;
    }
    if (exerciseLoading) {
      return <div className="flex flex-col items-center gap-4">Loading...</div>;
    }
    return exercise;
  })();

  return (
      <CenteredPage>
        <div className="flex flex-col items-center gap-4 h-full w-[90%] md:w-3/5">
          <div className="sticky top-4 flex items-center gap-3 w-full">
            <LessonHeader />
          </div>
          <div className="flex-1 min-h-0 overflow-y-auto w-full place-content-center">
            {content}
          </div>
          <Separator></Separator>
          <div className="sticky bottom-4 pt-4 flex items-center gap-3 w-full">
            <LessonFooter
                onCancel={() => setAnswer(null)}
                submitted={submitted}
                onConfirm={handleConfirm}
                onNext={handleNext}
                confirmationEnabled={answer !== null && !isAttemptLoading}
            />
          </div>
        </div>
      </CenteredPage>
  );
}

export default Lesson;
