import type { MindMapResponseDTO, NodeDTO } from "@/client/model";
import {
  useDeleteMindMap,
  useExtractMindMap,
} from "@/client/generated/mind-maps/mind-maps.ts";

class MindMap {
  nodes: NodeDTO[];

  private spaceId?: number;
  private deleteMutation?: ReturnType<typeof useDeleteMindMap>["mutate"];
  private generateMutation?: ReturnType<typeof useExtractMindMap>["mutate"];

  isDeleting: boolean = false;
  isGenerating: boolean = false;

  constructor(nodes: NodeDTO[]) {
    this.nodes = nodes;
  }

  static from(dto: MindMapResponseDTO): MindMap {
    return new MindMap(dto.nodes ?? []);
  }

  withRuntime(
    spaceId: number,
    deleteMutation: ReturnType<typeof useDeleteMindMap>["mutate"],
    generateMutation: ReturnType<typeof useExtractMindMap>["mutate"],
    isDeleting: boolean = false,
    isGenerating: boolean = false,
  ): this {
    this.spaceId = spaceId;
    this.deleteMutation = deleteMutation;
    this.generateMutation = generateMutation;
    this.isDeleting = isDeleting;
    this.isGenerating = isGenerating;
    return this;
  }

  delete(): void {
    this.assertRuntime();
    this.deleteMutation!({ spaceId: this.spaceId! });
  }

  regenerate(): void {
    this.assertRuntime();
    this.generateMutation!({ spaceId: this.spaceId! });
  }

  private assertRuntime(): void {
    if (
      this.spaceId === undefined ||
      !this.deleteMutation ||
      !this.generateMutation
    ) {
      throw new Error(
        "MindMap method called without runtime dependencies (call withRuntime first)",
      );
    }
  }

  static isMindMapDTO(
    data: { nodes?: unknown } | MindMapResponseDTO,
  ): data is MindMapResponseDTO {
    return "nodes" in data;
  }
}

export default MindMap;
