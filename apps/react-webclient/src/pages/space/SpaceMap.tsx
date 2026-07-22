import useMindMap from "@/hooks/useMindMap.ts";
import type Node from "@/classes/Node.ts";
import type { NodeTypeType } from "@/classes/NodeType.ts";
import NodeType from "@/classes/NodeType.ts";
import type Topic from "@/classes/Topic.ts";
import type Page from "@/classes/Page.ts";
import useTopics from "@/hooks/useTopics.tsx";
import usePages from "@/hooks/usePages.ts";
import SpaceMapEmpty from "@/pages/space/SpaceMapEmpty.tsx";
import LoadingScreen from "@/components/LoadingScreen.tsx";
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
import dagre from "dagre";

const nodeTypes = { mapNode: MapNode };

const NODE_WIDTH = 172;
const NODE_HEIGHT = 36;

type NodeKey = string;

function toNodeKey(nodeType?: NodeTypeType, entityId?: number): NodeKey | null {
  if (nodeType == null || entityId == null) return null;
  return `${nodeType}:${entityId}`;
}

function buildChildrenMap(nodes: Node[]): Map<NodeKey, NodeKey[]> {
  const childrenMap = new Map<NodeKey, NodeKey[]>();
  for (const n of nodes) {
    const childKey = toNodeKey(n.nodeType, n.entityId);
    const parentKey = toNodeKey(n.parentNodeType, n.parentEntityId);
    if (childKey == null || parentKey == null) continue;
    const siblings = childrenMap.get(parentKey) ?? [];
    siblings.push(childKey);
    childrenMap.set(parentKey, siblings);
  }
  return childrenMap;
}

function isNodeVisible(
  nodeKey: NodeKey,
  nodesByKey: Map<NodeKey, Node>,
  expandedIds: Set<NodeKey>,
): boolean {
  const node = nodesByKey.get(nodeKey);
  if (!node) return false;
  const parentKey = toNodeKey(node.parentNodeType, node.parentEntityId);
  if (parentKey == null) return true;
  if (!expandedIds.has(parentKey)) return false;
  return isNodeVisible(parentKey, nodesByKey, expandedIds);
}

function getVisibleNodes(
  nodes: Node[],
  expandedIds: Set<NodeKey>,
): Node[] {
  const nodesByKey = new Map<NodeKey, Node>();
  for (const n of nodes) {
    const key = toNodeKey(n.nodeType, n.entityId);
    if (key != null) nodesByKey.set(key, n);
  }
  return nodes.filter((n) => {
    const key = toNodeKey(n.nodeType, n.entityId);
    return key != null && isNodeVisible(key, nodesByKey, expandedIds);
  });
}

function collectDescendantIds(
  id: NodeKey,
  childrenMap: Map<NodeKey, NodeKey[]>,
): NodeKey[] {
  const descendants: NodeKey[] = [];
  const queue = [...(childrenMap.get(id) ?? [])];
  while (queue.length > 0) {
    const childId = queue.shift()!;
    descendants.push(childId);
    queue.push(...(childrenMap.get(childId) ?? []));
  }
  return descendants;
}

function toggleExpanded(
  id: NodeKey,
  expandedIds: Set<NodeKey>,
  childrenMap: Map<NodeKey, NodeKey[]>,
): Set<NodeKey> {
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

function resolveNodeContent(
  node: Node,
  topics: Topic[],
  pages: Page[],
): { title: string; summary: string; isPage: boolean } {
  if (node.nodeType === NodeType.PAGE) {
    const page = pages.find((p) => p.id === node.entityId);
    return {
      title: page?.title ?? "N/A",
      summary: page?.summary ?? "N/A",
      isPage: true,
    };
  }

  const topic = topics.find((t) => t.id === node.entityId);
  return {
    title: topic?.title ?? "N/A",
    summary: topic?.summary ?? "N/A",
    isPage: false,
  };
}

function getLayoutedElements(
  nodes: Node[],
  direction: "TB" | "LR" = "TB",
  topics: Topic[],
  pages: Page[],
  childrenMap: Map<NodeKey, NodeKey[]>,
  expandedIds: Set<NodeKey>,
): { nodes: RFNode[]; edges: RFEdge[] } {
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: direction });
  g.setDefaultEdgeLabel(() => ({}));

  const nodeMap = new Map<NodeKey, Node>();
  for (const n of nodes) {
    const key = toNodeKey(n.nodeType, n.entityId);
    if (key == null) continue;
    nodeMap.set(key, n);
    g.setNode(key, { width: NODE_WIDTH, height: NODE_HEIGHT });
  }

  for (const n of nodes) {
    const childKey = toNodeKey(n.nodeType, n.entityId);
    const parentKey = toNodeKey(n.parentNodeType, n.parentEntityId);
    if (childKey != null && parentKey != null && nodeMap.has(parentKey)) {
      g.setEdge(parentKey, childKey);
    }
  }

  dagre.layout(g);

  const rfNodes: RFNode[] = [];
  const rfEdges: RFEdge[] = [];

  for (const n of nodes) {
    const nodeKey = toNodeKey(n.nodeType, n.entityId);
    if (nodeKey == null) continue;
    const nodeWithPosition = g.node(nodeKey);
    const content = resolveNodeContent(n, topics, pages);

    rfNodes.push({
      id: nodeKey,
      type: "mapNode",
      position: {
        x: nodeWithPosition.x,
        y: nodeWithPosition.y,
      },
      data: {
        id: n.entityId,
        title: content.title,
        summary: content.summary,
        label: content.title,
        completed: false,
        isPage: content.isPage,
        hasChildren: (childrenMap.get(nodeKey)?.length ?? 0) > 0,
        isExpanded: expandedIds.has(nodeKey),
      },
      draggable: false,
    });

    const parentKey = toNodeKey(n.parentNodeType, n.parentEntityId);
    if (parentKey != null && nodeMap.has(parentKey)) {
      rfEdges.push({
        id: `${parentKey}-${nodeKey}`,
        source: parentKey,
        target: nodeKey,
        selectable: false,
      });
    }
  }

  return { nodes: rfNodes, edges: rfEdges };
}

function SpaceMapFlow({
  mindMapNodes,
  topics,
  pages,
}: {
  mindMapNodes: Node[];
  topics: Topic[];
  pages: Page[];
}) {
  const { theme } = useTheme();
  const { fitView } = useReactFlow();
  const [expandedIds, setExpandedIds] = useState<Set<NodeKey>>(() => new Set());

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
        pages,
        childrenMap,
        expandedIds,
      ),
    [visibleNodes, topics, pages, childrenMap, expandedIds],
  );

  const handleNodeClick = useCallback(
    (_: MouseEvent, node: RFNode) => {
      const id = node.id;
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
  const { mindMap, status, isMindMapLoading } = useMindMap();
  const { topics } = useTopics();
  const { pages } = usePages();

  const mindMapNodes = mindMap?.nodes ?? [];
  const mindMapKey = mindMapNodes
    .map((n) => toNodeKey(n.nodeType, n.entityId))
    .filter((key) => key != null)
    .join(",");

  if (status !== "NOT_CREATED" && isMindMapLoading) {
    return <LoadingScreen />;
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
