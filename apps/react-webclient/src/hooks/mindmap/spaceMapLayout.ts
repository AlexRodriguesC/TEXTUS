import type Node from "@/classes/Node.ts";
import type { NodeTypeType } from "@/classes/NodeType.ts";
import type Page from "@/classes/Page.ts";
import type Topic from "@/classes/Topic.ts";
import { resolveNodeContent } from "@/hooks/mindmap/resolveNodeContent.ts";
import dagre from "dagre";
import type { Edge as RFEdge, Node as RFNode } from "@xyflow/react";

export const NODE_WIDTH = 172;
export const NODE_HEIGHT = 36;

export type NodeKey = string;

export function toNodeKey(
  nodeType?: NodeTypeType,
  entityId?: number,
): NodeKey | null {
  if (nodeType == null || entityId == null) return null;
  return `${nodeType}:${entityId}`;
}

export function buildChildrenMap(nodes: Node[]): Map<NodeKey, NodeKey[]> {
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

export function getVisibleNodes(
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

export function collectDescendantIds(
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

export function toggleExpanded(
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

export function getLayoutedElements(
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

export function buildMindMapKey(nodes: Node[]): string {
  return nodes
    .map((n) => toNodeKey(n.nodeType, n.entityId))
    .filter((key) => key != null)
    .join(",");
}
