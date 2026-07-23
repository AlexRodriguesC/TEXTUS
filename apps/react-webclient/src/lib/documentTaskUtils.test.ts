import Document from "@/classes/Document.ts";
import Task from "@/classes/Task.ts";
import TaskType from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import {
  DOCUMENTS_POLL_INTERVAL_MS,
  shouldPollDocuments,
} from "@/lib/documentTaskUtils.ts";
import { describe, expect, it } from "vitest";

function buildDocument(tasks: Task[], name = "sample.pdf", id = 1): Document {
  return new Document(name, id, tasks);
}

function buildDoneTasks(): Task[] {
  return [
    new Task("1:SEG", TaskType.SEGMENTATION, 1, 1, TaskVariant.SUCCESS),
    new Task("1:OCR", TaskType.OCR, 1, 1, TaskVariant.SUCCESS),
    new Task("1:SUM", TaskType.SUMMARIZATION, 1, 1, TaskVariant.SUCCESS),
    new Task("1:TOP", TaskType.TOPIC, 1, 1, TaskVariant.SUCCESS),
  ];
}

describe("shouldPollDocuments", () => {
  it("returns false for an empty list with no awaiting upload", () => {
    expect(shouldPollDocuments([], false)).toBe(false);
  });

  it("returns the poll interval for a document with partial pipeline tasks", () => {
    const documents = [
      buildDocument([
        new Task("1:OCR", TaskType.OCR, 1, 5, TaskVariant.DEFAULT),
      ]),
    ];

    expect(shouldPollDocuments(documents, false)).toBe(
      DOCUMENTS_POLL_INTERVAL_MS,
    );
  });

  it("returns false when all documents are done or failed", () => {
    const documents = [
      buildDocument(buildDoneTasks()),
      buildDocument(
        [new Task("2:OCR", TaskType.OCR, 2, 5, TaskVariant.ERROR)],
        "failed.pdf",
        2,
      ),
    ];

    expect(shouldPollDocuments(documents, false)).toBe(false);
  });

  it("returns the poll interval when cache is settled but an upload awaits server acknowledgement", () => {
    const documents = [buildDocument(buildDoneTasks())];

    expect(shouldPollDocuments(documents, true)).toBe(
      DOCUMENTS_POLL_INTERVAL_MS,
    );
  });

  it("returns the poll interval for a new document with empty tasks in cache", () => {
    const documents = [buildDocument([])];

    expect(shouldPollDocuments(documents, false)).toBe(
      DOCUMENTS_POLL_INTERVAL_MS,
    );
  });
});
