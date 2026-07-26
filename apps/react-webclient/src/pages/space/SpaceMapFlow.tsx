import type Node from "@/classes/Node.ts";
import type Page from "@/classes/Page.ts";
import type Topic from "@/classes/Topic.ts";
import { useTheme } from "@/hooks/context/useTheme.ts";
import {
  buildChildrenMap,
  getLayoutedElements,
  getVisibleNodes,
  toggleExpanded,
  type NodeKey,
} from "@/hooks/mindmap/spaceMapLayout.ts";
import MapNode from "@/pages/space/MapNode.tsx";
import { useMemo, useState } from "react";
import {
  Controls,
  ReactFlow,
  useReactFlow,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";

const nodeTypes = { mapNode: MapNode };

interface SpaceMapFlowProps {
  mindMapNodes: Node[];
  topics: Topic[];
  pages: Page[];
}

function SpaceMapFlow({ mindMapNodes, topics, pages }: SpaceMapFlowProps) {
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

  const { nodes: layoutedNodes, edges: rfEdges } = useMemo(
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

  const rfNodes = useMemo(
    () =>
      layoutedNodes.map((node) => {
        if (!(childrenMap.get(node.id)?.length ?? 0)) {
          return node;
        }
        return {
          ...node,
          data: {
            ...node.data,
            onToggleExpand: () => {
              setExpandedIds((prev) =>
                toggleExpanded(node.id, prev, childrenMap),
              );
              requestAnimationFrame(() => {
                fitView({ padding: 0.2, duration: 200 });
              });
            },
          },
        };
      }),
    [layoutedNodes, childrenMap, fitView],
  );

  return (
    <div style={{ width: "100%", height: "100%" }}>
      <ReactFlow
        colorMode={theme}
        style={{ backgroundColor: "transparent", borderWidth: 0 }}
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

export default SpaceMapFlow;
