import TaskProgress from "@/components/ResourceProcessingStatus/TaskProgress.tsx";
import type { ResourceProcessingStatusProps } from "@/components/ResourceProcessingStatus/types.ts";

function ResourceProcessingStatus({
  tasks,
  variant,
}: ResourceProcessingStatusProps) {
  return (
    <div className="flex flex-col gap-3">
      {tasks.map((task) => (
        <TaskProgress key={task.id} task={task} variant={variant} />
      ))}
    </div>
  );
}

export default ResourceProcessingStatus;
export { TaskProgress };
export { default as useAnimatedNumber } from "@/hooks/useAnimatedNumber.ts";
export type {
  Task,
  TaskProgressProps,
  ResourceProcessingStatusProps,
  TaskVariant,
} from "@/components/ResourceProcessingStatus/types.ts";
