import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  getGetLearningPathJobQueryKey,
  getGetLearningPathQueryKey,
  useDeleteLearningPath,
  useExtractLearningPath,
  useGetLearningPath,
  useGetLearningPathJob,
  useUpdatePathSettings,
} from "@/client/generated/paths/paths.ts";
import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import type {
  ErrorResponseDTO,
  LearningPathJobDTO,
  LearningPathJobDTOStatus,
} from "@/client/model";
import LearningPath from "@/classes/LearningPath.ts";
import PathSettings from "@/classes/PathSettings.ts";
import LearningPathJobStatus, {
  type LearningPathJobStatusType,
} from "@/classes/LearningPathJobStatus.ts";
import {
  isLearningPathDto,
  mapLearningPath,
  toPathSettingsDto,
} from "@/hooks/dtoMappers.ts";
import { useCallback, useMemo } from "react";

function mapJobStatus(status: LearningPathJobDTOStatus): LearningPathJobStatusType {
  return status as LearningPathJobStatusType;
}

function useLearningPath() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const { data: jobData } = useGetLearningPathJob(spaceId ?? -1, {
    query: {
      enabled: spaceId !== undefined,
      refetchIntervalInBackground: true,
      refetchInterval: (query) => {
        const raw = query.state.data?.data;
        if (!raw || !isLearningPathJobDTO(raw)) return 2000;
        const done = raw.status === LearningPathJobStatus.DONE;
        const failed = raw.status === LearningPathJobStatus.FAILED;
        return done || failed ? false : 2000;
      },
    },
  });

  const { data: learningPathData, isLoading } = useGetLearningPath(spaceId, {
    query: {
      enabled:
        spaceId !== undefined &&
        !!jobData &&
        isLearningPathJobDTO(jobData.data) &&
        jobData.data.status === LearningPathJobStatus.DONE,
    },
  });

  let learningPathStatus: LearningPathJobStatusType =
    LearningPathJobStatus.NOT_CREATED;
  if (jobData && isLearningPathJobDTO(jobData.data)) {
    learningPathStatus = mapJobStatus(jobData.data.status);
  }

  const { mutate: updateSettingsMutation } = useUpdatePathSettings({
    mutation: {
      onSuccess: () => {
        successToast(t("learning-path.settings.success"));
        queryClient.invalidateQueries({
          queryKey: getGetLearningPathQueryKey(spaceId),
        });
      },
      onError: () => {
        failureToast(t("learning-path.settings.error"));
      },
    },
  });

  const { mutate: deleteMutation, isPending: isDeleting } =
    useDeleteLearningPath({
      mutation: {
        onSuccess: async () => {
          await queryClient.resetQueries({
            queryKey: getGetLearningPathQueryKey(spaceId),
          });
          await queryClient.resetQueries({
            queryKey: getGetLearningPathJobQueryKey(spaceId),
          });
        },
      },
    });

  const { mutate: generateMutation, isPending: isGenerating } =
    useExtractLearningPath({
      mutation: {
        onSuccess: () => {
          successToast(t("learning-path.creation.success"));
        },
        onError: () => {
          failureToast(t("learning-path.creation.error"));
        },
      },
    });

  const path = useMemo(() => {
    const maybePath = learningPathData?.data;
    if (maybePath && isLearningPathDto(maybePath)) {
      return mapLearningPath(maybePath);
    }
    return new LearningPath([], new PathSettings(0, 0, 0, 0));
  }, [learningPathData]);

  const updateSettings = useCallback(
    (settings: PathSettings) => {
      if (spaceId === undefined) return;
      updateSettingsMutation({
        spaceId,
        data: toPathSettingsDto(settings),
      });
    },
    [spaceId, updateSettingsMutation],
  );

  const deletePath = useCallback(() => {
    if (spaceId === undefined) return;
    deleteMutation({ spaceId });
  }, [spaceId, deleteMutation]);

  const regeneratePath = useCallback(() => {
    if (spaceId === undefined) return;
    generateMutation({ spaceId });
  }, [spaceId, generateMutation]);

  const invalidateLearningPath = useCallback(async () => {
    if (spaceId === undefined) return;
    await queryClient.invalidateQueries({
      queryKey: getGetLearningPathQueryKey(spaceId),
    });
  }, [queryClient, spaceId]);

  return {
    path,
    isPathLoading: isLoading,
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

function isLearningPathJobDTO(
  data: ErrorResponseDTO | LearningPathJobDTO,
): data is LearningPathJobDTO {
  return "status" in data;
}
