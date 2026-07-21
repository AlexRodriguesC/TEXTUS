import useDocuments, {
  type Document,
  type DocumentStatus,
} from "@/hooks/useDocuments.ts";
import { Check, FileUp, RotateCcw, X } from "lucide-react";
import { Spinner } from "@/components/ui/spinner.tsx";
import * as React from "react";
import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import { useTranslation } from "react-i18next";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useMindMap from "@/hooks/useMindMap.ts";
import { Button } from "@/components/ui/button.tsx";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip.tsx";

interface SidebarDocumentInfoProps {
  document: Document;
}

const statusToIcon = (status: DocumentStatus): React.ReactNode => {
  switch (status) {
    case "PROCESSING":
      return <Spinner />;
    case "DONE":
      return <Check color={"green"} />;
    case "RECEIVED":
      return <FileUp />;
    case "FAILED":
      return <X color={"red"} />;
    default:
      throw new Error("Unhandled document status in SidebarDocumentInfo");
  }
};

function SidebarDocumentInfo({ document }: SidebarDocumentInfoProps) {
  const { t } = useTranslation();
  const { spaceId } = useSpaceId();
  const { deleteDocument, retryDocument, isRetrying } = useDocuments(spaceId);
  const { status } = useMindMap();

  return (
    <div className={"flex flex-row w-full items-center justify-between"}>
      <div className={"flex flex-row gap-2 items-center"}>
        {statusToIcon(document.status)}
        <Tooltip>
          <TooltipTrigger>
            {document.name.length > 20
              ? document.name.substring(0, 20) + "..."
              : document.name}
          </TooltipTrigger>
          <TooltipContent>{document.name}</TooltipContent>
        </Tooltip>
      </div>
      <div className={"flex flex-row gap-1 items-center"}>
        {document.status === "FAILED" && (
          <Button
            variant="ghost"
            size="icon"
            disabled={isRetrying || status == "GENERATING" || status == "DONE"}
            onClick={() => retryDocument(document.id)}
            title={t("documents.retry.action")}
          >
            <RotateCcw className="h-4 w-4" />
          </Button>
        )}
        <DestructiveActionButton
          disabled={status == "GENERATING" || status == "DONE"}
          title={t("document.delete", { documentName: document.name })}
          onConfirm={() => {
            deleteDocument(document.id);
          }}
        />
      </div>
    </div>
  );
}

export default SidebarDocumentInfo;
