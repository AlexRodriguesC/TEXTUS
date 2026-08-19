import { cn } from "@/lib/utils.ts";

interface LessonAttemptsIndicatorProps {
  maxAttempts: number;
  attemptsLeft: number;
  className?: string;
}

function LessonAttemptsIndicator({
  maxAttempts,
  attemptsLeft,
  className,
}: LessonAttemptsIndicatorProps) {
  return (
    <div className={className}>
      {/* Mobile: single heart with count */}
      <div className={cn("flex sm:hidden items-center gap-1", className)}>
        <img src="/heart.webp" alt={"hearts"} className="h-10 w-10" />
        <span className="text-sm font-semibold">x{attemptsLeft}</span>
      </div>

      {/* Desktop/tablet: stacked hearts */}
      <div className={cn("hidden sm:flex w-full", className)}>
        {Array.from({ length: maxAttempts }).map((_, i) => (
          <img
            alt={"hearts"}
            src="/heart.webp"
            key={i}
            className={cn(
              "h-10 w-10 -ml-4 first:ml-0",
              i >= attemptsLeft && "grayscale",
            )}
            style={{ zIndex: maxAttempts - i }}
          />
        ))}
      </div>
    </div>
  );
}

export default LessonAttemptsIndicator;
