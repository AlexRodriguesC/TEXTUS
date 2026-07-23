import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  getGetLearningPathQueryKey,
  useDeleteLearningPath,
  useExtractLearningPath,
  useGetLearningPath,
  useUpdatePathSettings,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import type {
  ErrorResponseDTO,
  LearningPathResponseDTOStatus,
} from "@/client/model";
import { ApiError } from "@/client/mutator.ts";
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
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

type UpdateSettingsMutationContext = {
  previousPath: getLearningPathResponse | undefined;
};

type DeletePathMutationContext = {
  previousPath: getLearningPathResponse | undefined;
};

type GeneratePathMutationContext = {
  previousPath: getLearningPathResponse | undefined;
};

function setPathStatus(
  queryClient: ReturnType<typeof useQueryClient>,
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
          chapters: status === LearningPathJobStatus.GENERATING ? [] : old.data.chapters,
        },
      };
    }
    return old;
  });
}

function useLearningPath() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [statusOverride, setStatusOverride] =
    useState<LearningPathJobStatusType | null>(null);
  const statusOverrideRef = useRef(statusOverride);
  statusOverrideRef.current = statusOverride;

  const {
    data: learningPathData,
    error: learningPathError,
    isLoading,
  } = useGetLearningPath(spaceId ?? -1, {
    query: {
      enabled: spaceId !== undefined,
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
      if (
        learningPathError instanceof ApiError &&
        learningPathError.status === 404
      ) {
        setStatusOverride(null);
      }
    }
  }, [statusOverride, learningPathData, learningPathError]);

  let learningPathStatus: LearningPathJobStatusType =
    LearningPathJobStatus.NOT_CREATED;
  if (statusOverride !== null) {
    learningPathStatus = statusOverride;
  } else if (
    learningPathData?.status === 200 &&
    isLearningPathDto(learningPathData.data)
  ) {
    learningPathStatus = learningPathData.data
      .status as LearningPathJobStatusType;
  }

  const { mutate: updateSettingsMutation } = useUpdatePathSettings<
    ErrorResponseDTO,
    UpdateSettingsMutationContext
  >({
    mutation: {
      onMutate: async ({ spaceId: targetSpaceId, data }) => {
        const queryKey = getGetLearningPathQueryKey(targetSpaceId);
        await queryClient.cancelQueries({ queryKey });

        const previousPath =
          queryClient.getQueryData<getLearningPathResponse>(queryKey);

        queryClient.setQueryData<getLearningPathResponse>(queryKey, (old) => {
          if (!old || old.status !== 200 || !isLearningPathDto(old.data)) {
            return old;
          }
          return {
            ...old,
            data: {
              ...old.data,
              settings: {
                ...old.data.settings,
                ...data,
              },
            },
          };
        });

        return { previousPath };
      },
      onError: (_err, { spaceId: targetSpaceId }, context) => {
        if (context?.previousPath !== undefined) {
          queryClient.setQueryData(
            getGetLearningPathQueryKey(targetSpaceId),
            context.previousPath,
          );
        }
        failureToast(t("learning-path.settings.error"));
      },
      onSettled: (_data, _error, { spaceId: targetSpaceId }) => {
        void queryClient.invalidateQueries({
          queryKey: getGetLearningPathQueryKey(targetSpaceId),
        });
      },
      onSuccess: () => {
        successToast(t("learning-path.settings.success"));
      },
    },
  });

  const { mutate: deleteMutation, isPending: isDeleting } =
    useDeleteLearningPath<ErrorResponseDTO, DeletePathMutationContext>({
      mutation: {
        onMutate: async ({ spaceId: targetSpaceId }: { spaceId: number }) => {
          const pathKey = getGetLearningPathQueryKey(targetSpaceId);
          await queryClient.cancelQueries({ queryKey: pathKey });

          const previousPath =
            queryClient.getQueryData<getLearningPathResponse>(pathKey);

          setStatusOverride(LearningPathJobStatus.NOT_CREATED);
          queryClient.removeQueries({ queryKey: pathKey });

          return { previousPath };
        },
        onError: (_err, { spaceId: targetSpaceId }, context) => {
          setStatusOverride(null);
          if (context?.previousPath !== undefined) {
            queryClient.setQueryData(
              getGetLearningPathQueryKey(targetSpaceId),
              context.previousPath,
            );
          }
        },
        onSettled: async (_data, _error, { spaceId: targetSpaceId }) => {
          await queryClient.resetQueries({
            queryKey: getGetLearningPathQueryKey(targetSpaceId),
          });
        },
      },
    });

  const { mutate: generateMutation, isPending: isGenerating } =
    useExtractLearningPath<ErrorResponseDTO, GeneratePathMutationContext>({
      mutation: {
        onMutate: async ({ spaceId: targetSpaceId }) => {
          const pathKey = getGetLearningPathQueryKey(targetSpaceId);
          await queryClient.cancelQueries({ queryKey: pathKey });

          const previousPath =
            queryClient.getQueryData<getLearningPathResponse>(pathKey);

          setStatusOverride(LearningPathJobStatus.GENERATING);
          setPathStatus(
            queryClient,
            targetSpaceId,
            LearningPathJobStatus.GENERATING,
          );

          return { previousPath };
        },
        onError: (_err, { spaceId: targetSpaceId }, context) => {
          setStatusOverride(null);
          if (context?.previousPath !== undefined) {
            queryClient.setQueryData(
              getGetLearningPathQueryKey(targetSpaceId),
              context.previousPath,
            );
          }
          failureToast(t("learning-path.creation.error"));
        },
        onSettled: (_data, _error, { spaceId: targetSpaceId }) => {
          void queryClient.invalidateQueries({
            queryKey: getGetLearningPathQueryKey(targetSpaceId),
          });
        },
        onSuccess: () => {
          successToast(t("learning-path.creation.success"));
        },
      },
    });

  const path = useMemo(() => {
    if (
      learningPathData?.status === 200 &&
      isLearningPathDto(learningPathData.data) &&
      learningPathData.data.status === LearningPathJobStatus.DONE
    ) {
      return mapLearningPath(learningPathData.data);
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
