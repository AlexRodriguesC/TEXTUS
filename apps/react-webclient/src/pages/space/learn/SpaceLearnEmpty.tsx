import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import { useTranslation } from "react-i18next";
import GeneratePathButton from "@/pages/space/learn/GeneratePathButton.tsx";

function SpaceLearnEmpty() {
  const { t } = useTranslation();

  return (
    <CenteredPage>
      <IllustratedAside
        className="max-w-[80%]"
        lottieSrc="/animations/learn.lottie"
        lottieProps={{ autoplay: false }}
      >
        <AsideHeader>
          <p className="font-bold uppercase text-center">
            {t("learning-path.empty.title")}
          </p>
          <p className="text-center">{t("learning-path.empty.description")}</p>
        </AsideHeader>
        <AsideFooter>
          <GeneratePathButton />
        </AsideFooter>
      </IllustratedAside>
    </CenteredPage>
  );
}

export default SpaceLearnEmpty;
