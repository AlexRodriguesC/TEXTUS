import useSpaceId from "@/hooks/useSpaceId.ts";
import useLesson from "@/hooks/useLesson.tsx";
import useLessonId from "@/hooks/useLessonId.ts";
import {Link} from "@tanstack/react-router";
import {Button} from "@base-ui/react";
import {ArrowLeft} from "lucide-react";
import {Progress} from "@/components/ui/progress.tsx";

function LessonHeader() {
    const { lessonId } = useLessonId();
    const { strSpaceId } = useSpaceId();
    const { lesson, isLoading } = useLesson({lessonId: lessonId});

    if (isLoading) {
        return <div>Loading...</div>;
    }

    const maxAttempts = lesson!.maxAttempts;

    return (
        <>
            <Link
                to="/spaces/$spaceId/learn"
                params={{ spaceId: strSpaceId! }}
                className="inline-flex items-center justify-center"
            >
                <Button variant="ghost" size="icon">
                    <ArrowLeft className="h-5 w-5" />
                </Button>
            </Link>

            <Progress
                value={Math.min(0.95, Math.max(0.10, lesson!.mastery)) * 100}
                className="flex-1 **:data-[slot=progress-track]:h-3"
            />

            {/* Mobile: single heart with count */}
            <div className="flex sm:hidden items-center gap-1">
                <img src="/heart.webp" alt={"hearts"} className="h-10 w-10" />
                <span className="text-sm font-semibold">x{lesson!.attemptsLeft}</span>
            </div>

            {/* Desktop/tablet: stacked hearts */}
            <div className="hidden sm:flex">
                {Array.from({ length: maxAttempts }).map((_, i) => (
                    <img
                        alt={"hearts"}
                        src="/heart.webp"
                        key={i}
                        className={`h-10 w-10 -ml-4 first:ml-0 ${i >= lesson!.attemptsLeft ? "grayscale" : ""}`}
                        style={{ zIndex: maxAttempts - i }}
                    />
                ))}
            </div>
        </>
    )
}

export default LessonHeader;
