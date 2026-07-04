import {
    Sidebar,
    SidebarContent,
    SidebarFooter, SidebarGroup,
    SidebarHeader,
    SidebarTrigger,
} from "@/components/ui/sidebar.tsx";
import {PanelRightIcon} from "lucide-react";
import SidebarSpaceInfo from "@/components/left-sidebar/SidebarSpaceInfo.tsx";
import MindMapSidebarButton from "@/components/right-sidebar/MindMapSidebarButton.tsx";

export function RightSidebar() {

  return (
    <Sidebar collapsible="icon" side={"right"}>
      <SidebarHeader>
        <SidebarTrigger icon={<PanelRightIcon/>}/>
      </SidebarHeader>
      <SidebarContent>
        <SidebarSpaceInfo />
      </SidebarContent>
      <SidebarFooter>
          <SidebarGroup>
              <MindMapSidebarButton/>
          </SidebarGroup>
      </SidebarFooter>
    </Sidebar>
  );
}
