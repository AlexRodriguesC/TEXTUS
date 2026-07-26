import {
  getGetLearningPathQueryKey,
  useUpdatePathSettings,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import type { ErrorResponseDTO } from "@/client/model";
import PathSettings from "@/classes/PathSettings.ts";
import { isLearningPathDto, toPathSettingsDto } from "@/hooks/dtoMappers.ts";
import { isLearningPathsResponseDto } from "@/hooks/learning/learningPathUtils.ts";
import { failureToast, successToast } from "@/lib/toasts.tsx";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback } from "react";
import { useTranslation } from "react-i18next";

type UpdateSettingsMutationContext = {
  previousPath: getLearningPathResponse | undefined;
};

function useUpdateSettingsMutation(spaceId: number | undefined) {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

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
          if (
            old?.status !== 200 ||
            !isLearningPathsResponseDto(old.data)
          ) {
            return old;
          }
          const existing = old.data.paths[0];
          if (existing === undefined || !isLearningPathDto(existing)) {
            return old;
          }
          return {
            ...old,
            data: {
              paths: [
                {
                  ...existing,
                  settings: {
                    ...existing.settings,
                    ...data,
                  },
                },
              ],
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

  return { updateSettings };
}

export default useUpdateSettingsMutation;
