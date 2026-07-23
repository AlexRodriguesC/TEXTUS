import Document from "@/classes/Document.ts";
import Task from "@/classes/Task.ts";
import TaskType from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import DocumentRowStatus from "@/pages/space/menu/DocumentRowStatus.tsx";
import { TooltipProvider } from "@/components/ui/tooltip.tsx";
import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

vi.mock("@/components/ResourceProcessingStatus.tsx", () => ({
  default: () => <div data-testid="processing-status" />,
}));

vi.mock("@/components/DestructiveActionButton.tsx", () => ({
  default: () => <button type="button" aria-label="Delete" />,
}));

vi.mock("@/components/ui/spinner.tsx", () => ({
  Spinner: () => <div data-testid="spinner" />,
}));

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@/hooks/shadcn/useIsMobile.ts", () => ({
  useIsMobile: () => false,
}));

function buildDocument(tasks: Task[]): Document {
  return new Document("sample-document.pdf", 1, tasks);
}

function renderRow(document: Document) {
  return render(
    <TooltipProvider>
      <DocumentRowStatus
        document={document}
        onDelete={vi.fn()}
        onRetry={vi.fn()}
        isRetrying={false}
        actionsDisabled={false}
      />
    </TooltipProvider>,
  );
}

describe("DocumentRowStatus", () => {
  afterEach(() => {
    cleanup();
  });

  it("should show spinner and hint overlay trigger while in progress", () => {
    renderRow(
      buildDocument([
        new Task("1:OCR", TaskType.OCR, 1, 5, TaskVariant.DEFAULT),
      ]),
    );

    expect(screen.getByTestId("spinner")).toBeInTheDocument();
    expect(screen.getByText("sample-document.pdf")).toHaveAttribute(
      "data-slot",
      "tooltip-trigger",
    );
    expect(screen.queryByTitle("documents.retry.action")).not.toBeInTheDocument();
  });

  it("should show checkmark without processing tooltip when done", () => {
    renderRow(
      buildDocument([
        new Task("1:OCR", TaskType.OCR, 1, 1, TaskVariant.SUCCESS),
        new Task("1:SEG", TaskType.SEGMENTATION, 1, 1, TaskVariant.SUCCESS),
        new Task("1:SUM", TaskType.SUMMARIZATION, 1, 1, TaskVariant.SUCCESS),
        new Task("1:TOP", TaskType.TOPIC, 1, 1, TaskVariant.SUCCESS),
      ]),
    );

    expect(screen.queryByTestId("spinner")).not.toBeInTheDocument();
    expect(screen.queryByTestId("processing-status")).not.toBeInTheDocument();
  });

  it("should show retry action without processing tooltip when failed", () => {
    renderRow(
      buildDocument([
        new Task("1:OCR", TaskType.OCR, 2, 5, TaskVariant.ERROR),
      ]),
    );

    expect(screen.getByTitle("documents.retry.action")).toBeInTheDocument();
    expect(screen.queryByTestId("processing-status")).not.toBeInTheDocument();
  });
});
