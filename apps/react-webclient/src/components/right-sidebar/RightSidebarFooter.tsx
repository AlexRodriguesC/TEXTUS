import { SidebarGroup, useSidebar } from "@/components/ui/sidebar.tsx";
import PathSettings from "@/components/right-sidebar/settings/PathSettings.tsx";
import { useRouterState } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import DestructiveActionButton from "@/components/DestructiveActionButton.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { useTranslation } from "react-i18next";
import useLearningPath from "@/hooks/useLearningPath.ts";
import useMindMap from "@/hooks/useMindMap.ts";

function RightSidebarFooter() {
  const { t } = useTranslation();
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;
  const { open, openMobile } = useSidebar();
  const { path, status } = useLearningPath();
  const { mindMap, status: mindMapStatus } = useMindMap();

  // Match against the dynamic path patterns
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
              {path && status !== "NOT_CREATED" && (
                  <DestructiveActionButton
                      title={t("learning-path.delete.title")}
                      description={t("learning-path.delete.description")}
                      onConfirm={() => {
                        path.delete();
                      }}
                  />
              )}
              {!open && path && status === "DONE" && (
                  <Separator orientation={"horizontal"} />
              )}
              {path && status === "DONE" && <PathSettings />}
            </div>
        )}
        {isMindMap && (
            <div
                className={`flex flex-row gap-2 items-center w-full justify-between ${open || openMobile ? "justify-between" : "md:flex-col"}`}
            >
              {mindMap && mindMapStatus !== "NOT_CREATED" && (
                  <DestructiveActionButton
                      title={t("mind-map.delete.title")}
                      description={t("mind-map.delete.description")}
                      onConfirm={() => {
                        mindMap.delete();
                      }}
                  />
              )}
              {!open && mindMap && mindMapStatus === "DONE" && (
                  <Separator orientation={"horizontal"} />
              )}
            </div>
        )}
      </SidebarGroup>
  );
}

export default RightSidebarFooter;