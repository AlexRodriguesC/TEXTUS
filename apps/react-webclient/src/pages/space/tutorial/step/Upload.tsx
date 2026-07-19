import { StepperContent } from "@/components/reui/stepper.tsx";
import { useTranslation } from "react-i18next";
import UploadZone from "@/components/UploadZone.tsx";

interface UploadProps {
  index: number;
}

function Upload({ index }: UploadProps) {
  const { t } = useTranslation();

  return (
    <StepperContent
      key={index}
      value={index + 1}
      className="flex flex-row items-center justify-center"
    >
      <div
        className={
          "flex flex-col md:flex-row w-full justify-end items-center gap-8"
        }
      >
        <div
          className={
            "flex flex-col items-center md:items-start justify-center gap-8 min-w-0 flex-1"
          }
        >
          <h2 className="text-2xl font-semibold mb-2">{t("upload.title")}</h2>
          <p className="wrap-break-word overflow-hidden">
            {t("upload.description")}
          </p>
        </div>
        <UploadZone />
      </div>
    </StepperContent>
  );
}

export default Upload;
