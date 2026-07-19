import { StepperContent } from "@/components/reui/stepper.tsx";
import { DotLottieReact } from "@lottiefiles/dotlottie-react";
import { Trans, useTranslation } from "react-i18next";

interface IntroductionProps {
  index: number;
}

function Introduction({ index }: IntroductionProps) {
  const { t } = useTranslation();

  return (
    <StepperContent
      key={index}
      value={index + 1}
      className="flex w-full items-center justify-center"
    >
      <div
        className={
          "flex flex-col md:flex-row w-full items-center gap-8 overflow-hidden"
        }
      >
        <div className={"w-xs md:w-md xl:w-xl shrink-0"}>
          <DotLottieReact src="/animations/ideas.lottie" loop autoplay />
        </div>
        <div
          className={
            "flex flex-col items-center md:items-start justify-center gap-8 min-w-0 flex-1"
          }
        >
          <h2 className="text-2xl font-semibold mb-2">{t("intro.title")}</h2>
          <p className="wrap-break-word">
            <Trans
              i18nKey="intro.description"
              components={{ bold: <strong /> }}
            />
          </p>
        </div>
      </div>
    </StepperContent>
  );
}

export default Introduction;
