import useDocuments from "@/hooks/useDocuments.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import SidebarDocumentInfo from "@/components/left-sidebar/SidebarDocumentInfo.tsx";

function DocumentStatusList() {
  const { spaceId } = useSpaceId();
  const { documents } = useDocuments(spaceId);

  return (
    <div className={"flex flex-row w-full justify-between items-center h-fit"}>
      <div className={"flex flex-row w-full items-center gap-8"}>
        {documents.map(doc => {
          return <SidebarDocumentInfo key={doc.id} document={doc}/>
        })}
      </div>
    </div>
  );
}

export default DocumentStatusList;
