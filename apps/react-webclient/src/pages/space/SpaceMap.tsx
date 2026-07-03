import useMindMap from "@/hooks/useMindMap.ts";
import type { NodeDTO, TopicResponseDTO } from "@/api/model";
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
import { useGetTopics } from "@/api/generated/topics/topics.ts";

const nodeTypes = { mapNode: MapNode };

// Configuration for the layout
const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const NODE_WIDTH = 172;
const NODE_HEIGHT = 36;

function getLayoutedElements(
  nodes: NodeDTO[],
  direction: "TB" | "LR" = "TB",
  topics: TopicResponseDTO[],
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
      },
      draggable: false,
    });

    // Create edge if it has a parent
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

function SpaceMap() {
  const { spaceId } = useSpaceId();
  const { theme } = useTheme();
  const { mindMap } = useMindMap(spaceId);

  const { data } = useGetTopics(spaceId);

  let topics: TopicResponseDTO[] = []
  if (data && data.data && "topics" in data.data) {
    topics = data.data.topics;
  }

  const { nodes: rfNodes, edges: rfEdges } = useMemo(() => {
    const nodes = mindMap?.nodes ?? [];
    return getLayoutedElements(nodes, "TB", topics);
  }, [mindMap?.nodes, topics]);

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
