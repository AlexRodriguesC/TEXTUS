import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  getGetLearningPathQueryKey,
  useGetLearningPath,
} from "@/client/generated/paths/paths.ts";
import LearningPathJobStatus, {
  type LearningPathJobStatusType,
} from "@/classes/LearningPathJobStatus.ts";
import { mapLearningPath } from "@/hooks/dtoMappers.ts";
import useDeletePathMutation from "@/hooks/learning/useDeletePathMutation.ts";
import {
  isLearningPathsResponseDto,
} from "@/hooks/learning/learningPathUtils.ts";
import useRegeneratePathMutation from "@/hooks/learning/useRegeneratePathMutation.ts";
import useUpdateSettingsMutation from "@/hooks/learning/useUpdateSettingsMutation.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useMemo, useRef, useState } from "react";
import type LearningPath from "@/classes/LearningPath.ts";

function useLearningPath() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();
  const [statusOverride, setStatusOverride] =
    useState<LearningPathJobStatusType | null>(null);
  const statusOverrideRef = useRef(statusOverride);
  statusOverrideRef.current = statusOverride;

  const { data: learningPathData, isLoading } = useGetLearningPath(spaceId, {
    query: {
      refetchIntervalInBackground: true,
      retry: (failureCount) => failureCount < 2,
      refetchInterval: (query) => {
        if (statusOverrideRef.current === LearningPathJobStatus.GENERATING) {
          return 2000;
        }
        const raw = query.state.data;
        if (
          raw?.status === 200 &&
          isLearningPathsResponseDto(raw.data) &&
          raw.data.paths[0]?.status === LearningPathJobStatus.GENERATING
        ) {
          return 2000;
        }
        return false;
      },
    },
  });

  const paths =
    learningPathData?.status === 200 &&
    isLearningPathsResponseDto(learningPathData.data)
      ? learningPathData.data.paths
      : [];
  const pathDto = paths[0];
  const isListEmpty = paths.length === 0;

  const learningPathStatus = useMemo((): LearningPathJobStatusType => {
    if (statusOverride === LearningPathJobStatus.GENERATING) {
      const serverStatus = pathDto?.status as
        | LearningPathJobStatusType
        | undefined;
      if (
        serverStatus === LearningPathJobStatus.GENERATING ||
        serverStatus === LearningPathJobStatus.DONE ||
        serverStatus === LearningPathJobStatus.FAILED
      ) {
        return serverStatus;
      }
      return LearningPathJobStatus.GENERATING;
    }

    if (statusOverride === LearningPathJobStatus.NOT_CREATED) {
      return LearningPathJobStatus.NOT_CREATED;
    }

    if (statusOverride !== null) {
      return statusOverride;
    }

    if (isListEmpty) {
      return LearningPathJobStatus.NOT_CREATED;
    }

    if (pathDto) {
      return pathDto.status as LearningPathJobStatusType;
    }

    return LearningPathJobStatus.NOT_CREATED;
  }, [statusOverride, isListEmpty, pathDto]);

  const { updateSettings } = useUpdateSettingsMutation(spaceId);
  const { deletePath, isDeleting } = useDeletePathMutation(
    spaceId,
    setStatusOverride,
  );
  const { regeneratePath, isGenerating } = useRegeneratePathMutation(
    spaceId,
    setStatusOverride,
  );

  const path = useMemo((): LearningPath | undefined => {
    if (isListEmpty || pathDto === undefined) {
      return undefined;
    }
    if (pathDto.status === LearningPathJobStatus.DONE) {
      return mapLearningPath(pathDto);
    }
    return undefined;
  }, [isListEmpty, pathDto]);

  const invalidateLearningPath = useCallback(async () => {
    if (spaceId === undefined) return;
    await queryClient.invalidateQueries({
      queryKey: getGetLearningPathQueryKey(spaceId),
    });
  }, [queryClient, spaceId]);

  return {
    path,
    isPathLoading:
      isLoading && learningPathStatus === LearningPathJobStatus.DONE,
    status: learningPathStatus,
    updateSettings,
    deletePath,
    regeneratePath,
    isDeleting,
    isGenerating,
    invalidateLearningPath,
  };
}

export default useLearningPath;
