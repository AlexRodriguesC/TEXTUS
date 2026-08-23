import type { ReactNode } from "react";
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
  titleKey,
  descriptionKey,
  lottieSrc,
  lottieSide,
  aside,
}: TutorialStepProps) {
  const { t } = useTranslation();

  return (
    <div className="flex min-w-0 w-full items-center justify-center">
      <IllustratedAside
        lottieSrc={lottieSrc}
        lottieSide={lottieSide}
        aside={aside}
        contentClassName="min-w-0 flex-1 justify-center gap-4 md:gap-8"
      >
        <div className="flex min-w-0 flex-1 flex-col items-center justify-center gap-4 md:items-start md:gap-8">
          <h2 className="text-2xl font-semibold mb-2">{t(titleKey)}</h2>
          <p className="wrap-break-word">
            <Trans
              i18nKey={descriptionKey}
              components={{ bold: <strong /> }}
            />
          </p>
        </div>
      </IllustratedAside>
    </div>
  );
}

export default TutorialStep;
