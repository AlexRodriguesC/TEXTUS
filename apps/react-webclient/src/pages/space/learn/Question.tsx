import { Button } from "@/components/ui/button.tsx";
import type { QuestionResponseDTO } from "@/client/model";
import { useMemo, useState } from "react";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";
import { cn } from "@/lib/utils.ts";

interface QuestionProps {
  question: QuestionResponseDTO;
  submitted: boolean;
  onAnswer?: (answer: string | null) => void;
}

function Question({ question, submitted, onAnswer }: QuestionProps) {
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);

  const answers = useMemo(
    () => question.distractors.concat(question.answer),
    [question.answer, question.distractors],
  );

  return (
    <div className={"flex flex-col gap-8"}>
      <ExerciseHeader question={question.request} topicId={question.topicId} />
      <div className={"grid grid-cols-2 grid-rows-2 gap-4"}>
        {answers.map((answer: string, i: number) => {
          const isSelected = i === selectedAnswer;
          const isCorrectOption = answer === question.answer;

          return (
            <Button
              disabled={submitted}
              variant={isSelected ? "secondary" : "outline"}
              key={i}
              onClick={() => {
                if (selectedAnswer === i) {
                  setSelectedAnswer(null);
                  onAnswer?.(null);
                  return;
                }
                setSelectedAnswer(i);
                onAnswer?.(answer);
              }}
              className={cn(
                "border-primary w-full h-auto min-w-0 whitespace-normal wrap-break-word text-wrap py-3 px-3 text-center",
                {
                  "bg-primary/40 text-primary-foreground":
                    submitted && isCorrectOption,
                  "bg-destructive/40 text-destructive-foreground":
                    submitted && isSelected && !isCorrectOption,
                },
              )}
            >
              {answer}
            </Button>
          );
        })}
      </div>
    </div>
  );
}

export default Question;
