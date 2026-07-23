import {
  getGetLearningPathQueryKey,
  useDeleteLearningPath,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import type { ErrorResponseDTO } from "@/client/model";
import LearningPathJobStatus, {
  type LearningPathJobStatusType,
} from "@/classes/LearningPathJobStatus.ts";
import { setPathStatus } from "@/hooks/learning/learningPathUtils.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, type Dispatch, type SetStateAction } from "react";

type DeletePathMutationContext = {
  previousPath: getLearningPathResponse | undefined;
};

function useDeletePathMutation(
  spaceId: number,
  setStatusOverride: Dispatch<
    SetStateAction<LearningPathJobStatusType | null>
  >,
) {
  const queryClient = useQueryClient();

  const { mutate: deleteMutation, isPending: isDeleting } =
    useDeleteLearningPath<ErrorResponseDTO, DeletePathMutationContext>({
      mutation: {
        onMutate: async ({ spaceId: targetSpaceId }: { spaceId: number }) => {
          const pathKey = getGetLearningPathQueryKey(targetSpaceId);
          await queryClient.cancelQueries({ queryKey: pathKey });

          const previousPath =
            queryClient.getQueryData<getLearningPathResponse>(pathKey);

          setStatusOverride(LearningPathJobStatus.NOT_CREATED);
          setPathStatus(
            queryClient,
            targetSpaceId,
            LearningPathJobStatus.NOT_CREATED,
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
        },
        onSettled: async (_data, _error, { spaceId: targetSpaceId }) => {
          await queryClient.invalidateQueries({
            queryKey: getGetLearningPathQueryKey(targetSpaceId),
          });
        },
      },
    });

  const deletePath = useCallback(() => {
    deleteMutation({ spaceId });
  }, [spaceId, deleteMutation]);

  return { deletePath, isDeleting };
}

export default useDeletePathMutation;
