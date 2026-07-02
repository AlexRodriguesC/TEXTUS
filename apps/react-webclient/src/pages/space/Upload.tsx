import {
  Card,
  CardContent,
  CardDescription,
  CardTitle,
} from "@/components/ui/card.tsx";
import { CloudUpload, File, FileText, Trash2 } from "lucide-react";
import { useTranslation } from "react-i18next";
import {
  Dropzone,
  DropZoneArea,
  DropzoneDescription,
  DropzoneFileList,
  DropzoneFileListItem,
  DropzoneMessage,
  DropzoneRemoveFile,
  DropzoneTrigger,
  useDropzone,
} from "@/components/ui/dropzone.tsx";
import {
  failureToast,
  failureToastAction,
  successToast,
} from "@/lib/toasts.ts";
import { useState } from "react";
import { Button } from "@/components/ui/button.tsx";
import { useQueryClient } from "@tanstack/react-query";
import useSpaceId from "@/hooks/useSpaceId.ts";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import {
  getGetDocumentsQueryKey,
  useCreateDocument,
} from "@/api/generated/documents/documents.ts";

interface UploadProps {
  showHeaders: boolean;
}

function Upload({ showHeaders }: UploadProps) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { spaceId } = useSpaceId();

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
    if (!files || files.length == 0) {
      failureToast(t("spaces.upload.none"));
    }

    files.forEach((file: File) => {
      uploadDocument.mutate(
        { spaceId: spaceId, data: { document: file } },
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

      return {
        status: "success",
        result: file,
      };
    },
    onRemoveFile: (id: string) => {
      const fileName =
        dropzone.fileStatuses.findLast((f) => f.id == id)?.fileName ?? "N/A";
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
      accept: {
        "application/pdf": [".pdf"],
      },
      maxSize: 500 * 1024 * 1024,
      maxFiles: 20,
    },
  });

  return (
    <Card className={showHeaders ? "p-4" : "pt-2 pb-8 px-2"}>
      <div className={"flex flex-col gap-2"}>
        <div className={"w-full h-fit flex justify-end"}>
          <InformationQuestionMark
            tooltip={t("documents.upload.description")}
          />
        </div>
        <Dropzone {...dropzone}>
          <div className="flex flex-col gap-4 sm:gap-5 max-h-full overflow-auto">
            {showHeaders && (
              <div>
                <CardTitle className="flex flex-row items-start gap-2 sm:items-center">
                  <File className="size-5 shrink-0" />
                  <div className="min-w-0 text-base sm:text-lg">
                    {t("documents.upload.title")}
                  </div>
                </CardTitle>

                <CardDescription className="mt-2 text-sm sm:text-base">
                  {t("documents.upload.description")}
                </CardDescription>
              </div>
            )}

            {showHeaders && (
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <DropzoneDescription>
                  {t("documents.upload.helper")}
                </DropzoneDescription>
                <DropzoneMessage />
              </div>
            )}

            <CardContent className="p-0">
              <DropZoneArea className="min-w-full max-w-full border-2 border-dashed bg-muted/50 transition-colors">
                <DropzoneTrigger className="flex w-full flex-col items-center gap-3 bg-transparent px-4 py-8 text-center text-sm sm:gap-4 sm:p-10">
                  <CloudUpload className="size-7 sm:size-8" />
                  <div className="space-y-1">
                    <p className="font-semibold">
                      {t("documents.upload.dropzoneTitle")}
                    </p>
                    <p className="text-xs text-muted-foreground sm:text-sm">
                      {t("documents.upload.dropzoneDescription")}
                    </p>
                  </div>
                </DropzoneTrigger>
              </DropZoneArea>
            </CardContent>

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
                        <p className="min-w-0 flex-1 truncate text-sm font-medium">
                          {file.fileName}
                        </p>
                        <p className="shrink-0 text-xs text-muted-foreground">
                          {(file.file.size / (1024 * 1024)).toFixed(2)} MB
                        </p>
                      </div>

                      <DropzoneRemoveFile
                        variant="ghost"
                        children={<Trash2 className="size-4" color={"red"} />}
                      />
                    </DropzoneFileListItem>
                  ))}
                </DropzoneFileList>
                <Button onClick={submitUpload}>{t("button.upload")}</Button>
              </div>
            )}
          </div>
        </Dropzone>
      </div>
    </Card>
  );
}

export default Upload;
