import { useParams } from "@tanstack/react-router";

function useLessonId() {
    const params = useParams({
        strict: false,
    });

    const numericLessonId = params?.lessonId ? Number(params.lessonId) : undefined;
    const strLessonId = params?.lessonId ? params.lessonId : undefined;

    return { lessonId: numericLessonId, strLessonId };
}

export default useLessonId;