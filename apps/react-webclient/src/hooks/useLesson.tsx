import {
  type getLessonResponse,
  type getLessonResponseSuccess,
  type getNextExerciseResponse,
  type getNextExerciseResponseSuccess,
  useGetLesson,
  useGetNextExercise,
  useDeleteExercises, getGetNextExerciseQueryKey, getGetLessonQueryKey,
} from "@/client/generated/lessons/lessons.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type { ErrorResponseDTO, LessonResponseDTO } from "@/client/model";
import {useQueryClient} from "@tanstack/react-query";
import {getGetLearningPathQueryKey} from "@/client/generated/paths/paths.ts";
import {useMemo} from "react";

const ignored = -1;

function createLessonModel(
    data: LessonResponseDTO,
    spaceId: number,
    deleteExercisesMutation: ReturnType<typeof useDeleteExercises>["mutateAsync"],
    isDeleting: boolean = false,
) {
  return {
    ...data,
    deleteExercises() {
      return deleteExercisesMutation({
        spaceId,
        lessonId: data.id,
      });
    },
    isDeleting,
    isMastered() {
      return data && data.completed
    }
  };
}

export type Lesson = ReturnType<typeof createLessonModel>;

function useLesson({ lessonId }: { lessonId: number | undefined }) {
  const queryClient = useQueryClient();
  const { spaceId } = useSpaceId();

  const { data, isLoading } = useGetLesson(
      spaceId ?? ignored,
      lessonId ?? ignored,
      {
        query: {
          enabled: spaceId !== undefined && lessonId !== undefined,
        },
      },
  );

  const { data: nextExerciseData, isLoading: exerciseLoading } =
      useGetNextExercise(spaceId ?? ignored, lessonId ?? ignored, {
        query: {
          enabled: spaceId !== undefined && lessonId !== undefined && (!isLoading && data && isLessonResponse(data) && !data.data.completed),
        },
      });

  const { mutateAsync: deleteExercisesMutation, isPending } = useDeleteExercises({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: getGetLessonQueryKey(spaceId ?? ignored, lessonId ?? ignored),
        });
        await queryClient.invalidateQueries({
          queryKey: getGetNextExerciseQueryKey(spaceId ?? ignored, lessonId ?? ignored),
        });
        await queryClient.invalidateQueries({
          queryKey: getGetLearningPathQueryKey(spaceId ?? ignored),
        });
      }
    }
  });

  const nextExercise =
      nextExerciseData && isExerciseResponse(nextExerciseData)
          ? nextExerciseData.data
          : undefined;

  const lessonData = useMemo(() => data && isLessonResponse(data) ? data.data : undefined, [data]);

  const lesson = useMemo(() => lessonData && spaceId !== undefined
      ? createLessonModel(lessonData, spaceId, deleteExercisesMutation, isPending)
      : undefined, [lessonData, spaceId, deleteExercisesMutation, isPending])

  return {
    lesson,
    isLoading,
    nextExercise,
    exerciseLoading,
  };
}

export default useLesson;

function isLessonResponse(
    resp: ErrorResponseDTO | getLessonResponse,
): resp is getLessonResponseSuccess {
  return "status" in resp && resp.status === 200;
}

function isExerciseResponse(
    resp: ErrorResponseDTO | getNextExerciseResponse,
): resp is getNextExerciseResponseSuccess {
  return "status" in resp && resp.status === 200;
}