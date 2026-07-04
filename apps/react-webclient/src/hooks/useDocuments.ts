import {getGetDocumentsQueryKey, useDeleteDocument, useGetDocuments} from "@/api/generated/documents/documents.ts";
import type {DocumentResponseDTOStatus, DocumentsResponseDTO, ErrorResponseDTO} from "@/api/model";
import {successToast} from "@/lib/toasts.ts";
import {useQueryClient} from "@tanstack/react-query";
import {useTranslation} from "react-i18next";

export interface Document {
  name: string;
  id: number;
  status: DocumentStatus;
}

export type DocumentStatus = DocumentResponseDTOStatus;

function useDocuments(spaceId: number | undefined) {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const { data: documentStatuses } = useGetDocuments(spaceId ?? -1, {
    query: {
      refetchInterval: (query) => {
        const queryData = query.state.data?.data
        if (!queryData || !isDocumentResponseDTO(queryData)) return 2000
        const documents = queryData.documents
        const allDone =
            documents.length > 0 &&
            documents.every((doc) => doc.status === "DONE");
        return allDone ? false : 2000;
      },
      refetchIntervalInBackground: true,
      enabled: spaceId != undefined
    }
  });

  let documents: Document[] = [];

  const deleteMutation = useDeleteDocument()

  const deleteDocument = (documentId: number) => {
    if (spaceId == null) return
    deleteMutation.mutate({ spaceId, documentId },
        {
          onSuccess: async () => {
            await queryClient.invalidateQueries({
              queryKey: getGetDocumentsQueryKey(spaceId),
            });
            successToast(t("spaces.upload.success"));
          }
        }
    )
  }

  if (
    documentStatuses &&
    documentStatuses.data &&
    isDocumentResponseDTO(documentStatuses.data)
  ) {
    documents = documentStatuses.data.documents
  }

  return {
    documents: documents,
    deleteDocument
  };
}

function isDocumentResponseDTO(
    data: DocumentsResponseDTO | ErrorResponseDTO
): data is DocumentsResponseDTO {
  return "documents" in data;
}

export default useDocuments;
