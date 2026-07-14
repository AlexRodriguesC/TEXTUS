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
import {useAuth} from "@/hooks/useAuth.ts";
import CreateSpace from "@/pages/spaces/CreateSpace.tsx";
import {useNavigate} from "@tanstack/react-router";
import {useCallback} from "react";
import PathConstants from "@/routing/pathConstants.ts";

function SidebarAddSpace() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;

  const navigateToSpace = useCallback((spaceId: number) => {
    navigate({to: PathConstants.SPACE_MENU, params: {spaceId: String(spaceId)}})
  }, [navigate])

  return (
    <>
      {
        isAuthenticated &&
          <SidebarGroup>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem key="space_info">
                  <div
                      className={`flex ${open ? "flex-row justify-between items-center" : "flex-col items-center"}`}
                  >
                    <SidebarMenuButton asChild tooltip={t("sidebar.add-space")}>
                      {!open &&
                          <CreateSpace trigger={
                            <Plus className={"cursor-pointer"} />
                          } onSuccess={navigateToSpace}>
                          </CreateSpace>
                          }
                    </SidebarMenuButton>
                    {open && (
                        <div className={"flex flex-col w-full items-center"}>
                          <CreateSpace trigger={
                            <Button asChild variant={"default"}>
                              <div className={"flex flex-row items-center"}>
                                <Plus strokeWidth={4} />
                                <p className={"font-bold uppercase"}>
                                  {t("sidebar.add-space")}
                                </p>
                              </div>
                            </Button>
                          } onSuccess={navigateToSpace}></CreateSpace>
                        </div>
                    )}
                  </div>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
      }
    </>
  );
}

export default SidebarAddSpace;
