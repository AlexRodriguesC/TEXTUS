import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import { Button } from "@/components/ui/button.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import PathConstants from "@/routing/pathConstants.ts";
import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";

function EmptyTopics() {
  const { t } = useTranslation();
  const { strSpaceId } = useSpaceId();

  return (
    <CenteredPage>
      <IllustratedAside
        className="max-w-[80%]"
        lottieSrc="/animations/ideas.lottie"
        lottieProps={{ autoplay: false }}
      >
        <AsideHeader>
          <p className="font-bold uppercase text-center">
            {t("topics.empty.title")}
          </p>
          <p className="text-center">{t("topics.empty.description")}</p>
        </AsideHeader>
        <AsideFooter>
          <Button className="w-full" asChild>
            <Link to={PathConstants.SPACE_MENU} params={{ spaceId: strSpaceId }}>
              {t("topics.empty.cta")}
            </Link>
          </Button>
        </AsideFooter>
      </IllustratedAside>
    </CenteredPage>
  );
}

export default EmptyTopics;
