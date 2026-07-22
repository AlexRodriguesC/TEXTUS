import type Node from "@/classes/Node.ts";

class MindMap {
  nodes: Node[];

  constructor(nodes: Node[]) {
    this.nodes = nodes;
  }
}

export default MindMap;
