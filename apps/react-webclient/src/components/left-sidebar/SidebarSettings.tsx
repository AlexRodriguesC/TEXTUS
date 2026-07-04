import {
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar.tsx";
import Profile from "@/components/Profile.tsx";
import { ModeToggle } from "@/components/ModeToggle.tsx";
import { LanguageComboBox } from "@/components/LanguageComboBox.tsx";

function SidebarSettings() {
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;

  return (
    <SidebarMenuItem key="settings">
      <div
        className={`flex ${open ? "flex-row justify-between items-center w-full" : "flex-col items-center"}`}
      >
        <SidebarMenuButton asChild>
          <Profile />
        </SidebarMenuButton>
        {open && (
          <>
            <LanguageComboBox />
            <ModeToggle />
          </>
        )}
      </div>
    </SidebarMenuItem>
  );
}

export default SidebarSettings;
