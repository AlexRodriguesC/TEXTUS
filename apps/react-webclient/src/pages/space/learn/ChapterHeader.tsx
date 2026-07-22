import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { NotebookIcon } from "lucide-react";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { cn } from "@/lib/utils.ts";
import { useTheme } from "@/hooks/useTheme.ts";
import type Lesson from "@/classes/Lesson.ts";
import type Chapter from "@/classes/Chapter.ts";

interface ChapterHeaderProps {
  chapter: Chapter;
}

function ChapterHeader({ chapter }: ChapterHeaderProps) {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { strSpaceId } = useSpaceId();

  const totalLessons = chapter.lessons.length;
  const masteredLessons = chapter.lessons
    .map((l: Lesson) => l.completed)
    .filter((l: Lesson) => l).length;

  return (
    <div
      className={cn(
        "sticky top-1 z-10 flex flex-col w-full md:flex-row gap-2 items-center justify-between px-4 py-3",
        theme == "dark" ? "bg-primary/40" : "bg-primary/90",
      )}
    >
      <div
        className={
          "flex flex-col md:flex-row gap-4 items-center md:items-baseline"
        }
      >
        <p
          className={"font-extrabold text-primary-foreground truncate min-w-0"}
        >
          {chapter.title}
        </p>
        <p className={cn("text-secondary-foreground text-sm shrink-0", theme == "dark" ? "text-muted-foreground" : "text-primary-foreground")}>
          {t("chapter.header.lessons-left", {
            count: totalLessons - masteredLessons,
          })}
        </p>
      </div>
      <Button
        variant={"secondary"}
        className={cn(
          "w-full md:w-fit",
          theme == "dark" ? "bg-foreground/20" : "bg-background/20",
        )}
      >
        <Link
          to={PathConstants.SPACE_LEARN_GUIDE}
          params={{ spaceId: strSpaceId!, chapterId: chapter.index.toString() }}
        >
          <div className={"flex items-center gap-1 text-primary-foreground"}>
            <NotebookIcon size={16} />
            <p>{t("chapter.header.guide")}</p>
          </div>
        </Link>
      </Button>
    </div>
  );
}

export default ChapterHeader;
