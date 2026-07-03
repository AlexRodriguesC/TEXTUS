import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useDropzone } from "@/components/ui/dropzone.tsx";
import { failureToast, failureToastAction, successToast } from "@/lib/toasts.ts";
import {
    getGetDocumentsQueryKey,
    useCreateDocument,
} from "@/api/generated/documents/documents.ts";
import { Button } from "@/components/ui/button.tsx";

interface UseDocumentUploadOptions {
    spaceId: number;
}

export function useDocumentUpload({ spaceId }: UseDocumentUploadOptions) {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const [files, setFiles] = useState<File[]>([]);

    const uploadDocument = useCreateDocument({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: getGetDocumentsQueryKey(spaceId),
                });
                successToast(t("spaces.upload.success"));
            },
            onError: async () => {
                failureToastAction(
                    t("spaces.upload.error"),
                    <Button onClick={submitUpload}>{t("button.retry")}</Button>,
            );
            },
        },
    });

    const submitUpload = () => {
        if (!files || files.length === 0) {
            failureToast(t("spaces.upload.none"));
            return;
        }

        files.forEach((file: File) => {
            uploadDocument.mutate(
                { spaceId, data: { document: file } },
                {
                    onSuccess: () => {
                        const matchedStatus = dropzone.fileStatuses.find(
                            (s) =>
                                s.file.name === file.name &&
                                s.file.size === file.size &&
                                s.file.lastModified === file.lastModified,
                        );

                        if (matchedStatus) {
                            dropzone.clearFile(matchedStatus.id);
                            setFiles((prev) =>
                                prev.filter(
                                    (f) =>
                                        !(
                                            f.name === file.name &&
                                            f.size === file.size &&
                                            f.lastModified === file.lastModified
                                        ),
                                ),
                            );
                        }
                    },
                },
            );
        });
    };

    const dropzone = useDropzone({
        onDropFile: async (file: File) => {
            setFiles((prev) => [...prev, file]);
            return { status: "success", result: file };
        },
        onRemoveFile: (id: string) => {
            const fileName =
                dropzone.fileStatuses.findLast((f) => f.id === id)?.fileName ?? "N/A";
            setFiles((prev) =>
                prev.filter(
                    (file) => `${file.name}-${file.size}-${file.lastModified}` !== id,
                ),
            );
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

    return { dropzone, submitUpload, files };
}