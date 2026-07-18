import {Button} from "@/components/ui/button.tsx";
import type {QuestionResponseDTO} from "@/client/model";
import {useState} from "react";
import ExerciseHeader from "@/pages/space/learn/ExerciseHeader.tsx";

interface QuestionProps {
    question: QuestionResponseDTO;
    onAnswer?: (answer: string | null) => void;
}

function Question({ question, onAnswer }: QuestionProps) {
    const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);

    return (
        <div className={"flex flex-col gap-8"}>
            <ExerciseHeader question={question.request} topicId={question.topicId}/>
            <div className={"grid grid-cols-2 grid-rows-2 gap-4"}>
                {question.answers.map((answer:string, i:number) => (
                    <Button
                        variant={i == selectedAnswer ? "secondary" : "outline"}
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
                        className="border-primary w-full h-auto min-w-0 whitespace-normal wrap-break-word text-wrap py-3 px-3 text-center"
                    >
                        {answer}
                    </Button>
                ))}
            </div>
        </div>
    );
}

export default Question;
