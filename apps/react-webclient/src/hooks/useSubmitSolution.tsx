import type {AttemptRequestDTO, AttemptRequestDTOExerciseType} from "@/client/model";
import {getGetLessonQueryKey, useSubmitAttempt} from "@/client/generated/lessons/lessons.ts";
import {useQueryClient} from "@tanstack/react-query";

function useSubmitSolution({ spaceId, lessonId }: { spaceId: number | undefined; lessonId: number | undefined }) {
    const queryClient = useQueryClient();

    const { mutate, isPending } = useSubmitAttempt({
        mutation: {
            onSuccess: () => {
                queryClient.invalidateQueries({queryKey: getGetLessonQueryKey(spaceId!, lessonId!)})
            },
        }
    });

    const submitSolution = (
        exerciseId: number,
        exerciseType: AttemptRequestDTOExerciseType,
        response: unknown
    ) => {
        if (!spaceId || !lessonId) return;

        const payload: AttemptRequestDTO = {
            attemptId: crypto.randomUUID(),
            exerciseId,
            exerciseType,
            response,
        };

        return mutate({ spaceId, lessonId, data: payload });
    };

    return { submitSolution, isPending };
}

export default useSubmitSolution;