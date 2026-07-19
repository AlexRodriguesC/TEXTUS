import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import DocumentStatusList from "@/pages/space/DocumentStatusList.tsx";
import UploadZone from "@/components/UploadZone.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useSpaces from "@/hooks/useSpaces.ts";
import { useNavigate } from "@tanstack/react-router";
import useTutorial from "@/hooks/useTutorial.tsx";
import PathConstants from "@/routing/pathConstants.ts";
import EmptySpace from "@/pages/space/EmptySpace.tsx";
import InformationQuestionMark from "@/components/InformationQuestionMark.tsx";
import { useTranslation } from "react-i18next";

function SpaceMenu() {
  const { spaceId } = useSpaceId();
  const { spaces, isLoading } = useSpaces();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { tutorialEnabled, enableTutorial, disableTutorial } = useTutorial();

  if (!isLoading && !spaces.find((s) => s.spaceId === spaceId)) {
    navigate({ to: PathConstants.NOT_FOUND });
  }

  if (tutorialEnabled) {
    return (
      <CenteredPage>
        <div className={"min-w-[75%] md:min-w-[50%] lg:min-w-[50%]"}>
          <EmptySpace
            onCompleted={() => {
              disableTutorial();
            }}
          />
        </div>
      </CenteredPage>
    );
  }

  return (
    <CenteredPage>
      <div className={"max-w-[80%] place-content-center"}>
        <div className={"flex w-full justify-end"}>
          <span className="flex items-center h-[2em] leading-none overflow-hidden">
            {!tutorialEnabled && (
              <InformationQuestionMark
                size={16}
                onClick={enableTutorial}
                tooltip={t("tutorial.show")}
              />
            )}
          </span>
        </div>
        <div className="flex flex-col md:flex-row w-full gap-6 items-center place-content-center">
          <ResponsiveLottie src={"/animations/menu.lottie"} />

          <div className="flex flex-col gap-4">
            <UploadZone />

            <div className={"flex flex-col w-full items-center"}>
              <DocumentStatusList />
            </div>
          </div>
        </div>
      </div>
    </CenteredPage>
  );
}

export default SpaceMenu;
