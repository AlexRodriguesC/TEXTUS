import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  getGetMindMapQueryKey,
  getListMindMapsQueryKey,
  useDeleteMindMap,
  useExtractMindMap,
  useGetMindMap,
  useListMindMaps,
} from "@/client/generated/mind-maps/mind-maps.ts";
import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import type {
  MindMapResponseDTO,
  MindMapSummaryDTO,
  MindMapsResponseDTO,
} from "@/client/model";
import MindMap from "@/classes/MindMap.ts";
import MindMapJobStatus, {
  type MindMapJobStatusType,
} from "@/classes/MindMapJobStatus.ts";
import { isMindMapDto, mapMindMap } from "@/hooks/dtoMappers.ts";
import { useCallback, useMemo, useRef } from "react";

function isMindMapsResponseDto(data: unknown): data is MindMapsResponseDTO {
  return (
    typeof data === "object" &&
    data !== null &&
    "mindMaps" in data &&
    Array.isArray((data as MindMapsResponseDTO).mindMaps)
  );
}

function isMindMapSummaryDto(data: unknown): data is MindMapSummaryDTO {
  return (
    typeof data === "object" &&
    data !== null &&
    "id" in data &&
    "status" in data
  );
}

function useMindMap() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const statusRef = useRef<MindMapJobStatusType>(MindMapJobStatus.NOT_CREATED);

  const { data: listData } = useListMindMaps(spaceId ?? -1, {
    query: {
      enabled: spaceId !== undefined,
      refetchIntervalInBackground: true,
      refetchInterval: () => {
        return statusRef.current === MindMapJobStatus.GENERATING ? 2000 : false;
      },
    },
  });

  const listMindMaps =
    listData?.status === 200 && isMindMapsResponseDto(listData.data)
      ? listData.data.mindMaps
      : [];
  const summary = listMindMaps[0];
  const mindMapId = summary?.id;

  const { data: mindMapData, isLoading } = useGetMindMap(
    spaceId ?? -1,
    mindMapId ?? -1,
    {
      query: {
        enabled: spaceId !== undefined && mindMapId !== undefined,
        refetchIntervalInBackground: true,
        refetchInterval: () => {
          return statusRef.current === MindMapJobStatus.GENERATING ? 2000 : false;
        },
      },
    },
  );

  let mindMapStatus: MindMapJobStatusType = MindMapJobStatus.NOT_CREATED;
  if (
    mindMapData?.status === 200 &&
    isMindMapDto(mindMapData.data)
  ) {
    mindMapStatus = mindMapData.data.status as MindMapJobStatusType;
  } else if (summary) {
    mindMapStatus = summary.status as MindMapJobStatusType;
  }
  statusRef.current = mindMapStatus;

  const { mutate: deleteMutation, isPending: isDeleting } = useDeleteMindMap({
    mutation: {
      onSuccess: async () => {
        if (spaceId === undefined) return;
        await queryClient.resetQueries({
          queryKey: getListMindMapsQueryKey(spaceId),
        });
        if (mindMapId !== undefined) {
          await queryClient.resetQueries({
            queryKey: getGetMindMapQueryKey(spaceId, mindMapId),
          });
        }
      },
    },
  });

  const { mutate: generateMutation, isPending: isGenerating } =
    useExtractMindMap({
      mutation: {
        onSuccess: (response) => {
          successToast(t("mindmap.creation.success"));
          if (spaceId === undefined || response.status !== 202) return;
          const created = response.data;
          if (!isMindMapSummaryDto(created)) return;

          queryClient.setQueryData(getListMindMapsQueryKey(spaceId), {
            data: { mindMaps: [created] },
            status: 200,
          });
          queryClient.setQueryData(
            getGetMindMapQueryKey(spaceId, created.id),
            {
              data: {
                id: created.id,
                status: created.status,
                nodes: [],
              } satisfies MindMapResponseDTO,
              status: 200,
            },
          );
        },
        onError: () => {
          failureToast(t("mindmap.creation.error"));
        },
      },
    });

  const mindMap = useMemo(() => {
    if (
      mindMapData?.status === 200 &&
      isMindMapDto(mindMapData.data) &&
      mindMapData.data.status === MindMapJobStatus.DONE
    ) {
      return mapMindMap(mindMapData.data);
    }
    return new MindMap([]);
  }, [mindMapData]);

  const deleteMindMap = useCallback(() => {
    if (spaceId === undefined || mindMapId === undefined) return;
    deleteMutation({ spaceId, mindMapId });
  }, [spaceId, mindMapId, deleteMutation]);

  const regenerateMindMap = useCallback(() => {
    if (spaceId === undefined) return;
    generateMutation({ spaceId });
  }, [spaceId, generateMutation]);

  return {
    mindMap,
    mindMapId,
    isMindMapLoading:
      isLoading && mindMapStatus === MindMapJobStatus.DONE,
    status: mindMapStatus,
    deleteMindMap,
    regenerateMindMap,
    isDeleting,
    isGenerating,
  };
}

export default useMindMap;
