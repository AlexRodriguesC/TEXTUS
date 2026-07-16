import {ArrowLeft} from "lucide-react";
import {Progress} from "@/components/ui/progress.tsx";
import {Button} from "@/components/ui/button.tsx";
import Question from "@/pages/space/learn/Question.tsx";

function Lesson() {
    const attemptsLeft = 10;

    return (
        <div>
            <div className="flex flex-col items-center gap-4 w-full">
                <div className="">
                    <Button variant="ghost" size="icon" onClick={() => {}}>
                        <ArrowLeft className="h-5 w-5" />
                    </Button>

                    <Progress value={100} className="flex-1 **:data-[slot=progress-track]:h-3" />

                    <div className="flex">
                        {Array.from({ length: attemptsLeft }).map((_, i) => (
                            <img
                                src="/heart.webp"
                                key={i}
                                className="h-10 w-10 -ml-4 first:ml-0"
                                style={{ zIndex: attemptsLeft - i }}
                            />
                        ))}
                    </div>
                </div>
                <Question />
            </div>
        </div>
    );
}

export default Lesson;
