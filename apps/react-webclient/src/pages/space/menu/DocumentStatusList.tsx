import DocumentRowStatus from "@/pages/space/menu/DocumentRowStatus.tsx";
import useDocuments from "@/hooks/useDocuments.ts";
import useMindMap from "@/hooks/useMindMap.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import {
  DndContext,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";

function DocumentStatusList() {
  const { spaceId } = useSpaceId();
  const {
    documents,
    deleteDocument,
    retryDocument,
    reorderDocument,
    isRetrying,
  } = useDocuments(spaceId);
  const { status: mapStatus } = useMindMap();
  const { status: pathStatus } = useLearningPath();
  const actionsDisabled =
    mapStatus !== "NOT_CREATED" || pathStatus !== "NOT_CREATED";
  const persistedDocuments = documents.filter((document) => document.id >= 0);
  const canReorder = persistedDocuments.length > 1;
  const sensors = useSensors(useSensor(PointerSensor));

  async function handleDragEnd(event: DragEndEvent) {
    if (!canReorder) return;
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const oldIndex = persistedDocuments.findIndex(
      (document) => document.id === active.id,
    );
    const newIndex = persistedDocuments.findIndex(
      (document) => document.id === over.id,
    );
    if (oldIndex < 0 || newIndex < 0 || oldIndex === newIndex) return;

    await reorderDocument(Number(active.id), newIndex);
  }

  return (
    <div className="flex flex-row w-full justify-between items-center h-fit">
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={persistedDocuments.map((document) => document.id)}
          strategy={verticalListSortingStrategy}
        >
          <div className="flex flex-col w-full items-center gap-1">
            {documents.map((document) => (
              <DocumentRowStatus
                key={document.id}
                document={document}
                onDelete={() => deleteDocument(document.id)}
                onRetry={() => retryDocument(document.id)}
                isRetrying={isRetrying}
                actionsDisabled={actionsDisabled}
                sortable={canReorder && document.id >= 0}
              />
            ))}
          </div>
        </SortableContext>
      </DndContext>
    </div>
  );
}

export default DocumentStatusList;
