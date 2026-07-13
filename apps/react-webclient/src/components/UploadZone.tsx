import useSpaceId from "@/hooks/useSpaceId.ts";
import {useTranslation} from "react-i18next";
import {useDocumentUpload} from "@/hooks/useDocumentUpload.tsx";
import {Dropzone, DropZoneArea, DropzoneTrigger} from "@/components/ui/dropzone.tsx";
import {CloudUpload} from "lucide-react";

function UploadZone() {
    const { spaceId } = useSpaceId();
    const { t } = useTranslation();
    const { dropzone } = useDocumentUpload({ spaceId });

    return (
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
            </div>
        </Dropzone>
    )
}

export default UploadZone;
