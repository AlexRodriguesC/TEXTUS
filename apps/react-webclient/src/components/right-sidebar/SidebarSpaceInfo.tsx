import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
  useSidebar,
} from "@/components/ui/sidebar.tsx";
import useSpaces from "@/hooks/useSpaces.ts";
import { Emoji } from "emoji-picker-react";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { GraduationCap, Menu, MessagesSquare, Workflow } from "lucide-react";
import SidebarAction from "@/components/right-sidebar/SidebarAction.tsx";
import RightSidebarBreadcrumbs from "@/components/right-sidebar/RightSidebarBreadcrumbs.tsx";
import PathConstants from "@/routing/pathConstants.ts";

function SidebarSpaceInfo() {
  const { spaceId } = useSpaceId();
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;
  const spaces = useSpaces();

  if (!spaceId) {
    return <></>;
  }

  const space = spaces.spaces.find((s) => s.spaceId === spaceId);

  if (!space || !space.icon) {
    return <></>;
  }

  return (
    <SidebarGroup>
      <SidebarGroupLabel>
        <div className="flex flex-row items-center justify-between gap-2 w-sm">
          <RightSidebarBreadcrumbs space={space} />
        </div>
      </SidebarGroupLabel>
      <SidebarGroupContent className={"flex flex-col items-center"}>
        <SidebarAction
          icon={<Menu />}
          labelKey={"sidebar.actions.menu.label"}
          tooltipKey={"sidebar.actions.menu.tooltip"}
          to={PathConstants.SPACE_MENU}
        />
        <SidebarSeparator className={"mt-2 mb-2"} />
        <SidebarAction
          to={PathConstants.SPACE_CHAT}
          icon={<MessagesSquare />}
          labelKey={"sidebar.actions.chat.label"}
          tooltipKey={"sidebar.actions.chat.tooltip"}
        />
        <SidebarAction
          to={PathConstants.SPACE_MIND_MAP}
          icon={<Workflow />}
          labelKey={"sidebar.actions.map.label"}
          tooltipKey={"sidebar.actions.map.tooltip"}
        />
        <SidebarAction
          to={PathConstants.SPACE_LEARN}
          icon={<GraduationCap />}
          labelKey={"sidebar.actions.learn.label"}
          tooltipKey={"sidebar.actions.learn.tooltip"}
        />
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
