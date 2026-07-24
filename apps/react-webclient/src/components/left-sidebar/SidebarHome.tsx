import { useTranslation } from "react-i18next";
import {
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar.tsx";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import { Home } from "lucide-react";
import HoverResponseIcon from "@/components/HoverResponseIcon.tsx";

function SidebarHome() {
  const { t } = useTranslation();
  const home = t("sidebar.home");

  return (
    <SidebarMenuItem key={home}>
      <SidebarMenuButton asChild tooltip={home}>
        <Link to={PathConstants.HOME}>
          <HoverResponseIcon lucideIcon={Home} groupName="menu-button" />
          <span>{home}</span>
        </Link>
      </SidebarMenuButton>
    </SidebarMenuItem>
  );
}

export default SidebarHome;
