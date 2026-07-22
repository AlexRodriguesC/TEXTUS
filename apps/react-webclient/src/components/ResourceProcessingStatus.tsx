import * as React from "react";
import { CheckCircle2, type LucideIcon } from "lucide-react";
import { Progress } from "@/components/ui/progress.tsx";
import { cn } from "@/lib/utils.ts";

type TaskVariant = "default" | "success" | "error";

interface Task {
  id: string;
  label: string;
  icon: LucideIcon;
  progress: number;
  maxProgress: number;
  variant?: TaskVariant;
}

interface TaskProgressProps {
  task: Task;
  variant?: TaskVariant;
}

interface ResourceProcessingStatusProps {
  tasks: Task[];
  variant?: TaskVariant;
}

const ANIMATION_DURATION_MS = 500;

const INDICATOR_VARIANT_CLASSES: Record<TaskVariant, string> = {
  default: "**:data-[slot=progress-indicator]:bg-primary",
  success: "**:data-[slot=progress-indicator]:bg-green-500",
  error: "**:data-[slot=progress-indicator]:bg-destructive",
};

function easeOutCubic(t: number): number {
  return 1 - Math.pow(1 - t, 3);
}

function useAnimatedNumber(value: number, duration = ANIMATION_DURATION_MS): number {
  const [display, setDisplay] = React.useState(value);
  const displayRef = React.useRef(value);

  React.useEffect(() => {
    displayRef.current = display;
  }, [display]);

  React.useEffect(() => {
    const start = displayRef.current;
    const target = value;

    if (start === target) {
      return;
    }

    const startTime = performance.now();
    let frameId = 0;

    const animate = (now: number) => {
      const elapsed = now - startTime;
      const t = Math.min(1, elapsed / duration);
      const next = start + (target - start) * easeOutCubic(t);
      setDisplay(next);

      if (t < 1) {
        frameId = requestAnimationFrame(animate);
      }
    };

    frameId = requestAnimationFrame(animate);

    return () => {
      cancelAnimationFrame(frameId);
    };
  }, [value, duration]);

  return display;
}

function resolveVariant(
  task: Task,
  globalVariant: TaskVariant | undefined,
  percentage: number,
): TaskVariant {
  const base = task.variant ?? globalVariant ?? "default";

  if (percentage === 100 && base !== "error") {
    return "success";
  }

  return base;
}

function TaskProgress({ task, variant }: TaskProgressProps) {
  const percentage = Math.min(100, (task.progress / task.maxProgress) * 100);
  const animatedPercentage = useAnimatedNumber(percentage, ANIMATION_DURATION_MS);
  const displayPercent = Math.round(animatedPercentage);
  const effectiveVariant = resolveVariant(task, variant, percentage);
  const isComplete = percentage === 100 && effectiveVariant !== "error";

  const Icon = isComplete ? CheckCircle2 : task.icon;

  return (
      <div className="flex items-center gap-3">
        <Icon
            className={cn(
                "size-4.5 shrink-0",
                isComplete && "text-green-500",
                effectiveVariant === "error" && "text-destructive",
            )}
        />
        <span className="w-32 shrink-0 truncate text-sm">{task.label}</span>
        <Progress
            value={percentage}
            className={cn(
                "w-32 shrink-0 **:data-[slot=progress-track]:h-2 **:data-[slot=progress-indicator]:duration-500 **:data-[slot=progress-indicator]:ease-out",
                INDICATOR_VARIANT_CLASSES[effectiveVariant],
            )}
        />
        <span className="w-10 shrink-0 text-right text-sm tabular-nums text-muted-foreground">
      {displayPercent}%
    </span>
      </div>
  );
}

function ResourceProcessingStatus({ tasks, variant }: ResourceProcessingStatusProps) {
  return (
    <div className="flex flex-col gap-3">
      {tasks.map((task) => (
        <TaskProgress key={task.id} task={task} variant={variant} />
      ))}
    </div>
  );
}

export default ResourceProcessingStatus;
export { TaskProgress, useAnimatedNumber };
export type { Task, TaskProgressProps, ResourceProcessingStatusProps, TaskVariant };
