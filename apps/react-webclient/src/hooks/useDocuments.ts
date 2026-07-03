import { useGetDocuments } from "@/api/generated/documents/documents.ts";
import type {DocumentsResponseDTO, ErrorResponseDTO} from "@/api/model";

export interface Document {
  id: number;
  status: "RECEIVED" | "DONE" | "PROCESSING";
}

function useDocuments(spaceId: number) {
  const { data: documentStatuses } = useGetDocuments(spaceId);

  if (
    documentStatuses &&
    documentStatuses.data &&
    isDocumentResponseDTO(documentStatuses.data)
  ) {
    return {documents: documentStatuses.data.documents};
  }

  return {
    documents: [],
  };
}

function isDocumentResponseDTO(
    data: DocumentsResponseDTO | ErrorResponseDTO
): data is DocumentsResponseDTO {
  return "documents" in data;
}

export default useDocuments;
