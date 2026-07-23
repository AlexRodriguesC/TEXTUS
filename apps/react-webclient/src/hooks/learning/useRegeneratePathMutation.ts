import {
  getGetLearningPathQueryKey,
  useExtractLearningPath,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import type { ErrorResponseDTO } from "@/client/model";
import LearningPathJobStatus, {
  type LearningPathJobStatusType,
} from "@/classes/LearningPathJobStatus.ts";
import { setPathStatus } from "@/hooks/learning/learningPathUtils.ts";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, type Dispatch, type SetStateAction } from "react";
import { useTranslation } from "react-i18next";
import useTopics from "@/hooks/useTopics.tsx";

type GeneratePathMutationContext = {
  previousPath: getLearningPathResponse | undefined;
};

function useRegeneratePathMutation(
  spaceId: number,
  setStatusOverride: Dispatch<
    SetStateAction<LearningPathJobStatusType | null>
  >,
) {
  const queryClient = useQueryClient();
  const { prefetchTopics } = useTopics();
  const { t } = useTranslation();

  const { mutate: generateMutation, isPending: isGenerating } =
    useExtractLearningPath<ErrorResponseDTO, GeneratePathMutationContext>({
      mutation: {
        onMutate: async ({ spaceId: targetSpaceId }) => {
          const pathKey = getGetLearningPathQueryKey(targetSpaceId);
          await queryClient.cancelQueries({ queryKey: pathKey });

          const previousPath =
            queryClient.getQueryData<getLearningPathResponse>(pathKey);

          setStatusOverride(LearningPathJobStatus.GENERATING);

          // Topics exist already in the backend, can prefetch them so they're already there when the user navigates to the path
          await prefetchTopics();

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

  const regeneratePath = useCallback(() => {
    generateMutation({ spaceId });
  }, [spaceId, generateMutation]);

  return { regeneratePath, isGenerating };
}

export default useRegeneratePathMutation;
