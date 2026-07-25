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
import { shouldPollDocuments } from "@/lib/documentTaskUtils.ts";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useMutationState, useQueryClient } from "@tanstack/react-query";
import { useRef } from "react";
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
  const awaitingServerUploadRef = useRef(false);

  const uploadMutations = useMutationState({
    filters: { mutationKey: CREATE_DOCUMENT_MUTATION_KEY },
    select: (mutation): UploadMutationState => ({
      status: mutation.state.status,
      spaceId: mutation.state.variables?.spaceId,
      fileName: mutation.state.variables?.data?.document?.name,
      tempId: -mutation.mutationId,
    }),
  });

  const { data: documentStatuses, isLoading } = useGetDocuments(spaceId ?? -1, {
    query: {
      refetchInterval: (query) => {
        const queryData = query.state.data?.data;
        const documents =
          queryData && isDocumentsResponseDTO(queryData)
            ? queryData.documents.map(mapDocument)
            : [];
        return shouldPollDocuments(documents, awaitingServerUploadRef.current);
      },
      refetchIntervalInBackground: true,
      enabled: spaceId != undefined,
    },
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
  awaitingServerUploadRef.current = uploadMutations.some(
    (mutation) =>
      mutation.spaceId === spaceId &&
      mutation.fileName != null &&
      (mutation.status === "pending" ||
        (mutation.status === "success" &&
          !serverDocumentNames.has(mutation.fileName))),
  );
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
    isLoading,
  };
}

function isDocumentsResponseDTO(
  data: DocumentsResponseDTO | ErrorResponseDTO,
): data is DocumentsResponseDTO {
  return "documents" in data;
}

export default useDocuments;
