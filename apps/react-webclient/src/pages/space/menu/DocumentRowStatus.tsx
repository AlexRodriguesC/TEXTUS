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
import {Check, RotateCcw, X} from "lucide-react";
import { useTranslation } from "react-i18next";
import {Marker, MarkerContent, MarkerIcon} from "@/components/ui/marker.tsx";
import {cn} from "@/lib/utils.ts";
import {HintOverlay} from "@/components/HintOverlay.tsx";

interface DocumentRowStatusProps {
  document: Document;
  onDelete: () => void;
  onRetry: () => void;
  isRetrying: boolean;
  actionsDisabled: boolean;
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
}: DocumentRowStatusProps) {
  const { t } = useTranslation();
  const tasks = document.tasks;
  const inProgress = isInProgress(tasks);
  const done = isDone(tasks);
  const failed = isFailed(tasks);
  const displayName = truncateName(document.name);

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
      <div className="flex flex-row w-full items-center">
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
          <MarkerContent className={cn(
              "flex items-center justify-between gap-2",
              inProgress && "shimmer",
          )}>
            {done ? displayNameEl : (
                <HintOverlay
                    variant="panel"
                    trigger={displayNameEl}
                    className="w-fit max-h-80 overflow-hidden"
                    content={<ResourceProcessingStatus tasks={toDisplayTasks(tasks, t)} />}
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
          ): (
              deleteDocumentEl
          )}

        </div>
      </div>
  );
}

export default DocumentRowStatus;
