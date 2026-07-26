import {
  getGetMindMapQueryKey,
  getListMindMapsQueryKey,
  useDeleteMindMap,
  type getMindMapResponse,
  type listMindMapsResponse,
} from "@/client/generated/mind-maps/mind-maps.ts";
import type { ErrorResponseDTO } from "@/client/model";
import MindMapJobStatus, {
  type MindMapJobStatusType,
} from "@/classes/MindMapJobStatus.ts";
import { setMindMapStatus } from "@/hooks/mindmap/mindMapUtils.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useCallback, type Dispatch, type SetStateAction } from "react";

type DeleteMindMapMutationContext = {
  previousList: listMindMapsResponse | undefined;
  previousMindMap: getMindMapResponse | undefined;
};

function useDeleteMindMapMutation(
  spaceId: number,
  mindMapId: number | undefined,
  setStatusOverride: Dispatch<SetStateAction<MindMapJobStatusType | null>>,
) {
  const queryClient = useQueryClient();

  const { mutate: deleteMutation, isPending: isDeleting } =
    useDeleteMindMap<ErrorResponseDTO, DeleteMindMapMutationContext>({
      mutation: {
        onMutate: async ({
          spaceId: targetSpaceId,
          mindMapId: targetMindMapId,
        }) => {
          const listKey = getListMindMapsQueryKey(targetSpaceId);
          const getKey = getGetMindMapQueryKey(targetSpaceId, targetMindMapId);

          await queryClient.cancelQueries({ queryKey: listKey });
          await queryClient.cancelQueries({ queryKey: getKey });

          const previousList =
            queryClient.getQueryData<listMindMapsResponse>(listKey);
          const previousMindMap =
            queryClient.getQueryData<getMindMapResponse>(getKey);

          setStatusOverride(MindMapJobStatus.NOT_CREATED);
          setMindMapStatus(
            queryClient,
            targetSpaceId,
            targetMindMapId,
            MindMapJobStatus.NOT_CREATED,
          );

          return { previousList, previousMindMap };
        },
        onError: (
          _err,
          { spaceId: targetSpaceId, mindMapId: targetMindMapId },
          context,
        ) => {
          setStatusOverride(null);
          if (context?.previousList !== undefined) {
            queryClient.setQueryData(
              getListMindMapsQueryKey(targetSpaceId),
              context.previousList,
            );
          }
          if (context?.previousMindMap !== undefined) {
            queryClient.setQueryData(
              getGetMindMapQueryKey(targetSpaceId, targetMindMapId),
              context.previousMindMap,
            );
          }
        },
        onSettled: async (
          _data,
          _error,
          { spaceId: targetSpaceId, mindMapId: targetMindMapId },
        ) => {
          setStatusOverride(null);
          await queryClient.invalidateQueries({
            queryKey: getListMindMapsQueryKey(targetSpaceId),
          });
          await queryClient.invalidateQueries({
            queryKey: getGetMindMapQueryKey(targetSpaceId, targetMindMapId),
          });
        },
      },
    });

  const deleteMindMap = useCallback(() => {
    if (mindMapId === undefined) return;
    deleteMutation({ spaceId, mindMapId });
  }, [spaceId, mindMapId, deleteMutation]);

  return { deleteMindMap, isDeleting };
}

export default useDeleteMindMapMutation;
