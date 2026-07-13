import {StepperContent} from "@/components/reui/stepper.tsx";
import {
    Dropzone,
    DropZoneArea, DropzoneFileList, DropzoneFileListItem, DropzoneRemoveFile,
    DropzoneTrigger
} from "@/components/ui/dropzone.tsx";
import {CloudUpload, FileText, Trash2} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import { useDocumentUpload } from "@/hooks/useDocumentUpload";
import useSpaceId from "@/hooks/useSpaceId.ts";
import {useTranslation} from "react-i18next";

interface UploadProps {
    index: number
}

function Upload({ index }: UploadProps) {
    const { spaceId } = useSpaceId();
    const { t } = useTranslation();
    const { dropzone, submitUpload } = useDocumentUpload({ spaceId });

    return (
        <StepperContent
            key={index}
            value={index + 1}
            className="flex flex-row items-center justify-center"
        >
            <div className={"flex flex-col md:flex-row w-full justify-end items-center gap-8"}>
                <div className={"flex flex-col items-center md:items-start justify-center gap-8 min-w-0 flex-1"}>
                    <h2 className="text-2xl font-semibold mb-2">
                        {t("upload.title")}
                    </h2>
                    <p className="break-words">
                        {t("upload.description")}
                    </p>
                </div>
                <Dropzone {...dropzone}>
                    <div className="flex flex-col gap-4 sm:gap-5 max-h-full overflow-auto">
                        <DropZoneArea className="min-w-full max-w-full border-2 border-dashed bg-muted/50 transition-colors">
                            <DropzoneTrigger className="flex w-full flex-col items-center gap-3 bg-transparent px-4 py-8 text-center text-sm sm:gap-4 sm:p-10">
                                <CloudUpload className="size-7 sm:size-8" />
                                <div className="space-y-1">
                                    <p className="font-semibold">{t("documents.upload.dropzoneTitle")}</p>
                                    <p className="text-xs text-muted-foreground sm:text-sm">
                                        {t("documents.upload.dropzoneDescription")}
                                    </p>
                                </div>
                            </DropzoneTrigger>
                        </DropZoneArea>

                        {dropzone.fileStatuses.length > 0 && (
                            <div className={"flex flex-col gap-2 min-w-full h-fit"}>
                                <DropzoneFileList className="flex flex-col gap-2 p-0 max-h-64 overflow-y-auto">
                                    {dropzone.fileStatuses.map((file) => (
                                        <DropzoneFileListItem
                                            key={file.id}
                                            file={file}
                                            className="flex flex-col gap-3 bg-secondary p-3 sm:flex-row sm:items-center sm:justify-between"
                                        >
                                            <div className="flex min-w-0 flex-1 flex-row items-center gap-3 sm:gap-4">
                                                <FileText className="shrink-0" />
                                                <p className="min-w-0 flex-1 truncate text-sm font-medium">{file.fileName}</p>
                                                <p className="shrink-0 text-xs text-muted-foreground">
                                                    {(file.file.size / (1024 * 1024)).toFixed(2)} MB
                                                </p>
                                            </div>
                                            <DropzoneRemoveFile>
                                                <Trash2 className="size-4" color={"red"} />
                                            </DropzoneRemoveFile>
                                        </DropzoneFileListItem>
                                    ))}
                                </DropzoneFileList>
                                <Button onClick={submitUpload} disabled={spaceId === undefined}>{t("button.upload")}</Button>
                            </div>
                        )}
                    </div>
                </Dropzone>
            </div>
        </StepperContent>
    )
}

export default Upload;
