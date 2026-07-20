import { useParams } from "@tanstack/react-router";

function useLessonId() {
  const params = useParams({
    strict: false,
  });

  const numericLessonId = params?.lessonId
    ? Number(params.lessonId)
    : undefined;
  const strLessonId = params?.lessonId ? params.lessonId : undefined;

  if (!numericLessonId && strLessonId) throw new Error(
      "useLessonId must be used within a valid path containing a lessonId"
  )

  return { lessonId: numericLessonId!, strLessonId: strLessonId! };
}

export default useLessonId;
