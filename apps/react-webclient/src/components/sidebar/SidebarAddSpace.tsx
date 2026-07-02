import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar.tsx";
import { Plus } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";

function SidebarAddSpace() {
  const { t } = useTranslation();
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;

  return (
    <SidebarGroup>
      <SidebarGroupContent>
        <SidebarMenu>
          <SidebarMenuItem key="space_info">
            <div
              className={`flex ${open ? "flex-row justify-between items-center w-full" : "flex-col items-center"}`}
            >
              <SidebarMenuButton asChild tooltip={t("sidebar.add-space")}>
                {!open && <Plus />}
              </SidebarMenuButton>
              {open && (
                <div className={"flex flex-col w-full items-center"}>
                  <Button asChild variant={"default"}>
                    <div className={"flex flex-row items-center"}>
                      <Plus strokeWidth={4} />
                      <p className={"font-bold uppercase"}>
                        {t("sidebar.add-space")}
                      </p>
                    </div>
                  </Button>
                </div>
              )}
            </div>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

export default SidebarAddSpace;
