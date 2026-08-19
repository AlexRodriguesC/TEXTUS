import { Button } from "@/components/ui/button.tsx";
import { useTranslation } from "react-i18next";
import { ArrowRight } from "lucide-react";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import ReviewTopicsButton from "@/components/ReviewTopicsButton.tsx";
import type { LessonNavigation } from "@/hooks/learning/useLessonNavigation.ts";

interface MasteredFooterProps {
  strSpaceId: string;
  navigation: LessonNavigation;
  onInvalidatePath: () => void;
}

function MasteredFooter({
  strSpaceId,
  navigation,
  onInvalidatePath,
}: MasteredFooterProps) {
  const { t } = useTranslation();
  const { chapterId, nextLesson, nextChapter } = navigation;

  return (
    <>
      <ReviewTopicsButton
        strSpaceId={strSpaceId}
        chapterId={chapterId}
        onCancel={() => undefined}
      />

      <Link
        to={PathConstants.LESSON}
        params={{
          spaceId: strSpaceId,
          lessonId: nextLesson?.id.toString() ?? "",
          chapterId: nextChapter?.index.toString() ?? "",
        }}
      >
        <Button
          className={"flex flex-row items-center"}
          onClick={onInvalidatePath}
        >
          <ArrowRight size={16} />
          {t("button.next")}
        </Button>
      </Link>
    </>
  );
}

export default MasteredFooter;
