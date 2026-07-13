import {
    Sidebar,
    SidebarContent,
    SidebarFooter, SidebarGroup,
    SidebarHeader,
    SidebarTrigger, useSidebar,
} from "@/components/ui/sidebar.tsx";
import {PanelRightIcon} from "lucide-react";
import SidebarSpaceInfo from "@/components/right-sidebar/SidebarSpaceInfo.tsx";
import MindMapSidebarButton from "@/components/right-sidebar/MindMapSidebarButton.tsx";

export function RightSidebar() {
    const { state, openMobile } = useSidebar();
    const open = state === "expanded" || openMobile;

  return (
    <Sidebar collapsible="icon" side={"right"} >
      <SidebarHeader>
        <SidebarTrigger icon={<PanelRightIcon/>}/>
      </SidebarHeader>
      <SidebarContent>
        <SidebarSpaceInfo />
      </SidebarContent>
      <SidebarFooter>
          <SidebarGroup>
              {open && <MindMapSidebarButton/>}
          </SidebarGroup>
      </SidebarFooter>
    </Sidebar>
  );
}
