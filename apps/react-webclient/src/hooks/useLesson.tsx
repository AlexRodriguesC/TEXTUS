import {
  useGetLesson,
  useGetNextExercise,
  useDeleteExercises,
  getGetNextExerciseQueryKey,
  getGetLessonQueryKey,
} from "@/client/generated/lessons/lessons.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import { useQueryClient } from "@tanstack/react-query";
import { getGetLearningPathQueryKey } from "@/client/generated/paths/paths.ts";
import { useCallback, useMemo } from "react";
import type { Exercise } from "@/classes/Exercise.ts";
import { isExerciseDto, mapExercise, mapLesson } from "@/hooks/dtoMappers.ts";

const ignored = -1;

function useLesson({ lessonId }: { lessonId: number | undefined }) {
  const queryClient = useQueryClient();
  const { spaceId } = useSpaceId();

  const { data, isLoading } = useGetLesson(
    spaceId ?? ignored,
    lessonId ?? ignored,
    {
      query: { enabled: spaceId !== undefined && lessonId !== undefined },
    },
  );

  const { mutateAsync: deleteExercisesMutation, isPending: isDeletingExercises } =
    useDeleteExercises({
      mutation: {
        onMutate: async () => {
          await queryClient.resetQueries({
            queryKey: getGetNextExerciseQueryKey(
              spaceId ?? ignored,
              lessonId ?? ignored,
            ),
          });
        },
        onSuccess: async () => {
          await queryClient.invalidateQueries({
            queryKey: getGetLessonQueryKey(
              spaceId ?? ignored,
              lessonId ?? ignored,
            ),
          });
          await queryClient.invalidateQueries({
            queryKey: getGetNextExerciseQueryKey(
              spaceId ?? ignored,
              lessonId ?? ignored,
            ),
          });
          await queryClient.invalidateQueries({
            queryKey: getGetLearningPathQueryKey(spaceId ?? ignored),
          });
        },
      },
    });

  const lesson = useMemo(() => {
    if (
      !data ||
      !("status" in data) ||
      data.status !== 200 ||
      spaceId === undefined
    )
      return undefined;
    return mapLesson(data.data);
  }, [data, spaceId]);

  const { data: nextExerciseData, isLoading: exerciseLoading } =
    useGetNextExercise(spaceId ?? ignored, lessonId ?? ignored, {
      query: {
        enabled:
          spaceId !== undefined &&
          lessonId !== undefined &&
          !isLoading &&
          !!lesson &&
          lesson.shouldFetchNextExercise(),
      },
    });

  const nextExercise: Exercise | undefined = useMemo(() => {
    if (
      !nextExerciseData ||
      !("status" in nextExerciseData) ||
      nextExerciseData.status !== 200
    ) {
      return undefined;
    }
    const raw = nextExerciseData.data;
    if (!isExerciseDto(raw)) return undefined;
    return mapExercise(raw);
  }, [nextExerciseData]);

  const deleteExercises = useCallback(() => {
    if (spaceId === undefined || lessonId === undefined) {
      throw new Error("spaceId and lessonId are required to delete exercises");
    }
    return deleteExercisesMutation({ spaceId, lessonId });
  }, [deleteExercisesMutation, spaceId, lessonId]);

  const resetNextExercise = useCallback(() => {
    if (spaceId === undefined || lessonId === undefined) return;
    return queryClient.resetQueries({
      queryKey: getGetNextExerciseQueryKey(spaceId, lessonId),
    });
  }, [queryClient, spaceId, lessonId]);

  const invalidateLearningPath = useCallback(() => {
    if (spaceId === undefined) return;
    return queryClient.invalidateQueries({
      queryKey: getGetLearningPathQueryKey(spaceId),
    });
  }, [queryClient, spaceId]);

  return {
    lesson,
    isLoading,
    nextExercise,
    exerciseLoading,
    deleteExercises,
    isDeletingExercises,
    resetNextExercise,
    invalidateLearningPath,
  };
}

export default useLesson;
