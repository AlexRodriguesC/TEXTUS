import {
  getGetDocumentsQueryKey,
  useDeleteDocument,
  useGetDocuments,
  useRetryDocument,
} from "@/client/generated/documents/documents.ts";
import type { DocumentsResponseDTO, ErrorResponseDTO } from "@/client/model";
import Document from "@/classes/Document.ts";
import { mapDocument } from "@/hooks/dtoMappers.ts";
import { CREATE_DOCUMENT_MUTATION_KEY } from "@/hooks/useDocumentUpload.tsx";
import { isDone, isFailed } from "@/lib/documentTaskUtils.ts";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useMutationState, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";

export { isDone, isFailed, isInProgress } from "@/lib/documentTaskUtils.ts";
export { default as Task } from "@/classes/Task.ts";

type UploadMutationState = {
  status: string;
  spaceId: number | undefined;
  fileName: string | undefined;
  tempId: number;
};

function useDocuments(spaceId: number) {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const { data: documentStatuses } = useGetDocuments(spaceId ?? -1, {
    query: {
      refetchInterval: (query) => {
        const queryData = query.state.data?.data;
        if (!queryData || !isDocumentsResponseDTO(queryData)) return 2000;
        const documents = queryData.documents.map(mapDocument);
        const allSettled =
          documents.length > 0 &&
          documents.every(
            (doc) => isDone(doc.tasks) || isFailed(doc.tasks),
          );
        return allSettled ? false : 2000;
      },
      refetchIntervalInBackground: true,
      enabled: spaceId != undefined,
    },
  });

  const uploadMutations = useMutationState({
    filters: { mutationKey: CREATE_DOCUMENT_MUTATION_KEY },
    select: (mutation): UploadMutationState => ({
      status: mutation.state.status,
      spaceId: mutation.state.variables?.spaceId,
      fileName: mutation.state.variables?.data?.document?.name,
      tempId: -mutation.mutationId,
    }),
  });

  let documents: Document[] = [];

  const deleteMutation = useDeleteDocument();

  const retryMutation = useRetryDocument({
    mutation: {
      onSuccess: async () => {
        if (spaceId == null) return;
        await queryClient.invalidateQueries({
          queryKey: getGetDocumentsQueryKey(spaceId),
        });
        successToast(t("documents.retry.success"));
      },
      onError: () => {
        failureToast(t("documents.retry.error"));
      },
    },
  });

  const deleteDocument = (documentId: number) => {
    if (documentId < 0) return;
    const fileName = documents.find((doc) => doc.id === documentId)?.name;
    deleteMutation.mutate(
      { spaceId, documentId },
      {
        onSuccess: async () => {
          await queryClient.invalidateQueries({
            queryKey: getGetDocumentsQueryKey(spaceId),
          });
          successToast(t("documents.delete.success", {fileName}));
        },
      },
    );
  };

  if (
    documentStatuses &&
    documentStatuses.data &&
    isDocumentsResponseDTO(documentStatuses.data)
  ) {
    documents = documentStatuses.data.documents.map(mapDocument);
  }

  const serverDocumentNames = new Set(documents.map((doc) => doc.name));
  const optimisticDocuments = uploadMutations
    .filter(
      (mutation) =>
        mutation.spaceId === spaceId &&
        mutation.fileName != null &&
        (mutation.status === "pending" ||
          (mutation.status === "success" &&
            !serverDocumentNames.has(mutation.fileName))),
    )
    .filter((mutation) => !serverDocumentNames.has(mutation.fileName!))
    .map(
      (mutation) =>
        new Document(mutation.fileName!, mutation.tempId, []),
    );

  return {
    documents: [...documents, ...optimisticDocuments],
    deleteDocument,
    retryDocument: (documentId: number) =>
      retryMutation.mutate({ spaceId, documentId }),
    isRetrying: retryMutation.isPending,
  };
}

function isDocumentsResponseDTO(
  data: DocumentsResponseDTO | ErrorResponseDTO,
): data is DocumentsResponseDTO {
  return "documents" in data;
}

export default useDocuments;
