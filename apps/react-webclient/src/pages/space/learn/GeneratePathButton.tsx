import useLearningPath from "@/hooks/learning/useLearningPath.ts";
import LearningPathJobStatus from "@/classes/LearningPathJobStatus.ts";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {useMemo} from "react";
import useDocuments from "@/hooks/useDocuments.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import {Marker, MarkerContent, MarkerIcon} from "@/components/ui/marker.tsx";

function GeneratePathButton() {
  const { t } = useTranslation();
    const { spaceId, strSpaceId } = useSpaceId();
    const { documents } = useDocuments(spaceId);
    const hasDocuments = useMemo(() => documents.length > 0, [documents.length]);
  const { status, regeneratePath, deletePath } = useLearningPath();

    if (!hasDocuments) {
        return (
            <Button className={"w-full"} asChild>
                <Link
                    to={PathConstants.SPACE_MENU}
                    params={{ spaceId: strSpaceId! }}
                >
                    {t("learning-path.empty.cta-add")}
                </Link>
            </Button>
        )
    }

    if (status === LearningPathJobStatus.FAILED) {
        return (
            <Button
                onClick={() => {
                    deletePath();
                    regeneratePath();
                }}
                className={"w-full border-destructive bg-destructive/80 hover:bg-destructive "}
            >
                <p className={"flex items-center justify-between gap-2"}>
                    {t("learning-path.empty.cta-regenerate")}
                </p>
            </Button>
        );
    }

  if (status === LearningPathJobStatus.NOT_CREATED) {
    return (
      <Button onClick={() => regeneratePath()} className={"w-full"}>
        {t("learning-path.empty.cta")}
      </Button>
    );
  }

  if (status === LearningPathJobStatus.GENERATING) {
    return (
        <Button
            disabled={true}
            className="w-full"
        >
            <Marker role="status" className="w-full flex place-content-center">
                <MarkerIcon>
                    <Spinner />
                </MarkerIcon>
                <MarkerContent className="flex items-center justify-between gap-2 shimmer">
                    {t("learning-path.empty.generating")}
                </MarkerContent>
            </Marker>
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
