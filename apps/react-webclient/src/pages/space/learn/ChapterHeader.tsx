import { type Chapter } from "@/hooks/useLearningPath.ts";
import {useTranslation} from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { NotebookIcon } from "lucide-react";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";

interface ChapterHeaderProps {
  chapter: Chapter;
}

function ChapterHeader({ chapter }: ChapterHeaderProps) {
  const { t } = useTranslation();
  const { strSpaceId } = useSpaceId();

  const totalLessons = chapter.lessons.length;
  const masteredLessons = chapter.lessons.map(l => l.completed).filter(l => l).length

  return (
    <div
      className={
        "sticky top-1 z-10 flex flex-col w-full md:flex-row gap-2 items-center justify-between px-4 py-3 bg-primary/40"
      }
    >
      <div className={"flex flex-col md:flex-row gap-4 items-center md:items-baseline"}>
        <p className={"font-extrabold text-primary-foreground truncate min-w-0"}>
          {chapter.title}
        </p>
        <p className="text-muted-foreground text-sm shrink-0">
          {t("chapter.header.lessons-left", {count: totalLessons - masteredLessons})}
        </p>
      </div>
      <Button
        variant={"secondary"}
        className={"bg-foreground/20 w-full md:w-fit"}
      >
        <Link
          to={PathConstants.SPACE_LEARN_GUIDE}
          params={{ spaceId: strSpaceId!, chapterId: chapter.index.toString() }}
        >
          <div className={"flex items-center gap-1"}>
            <NotebookIcon size={16} />
            <p>{t("chapter.header.guide")}</p>
          </div>
        </Link>
      </Button>
    </div>
  );
}

export default ChapterHeader;
