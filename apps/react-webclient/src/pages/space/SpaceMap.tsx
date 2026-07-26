import useMindMap from "@/hooks/useMindMap.ts";
import useTopics from "@/hooks/useTopics.tsx";
import usePages from "@/hooks/usePages.ts";
import { buildMindMapKey } from "@/hooks/mindmap/spaceMapLayout.ts";
import SpaceMapEmpty from "@/pages/space/SpaceMapEmpty.tsx";
import SpaceMapSkeleton from "@/pages/space/SpaceMapSkeleton.tsx";
import SpaceMapFlow from "@/pages/space/SpaceMapFlow.tsx";

function SpaceMap() {
  const { mindMap, status, isMindMapLoading } = useMindMap();
  const { topics } = useTopics();
  const { pages } = usePages();

  const mindMapNodes = mindMap?.nodes ?? [];
  const mindMapKey = buildMindMapKey(mindMapNodes);

  if (status !== "NOT_CREATED" && isMindMapLoading) {
    return <SpaceMapSkeleton />;
  }

  if (
    mindMap === undefined ||
    status === "NOT_CREATED" ||
    mindMapNodes.length === 0
  ) {
    return <SpaceMapEmpty />;
  }

  return (
    <SpaceMapFlow
      key={mindMapKey}
      mindMapNodes={mindMapNodes}
      topics={topics}
      pages={pages}
    />
  );
}

export default SpaceMap;
