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
import { Check, RotateCcw } from "lucide-react";
import { useTranslation } from "react-i18next";
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover.tsx";
import {Marker, MarkerContent, MarkerIcon} from "@/components/ui/marker.tsx";

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

  return (
      <div className="flex flex-row w-full items-center justify-between">
        <div className="flex flex-row gap-2 items-center min-w-0">
          {done && <Check color="green" />}
          {inProgress ? (
                  <Marker role="status" className="w-full flex place-content-center items-center">
                    <MarkerIcon>
                      <Spinner />
                    </MarkerIcon>
                    <MarkerContent className="flex items-center justify-between gap-2 shimmer">
                      <Popover>
                        <PopoverTrigger asChild>
                          <span className="min-w-0 truncate cursor-pointer">{displayName}</span>
                        </PopoverTrigger>
                        <PopoverContent className="w-96 max-h-80 overflow-auto bg-muted/80">
                          <ResourceProcessingStatus tasks={toDisplayTasks(tasks, t)} />
                        </PopoverContent>
                      </Popover>
                    </MarkerContent>
                  </Marker>
          ) : (
              <span className="min-w-0 truncate uppercase">{displayName}</span>
          )}
        </div>
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
          <DestructiveActionButton
              disabled={actionsDisabled}
              title={t("document.delete", { documentName: document.name })}
              onConfirm={onDelete}
          />
        </div>
      </div>
  );
}

export default DocumentRowStatus;
