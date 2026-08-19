import Document from "@/classes/Document.ts";
import Task from "@/classes/Task.ts";
import TaskType from "@/classes/TaskType.ts";
import TaskVariant from "@/classes/TaskVariant.ts";
import DocumentStatusList from "@/pages/space/menu/DocumentStatusList.tsx";
import { TooltipProvider } from "@/components/ui/tooltip.tsx";
import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const documentsHookState = vi.fn();
const mindMapHookState = vi.fn();
const spaceIdHookState = vi.fn();
const learningPathHookState = vi.fn();
const dragEndHandler = vi.fn();

vi.mock("@/hooks/useDocuments.ts", () => ({
  default: () => documentsHookState(),
}));

vi.mock("@/hooks/useMindMap.ts", () => ({
  default: () => mindMapHookState(),
}));

vi.mock("@/hooks/learning/useLearningPath.ts", () => ({
  default: () => learningPathHookState(),
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

vi.mock("@/hooks/shadcn/useIsMobile.ts", () => ({
  useIsMobile: () => false,
}));

vi.mock("@dnd-kit/core", async () => {
  const actual = await vi.importActual<typeof import("@dnd-kit/core")>(
    "@dnd-kit/core",
  );
  return {
    ...actual,
    DndContext: ({
      children,
      onDragEnd,
    }: {
      children: React.ReactNode;
      onDragEnd?: (event: {
        active: { id: number };
        over: { id: number } | null;
      }) => void;
    }) => {
      dragEndHandler.mockImplementation(onDragEnd);
      return <div data-testid="dnd-context">{children}</div>;
    },
  };
});

function buildDocument(id: number, name: string, tasks: Task[]): Document {
  return new Document(name, id, tasks, id - 1);
}

function doneTasks(id: number): Task[] {
  return [
    new Task(`${id}:OCR`, TaskType.OCR, 1, 1, TaskVariant.SUCCESS),
    new Task(`${id}:SEG`, TaskType.SEGMENTATION, 1, 1, TaskVariant.SUCCESS),
    new Task(`${id}:SUM`, TaskType.SUMMARIZATION, 1, 1, TaskVariant.SUCCESS),
    new Task(`${id}:TOP`, TaskType.TOPIC, 1, 1, TaskVariant.SUCCESS),
  ];
}

describe("DocumentStatusList", () => {
  const reorderDocument = vi.fn().mockResolvedValue(undefined);

  beforeEach(() => {
    reorderDocument.mockClear();
    dragEndHandler.mockClear();
    spaceIdHookState.mockReturnValue({ spaceId: 1 });
    mindMapHookState.mockReturnValue({ status: "NOT_CREATED" });
    learningPathHookState.mockReturnValue({ status: "NOT_CREATED" });
    documentsHookState.mockReturnValue({
      documents: [
        buildDocument(1, "in-progress.pdf", [
          new Task("1:OCR", TaskType.OCR, 1, 3, TaskVariant.DEFAULT),
        ]),
        buildDocument(2, "done.pdf", doneTasks(2)),
        buildDocument(3, "failed.pdf", [
          new Task("3:OCR", TaskType.OCR, 1, 3, TaskVariant.ERROR),
        ]),
      ],
      deleteDocument: vi.fn(),
      retryDocument: vi.fn(),
      reorderDocument,
      isRetrying: false,
    });
  });

  afterEach(() => {
    cleanup();
  });

  it("should render rows for in-progress, done, and failed documents", () => {
    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    expect(screen.getByText("in-progress.pdf")).toBeInTheDocument();
    expect(screen.getByText("done.pdf")).toBeInTheDocument();
    expect(screen.getAllByTestId("spinner")).toHaveLength(1);
    expect(screen.getByTitle("documents.retry.action")).toBeInTheDocument();
  });

  it("should show drag handles for multi-document spaces", () => {
    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    expect(
      screen.getByRole("button", { name: "Reorder in-progress.pdf" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Reorder done.pdf" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Reorder failed.pdf" }),
    ).toBeInTheDocument();
  });

  it("should hide drag handles for single-document spaces", () => {
    documentsHookState.mockReturnValue({
      documents: [buildDocument(2, "done.pdf", doneTasks(2))],
      deleteDocument: vi.fn(),
      retryDocument: vi.fn(),
      reorderDocument,
      isRetrying: false,
    });

    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    expect(
      screen.queryByRole("button", { name: /Reorder/ }),
    ).not.toBeInTheDocument();
  });

  it("should keep drag handles available when a learning path exists", () => {
    learningPathHookState.mockReturnValue({ status: "DONE" });

    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    expect(
      screen.getByRole("button", { name: "Reorder done.pdf" }),
    ).toBeInTheDocument();
  });

  it("should call reorderDocument when a drag ends on another item", async () => {
    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    await dragEndHandler({
      active: { id: 1 },
      over: { id: 3 },
    });

    expect(reorderDocument).toHaveBeenCalledWith(1, 2);
  });

  it("should not call reorderDocument when dropping on the same item", async () => {
    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    await dragEndHandler({
      active: { id: 1 },
      over: { id: 1 },
    });

    expect(reorderDocument).not.toHaveBeenCalled();
  });

  it("should not expose drag handles for temporary upload rows", () => {
    documentsHookState.mockReturnValue({
      documents: [
        buildDocument(1, "done.pdf", doneTasks(1)),
        buildDocument(2, "done-2.pdf", doneTasks(2)),
        new Document("uploading.pdf", -5, [], 2),
      ],
      deleteDocument: vi.fn(),
      retryDocument: vi.fn(),
      reorderDocument,
      isRetrying: false,
    });

    render(
      <TooltipProvider>
        <DocumentStatusList />
      </TooltipProvider>,
    );

    expect(
      screen.getByRole("button", { name: "Reorder done.pdf" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Reorder uploading.pdf" }),
    ).not.toBeInTheDocument();
  });
});
