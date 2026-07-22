import DocumentRowStatus from "@/components/left-sidebar/DocumentRowStatus.tsx";
import useDocuments from "@/hooks/useDocuments.ts";
import useMindMap from "@/hooks/useMindMap.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type Document from "@/classes/Document.ts";

interface SidebarDocumentInfoProps {
  document: Document;
}

function SidebarDocumentInfo({ document }: SidebarDocumentInfoProps) {
  const { spaceId } = useSpaceId();
  const { deleteDocument, retryDocument, isRetrying } = useDocuments(spaceId);
  const { status } = useMindMap();

  return (
    <DocumentRowStatus
      document={document}
      onDelete={() => deleteDocument(document.id)}
      onRetry={() => retryDocument(document.id)}
      isRetrying={isRetrying}
      actionsDisabled={status === "GENERATING" || status === "DONE"}
    />
  );
}

export default SidebarDocumentInfo;
