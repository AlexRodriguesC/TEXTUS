import { Button } from "@/components/ui/button.tsx";
import type { QuestionResponseDTO } from "@/client/model";
import {useMemo, useState} from "react";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import {cn} from "@/lib/utils.ts";

interface QuestionProps {
  question: QuestionResponseDTO;
  submitted: boolean;
  onAnswer?: (answer: string | null, isCorrect: boolean | null) => void;
}

function Question({ question, submitted, onAnswer }: QuestionProps) {
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [isCorrect, setIsCorrect] = useState<boolean | null>(null);

  const answers = useMemo(() => question.distractors.concat(question.answer), [question.answer, question.distractors]);

  return (
    <div className={"flex flex-col gap-8"}>
      <ExerciseHeader question={question.request} topicId={question.topicId} />
      <div className={"grid grid-cols-2 grid-rows-2 gap-4"}>
        {answers.map((answer: string, i: number) => (
          <Button
              disabled={submitted}
            variant={i == selectedAnswer ? "secondary" : "outline"}
            key={i}
            onClick={() => {
              if (selectedAnswer === i) {
                setSelectedAnswer(null);
                onAnswer?.(null, null);
                return;
              }
              setIsCorrect(answer === question.answer);
              setSelectedAnswer(i);
              onAnswer?.(answer, isCorrect);
            }}
            className={cn("border-primary w-full h-auto min-w-0 whitespace-normal wrap-break-word text-wrap py-3 px-3 text-center", {
              "bg-primary text-primary-foreground": i == selectedAnswer && isCorrect && submitted,
              "bg-destructive text-destructive-foreground": i == selectedAnswer && !isCorrect  && submitted,
            })}
          >
            {answer}
          </Button>
        ))}
      </div>
    </div>
  );
}

export default Question;
