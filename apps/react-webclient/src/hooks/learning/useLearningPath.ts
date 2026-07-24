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
  isLearningPathListEmpty,
  isLearningPathsResponseDto,
} from "@/hooks/learning/learningPathUtils.ts";
import useRegeneratePathMutation from "@/hooks/learning/useRegeneratePathMutation.ts";
import useUpdateSettingsMutation from "@/hooks/learning/useUpdateSettingsMutation.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
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

  useEffect(() => {
    if (statusOverride === LearningPathJobStatus.GENERATING) {
      const status = pathDto?.status as LearningPathJobStatusType | undefined;
      if (
        status === LearningPathJobStatus.GENERATING ||
        status === LearningPathJobStatus.DONE ||
        status === LearningPathJobStatus.FAILED
      ) {
        setStatusOverride(null);
      }
    }

    if (statusOverride === LearningPathJobStatus.NOT_CREATED) {
      if (isLearningPathListEmpty(learningPathData)) {
        setStatusOverride(null);
      }
    }
  }, [statusOverride, learningPathData, pathDto?.status]);

  let learningPathStatus: LearningPathJobStatusType =
    LearningPathJobStatus.NOT_CREATED;
  if (statusOverride !== null) {
    learningPathStatus = statusOverride;
  } else if (isListEmpty) {
    learningPathStatus = LearningPathJobStatus.NOT_CREATED;
  } else if (pathDto) {
    learningPathStatus = pathDto.status as LearningPathJobStatusType;
  }

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
