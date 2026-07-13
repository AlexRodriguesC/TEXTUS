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
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import useTutorial from "@/hooks/useTutorial.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import {GraduationCap, MessagesSquare, Workflow} from "lucide-react";
import SidebarAction from "@/components/right-sidebar/SidebarAction.tsx";
import useViewMode from "@/hooks/useViewMode.ts";
import RightSidebarBreadcrumbs from "@/components/right-sidebar/RightSidebarBreadcrumbs.tsx";

function SidebarSpaceInfo() {
  const { spaceId } = useSpaceId();
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;
  const { t } = useTranslation();
  const spaces = useSpaces();
  const { tutorialEnabled, enableTutorial } = useTutorial();
  const { setViewMode } = useViewMode();


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
          <RightSidebarBreadcrumbs space={space} />
          <span className="flex items-center h-[2em] leading-none overflow-hidden">
      {!tutorialEnabled && (
          <InformationQuestionMark
              size={16}
              onClick={enableTutorial}
              tooltip={t("tutorial.show")}
          />
      )}
    </span>
        </div>
      </SidebarGroupLabel>
      <SidebarGroupContent>
        <SidebarAction onClick={() => setViewMode("chat")} icon={<MessagesSquare/>} labelKey={"sidebar.actions.chat.label"} tooltipKey={"sidebar.actions.chat.tooltip"}/>
        <SidebarAction onClick={() => setViewMode("mind-map")} icon={<Workflow/>} labelKey={"sidebar.actions.map.label"} tooltipKey={"sidebar.actions.map.tooltip"} backgroundColor="#2f2733"/>
        <SidebarAction onClick={() => setViewMode("learning")} icon={<GraduationCap/>} labelKey={"sidebar.actions.learn.label"} tooltipKey={"sidebar.actions.learn.tooltip"} backgroundColor="#E9ECFC"/>
      </SidebarGroupContent>

      <SidebarGroupContent>
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
            </div>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

export default SidebarSpaceInfo;
