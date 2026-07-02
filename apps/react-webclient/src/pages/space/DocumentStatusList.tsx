import useDocuments, { type Document } from "@/hooks/useDocuments.ts";
import { useMemo } from "react";
import { Check, FileDown } from "lucide-react";
import { Spinner } from "@/components/ui/spinner.tsx";
import * as React from "react";
import { useTranslation } from "react-i18next";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useGetDocuments } from "@/api/generated/documents/documents.ts";

function countForStatus(
  documents: Document[],
  status: "DONE" | "RECEIVED" | "PROCESSING",
): number {
  return documents.map((d) => d.status).filter((s) => s == status).length;
}

function fileStatusEntry(
  node: React.ReactNode,
  count: number,
  tooltip: string,
): React.ReactNode {
  return (
    <div className="flex flex-row items-center gap-2 min-w-12">
      <Tooltip>
        <TooltipTrigger asChild>
          <span className="flex size-4 items-center justify-center">
            {node}
          </span>
        </TooltipTrigger>
        <TooltipContent>{tooltip}</TooltipContent>
      </Tooltip>
      <span>{count}</span>
    </div>
  );
}

function DocumentStatusList() {
  const { spaceId } = useSpaceId();

  const { t } = useTranslation();

  const { documents } = useDocuments(spaceId);

  useGetDocuments(spaceId, {
    query: {
      refetchInterval: () => {
        const allDone =
          documents.length > 0 &&
          documents.every((doc) => doc.status === "DONE");
        return allDone ? false : 2000;
      },
      refetchIntervalInBackground: true,
    },
  });

  const done = useMemo(() => {
    return countForStatus(documents, "DONE");
  }, [documents]);

  const received = useMemo(() => {
    return countForStatus(documents, "RECEIVED");
  }, [documents]);

  const processing = useMemo(() => {
    return countForStatus(documents, "PROCESSING");
  }, [documents]);

  return (
    <div className={"flex flex-row justify-between items-center h-fit"}>
      <div className={"flex flex-row items-center gap-8"}>
        {fileStatusEntry(<Check />, done, t("document.status.tooltip.done"))}
        {fileStatusEntry(
          <Spinner className={processing > 0 ? "" : "animate-none"} />,
          processing,
          t("document.status.tooltip.loading"),
        )}
        {fileStatusEntry(
          <FileDown />,
          received,
          t("document.status.tooltip.received"),
        )}
      </div>
    </div>
  );
}

export default DocumentStatusList;
