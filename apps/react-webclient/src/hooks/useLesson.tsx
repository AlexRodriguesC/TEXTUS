import {
    type getLessonResponse,
    type getLessonResponseSuccess, type getNextExerciseResponse, type getNextExerciseResponseSuccess,
    useGetLesson, useGetNextExercise
} from "@/client/generated/lessons/lessons.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type {ErrorResponseDTO} from "@/client/model";

const ignored = -1;

function useLesson({ lessonId }: { lessonId: number | undefined }) {
    const { spaceId } = useSpaceId();

    const { data, isLoading } = useGetLesson(spaceId ?? ignored, lessonId ?? ignored, {
        query: {
            enabled: spaceId !== undefined && lessonId !== undefined,
        },
    });

    const { data:nextExerciseData, isLoading:exerciseLoading } = useGetNextExercise(spaceId ?? ignored, lessonId ?? ignored, {
        query: {
            enabled: spaceId !== undefined && lessonId !== undefined,
        },
    })

    const nextExercise = nextExerciseData && isExerciseResponse(nextExerciseData) ? nextExerciseData.data : undefined;

    const lesson = data && isLessonResponse(data) ? data.data : undefined;

    return { lesson, isLoading, nextExercise, exerciseLoading };
}

export default useLesson;

function isLessonResponse(
    resp: ErrorResponseDTO | getLessonResponse
): resp is getLessonResponseSuccess {
    return "status" in resp && resp.status === 200;
}

function isExerciseResponse(resp: ErrorResponseDTO | getNextExerciseResponse): resp is getNextExerciseResponseSuccess {
    return "status" in resp && resp.status === 200;
}