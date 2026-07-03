import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
} from "@/components/ui/sidebar.tsx";
import SidebarLogo from "@/components/sidebar/SidebarLogo.tsx";
import SidebarSpaces from "@/components/sidebar/SidebarSpaces.tsx";
import SidebarHome from "@/components/sidebar/SidebarHome.tsx";
import { useTranslation } from "react-i18next";
import SidebarSettings from "@/components/sidebar/SidebarSettings.tsx";
import SidebarSpaceInfo from "@/components/sidebar/SidebarSpaceInfo.tsx";
import SidebarAddSpace from "@/components/sidebar/SidebarAddSpace.tsx";

export function AppSidebar() {
  const { t } = useTranslation();

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarLogo />
      </SidebarHeader>
      <SidebarContent>
        <SidebarAddSpace />
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.navigation")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarHome />
              <SidebarSpaces />
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
        <SidebarSpaceInfo />
      </SidebarContent>
      <SidebarFooter>
        <SidebarSettings />
      </SidebarFooter>
    </Sidebar>
  );
}
