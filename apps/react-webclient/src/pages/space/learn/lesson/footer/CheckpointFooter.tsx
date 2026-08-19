import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import { ArrowLeft, ArrowRight } from "lucide-react";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import type { LessonNavigation } from "@/hooks/learning/useLessonNavigation.ts";

interface CheckpointFooterProps {
  strSpaceId: string;
  navigation: LessonNavigation;
  exerciseLoading: boolean;
}

function CheckpointFooter({
  strSpaceId,
  navigation,
  exerciseLoading,
}: CheckpointFooterProps) {
  const { t } = useTranslation();
  const { nextLesson, nextChapter } = navigation;

  return (
    <>
      <Link to={PathConstants.SPACE_LEARN} params={{ spaceId: strSpaceId }}>
        <Button
          className={"flex flex-row items-center"}
          disabled={exerciseLoading}
          variant={"outline"}
        >
          <ArrowLeft size={16} />
          {t("button.back")}
        </Button>
      </Link>

      <Link
        to={PathConstants.LESSON}
        params={{
          spaceId: strSpaceId,
          lessonId: nextLesson?.id.toString() ?? "",
          chapterId: nextChapter?.index.toString() ?? "",
        }}
      >
        <Button className={"flex flex-row items-center"}>
          <ArrowRight size={16} />
          {t("button.next")}
        </Button>
      </Link>
    </>
  );
}

export default CheckpointFooter;
