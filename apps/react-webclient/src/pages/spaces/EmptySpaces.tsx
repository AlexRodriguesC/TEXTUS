import { useTranslation } from "react-i18next";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";

function EmptySpaces() {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col items-center">
      <div className={"flex flex-col items-center gap-4"}>
        <ResponsiveLottie src="/animations/think.lottie" loop autoplay/>
        <p className={"text-center"}>{t("spaces.add-spaces")}</p>
      </div>
    </div>
  );
}

export default EmptySpaces;
