import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar.tsx";
import { useTranslation } from "react-i18next";
import useSpaces from "@/hooks/useSpaces.ts";
import { Emoji } from "emoji-picker-react";
import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import useTutorial from "@/hooks/useTutorial.tsx";
import {Button} from "@/components/ui/button.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  Dropzone,
  DropZoneArea,
  DropzoneFileList,
  DropzoneFileListItem,
  DropzoneRemoveFile,
  DropzoneTrigger
} from "@/components/ui/dropzone.tsx";
import {CloudUpload, FileText, Trash2} from "lucide-react";
import {useDocumentUpload} from "@/hooks/useDocumentUpload.tsx";

function SidebarSpaceInfo() {
  const { spaceId } = useSpaceId();
  const { state, openMobile } = useSidebar();
  const { t } = useTranslation();
  const spaces = useSpaces();
  const open = state === "expanded" || openMobile;
  const { tutorialEnabled, enableTutorial } = useTutorial();
  const { dropzone, submitUpload } = useDocumentUpload({ spaceId });

  if (!spaceId) {
    return <></>;
  }

  const space = spaces.spaces.find((s) => s.spaceId === spaceId);

  if (!space || !space.icon) {
    return <></>
  }

  return (
    <SidebarGroup>
      <SidebarGroupLabel>
        <div className="flex flex-row items-center justify-between gap-2 w-sm">
          <div className={"flex flex-row gap-2 items-center"}>
            <span>{t("sidebar.space", {spaceId: String(spaceId)})}</span>
            <span className="flex items-center h-[2em] leading-none overflow-hidden">
              <Emoji unified={space.icon} size={16} />
            </span>
          </div>
          <span className="flex items-center h-[2em] leading-none overflow-hidden">
              {!tutorialEnabled && <InformationQuestionMark size={16} onClick={enableTutorial} tooltip={t("tutorial.show")}></InformationQuestionMark>}
          </span>

        </div>
      </SidebarGroupLabel>
      <SidebarGroupContent>
        <Dropzone {...dropzone}>
          <div className="flex flex-col max-h-fit overflow-auto">
            <DropZoneArea className="p-0 w-full border-2 border-dashed bg-muted/50 transition-colors items-center place-content-center">
              <DropzoneTrigger className="flex w-full flex-row items-center gap-3 bg-transparent text-sm">
                <CloudUpload className="size-7 sm:size-8" />
                <p className="font-semibold flex w-full place-content-center">{t("documents.upload.dropzoneTitle")}</p>
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
                          <DropzoneRemoveFile variant="ghost" children={<Trash2 className="size-4" color={"red"} />} />
                        </DropzoneFileListItem>
                    ))}
                  </DropzoneFileList>
                  <Button onClick={submitUpload}>{t("button.upload")}</Button>
                </div>
            )}
          </div>
        </Dropzone>
        <SidebarMenu>
          <SidebarMenuItem key="space_info">
            <div
              className={`flex ${open ? "flex-row justify-between items-center w-full" : "flex-col items-center"}`}
            >
              <SidebarMenuButton asChild>
                {!open && (
                  <span className="flex items-center h-[1em] leading-none overflow-hidden">
                    <Emoji unified={space.icon} size={16} />
                  </span>
                )}
              </SidebarMenuButton>
              {open && (
                <div className={"flex flex-col w-full items-center"}>
                  <DocumentStatusList />
                </div>
              )}
            </div>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

export default SidebarSpaceInfo;
