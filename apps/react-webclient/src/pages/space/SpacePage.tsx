import useSpaceId from "@/hooks/useSpaceId.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import useSpaces from "@/hooks/useSpaces.ts";
import { useTranslation } from "react-i18next";
import EmptySpace from "@/pages/space/EmptySpace.tsx";
import useTutorial from "@/hooks/useTutorial.tsx";
import SpaceMap from "@/pages/space/SpaceMap.tsx";
import {useNavigate} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

function SpacePage() {
  const { spaceId } = useSpaceId();
  const { spaces } = useSpaces();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { tutorialEnabled, disableTutorial } = useTutorial()

  if (!spaces.find(s => s.spaceId === spaceId)) {
    navigate({to: PathConstants.NOT_FOUND})
  }

  if (tutorialEnabled) {
    return (
      <div className={"min-w-[75%] md:min-w-[50%] lg:min-w-[50%]"}>
        <EmptySpace onCompleted={() => {
          disableTutorial();
        }}/>
      </div>
    );
  }

  return (
      <CenteredPage>
        <div className={"w-full h-full pt-8 pb-8"}>
          <div className="flex flex-col lg:flex-row gap-4 h-full w-full">
            {/* Main content */}
            <div className="flex-1 w-full">
              <SpaceMap />
            </div>
          </div>
          <div className={"w-full h-fit"}>
            <p className={"text-muted-foreground text-xs place-self-center text-center"}>
              {t("textus.disclaimer")}
            </p>
          </div>
        </div>
      </CenteredPage>
  );
}

export default SpacePage;
