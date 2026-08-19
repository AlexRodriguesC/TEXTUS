export type NodeTypeType = (typeof NodeType)[keyof typeof NodeType];

const NodeType = {
  PAGE: "PAGE",
  TOPIC: "TOPIC",
} as const;

export default NodeType;
