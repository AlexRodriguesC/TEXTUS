import { SidebarGroup, useSidebar } from "@/components/ui/sidebar.tsx";
import PathSettings from "@/components/right-sidebar/settings/PathSettings.tsx";
import { useRouterState } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { useTranslation } from "react-i18next";
import useLearningPath from "@/hooks/useLearningPath.ts";
import useMindMap from "@/hooks/useMindMap.ts";
import LearningPathJobStatus from "@/classes/LearningPathJobStatus.ts";
import MindMapJobStatus from "@/classes/MindMapJobStatus.ts";

function RightSidebarFooter() {
  const { t } = useTranslation();
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;
  const { open, openMobile } = useSidebar();
  const { path, status, deletePath } = useLearningPath();
  const { mindMap, status: mindMapStatus, deleteMindMap } = useMindMap();

  const isMindMap = PathConstants.SPACE_MIND_MAP.startsWith("/spaces/")
    ? currentPath.match(/^\/spaces\/[^/]+\/mind_map$/)
    : false;
  const isLearn = PathConstants.SPACE_LEARN.startsWith("/spaces/")
    ? currentPath.match(/^\/spaces\/[^/]+\/learn$/)
    : false;

  return (
    <SidebarGroup className="flex items-center">
      {isLearn && (
        <div
          className={`flex flex-row gap-2 items-center w-full justify-between ${open || openMobile ? "justify-between" : "md:flex-col"}`}
        >
          {path && status !== LearningPathJobStatus.NOT_CREATED && (
            <DestructiveActionButton
              title={t("learning-path.delete.title")}
              description={t("learning-path.delete.description")}
              onConfirm={() => {
                deletePath();
              }}
            />
          )}
          {!open && path && status === LearningPathJobStatus.DONE && (
            <Separator orientation={"horizontal"} />
          )}
          {path && status === LearningPathJobStatus.DONE && <PathSettings />}
        </div>
      )}
      {isMindMap && (
        <div
          className={`flex flex-row gap-2 items-center w-full justify-between ${open || openMobile ? "justify-between" : "md:flex-col"}`}
        >
          {mindMap && mindMapStatus !== MindMapJobStatus.NOT_CREATED && (
            <DestructiveActionButton
              title={t("mind-map.delete.title")}
              description={t("mind-map.delete.description")}
              onConfirm={() => {
                deleteMindMap();
              }}
            />
          )}
          {!open && mindMap && mindMapStatus === MindMapJobStatus.DONE && (
            <Separator orientation={"horizontal"} />
          )}
        </div>
      )}
    </SidebarGroup>
  );
}

export default RightSidebarFooter;
