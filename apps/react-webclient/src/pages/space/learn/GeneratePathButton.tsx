import useLearningPath from "@/hooks/useLearningPath.ts";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { X } from "lucide-react";

function GeneratePathButton() {
  const { t } = useTranslation();
  const { spaceId } = useSpaceId();
  const { status, deleteLearningPath, generateLearningPath } =
    useLearningPath();

  if (status === "NOT_CREATED") {
    return (
      <Button
        onClick={() => generateLearningPath.mutate({ spaceId: spaceId! })}
        className={"w-full"}
      >
        {t("learning-path.empty.cta")}
      </Button>
    );
  }

  if (status === "GENERATING") {
    return (
      <Button disabled={true} className={"w-full"}>
        <div className={"flex items-center justify-between gap-2"}>
          {t("learning-path.empty.cta")}
          <Spinner />
        </div>
      </Button>
    );
  }

  if (status === "FAILED") {
    return (
      <Button
        onClick={() => {
          deleteLearningPath.mutate({ spaceId: spaceId! });
          generateLearningPath.mutate({ spaceId: spaceId! });
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

  if (status === "DONE") {
    return (
      <Button
        onClick={() => {
          deleteLearningPath.mutate(
            { spaceId: spaceId! },
            {
              onSuccess: () => {
                generateLearningPath.mutate({ spaceId: spaceId! });
              },
            },
          );
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
