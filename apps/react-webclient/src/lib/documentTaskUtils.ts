import type Document from "@/classes/Document.ts";
import Task from "@/classes/Task.ts";
import TaskType, { type TaskTypeType } from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";

export const DOCUMENTS_POLL_INTERVAL_MS = 2000;

export const PIPELINE_TASK_TYPES: TaskTypeType[] = [
  TaskType.SEGMENTATION,
  TaskType.OCR,
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

export function shouldPollDocuments(
  documents: Document[],
  awaitingServerUpload: boolean,
): number | false {
  if (awaitingServerUpload) return DOCUMENTS_POLL_INTERVAL_MS;
  if (documents.length === 0) return false;
  const hasUnsettled = documents.some(
    (doc) => !isDone(doc.tasks) && !isFailed(doc.tasks),
  );
  return hasUnsettled ? DOCUMENTS_POLL_INTERVAL_MS : false;
}
