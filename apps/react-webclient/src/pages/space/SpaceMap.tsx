import useMindMap from "@/hooks/useMindMap.ts";
import type { NodeDTO, TopicResponseDTO } from "@/client/model";
import { useCallback, useMemo, useState, type MouseEvent } from "react";
import {
  Controls,
  type Edge as RFEdge,
  type Node as RFNode,
  ReactFlow,
  useReactFlow,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useTheme } from "@/hooks/useTheme.ts";
import MapNode from "@/pages/space/MapNode.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import dagre from "dagre";
import { useGetTopics } from "@/client/generated/topics/topics.ts";
import SpaceMapEmpty from "@/pages/space/SpaceMapEmpty.tsx";
import LoadingScreen from "@/components/LoadingScreen.tsx";

const nodeTypes = { mapNode: MapNode };

const NODE_WIDTH = 172;
const NODE_HEIGHT = 36;

function buildChildrenMap(nodes: NodeDTO[]): Map<number, number[]> {
  const childrenMap = new Map<number, number[]>();
  for (const n of nodes) {
    if (n.chunkId == null || n.parentChunkId == null) continue;
    const siblings = childrenMap.get(n.parentChunkId) ?? [];
    siblings.push(n.chunkId);
    childrenMap.set(n.parentChunkId, siblings);
  }
  return childrenMap;
}

function isNodeVisible(
  chunkId: number,
  nodesById: Map<number, NodeDTO>,
  expandedIds: Set<number>,
): boolean {
  const node = nodesById.get(chunkId);
  if (!node) return false;
  if (node.parentChunkId == null) return true;
  if (!expandedIds.has(node.parentChunkId)) return false;
  return isNodeVisible(node.parentChunkId, nodesById, expandedIds);
}

function getVisibleNodes(
  nodes: NodeDTO[],
  expandedIds: Set<number>,
): NodeDTO[] {
  const nodesById = new Map<number, NodeDTO>();
  for (const n of nodes) {
    if (n.chunkId != null) nodesById.set(n.chunkId, n);
  }
  return nodes.filter(
    (n) => n.chunkId != null && isNodeVisible(n.chunkId, nodesById, expandedIds),
  );
}

function collectDescendantIds(
  id: number,
  childrenMap: Map<number, number[]>,
): number[] {
  const descendants: number[] = [];
  const queue = [...(childrenMap.get(id) ?? [])];
  while (queue.length > 0) {
    const childId = queue.shift()!;
    descendants.push(childId);
    queue.push(...(childrenMap.get(childId) ?? []));
  }
  return descendants;
}

function toggleExpanded(
  id: number,
  expandedIds: Set<number>,
  childrenMap: Map<number, number[]>,
): Set<number> {
  const next = new Set(expandedIds);
  if (next.has(id)) {
    next.delete(id);
    for (const descendantId of collectDescendantIds(id, childrenMap)) {
      next.delete(descendantId);
    }
  } else {
    next.add(id);
  }
  return next;
}

function getLayoutedElements(
  nodes: NodeDTO[],
  direction: "TB" | "LR" = "TB",
  topics: TopicResponseDTO[],
  childrenMap: Map<number, number[]>,
  expandedIds: Set<number>,
): { nodes: RFNode[]; edges: RFEdge[] } {
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: direction });
  g.setDefaultEdgeLabel(() => ({}));

  const nodeMap = new Map<number, NodeDTO>();
  for (const n of nodes) {
    if (n.chunkId == null) continue;
    nodeMap.set(n.chunkId, n);
    g.setNode(String(n.chunkId), { width: NODE_WIDTH, height: NODE_HEIGHT });
  }

  for (const n of nodes) {
    if (n.parentChunkId != null && nodeMap.has(n.parentChunkId)) {
      g.setEdge(String(n.parentChunkId), String(n.chunkId));
    }
  }

  dagre.layout(g);

  const rfNodes: RFNode[] = [];
  const rfEdges: RFEdge[] = [];

  for (const n of nodes) {
    if (n.chunkId == null) continue;
    const nodeId = String(n.chunkId);
    const nodeWithPosition = g.node(nodeId);
    const lbl = topics.find((t) => t.id == n.chunkId);

    rfNodes.push({
      id: nodeId,
      type: "mapNode",
      position: {
        x: nodeWithPosition.x,
        y: nodeWithPosition.y,
      },
      data: {
        id: n.chunkId,
        title: lbl?.title ?? "N/A",
        summary: lbl?.summary ?? "N/A",
        label: lbl?.title ?? n.chunkId,
        completed: false,
        hasChildren: (childrenMap.get(n.chunkId)?.length ?? 0) > 0,
        isExpanded: expandedIds.has(n.chunkId),
      },
      draggable: false,
    });

    if (n.parentChunkId != null && nodeMap.has(n.parentChunkId)) {
      rfEdges.push({
        id: `${n.parentChunkId}-${n.chunkId}`,
        source: String(n.parentChunkId),
        target: nodeId,
        selectable: false,
      });
    }
  }

  return { nodes: rfNodes, edges: rfEdges };
}

function SpaceMapFlow({
  mindMapNodes,
  topics,
}: {
  mindMapNodes: NodeDTO[];
  topics: TopicResponseDTO[];
}) {
  const { theme } = useTheme();
  const { fitView } = useReactFlow();
  const [expandedIds, setExpandedIds] = useState<Set<number>>(() => new Set());

  const childrenMap = useMemo(
    () => buildChildrenMap(mindMapNodes),
    [mindMapNodes],
  );

  const visibleNodes = useMemo(
    () => getVisibleNodes(mindMapNodes, expandedIds),
    [mindMapNodes, expandedIds],
  );

  const { nodes: rfNodes, edges: rfEdges } = useMemo(
    () =>
      getLayoutedElements(
        visibleNodes,
        "TB",
        topics,
        childrenMap,
        expandedIds,
      ),
    [visibleNodes, topics, childrenMap, expandedIds],
  );

  const handleNodeClick = useCallback(
    (_: MouseEvent, node: RFNode) => {
      const id = Number(node.id);
      if (!childrenMap.has(id)) return;
      setExpandedIds((prev) => toggleExpanded(id, prev, childrenMap));
      requestAnimationFrame(() => {
        fitView({ padding: 0.2, duration: 200 });
      });
    },
    [childrenMap, fitView],
  );

  return (
    <div style={{ width: "100%", height: "100%" }}>
      <ReactFlow
        proOptions={{
          hideAttribution: true,
        }}
        colorMode={theme}
        style={{ backgroundColor: "transparent", borderWidth: 2 }}
        nodes={rfNodes}
        edges={rfEdges}
        nodeTypes={nodeTypes}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={true}
        onNodeClick={handleNodeClick}
        fitView
      >
        <Controls showInteractive={false} />
      </ReactFlow>
    </div>
  );
}

function SpaceMap() {
  const { spaceId } = useSpaceId();
  const { mindMap, status } = useMindMap(spaceId);

  const { data } = useGetTopics(spaceId ?? 0, {
    query: {
      enabled: spaceId !== undefined,
    },
  });

  let topics: TopicResponseDTO[] = [];
  if (data && data.data && "topics" in data.data) {
    topics = data.data.topics;
  }

  const mindMapNodes = mindMap?.nodes ?? [];
  const mindMapKey = mindMapNodes
    .map((n) => n.chunkId)
    .filter((id) => id != null)
    .join(",");

  if (status === "GENERATING" && mindMapNodes.length === 0) {
    return <LoadingScreen />;
  }

  if (mindMapNodes.length === 0) {
    return <SpaceMapEmpty />;
  }

  return (
    <SpaceMapFlow
      key={mindMapKey}
      mindMapNodes={mindMapNodes}
      topics={topics}
    />
  );
}

export default SpaceMap;
