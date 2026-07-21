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
import { useMemo } from "react";

type LearningPathStatus = LearningPathJobDTOStatus;

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
        const done = raw.status === "DONE";
        const failed = raw.status === "FAILED";
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
        jobData.data.status === "DONE",
    },
  });

  let learningPathStatus: LearningPathStatus = "NOT_CREATED";
  if (jobData && isLearningPathJobDTO(jobData.data)) {
    learningPathStatus = jobData.data.status;
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
    if (spaceId === undefined) return undefined;

    const maybePath = learningPathData?.data;
    const base =
      maybePath && LearningPath.isLearningPathDTO(maybePath)
        ? LearningPath.from(maybePath)
        : new LearningPath([], {} as never);

    return base.withRuntime(
      spaceId,
      updateSettingsMutation,
      deleteMutation,
      generateMutation,
      isDeleting,
      isGenerating,
    );
  }, [
    learningPathData,
    spaceId,
    updateSettingsMutation,
    deleteMutation,
    generateMutation,
    isDeleting,
    isGenerating,
  ]);

  return {
    path,
    isPathLoading: isLoading,
    status: learningPathStatus,
  };
}

export default useLearningPath;

function isLearningPathJobDTO(
  data: ErrorResponseDTO | LearningPathJobDTO,
): data is LearningPathJobDTO {
  return "status" in data;
}
