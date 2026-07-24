import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
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
      <IllustratedAside
        className="max-w-[80%]"
        lottieSrc="/animations/ideas.lottie"
        lottieProps={{ autoplay: false }}
      >
        <AsideHeader>
          <p className="font-bold uppercase text-center">
            {t("mindmap.empty.title")}
          </p>
          <p className="text-center">{t("mindmap.empty.description")}</p>
        </AsideHeader>
        <AsideFooter>
          {hasDocuments ? (
            <GenerateMindMapButton />
          ) : (
            <Button className="w-full" asChild>
              <Link
                to={PathConstants.SPACE_MENU}
                params={{ spaceId: strSpaceId! }}
              >
                {t("mindmap.empty.cta-add")}
              </Link>
            </Button>
          )}
        </AsideFooter>
      </IllustratedAside>
    </CenteredPage>
  );
}

export default SpaceMapEmpty;
