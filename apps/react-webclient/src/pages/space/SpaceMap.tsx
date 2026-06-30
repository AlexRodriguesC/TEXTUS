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
import dagre from "dagre";

const nodeTypes = { mapNode: MapNode };

// Configuration for the layout
const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const NODE_WIDTH = 172;
const NODE_HEIGHT = 36;

function getLayoutedElements(
  nodes: NodeDTO[],
  direction: "TB" | "LR" = "TB",
): { nodes: RFNode[]; edges: RFEdge[] } {
  // 1. Build the graph
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: direction });
  g.setDefaultEdgeLabel(() => ({}));

  // 2. Add nodes to the graph
  const nodeMap = new Map<number, NodeDTO>();
  for (const n of nodes) {
    if (n.chunkId == null) continue;
    nodeMap.set(n.chunkId, n);
    g.setNode(String(n.chunkId), { width: NODE_WIDTH, height: NODE_HEIGHT });
  }

  // 3. Add edges to the graph (parent -> child)
  for (const n of nodes) {
    if (n.parentChunkId != null && nodeMap.has(n.parentChunkId)) {
      g.setEdge(String(n.parentChunkId), String(n.chunkId));
    }
  }

  // 4. Run the layout algorithm
  dagre.layout(g);

  // 5. Convert back to React Flow nodes & edges
  const rfNodes: RFNode[] = [];
  const rfEdges: RFEdge[] = [];

  for (const n of nodes) {
    if (n.chunkId == null) continue;
    const nodeId = String(n.chunkId);
    const nodeWithPosition = g.node(nodeId);

    rfNodes.push({
      id: nodeId,
      type: "mapNode",
      position: {
        x: nodeWithPosition.x,
        y: nodeWithPosition.y,
      },
      data: { label: n.chunkId, completed: false },
      draggable: false,
    });

    // Create edge if it has a parent
    if (n.parentChunkId != null && nodeMap.has(n.parentChunkId)) {
      rfEdges.push({
        id: `${n.parentChunkId}-${n.chunkId}`,
        source: String(n.parentChunkId),
        target: nodeId,
        type: "smooth",
        selectable: false,
      });
    }
  }

  return { nodes: rfNodes, edges: rfEdges };
}

function SpaceMap() {
  const { spaceId } = useSpaceId();
  const { theme } = useTheme();
  const { mindMap } = useMindMap(spaceId);

  const { nodes: rfNodes, edges: rfEdges } = useMemo(() => {
    const nodes = mindMap?.nodes ?? [];
    return getLayoutedElements(nodes, "TB");
  }, [mindMap?.nodes]);

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
        fitView
      >
        <Controls showInteractive={false} />
      </ReactFlow>
    </div>
  );
}

export default SpaceMap;
