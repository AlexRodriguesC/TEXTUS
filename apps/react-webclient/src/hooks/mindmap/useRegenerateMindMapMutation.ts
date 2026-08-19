import {
  getGetMindMapQueryKey,
  getListMindMapsQueryKey,
  useExtractMindMap,
  type getMindMapResponse,
  type listMindMapsResponse,
} from "@/client/generated/mind-maps/mind-maps.ts";
import type { ErrorResponseDTO } from "@/client/model";
import MindMapJobStatus, {
  type MindMapJobStatusType,
} from "@/classes/MindMapJobStatus.ts";
import { setMindMapStatus } from "@/hooks/mindmap/mindMapUtils.ts";
import { failureToast, successToast } from "@/lib/toasts.tsx";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, type Dispatch, type SetStateAction } from "react";
import { useTranslation } from "react-i18next";

type RegenerateMindMapMutationContext = {
  previousList: listMindMapsResponse | undefined;
  previousMindMap: getMindMapResponse | undefined;
};

function useRegenerateMindMapMutation(
  spaceId: number,
  mindMapId: number | undefined,
  setStatusOverride: Dispatch<SetStateAction<MindMapJobStatusType | null>>,
) {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const { mutate: generateMutation, isPending: isGenerating } =
    useExtractMindMap<ErrorResponseDTO, RegenerateMindMapMutationContext>({
      mutation: {
        onMutate: async ({ spaceId: targetSpaceId }) => {
          const listKey = getListMindMapsQueryKey(targetSpaceId);
          const getKey =
            mindMapId !== undefined
              ? getGetMindMapQueryKey(targetSpaceId, mindMapId)
              : undefined;

          await queryClient.cancelQueries({ queryKey: listKey });
          if (getKey !== undefined) {
            await queryClient.cancelQueries({ queryKey: getKey });
          }

          const previousList =
            queryClient.getQueryData<listMindMapsResponse>(listKey);
          const previousMindMap =
            getKey !== undefined
              ? queryClient.getQueryData<getMindMapResponse>(getKey)
              : undefined;

          setStatusOverride(MindMapJobStatus.GENERATING);
          setMindMapStatus(
            queryClient,
            targetSpaceId,
            mindMapId,
            MindMapJobStatus.GENERATING,
          );

          return { previousList, previousMindMap };
        },
        onError: (_err, { spaceId: targetSpaceId }, context) => {
          setStatusOverride(null);
          if (context?.previousList !== undefined) {
            queryClient.setQueryData(
              getListMindMapsQueryKey(targetSpaceId),
              context.previousList,
            );
          }
          if (
            mindMapId !== undefined &&
            context?.previousMindMap !== undefined
          ) {
            queryClient.setQueryData(
              getGetMindMapQueryKey(targetSpaceId, mindMapId),
              context.previousMindMap,
            );
          }
          failureToast(t("mindmap.creation.error"));
        },
        onSettled: (_data, _error, { spaceId: targetSpaceId }) => {
          void queryClient.invalidateQueries({
            queryKey: getListMindMapsQueryKey(targetSpaceId),
          });
          if (mindMapId !== undefined) {
            void queryClient.invalidateQueries({
              queryKey: getGetMindMapQueryKey(targetSpaceId, mindMapId),
            });
          }
        },
        onSuccess: () => {
          successToast(t("mindmap.creation.success"));
        },
      },
    });

  const regenerateMindMap = useCallback(() => {
    generateMutation({ spaceId });
  }, [spaceId, generateMutation]);

  return { regenerateMindMap, isGenerating };
}

export default useRegenerateMindMapMutation;
