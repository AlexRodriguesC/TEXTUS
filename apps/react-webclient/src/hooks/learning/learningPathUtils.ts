import {
  getGetLearningPathQueryKey,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import type { LearningPathResponseDTOStatus } from "@/client/model";
import { ApiError } from "@/client/mutator.ts";
import LearningPathJobStatus from "@/classes/LearningPathJobStatus.ts";
import { isLearningPathDto } from "@/hooks/dtoMappers.ts";
import type { QueryClient } from "@tanstack/react-query";

export function isLearningPathNotFound(error: unknown): boolean {
  return error instanceof ApiError && error.status === 404;
}

export function setPathStatus(
  queryClient: QueryClient,
  spaceId: number,
  status: LearningPathResponseDTOStatus,
) {
  const queryKey = getGetLearningPathQueryKey(spaceId);
  queryClient.setQueryData<getLearningPathResponse>(queryKey, (old) => {
    if (old?.status === 200 && isLearningPathDto(old.data)) {
      return {
        ...old,
        data: {
          ...old.data,
          status,
          chapters:
            status === LearningPathJobStatus.GENERATING ||
            status === LearningPathJobStatus.NOT_CREATED
              ? []
              : old.data.chapters,
        },
      };
    }
    return old;
  });
}
