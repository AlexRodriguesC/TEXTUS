import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  getGetLearningPathQueryKey,
  useGetLearningPath,
} from "@/client/generated/paths/paths.ts";
import { ApiError } from "@/client/mutator.ts";
import LearningPath from "@/classes/LearningPath.ts";
import PathSettings from "@/classes/PathSettings.ts";
import LearningPathJobStatus, {
  type LearningPathJobStatusType,
} from "@/classes/LearningPathJobStatus.ts";
import { isLearningPathDto, mapLearningPath } from "@/hooks/dtoMappers.ts";
import useDeletePathMutation from "@/hooks/learning/useDeletePathMutation.ts";
import { isLearningPathNotFound } from "@/hooks/learning/learningPathUtils.ts";
import useRegeneratePathMutation from "@/hooks/learning/useRegeneratePathMutation.ts";
import useUpdateSettingsMutation from "@/hooks/learning/useUpdateSettingsMutation.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

function useLearningPath() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();
  const [statusOverride, setStatusOverride] =
    useState<LearningPathJobStatusType | null>(null);
  const statusOverrideRef = useRef(statusOverride);
  statusOverrideRef.current = statusOverride;

  const {
    data: learningPathData,
    error: learningPathError,
    isLoading,
  } = useGetLearningPath(spaceId, {
    query: {
      refetchIntervalInBackground: true,
      retry: (_failureCount, error) => {
        if (error instanceof ApiError && error.status === 404) return false;
        return _failureCount < 2;
      },
      refetchInterval: (query) => {
        if (statusOverrideRef.current === LearningPathJobStatus.GENERATING) {
          return 2000;
        }
        const raw = query.state.data;
        if (
          raw?.status === 200 &&
          isLearningPathDto(raw.data) &&
          raw.data.status === LearningPathJobStatus.GENERATING
        ) {
          return 2000;
        }
        return false;
      },
    },
  });

  useEffect(() => {
    if (statusOverride === LearningPathJobStatus.GENERATING) {
      if (
        learningPathData?.status === 200 &&
        isLearningPathDto(learningPathData.data)
      ) {
        const status = learningPathData.data.status;
        if (
          status === LearningPathJobStatus.GENERATING ||
          status === LearningPathJobStatus.DONE ||
          status === LearningPathJobStatus.FAILED
        ) {
          setStatusOverride(null);
        }
      }
    }

    if (statusOverride === LearningPathJobStatus.NOT_CREATED) {
      if (isLearningPathNotFound(learningPathError)) {
        setStatusOverride(null);
      }
    }
  }, [statusOverride, learningPathData, learningPathError]);

  const isNotFound = isLearningPathNotFound(learningPathError);
  const emptyPath = useMemo(
    () => new LearningPath([], new PathSettings(0, 0, 0, 0)),
    [],
  );

  let learningPathStatus: LearningPathJobStatusType =
    LearningPathJobStatus.NOT_CREATED;
  if (statusOverride !== null) {
    learningPathStatus = statusOverride;
  } else if (isNotFound) {
    learningPathStatus = LearningPathJobStatus.NOT_CREATED;
  } else if (
    learningPathData?.status === 200 &&
    isLearningPathDto(learningPathData.data)
  ) {
    learningPathStatus = learningPathData.data
      .status as LearningPathJobStatusType;
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

  const path = useMemo(() => {
    if (isNotFound) {
      return emptyPath;
    }
    if (
      learningPathData?.status === 200 &&
      isLearningPathDto(learningPathData.data) &&
      learningPathData.data.status === LearningPathJobStatus.DONE
    ) {
      return mapLearningPath(learningPathData.data);
    }
    return emptyPath;
  }, [isNotFound, learningPathData, emptyPath]);

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
