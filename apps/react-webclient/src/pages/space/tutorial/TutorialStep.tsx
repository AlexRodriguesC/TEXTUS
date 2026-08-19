import type { ReactNode } from "react";
import { StepperContent } from "@/components/reui/stepper.tsx";
import { Trans, useTranslation } from "react-i18next";
import IllustratedAside from "@/components/IllustratedAside.tsx";

interface TutorialStepProps {
  index: number;
  titleKey: string;
  descriptionKey: string;
  lottieSrc?: string;
  lottieSide?: "left" | "right";
  aside?: ReactNode;
}

function TutorialStep({
  index,
  titleKey,
  descriptionKey,
  lottieSrc,
  lottieSide,
  aside,
}: TutorialStepProps) {
  const { t } = useTranslation();

  return (
    <StepperContent
      key={index}
      value={index + 1}
      className="flex w-full items-center justify-center"
    >
      <IllustratedAside
        lottieSrc={lottieSrc}
        lottieSide={lottieSide}
        aside={aside}
        contentClassName="flex-1 min-w-0 justify-center gap-8"
      >
        <div className="flex flex-col items-center md:items-start justify-center gap-8 min-w-0 flex-1">
          <h2 className="text-2xl font-semibold mb-2">{t(titleKey)}</h2>
          <p className="wrap-break-word">
            <Trans
              i18nKey={descriptionKey}
              components={{ bold: <strong /> }}
            />
          </p>
        </div>
      </IllustratedAside>
    </StepperContent>
  );
}

export default TutorialStep;
