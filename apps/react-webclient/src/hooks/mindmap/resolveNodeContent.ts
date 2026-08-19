import type Node from "@/classes/Node.ts";
import NodeType from "@/classes/NodeType.ts";
import type Page from "@/classes/Page.ts";
import type Topic from "@/classes/Topic.ts";

export function resolveNodeContent(
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
