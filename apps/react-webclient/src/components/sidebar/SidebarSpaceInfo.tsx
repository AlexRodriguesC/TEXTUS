import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar.tsx";
import { useParams } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import useSpaces from "@/hooks/useSpaces.ts";
import { Emoji } from "emoji-picker-react";
import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import useTutorial from "@/hooks/useTutorial.tsx";
import useMindMap from "@/hooks/useMindMap.ts";
import {Button} from "@/components/ui/button.tsx";

function SidebarSpaceInfo() {
  const spaceId = useParams({
    from: "/main/spaces/$spaceId",
    shouldThrow: false,
  });
  const { state, openMobile } = useSidebar();
  const { t } = useTranslation();
  const spaces = useSpaces();
  const open = state === "expanded" || openMobile;
  const { tutorialEnabled, enableTutorial } = useTutorial();
  const { extractMindMap, status, mindMap } = useMindMap(Number(spaceId ? spaceId.spaceId : -1)) // todO: fix

  if (!spaceId) {
    return <></>;
  }

  const space = spaces.spaces.filter(
    (s) => s.spaceId == Number(spaceId.spaceId),
  )[0];

  if (!space || !space.icon) {
    return <></>
  }

  return (
    <SidebarGroup>
      <SidebarGroupLabel>
        <div className="flex flex-row items-center justify-between gap-2 w-sm">
          <div className={"flex flex-row gap-2 items-center"}>
            <span>{t("sidebar.space", spaceId)}</span>
            <span className="flex items-center h-[1em] leading-none overflow-hidden">
              <Emoji unified={space.icon} size={16} />
            </span>
          </div>
          <span className="flex items-center h-[1em] leading-none overflow-hidden">
              {!tutorialEnabled && <InformationQuestionMark size={16} onClick={enableTutorial} tooltip={t("tutorial.show")}></InformationQuestionMark>}
            </span>

        </div>
      </SidebarGroupLabel>
      <SidebarGroupContent>
        <SidebarMenu>
          <SidebarMenuItem key="space_info">
            <div
              className={`flex ${open ? "flex-row justify-between items-center w-full" : "flex-col items-center"}`}
            >
              <SidebarMenuButton asChild>
                {!open && (
                  <span className="flex items-center h-[1em] leading-none overflow-hidden">
                    <Emoji unified={space.icon} size={16} />
                  </span>
                )}
              </SidebarMenuButton>
              {open && (
                <div className={"flex flex-col w-full items-center"}>
                  <DocumentStatusList />
                  <Button onClick={() => extractMindMap.mutate({spaceId: space.spaceId})}>Generate {status == "GENERATING" ? "gen" : "idk"}</Button>
                </div>
              )}
            </div>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  );
}

export default SidebarSpaceInfo;
