import type Document from "@/classes/Document.ts";
import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import ResourceProcessingStatus from "@/components/ResourceProcessingStatus.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import {
  isDone,
  isFailed,
  isInProgress,
} from "@/lib/documentTaskUtils.ts";
import { toDisplayTasks } from "@/lib/toDisplayTasks.ts";
import { Check, GripVertical, RotateCcw, X } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Marker, MarkerContent, MarkerIcon } from "@/components/ui/marker.tsx";
import { cn } from "@/lib/utils.ts";
import { HintOverlay } from "@/components/HintOverlay.tsx";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

interface DocumentRowStatusProps {
  document: Document;
  onDelete: () => void;
  onRetry: () => void;
  isRetrying: boolean;
  actionsDisabled: boolean;
  sortable: boolean;
}

function truncateName(name: string, maxLength = 20): string {
  return name.length > maxLength ? `${name.substring(0, maxLength)}...` : name;
}

function DocumentRowStatus({
  document,
  onDelete,
  onRetry,
  isRetrying,
  actionsDisabled,
  sortable,
}: DocumentRowStatusProps) {
  const { t } = useTranslation();
  const tasks = document.tasks;
  const inProgress = isInProgress(tasks);
  const done = isDone(tasks);
  const failed = isFailed(tasks);
  const displayName = truncateName(document.name);
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: document.id,
    disabled: !sortable,
  });

  const displayNameEl = (
    <span className="min-w-0 truncate uppercase">{displayName}</span>
  );
  const deleteDocumentEl = (
    <DestructiveActionButton
      disabled={actionsDisabled}
      title={t("document.delete", { documentName: document.name })}
      onConfirm={onDelete}
    />
  );

  return (
    <div
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
      }}
      className="flex flex-row w-full items-center"
    >
      {sortable ? (
        <button
          type="button"
          className="mr-1 cursor-grab touch-none text-muted-foreground shrink-0"
          aria-label={`Reorder ${document.name}`}
          {...attributes}
          {...listeners}
        >
          <GripVertical className="h-4 w-4" />
        </button>
      ) : null}
      <Marker
        role="status"
        className={cn(
          "w-full justify-start",
          done && "text-(--success)",
          inProgress && "text-primary-background",
          failed && "text-destructive",
        )}
      >
        <MarkerIcon>
          {done && <Check />}
          {inProgress && <Spinner />}
          {failed && <X />}
        </MarkerIcon>
        <MarkerContent
          className={cn(
            "flex items-center justify-between gap-2",
            inProgress && "shimmer",
          )}
        >
          {done ? (
            displayNameEl
          ) : (
            <HintOverlay
              variant="panel"
              trigger={displayNameEl}
              className="w-fit max-h-80 overflow-hidden"
              content={
                <ResourceProcessingStatus tasks={toDisplayTasks(tasks, t)} />
              }
            />
          )}
        </MarkerContent>
      </Marker>
      <div className="flex flex-row gap-1 items-center shrink-0">
        {failed && (
          <Button
            variant="ghost"
            size="icon"
            disabled={isRetrying || actionsDisabled}
            onClick={onRetry}
            title={t("documents.retry.action")}
          >
            <RotateCcw className="h-4 w-4" />
          </Button>
        )}
        {actionsDisabled ? (
          <HintOverlay
            trigger={deleteDocumentEl}
            content={t("documents.delete.information")}
          />
        ) : (
          deleteDocumentEl
        )}
      </div>
    </div>
  );
}

export default DocumentRowStatus;
