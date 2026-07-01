import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from "@/components/ui/sidebar";
import * as React from "react";
import Logo from "@/components/Logo.tsx";
import Profile from "@/components/Profile.tsx";
import { LanguageComboBox } from "@/components/LanguageComboBox.tsx";
import { ModeToggle } from "@/components/ModeToggle.tsx";

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
    <Sidebar
      className={"border-r-0"}
      collapsible="icon"
      draggable={false}
      {...props}
    >
      <SidebarHeader className={"w-full items-center"}>
        <Logo></Logo>
      </SidebarHeader>
      <SidebarContent></SidebarContent>
      <SidebarFooter className={"flex flex-row justify-between"}>
        <Profile />
        <LanguageComboBox />
        <ModeToggle />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  );
}
