import {Card, CardContent, CardDescription, CardTitle} from "@/components/ui/card.tsx";
import {CloudUpload, File, FileText, Trash2} from "lucide-react";
import {useTranslation} from "react-i18next";
import {
    Dropzone,
    DropZoneArea,
    DropzoneDescription,
    DropzoneFileList,
    DropzoneFileListItem,
    DropzoneMessage,
    DropzoneRemoveFile,
    DropzoneTrigger,
    useDropzone
} from "@/components/ui/dropzone.tsx";
import {failureToast, failureToastAction, successToast} from "@/lib/toasts.ts";
import {useState} from "react";
import {getGetDocumentStatusesQueryKey, useCreateDocument} from "@/api/generated/spaces/spaces.ts";
import {Button} from "@/components/ui/button.tsx";
import {useQueryClient} from "@tanstack/react-query";
import {useParams} from "@tanstack/react-router";

function Upload() {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const { spaceId } = useParams({
        from: "/main/spaces/$spaceId",
    });

    const [files, setFiles] = useState<File[]>([]);

    const uploadDocument = useCreateDocument({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({queryKey: getGetDocumentStatusesQueryKey(Number(spaceId))})
                successToast(t("spaces.upload.success"));
            },
            onError: async () => {
                failureToastAction(t("spaces.upload.error"), <Button onClick={submitUpload}>{t("button.retry")}</Button>)
            }
        }
    })

    const submitUpload = () => {
        if (!files || files.length == 0) {
            failureToast(t("spaces.upload.none"))
        }

    }

    const dropzone = useDropzone({
        onDropFile: async (file: File) => {
            setFiles((prev) => [...prev, file]);

            return {
                status: "success",
                result: file,
            };
        },
        onRemoveFile: (id: string) => {
            const fileName = dropzone.fileStatuses.findLast(f => f.id == id)?.fileName ?? "N/A";
            setFiles((prev) => prev.filter((file) => `${file.name}-${file.size}-${file.lastModified}` !== id));
            successToast(t("documents.delete.success", {fileName}));
        },
        onFileUploaded: (file: File) => {
            successToast(t("documents.upload.success", {fileName: file.name}));
        },
        validation: {
            accept: {
                "application/pdf": [".pdf"],
            },
            maxSize: 500 * 1024 * 1024,
            maxFiles: 20,
        },
    });

    return (
        <Card className="p-4">
            <Dropzone {...dropzone}>
                <div className="flex flex-col gap-4">
                    <div>
                        <CardTitle className="flex flex-row items-center gap-2">
                            <File className="size-5" />
                            <div>{t("documents.upload.title")}</div>
                        </CardTitle>

                        <CardDescription className="mt-2">
                            {t("documents.upload.description")}
                        </CardDescription>
                    </div>

                    <div className="flex items-center justify-between">
                        <DropzoneDescription>
                            {t("documents.upload.helper")}
                        </DropzoneDescription>
                        <DropzoneMessage />
                    </div>

                    <CardContent className="p-0">
                        <DropZoneArea className="min-w-full max-w-full border-2 border-dashed bg-muted/50 transition-colors">
                            <DropzoneTrigger className="flex w-full flex-col items-center gap-4 bg-transparent p-10 text-center text-sm">
                                <CloudUpload className="size-8" />
                                <div>
                                    <p className="font-semibold">{t("documents.upload.dropzoneTitle")}</p>
                                    <p className="text-sm text-muted-foreground">
                                        {t("documents.upload.dropzoneDescription")}
                                    </p>
                                </div>
                            </DropzoneTrigger>
                        </DropZoneArea>
                    </CardContent>

                    {dropzone.fileStatuses.length > 0 && (
                        <DropzoneFileList className="flex flex-col gap-2 p-0">
                            {dropzone.fileStatuses.map((file) => (
                                <DropzoneFileListItem
                                    key={file.id}
                                    file={file}
                                    className="flex flex-row justify-between bg-secondary p-3"
                                >
                                    <div className="flex flex-row gap-4 items-center min-w-0">
                                        <FileText/>
                                        <p className="truncate text-sm font-medium">{file.fileName}</p>
                                        <p className="text-xs text-muted-foreground">
                                            {(file.file.size / (1024 * 1024)).toFixed(2)} MB
                                        </p>
                                    </div>

                                    <DropzoneRemoveFile variant="ghost" className="shrink-0" children={<Trash2 className="size-4" color={"red"} />}/>
                                </DropzoneFileListItem>
                            ))}
                        </DropzoneFileList>
                    )}
                </div>
            </Dropzone>
        </Card>
    );
}

export default Upload;
