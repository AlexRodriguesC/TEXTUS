import type { TFunction } from "i18next";
import { FileText, ScanText, Split, Tags, type LucideIcon } from "lucide-react";
import Task from "@/classes/Task.ts";
import TaskType, { type TaskTypeType } from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import type { Task as DisplayTask, TaskVariant as DisplayTaskVariant } from "@/components/ResourceProcessingStatus.tsx";

const TASK_ICONS: Record<TaskTypeType, LucideIcon> = {
  [TaskType.SEGMENTATION]: Split,
  [TaskType.OCR]: ScanText,
  [TaskType.SUMMARIZATION]: FileText,
  [TaskType.TOPIC]: Tags,
};

function toDisplayVariant(
  variant: (typeof TaskVariant)[keyof typeof TaskVariant],
): DisplayTaskVariant {
  switch (variant) {
    case TaskVariant.SUCCESS:
      return "success";
    case TaskVariant.ERROR:
      return "error";
    default:
      return "default";
  }
}

export function toDisplayTasks(tasks: Task[], t: TFunction): DisplayTask[] {
  return tasks.map((task) => ({
    id: task.id,
    label: t(`documents.tasks.${task.type}`),
    icon: TASK_ICONS[task.type],
    progress: task.progress,
    maxProgress: task.maxProgress,
    variant: toDisplayVariant(task.variant),
  }));
}
