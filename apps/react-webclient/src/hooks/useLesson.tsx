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
import { useMemo } from "react";
import Lesson from "@/classes/Lesson.ts";

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

  const { mutateAsync: deleteExercisesMutation, isPending } =
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
    return Lesson.from(data.data).withRuntime(
      spaceId,
      deleteExercisesMutation,
      isPending,
    );
  }, [data, spaceId, deleteExercisesMutation, isPending]);

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

  const nextExercise =
    nextExerciseData &&
    "status" in nextExerciseData &&
    nextExerciseData.status === 200
      ? nextExerciseData.data
      : undefined;

  return { lesson, isLoading, nextExercise, exerciseLoading };
}

export default useLesson;
