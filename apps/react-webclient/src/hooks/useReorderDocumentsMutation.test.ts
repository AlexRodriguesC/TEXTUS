import { describe, expect, it } from "vitest";
import { reorderDocuments } from "@/hooks/useReorderDocumentsMutation.ts";
import type { DocumentResponseDTO } from "@/client/model";

function documentDto(
  id: number,
  name: string,
  orderIndex: number,
): DocumentResponseDTO {
  return {
    id,
    name,
    orderIndex,
    tasks: [],
  };
}

describe("reorderDocuments", () => {
  it("should move a document and rewrite contiguous order indexes", () => {
    const documents = [
      documentDto(1, "a.pdf", 0),
      documentDto(2, "b.pdf", 1),
      documentDto(3, "c.pdf", 2),
    ];

    expect(reorderDocuments(documents, 1, 2)).toEqual([
      documentDto(2, "b.pdf", 0),
      documentDto(3, "c.pdf", 1),
      documentDto(1, "a.pdf", 2),
    ]);
  });

  it("should leave the list unchanged when the document is missing", () => {
    const documents = [documentDto(1, "a.pdf", 0)];
    expect(reorderDocuments(documents, 99, 0)).toEqual(documents);
  });
});
