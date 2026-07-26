import type Chapter from "@/classes/Chapter.ts";
import type Lesson from "@/classes/Lesson.ts";
import type LearningPath from "@/classes/LearningPath.ts";
import useLessonId from "@/hooks/useLessonId.ts";
import useLearningPath from "@/hooks/learning/useLearningPath.ts";

export interface LessonNavigation {
  chapterId: number;
  nextLesson: Lesson | null;
  nextChapter: Chapter | null;
}

export function computeLessonNavigation(
  path: LearningPath | undefined,
  lessonId: number | undefined,
): LessonNavigation {
  const flatLessons = path?.chapters.flatMap((chapter) => chapter.lessons) ?? [];
  const chapterId =
    flatLessons.findIndex((item) => item.id === lessonId) ?? 0;
  const currentIndex = flatLessons.findIndex((l) => l.id === lessonId);
  const nextLesson =
    currentIndex >= 0 && currentIndex < flatLessons.length - 1
      ? flatLessons[currentIndex + 1]
      : null;
  const nextChapter = nextLesson
    ? path?.chapters.find((c) =>
        c.lessons.some((l) => l.id === nextLesson.id),
      ) ?? null
    : null;

  return { chapterId, nextLesson, nextChapter };
}

function useLessonNavigation(): LessonNavigation {
  const { lessonId } = useLessonId();
  const { path } = useLearningPath();
  return computeLessonNavigation(path, lessonId);
}

export default useLessonNavigation;
