import DocumentRowStatus from "@/components/left-sidebar/DocumentRowStatus.tsx";
import useDocuments from "@/hooks/useDocuments.ts";
import useMindMap from "@/hooks/useMindMap.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";

function DocumentStatusList() {
  const { spaceId } = useSpaceId();
  const { documents, deleteDocument, retryDocument, isRetrying } =
    useDocuments(spaceId);
  const { status } = useMindMap();
  const actionsDisabled = status === "GENERATING" || status === "DONE";

  return (
    <div className="flex flex-row w-full justify-between items-center h-fit">
      <div className="flex flex-col w-full items-center gap-1">
        {documents.map((document) => (
          <DocumentRowStatus
            key={document.id}
            document={document}
            onDelete={() => deleteDocument(document.id)}
            onRetry={() => retryDocument(document.id)}
            isRetrying={isRetrying}
            actionsDisabled={actionsDisabled}
          />
        ))}
      </div>
    </div>
  );
}

export default DocumentStatusList;
