import { CheckCircle2 } from "lucide-react";
import { Progress } from "@/components/ui/progress.tsx";
import { cn } from "@/lib/utils.ts";
import useAnimatedNumber from "@/hooks/useAnimatedNumber.ts";
import type {
  TaskProgressProps,
  TaskVariant,
} from "@/components/ResourceProcessingStatus/types.ts";

const ANIMATION_DURATION_MS = 500;

const INDICATOR_VARIANT_CLASSES: Record<TaskVariant, string> = {
  default: "**:data-[slot=progress-indicator]:bg-primary",
  success: "**:data-[slot=progress-indicator]:bg-green-500",
  error: "**:data-[slot=progress-indicator]:bg-destructive",
};

function resolveVariant(
  task: TaskProgressProps["task"],
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

export default TaskProgress;
