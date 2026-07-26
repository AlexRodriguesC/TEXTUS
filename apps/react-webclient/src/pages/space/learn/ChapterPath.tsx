import PathNode from "@/pages/space/learn/PathNode.tsx";
import type Chapter from "@/classes/Chapter.ts";
import type Lesson from "@/classes/Lesson.ts";
import { getPrerequisiteLesson } from "@/hooks/learning/learningPathUtils.ts";

const OFFSETS = [0, 60, 0, -60];

interface ChapterPathProps {
  chapter: Chapter;
  chapters: Chapter[];
}

function ChapterPath({ chapter, chapters }: ChapterPathProps) {
  return (
    <div
      className="flex flex-col items-center gap-2 pb-8"
      data-chapter-index={chapter.index}
    >
      <div className="flex flex-col items-center gap-6 pt-4">
        {chapter.lessons.map((lesson: Lesson, i: number) => {
          const prerequisite = getPrerequisiteLesson(
            chapters,
            chapter.index,
            lesson,
          );
          const disabled = prerequisite !== null && !prerequisite.completed;

          return (
            <PathNode
              key={lesson.id}
              node={lesson}
              offset={OFFSETS[i % OFFSETS.length]}
              disabled={disabled}
            />
          );
        })}
      </div>
    </div>
  );
}

export default ChapterPath;
