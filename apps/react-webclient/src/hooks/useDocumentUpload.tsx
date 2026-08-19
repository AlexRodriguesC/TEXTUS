import { useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useDropzone } from "@/components/ui/dropzone.tsx";
import { failureToast, successToast } from "@/lib/toasts.tsx";
import {
  getGetDocumentsQueryKey,
  useCreateDocument,
  type getDocumentsResponse,
} from "@/client/generated/documents/documents.ts";
import type { DocumentResponseDTO, ErrorResponseDTO } from "@/client/model";

export const CREATE_DOCUMENT_MUTATION_KEY = ["createDocument"] as const;

interface UseDocumentUploadOptions {
  spaceId: number;
}

type CreateDocumentMutationContext = {
  previousDocuments: getDocumentsResponse | undefined;
};

export function useDocumentUpload({ spaceId }: UseDocumentUploadOptions) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const uploadDocument = useCreateDocument<
    ErrorResponseDTO,
    CreateDocumentMutationContext
  >({
    mutation: {
      onMutate: async ({ spaceId: targetSpaceId, data }) => {
        const queryKey = getGetDocumentsQueryKey(targetSpaceId);
        await queryClient.cancelQueries({ queryKey });

        const previousDocuments =
          queryClient.getQueryData<getDocumentsResponse>(queryKey);

        const file = data?.document;
        if (!file) {
          return { previousDocuments };
        }

        const optimisticDocument: DocumentResponseDTO = {
          id: -Date.now(),
          name: file.name,
          orderIndex: Number.MAX_SAFE_INTEGER,
          tasks: [],
        };

        queryClient.setQueryData<getDocumentsResponse>(queryKey, (old) => {
          if (!old || !("documents" in old.data)) {
            return {
              data: { documents: [optimisticDocument] },
              status: 200,
              headers: new Headers(),
            };
          }
          return {
            ...old,
            data: {
              ...old.data,
              documents: [...old.data.documents, optimisticDocument],
            },
          };
        });

        return { previousDocuments };
      },
      onError: (_err, { spaceId: targetSpaceId }, context) => {
        if (context?.previousDocuments !== undefined) {
          queryClient.setQueryData(
            getGetDocumentsQueryKey(targetSpaceId),
            context.previousDocuments,
          );
        }
        failureToast(t("spaces.upload.error"));
      },
      onSettled: async (_data, _error, { spaceId: targetSpaceId }) => {
        await queryClient.invalidateQueries({
          queryKey: getGetDocumentsQueryKey(targetSpaceId),
        });
      },
    },
  });

  const dropzone = useDropzone({
    onDropFile: async (file: File) => {
      try {
        await uploadDocument.mutateAsync({ spaceId, data: { document: file } });
        return { status: "success", result: file };
      } catch {
        return { status: "error", error: t("spaces.upload.error") };
      }
    },
    onRemoveFile: (id: string) => {
      const fileName =
        dropzone.fileStatuses.findLast((f) => f.id === id)?.fileName ?? "N/A";
      successToast(t("documents.delete.success", { fileName }));
    },
    onFileUploaded: (file: File) => {
      successToast(t("documents.upload.success", { fileName: file.name }));
    },
    validation: {
      accept: { "application/pdf": [".pdf"] },
      maxSize: 500 * 1024 * 1024,
      maxFiles: 5,
    },
  });

  return { dropzone };
}
