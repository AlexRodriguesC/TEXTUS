import {
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar.tsx";
import { useTranslation } from "react-i18next";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import { Box } from "lucide-react";
import HoverResponseIcon from "@/components/HoverResponseIcon.tsx";

function SidebarSpaces() {
  const { t } = useTranslation();
  const spaces = t("sidebar.spaces");

  return (
    <SidebarMenuItem key={spaces}>
      <SidebarMenuButton asChild tooltip={spaces}>
        <Link to={PathConstants.SPACES}>
          <HoverResponseIcon lucideIcon={Box} groupName="menu-button" />
          <span>{spaces}</span>
        </Link>
      </SidebarMenuButton>
    </SidebarMenuItem>
  );
}

export default SidebarSpaces;
