import CenteredPage from "@/components/CenteredPage.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import { useTranslation } from "react-i18next";
import GeneratePathButton from "@/pages/space/learn/GeneratePathButton.tsx";

function SpaceLearnEmpty() {
  const { t } = useTranslation();

  return (
    <CenteredPage>
      <div
        className={
          "flex flex-col items-center max-w-[80%] gap-2 md:flex-row md:items-stretch"
        }
      >
        <ResponsiveLottie
          src={"/animations/learn.lottie"}
          autoplay={false}
        ></ResponsiveLottie>
        <div className={"flex w-full md:w-md flex-col justify-between gap-4"}>
          <div
            className={"flex flex-col h-full items-center justify-center gap-2"}
          >
            <p className={"font-bold uppercase text-center"}>
              {t("learning-path.empty.title")}
            </p>
            <p className={"text-center"}>
              {t("learning-path.empty.description")}
            </p>
          </div>
          <div className={"h-fit w-full"}>
            <GeneratePathButton />
          </div>
        </div>
      </div>
    </CenteredPage>
  );
}

export default SpaceLearnEmpty;
