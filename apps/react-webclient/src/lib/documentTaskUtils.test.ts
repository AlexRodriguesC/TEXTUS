import { describe, expect, it } from "vitest";
import Task from "@/classes/Task.ts";
import TaskType from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import { isDone, isFailed, isInProgress } from "@/lib/documentTaskUtils.ts";

function task(
  type: (typeof TaskType)[keyof typeof TaskType],
  variant: (typeof TaskVariant)[keyof typeof TaskVariant],
  progress = 0,
  maxProgress = 1,
): Task {
  return new Task(`${type}`, type, progress, maxProgress, variant);
}

function allSuccessTasks(): Task[] {
  return [
    task(TaskType.OCR, TaskVariant.SUCCESS, 1, 1),
    task(TaskType.SEGMENTATION, TaskVariant.SUCCESS, 2, 2),
    task(TaskType.SUMMARIZATION, TaskVariant.SUCCESS, 3, 3),
    task(TaskType.TOPIC, TaskVariant.SUCCESS, 4, 4),
  ];
}

describe("documentTaskUtils", () => {
  it("should mark document as failed when any task has error variant", () => {
    const tasks = [
      task(TaskType.OCR, TaskVariant.ERROR, 1, 5),
      task(TaskType.SEGMENTATION, TaskVariant.DEFAULT, 0, 2),
    ];

    expect(isFailed(tasks)).toBe(true);
    expect(isDone(tasks)).toBe(false);
    expect(isInProgress(tasks)).toBe(false);
  });

  it("should mark document as done only when all pipeline tasks succeed", () => {
    expect(isDone(allSuccessTasks())).toBe(true);
    expect(isDone([task(TaskType.OCR, TaskVariant.SUCCESS, 1, 1)])).toBe(
      false,
    );
    expect(isDone([])).toBe(false);
  });

  it("should mark partial pipeline progress as in progress", () => {
    const tasks = [
      task(TaskType.OCR, TaskVariant.SUCCESS, 1, 1),
      task(TaskType.SEGMENTATION, TaskVariant.DEFAULT, 0, 2),
    ];

    expect(isInProgress(tasks)).toBe(true);
    expect(isDone(tasks)).toBe(false);
    expect(isFailed(tasks)).toBe(false);
  });
});
