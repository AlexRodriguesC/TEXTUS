import useLearningPath from "@/hooks/useLearningPath.ts";
import LearningPathJobStatus from "@/classes/LearningPathJobStatus.ts";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import { X } from "lucide-react";

function GeneratePathButton() {
  const { t } = useTranslation();
  const { status, regeneratePath, deletePath } = useLearningPath();

  if (status === LearningPathJobStatus.NOT_CREATED) {
    return (
      <Button onClick={() => regeneratePath()} className={"w-full"}>
        {t("learning-path.empty.cta")}
      </Button>
    );
  }

  if (status === LearningPathJobStatus.GENERATING) {
    return (
      <Button disabled={true} className={"w-full"}>
        <div className={"flex items-center justify-between gap-2"}>
          {t("learning-path.empty.cta")}
          <Spinner />
        </div>
      </Button>
    );
  }

  if (status === LearningPathJobStatus.FAILED) {
    return (
      <Button
        onClick={() => {
          deletePath();
          regeneratePath();
        }}
        className={"w-full"}
      >
        <div className={"flex items-center justify-between gap-2"}>
          {t("learning-path.empty.cta-regenerate")}
          <X color={"red"} />
        </div>
      </Button>
    );
  }

  if (status === LearningPathJobStatus.DONE) {
    return (
      <Button
        onClick={() => {
          deletePath();
          regeneratePath();
        }}
        className={"w-full"}
      >
        <div className={"flex items-center justify-between gap-2"}>
          {t("learning-path.empty.cta-regenerate")}
        </div>
      </Button>
    );
  }
}

export default GeneratePathButton;
