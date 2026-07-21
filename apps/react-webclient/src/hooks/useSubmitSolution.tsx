import type {
  AttemptRequestDTO,
  AttemptRequestDTOExerciseType,
  AttemptResponseDTO,
} from "@/client/model";
import {
  getGetLessonQueryKey,
  useSubmitAttempt,
} from "@/client/generated/lessons/lessons.ts";
import { useQueryClient } from "@tanstack/react-query";

function useSubmitSolution({
  spaceId,
  lessonId,
}: {
  spaceId: number | undefined;
  lessonId: number | undefined;
}) {
  const queryClient = useQueryClient();

  const { mutateAsync, isPending } = useSubmitAttempt({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getGetLessonQueryKey(spaceId!, lessonId!),
        });
      },
    },
  });

  const submitSolution = async (
    exerciseId: number,
    exerciseType: AttemptRequestDTOExerciseType,
    response: unknown,
  ): Promise<AttemptResponseDTO | undefined> => {
    if (!spaceId || !lessonId) return undefined;

    const payload: AttemptRequestDTO = {
      attemptId: crypto.randomUUID(),
      exerciseId,
      exerciseType,
      response,
    };

    const result = await mutateAsync({ spaceId, lessonId, data: payload });
    if (result.status === 201) {
      return result.data;
    }
    return undefined;
  };

  return { submitSolution, isPending };
}

export default useSubmitSolution;
