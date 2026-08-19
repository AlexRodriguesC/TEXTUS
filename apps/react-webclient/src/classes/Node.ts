import type { NodeTypeType } from "@/classes/NodeType.ts";

class Node {
  nodeType?: NodeTypeType;
  entityId?: number;
  parentNodeType?: NodeTypeType;
  parentEntityId?: number;

  constructor(
    nodeType: NodeTypeType | undefined,
    entityId: number | undefined,
    parentNodeType: NodeTypeType | undefined,
    parentEntityId: number | undefined,
  ) {
    this.nodeType = nodeType;
    this.entityId = entityId;
    this.parentNodeType = parentNodeType;
    this.parentEntityId = parentEntityId;
  }
}

export default Node;
