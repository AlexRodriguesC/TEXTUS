import Document from "@/classes/Document.ts";
import Task from "@/classes/Task.ts";
import TaskType from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import { TooltipProvider } from "@/components/ui/tooltip.tsx";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const documentsHookState = vi.fn();
const mindMapHookState = vi.fn();
const spaceIdHookState = vi.fn();

vi.mock("@/hooks/useDocuments.ts", () => ({
  default: () => documentsHookState(),
}));

vi.mock("@/hooks/useMindMap.ts", () => ({
  default: () => mindMapHookState(),
}));

vi.mock("@/hooks/useSpaceId.ts", () => ({
  default: () => spaceIdHookState(),
}));

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

function buildDocument(
  id: number,
  name: string,
  tasks: Task[],
): Document {
  return new Document(name, id, tasks);
}

describe("DocumentStatusList", () => {
  beforeEach(() => {
    spaceIdHookState.mockReturnValue({ spaceId: 1 });
    mindMapHookState.mockReturnValue({ status: "NOT_CREATED" });
    documentsHookState.mockReturnValue({
      documents: [
        buildDocument(1, "in-progress.pdf", [
          new Task("1:OCR", TaskType.OCR, 1, 3, TaskVariant.DEFAULT),
        ]),
        buildDocument(2, "done.pdf", [
          new Task("2:OCR", TaskType.OCR, 1, 1, TaskVariant.SUCCESS),
          new Task("2:SEG", TaskType.SEGMENTATION, 1, 1, TaskVariant.SUCCESS),
          new Task("2:SUM", TaskType.SUMMARIZATION, 1, 1, TaskVariant.SUCCESS),
          new Task("2:TOP", TaskType.TOPIC, 1, 1, TaskVariant.SUCCESS),
        ]),
        buildDocument(3, "failed.pdf", [
          new Task("3:OCR", TaskType.OCR, 1, 3, TaskVariant.ERROR),
        ]),
      ],
      deleteDocument: vi.fn(),
      retryDocument: vi.fn(),
      isRetrying: false,
    });
  });

  it("should render rows for in-progress, done, and failed documents", () => {
    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    expect(screen.getByText("in-progress.pdf")).toBeInTheDocument();
    expect(screen.getByText("done.pdf")).toBeInTheDocument();
    expect(screen.getByText("failed.pdf")).toBeInTheDocument();
    expect(screen.getAllByTestId("spinner")).toHaveLength(1);
    expect(screen.getByTitle("documents.retry.action")).toBeInTheDocument();
  });
});
