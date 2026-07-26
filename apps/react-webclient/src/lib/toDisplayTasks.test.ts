import Task from "@/classes/Task.ts";
import TaskType from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import { toDisplayTasks } from "@/lib/toDisplayTasks.ts";
import { describe, expect, it } from "vitest";

const t = (key: string) => key;

describe("toDisplayTasks", () => {
  it("should return all pipeline tasks with placeholders at 0% when only some are present", () => {
    const result = toDisplayTasks(
      [new Task("1:OCR", TaskType.OCR, 2, 5, TaskVariant.DEFAULT)],
      t,
    );

    expect(result).toHaveLength(4);
    expect(result.map((task) => task.id)).toEqual([
      TaskType.SEGMENTATION,
      "1:OCR",
      TaskType.SUMMARIZATION,
      TaskType.TOPIC,
    ]);
    expect(result[0]).toMatchObject({
      progress: 0,
      maxProgress: 1,
      variant: "default",
      label: "documents.tasks.SEGMENTATION",
    });
    expect(result[1]).toMatchObject({
      progress: 2,
      maxProgress: 5,
      variant: "default",
      label: "documents.tasks.OCR",
    });
    expect(result[2]).toMatchObject({
      progress: 0,
      maxProgress: 1,
      variant: "default",
      label: "documents.tasks.SUMMARIZATION",
    });
    expect(result[3]).toMatchObject({
      progress: 0,
      maxProgress: 1,
      variant: "default",
      label: "documents.tasks.TOPIC",
    });
  });

  it("should preserve real task data when all pipeline tasks are present", () => {
    const tasks = [
      new Task("1:SEG", TaskType.SEGMENTATION, 3, 3, TaskVariant.SUCCESS),
      new Task("1:OCR", TaskType.OCR, 5, 5, TaskVariant.SUCCESS),
      new Task("1:SUM", TaskType.SUMMARIZATION, 2, 2, TaskVariant.SUCCESS),
      new Task("1:TOP", TaskType.TOPIC, 4, 4, TaskVariant.ERROR),
    ];

    const result = toDisplayTasks(tasks, t);

    expect(result).toHaveLength(4);
    expect(result.map((task) => task.id)).toEqual([
      "1:SEG",
      "1:OCR",
      "1:SUM",
      "1:TOP",
    ]);
    expect(result[0]).toMatchObject({
      progress: 3,
      maxProgress: 3,
      variant: "success",
    });
    expect(result[3]).toMatchObject({
      progress: 4,
      maxProgress: 4,
      variant: "error",
    });
  });

  it("should always return tasks in segmentation, ocr, summarisation, topic order", () => {
    const tasks = [
      new Task("1:TOP", TaskType.TOPIC, 1, 1, TaskVariant.DEFAULT),
      new Task("1:OCR", TaskType.OCR, 1, 5, TaskVariant.DEFAULT),
      new Task("1:SEG", TaskType.SEGMENTATION, 1, 1, TaskVariant.SUCCESS),
    ];

    const result = toDisplayTasks(tasks, t);

    expect(result.map((task) => task.label)).toEqual([
      "documents.tasks.SEGMENTATION",
      "documents.tasks.OCR",
      "documents.tasks.SUMMARIZATION",
      "documents.tasks.TOPIC",
    ]);
  });
});
