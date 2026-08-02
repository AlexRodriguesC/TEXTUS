import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarTrigger,
} from "@/components/ui/sidebar.tsx";
import SidebarSpaces from "@/components/left-sidebar/SidebarSpaces.tsx";
import SidebarHome from "@/components/left-sidebar/SidebarHome.tsx";
import { useTranslation } from "react-i18next";
import SidebarSettings from "@/components/left-sidebar/SidebarSettings.tsx";
import HoverResponseIcon from "@/components/HoverResponseIcon.tsx";
import { PanelLeftIcon } from "lucide-react";

export function AppSidebar() {
  const { t } = useTranslation();

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarTrigger
          icon={<HoverResponseIcon lucideIcon={PanelLeftIcon} />}
        />
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.navigation")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarHome />
              <SidebarSpaces />
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter>
        <SidebarSettings />
      </SidebarFooter>
    </Sidebar>
  );
}
