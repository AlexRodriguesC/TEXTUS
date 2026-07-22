import Task from "@/classes/Task.ts";
import TaskType, { type TaskTypeType } from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";

export const PIPELINE_TASK_TYPES: TaskTypeType[] = [
  TaskType.OCR,
  TaskType.SEGMENTATION,
  TaskType.SUMMARIZATION,
  TaskType.TOPIC,
];

export function isFailed(tasks: Task[]): boolean {
  return tasks.some((task) => task.variant === TaskVariant.ERROR);
}

export function isDone(tasks: Task[]): boolean {
  return PIPELINE_TASK_TYPES.every((type) =>
    tasks.some(
      (task) => task.type === type && task.variant === TaskVariant.SUCCESS,
    ),
  );
}

export function isInProgress(tasks: Task[]): boolean {
  if (isFailed(tasks) || isDone(tasks)) {
    return false;
  }
  return true;
}
