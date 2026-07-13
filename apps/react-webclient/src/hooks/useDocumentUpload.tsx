import { useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useDropzone } from "@/components/ui/dropzone.tsx";
import { failureToast, successToast } from "@/lib/toasts.ts";
import {
    getGetDocumentsQueryKey,
    useCreateDocument,
} from "@/client/generated/documents/documents.ts";

interface UseDocumentUploadOptions {
    spaceId: number | undefined;
}

export function useDocumentUpload({ spaceId }: UseDocumentUploadOptions) {
    const { t } = useTranslation();
    const queryClient = useQueryClient();

    const uploadDocument = useCreateDocument({
        mutation: {
            onSuccess: async () => {
                if (spaceId === undefined) return;
                await queryClient.invalidateQueries({
                    queryKey: getGetDocumentsQueryKey(spaceId),
                });
            },
            onError: async () => {
                failureToast(t("spaces.upload.error"));
            },
        },
    });

    const dropzone = useDropzone({
        onDropFile: async (file: File) => {
            if (spaceId === undefined) {
                failureToast(t("spaces.upload.noSpace"));
                return { status: "error", error: "No space selected" };
            }

            try {
                await uploadDocument.mutateAsync({ spaceId, data: { document: file } });
                return { status: "success", result: file };
            } catch (err) {
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
            maxFiles: 20,
        },
    });

    return { dropzone };
}