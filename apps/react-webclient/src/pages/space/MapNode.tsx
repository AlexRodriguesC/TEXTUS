import { Handle, type NodeProps, Position } from "@xyflow/react";
import Node from "@/pages/space/Node.tsx";

function MapNode({ data }: NodeProps) {
  const hideHandle = {
    width: 0,
    height: 0,
    border: "none",
    background: "transparent",
  };
  return (
    <>
      <Handle type="target" position={Position.Top} style={hideHandle} />
      <Node completed={data.completed}>{data.label}</Node>
      <Handle type="source" position={Position.Bottom} style={hideHandle} />
    </>
  );
}

export default MapNode;
