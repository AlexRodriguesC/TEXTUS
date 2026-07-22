import type { TFunction } from "i18next";
import { FileText, ScanText, Split, Tags, type LucideIcon } from "lucide-react";
import Task from "@/classes/Task.ts";
import TaskType, { type TaskTypeType } from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import type { Task as DisplayTask, TaskVariant as DisplayTaskVariant } from "@/components/ResourceProcessingStatus.tsx";
import { PIPELINE_TASK_TYPES } from "@/lib/documentTaskUtils.ts";

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

function toDisplayTask(task: Task, t: TFunction): DisplayTask {
  return {
    id: task.id,
    label: t(`documents.tasks.${task.type}`),
    icon: TASK_ICONS[task.type],
    progress: task.progress,
    maxProgress: task.maxProgress,
    variant: toDisplayVariant(task.variant),
  };
}

function toPlaceholderDisplayTask(type: TaskTypeType, t: TFunction): DisplayTask {
  return {
    id: type,
    label: t(`documents.tasks.${type}`),
    icon: TASK_ICONS[type],
    progress: 0,
    maxProgress: 1,
    variant: "default",
  };
}

export function toDisplayTasks(tasks: Task[], t: TFunction): DisplayTask[] {
  const tasksByType = new Map(tasks.map((task) => [task.type, task]));

  return PIPELINE_TASK_TYPES.map((type) => {
    const task = tasksByType.get(type);
    return task ? toDisplayTask(task, t) : toPlaceholderDisplayTask(type, t);
  });
}
