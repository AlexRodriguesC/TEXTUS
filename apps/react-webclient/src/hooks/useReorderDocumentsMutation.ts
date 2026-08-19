import {
  getGetDocumentsQueryKey,
  useMoveDocument,
  type getDocumentsResponse,
} from "@/client/generated/documents/documents.ts";
import type { DocumentResponseDTO, ErrorResponseDTO } from "@/client/model";
import { arrayMove } from "@dnd-kit/sortable";
import { useQueryClient } from "@tanstack/react-query";

type MoveDocumentMutationContext = {
  previousDocuments: getDocumentsResponse | undefined;
};

function reorderDocuments(
  documents: DocumentResponseDTO[],
  documentId: number,
  orderIndex: number,
): DocumentResponseDTO[] {
  const fromIndex = documents.findIndex((document) => document.id === documentId);
  if (fromIndex < 0) {
    return documents;
  }

  const toIndex = Math.max(0, Math.min(orderIndex, documents.length - 1));
  return arrayMove(documents, fromIndex, toIndex).map((document, index) => ({
    ...document,
    orderIndex: index,
  }));
}

export function useReorderDocumentsMutation() {
  const queryClient = useQueryClient();

  return useMoveDocument<ErrorResponseDTO, MoveDocumentMutationContext>({
    mutation: {
      onMutate: async ({ spaceId: targetSpaceId, documentId, data }) => {
        const queryKey = getGetDocumentsQueryKey(targetSpaceId);
        await queryClient.cancelQueries({ queryKey });

        const previousDocuments =
          queryClient.getQueryData<getDocumentsResponse>(queryKey);

        queryClient.setQueryData<getDocumentsResponse>(queryKey, (old) => {
          if (!old || !("documents" in old.data)) {
            return old;
          }

          return {
            ...old,
            data: {
              ...old.data,
              documents: reorderDocuments(
                old.data.documents,
                documentId,
                data.orderIndex,
              ),
            },
          };
        });

        return { previousDocuments };
      },
      onError: (_error, { spaceId: targetSpaceId }, context) => {
        if (context?.previousDocuments !== undefined) {
          queryClient.setQueryData(
            getGetDocumentsQueryKey(targetSpaceId),
            context.previousDocuments,
          );
        }
      },
      onSettled: async (_data, _error, { spaceId: targetSpaceId }) => {
        await queryClient.invalidateQueries({
          queryKey: getGetDocumentsQueryKey(targetSpaceId),
        });
      },
    },
  });
}

export function useReorderDocument(spaceId: number) {
  const mutation = useReorderDocumentsMutation();

  return {
    reorderDocument: (documentId: number, orderIndex: number) =>
      mutation.mutateAsync({
        spaceId,
        documentId,
        data: { orderIndex },
      }),
    isReordering: mutation.isPending,
  };
}

export { reorderDocuments };
