import type { ReactNode } from "react";
import { cn } from "@/lib/utils.ts";

interface LessonOutcomePanelProps {
  title: string;
  description: string;
  children: ReactNode;
  className?: string;
}

function LessonOutcomePanel({
  title,
  description,
  children,
  className,
}: LessonOutcomePanelProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-6 text-center max-w-md mx-auto py-12",
        className,
      )}
    >
      {children}
      <div className="flex flex-col gap-2">
        <h2 className="text-lg font-semibold">{title}</h2>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
    </div>
  );
}

export default LessonOutcomePanel;
