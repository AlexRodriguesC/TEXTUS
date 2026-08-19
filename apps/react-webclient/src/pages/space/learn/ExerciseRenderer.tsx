import type { Exercise } from "@/classes/Exercise.ts";
import ExerciseType from "@/classes/ExerciseType.ts";
import Question from "@/pages/space/learn/Question.tsx";
import List from "@/pages/space/learn/List.tsx";
import Group from "@/pages/space/learn/group/Group.tsx";
import Pair from "@/pages/space/learn/pair/Pair.tsx";
import LessonQuestionSkeleton from "@/pages/space/learn/LessonQuestionSkeleton.tsx";

interface ExerciseRendererProps {
  exercise?: Exercise;
  submitted: boolean;
  onAnswer: (answer: unknown | null) => void;
}

function ExerciseRenderer({
  exercise,
  submitted,
  onAnswer,
}: ExerciseRendererProps) {
  if (!exercise) {
    return <LessonQuestionSkeleton />;
  }

  switch (exercise.type) {
    case ExerciseType.LIST:
      return (
        <List
          key={exercise.id}
          list={exercise}
          submitted={submitted}
          onAnswer={onAnswer}
        />
      );
    case ExerciseType.QUESTION:
      return (
        <Question
          key={exercise.id}
          question={exercise}
          submitted={submitted}
          onAnswer={onAnswer}
        />
      );
    case ExerciseType.GROUP:
      return (
        <Group
          key={exercise.id}
          group={exercise}
          onAnswer={onAnswer}
          submitted={submitted}
        />
      );
    case ExerciseType.PAIR:
      return (
        <Pair
          key={exercise.id}
          pair={exercise}
          onAnswer={onAnswer}
          submitted={submitted}
        />
      );
    default:
      throw new Error("Invalid exercise type");
  }
}

export default ExerciseRenderer;
