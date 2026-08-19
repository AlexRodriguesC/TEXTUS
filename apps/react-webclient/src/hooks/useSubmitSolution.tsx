import type { AttemptRequestDTO } from "@/client/model";
import { AttemptRequestDTOExerciseType } from "@/client/model";
import {
  getGetLessonQueryKey,
  useSubmitAttempt,
} from "@/client/generated/lessons/lessons.ts";
import { useQueryClient } from "@tanstack/react-query";
import Attempt from "@/classes/Attempt.ts";
import type { ExerciseTypeType } from "@/classes/ExerciseType.ts";
import ExerciseType from "@/classes/ExerciseType.ts";
import { mapAttempt } from "@/hooks/dtoMappers.ts";

function toAttemptExerciseType(
  type: ExerciseTypeType,
): AttemptRequestDTOExerciseType {
  switch (type) {
    case ExerciseType.LIST:
      return AttemptRequestDTOExerciseType.LIST;
    case ExerciseType.GROUP:
      return AttemptRequestDTOExerciseType.GROUP;
    case ExerciseType.QUESTION:
      return AttemptRequestDTOExerciseType.QUESTION;
    case ExerciseType.PAIR:
      return AttemptRequestDTOExerciseType.PAIR;
  }
}

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
    exerciseType: ExerciseTypeType,
    response: unknown,
  ): Promise<Attempt | undefined> => {
    if (!spaceId || !lessonId) return undefined;

    const payload: AttemptRequestDTO = {
      attemptId: crypto.randomUUID(),
      exerciseId,
      exerciseType: toAttemptExerciseType(exerciseType),
      response,
    };

    const result = await mutateAsync({ spaceId, lessonId, data: payload });
    if (result.status === 201) {
      return mapAttempt(result.data);
    }
    return undefined;
  };

  return { submitSolution, isPending };
}

export default useSubmitSolution;
