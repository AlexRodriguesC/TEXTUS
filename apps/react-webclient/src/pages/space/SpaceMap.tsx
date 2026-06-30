import useMindMap from "@/hooks/useMindMap.ts";
import type { NodeDTO } from "@/api/model";
import { useMemo } from "react";
import {
  Controls,
  type Edge as RFEdge,
  type Node as RFNode,
  ReactFlow,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useTheme } from "@/hooks/useTheme.ts";
import MapNode from "@/pages/space/MapNode.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";

export interface MindMapTreeNode extends NodeDTO {
  children: MindMapTreeNode[];
}

export function buildMindMapTree(nodes: NodeDTO[]): MindMapTreeNode[] {
  const byId = new Map<number, MindMapTreeNode>();

  // Pass 1: create a tree node for every entry that has an id
  for (const n of nodes) {
    if (n.chunkId == null) continue;
    byId.set(n.chunkId, { ...n, children: [] });
  }

  const roots: MindMapTreeNode[] = [];

  // Pass 2: attach each node to its parent; anything with no
  // resolvable parent becomes a root (handles multiple roots too)
  for (const node of byId.values()) {
    const parent =
      node.parentChunkId != null ? byId.get(node.parentChunkId) : undefined;

    if (parent) {
      parent.children.push(node);
    } else {
      roots.push(node);
    }
  }

  return roots;
}

const X_GAP = 160;
const Y_GAP = 180;

interface LaidOutNode {
  id: string;
  x: number;
  y: number;
  parentId?: string;
  dto: NodeDTO;
}

function layoutTree(roots: MindMapTreeNode[]): LaidOutNode[] {
  const laidOut: LaidOutNode[] = [];
  let nextLeafSlot = 0;

  function visit(
    node: MindMapTreeNode,
    depth: number,
    parentId?: string,
  ): number {
    const id = String(node.chunkId);

    if (node.children.length === 0) {
      const x = nextLeafSlot * X_GAP;
      nextLeafSlot += 1;
      laidOut.push({ id, x, y: depth * Y_GAP, parentId, dto: node });
      return x;
    }

    const childXs = node.children.map((child) => visit(child, depth + 1, id));
    const x = childXs.reduce((sum, v) => sum + v, 0) / childXs.length;
    laidOut.push({ id, x, y: depth * Y_GAP, parentId, dto: node });
    return x;
  }

  for (const root of roots) visit(root, 0);
  return laidOut;
}

const nodeTypes = { mapNode: MapNode };

function SpaceMap() {
  const { spaceId } = useSpaceId();
  const { theme } = useTheme();
  const { mindMap } = useMindMap(spaceId);

  const { rfNodes, rfEdges } = useMemo(() => {
    const tree = buildMindMapTree(mindMap?.nodes ?? []);
    const laidOut = layoutTree(tree);

    const rfNodes: RFNode[] = laidOut.map((n) => ({
      id: n.id,
      type: "mapNode",
      position: { x: n.x, y: n.y },
      data: { label: n.dto.chunkId, completed: false },
      draggable: false,
    }));

    const rfEdges: RFEdge[] = laidOut
      .filter((n) => n.parentId != null)
      .map((n) => ({
        id: `${n.parentId}-${n.id}`,
        source: n.parentId!,
        target: n.id,
        type: "step",
      }));

    return { rfNodes, rfEdges };
  }, [mindMap?.nodes]);

  return (
    <div style={{ width: "100%", height: "100%" }}>
      <ReactFlow
        colorMode={theme}
        style={{ backgroundColor: "transparent", borderWidth: 2 }}
        nodes={rfNodes}
        edges={rfEdges}
        nodeTypes={nodeTypes}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={true}
        fitView
      >
        <Controls showInteractive={false} />
      </ReactFlow>
    </div>
  );
}

export default SpaceMap;
