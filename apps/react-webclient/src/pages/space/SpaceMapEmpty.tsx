import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import GenerateMindMapButton from "@/pages/space/GenerateMindMapButton.tsx";
import { useTranslation } from "react-i18next";

function SpaceMapEmpty() {
  const { t } = useTranslation();

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
          <GenerateMindMapButton />
        </AsideFooter>
      </IllustratedAside>
    </CenteredPage>
  );
}

export default SpaceMapEmpty;
