import ChatHistorySidebar from "@/components/right-sidebar/ChatHistorySidebar.tsx";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarTrigger,
} from "@/components/ui/sidebar.tsx";
import { PanelRightIcon } from "lucide-react";
import SidebarSpaceInfo from "@/components/right-sidebar/SidebarSpaceInfo.tsx";
import RightSidebarFooter from "@/components/right-sidebar/RightSidebarFooter.tsx";

export function RightSidebar() {
  return (
    <Sidebar collapsible="icon" side={"right"}>
      <SidebarHeader>
        <SidebarTrigger icon={<PanelRightIcon />} />
      </SidebarHeader>
      <SidebarContent className="flex min-h-0 flex-col">
        <SidebarSpaceInfo />
        <ChatHistorySidebar />
      </SidebarContent>
      <SidebarFooter className="p-0">
        <RightSidebarFooter />
      </SidebarFooter>
    </Sidebar>
  );
}
