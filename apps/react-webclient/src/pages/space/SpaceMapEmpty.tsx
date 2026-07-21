import CenteredPage from "@/components/CenteredPage.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import useDocuments from "@/hooks/useDocuments.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useMemo } from "react";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import GenerateMindMapButton from "@/pages/space/GenerateMindMapButton.tsx";

function SpaceMapEmpty() {
  const { t } = useTranslation();
  const { spaceId, strSpaceId } = useSpaceId();
  const { documents } = useDocuments(spaceId);

  const hasDocuments = useMemo(() => documents.length > 0, [documents.length]);

  return (
    <CenteredPage>
      <div
        className={
          "flex flex-col items-center max-w-[80%] gap-2 md:flex-row md:items-stretch"
        }
      >
        <ResponsiveLottie
          src={"/animations/ideas.lottie"}
          autoplay={false}
        ></ResponsiveLottie>
        <div className={"flex w-full md:w-md flex-col justify-between gap-4"}>
          <div
            className={"flex flex-col h-full items-center justify-center gap-2"}
          >
            <p className={"font-bold uppercase text-center"}>
              {t("mindmap.empty.title")}
            </p>
            <p className={"text-center"}>
              {t("mindmap.empty.description")}
            </p>
          </div>
          <div className={"h-fit w-full"}>
            {hasDocuments ? (
              <GenerateMindMapButton />
            ) : (
              <Button className={"w-full"} asChild>
                <Link
                  to={PathConstants.SPACE_MENU}
                  params={{ spaceId: strSpaceId! }}
                >
                  {t("mindmap.empty.cta-add")}
                </Link>
              </Button>
            )}
          </div>
        </div>
      </div>
    </CenteredPage>
  );
}

export default SpaceMapEmpty;
