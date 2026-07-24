import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  useGetMindMap,
  useListMindMaps,
} from "@/client/generated/mind-maps/mind-maps.ts";
import type { MindMapsResponseDTO } from "@/client/model";
import MindMap from "@/classes/MindMap.ts";
import MindMapJobStatus, {
  type MindMapJobStatusType,
} from "@/classes/MindMapJobStatus.ts";
import { isMindMapDto, mapMindMap } from "@/hooks/dtoMappers.ts";
import useDeleteMindMapMutation from "@/hooks/mindmap/useDeleteMindMapMutation.ts";
import { isMindMapListEmpty } from "@/hooks/mindmap/mindMapUtils.ts";
import useRegenerateMindMapMutation from "@/hooks/mindmap/useRegenerateMindMapMutation.ts";
import { useEffect, useMemo, useRef, useState } from "react";

function isMindMapsResponseDto(data: unknown): data is MindMapsResponseDTO {
  return (
    typeof data === "object" &&
    data !== null &&
    "mindMaps" in data &&
    Array.isArray((data as MindMapsResponseDTO).mindMaps)
  );
}

function useMindMap() {
  const { spaceId } = useSpaceId();
  const [statusOverride, setStatusOverride] =
    useState<MindMapJobStatusType | null>(null);
  const statusOverrideRef = useRef(statusOverride);
  statusOverrideRef.current = statusOverride;

  const { data: listData } = useListMindMaps(spaceId, {
    query: {
      enabled: true,
      refetchIntervalInBackground: true,
      refetchInterval: (query) => {
        if (statusOverrideRef.current === MindMapJobStatus.GENERATING) {
          return 2000;
        }
        const raw = query.state.data;
        if (
          raw?.status === 200 &&
          isMindMapsResponseDto(raw.data) &&
          raw.data.mindMaps[0]?.status === MindMapJobStatus.GENERATING
        ) {
          return 2000;
        }
        return false;
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
    spaceId,
    mindMapId ?? -1,
    {
      query: {
        enabled: mindMapId !== undefined,
        refetchIntervalInBackground: true,
        refetchInterval: (query) => {
          if (statusOverrideRef.current === MindMapJobStatus.GENERATING) {
            return 2000;
          }
          const raw = query.state.data;
          if (
            raw?.status === 200 &&
            isMindMapDto(raw.data) &&
            raw.data.status === MindMapJobStatus.GENERATING
          ) {
            return 2000;
          }
          return false;
        },
      },
    },
  );

  useEffect(() => {
    if (statusOverride === MindMapJobStatus.GENERATING) {
      const listStatus = summary?.status as MindMapJobStatusType | undefined;
      const getStatus =
        mindMapData?.status === 200 && isMindMapDto(mindMapData.data)
          ? (mindMapData.data.status as MindMapJobStatusType)
          : undefined;
      const status = getStatus ?? listStatus;
      if (
        status === MindMapJobStatus.GENERATING ||
        status === MindMapJobStatus.DONE ||
        status === MindMapJobStatus.FAILED
      ) {
        setStatusOverride(null);
      }
    }

    if (statusOverride === MindMapJobStatus.NOT_CREATED) {
      if (isMindMapListEmpty(listData)) {
        setStatusOverride(null);
      }
    }
  }, [statusOverride, listData, mindMapData, summary?.status]);

  const isListEmpty = isMindMapListEmpty(listData);

  let mindMapStatus: MindMapJobStatusType = MindMapJobStatus.NOT_CREATED;
  if (statusOverride !== null) {
    mindMapStatus = statusOverride;
  } else if (isListEmpty) {
    mindMapStatus = MindMapJobStatus.NOT_CREATED;
  } else if (
    mindMapData?.status === 200 &&
    isMindMapDto(mindMapData.data)
  ) {
    mindMapStatus = mindMapData.data.status as MindMapJobStatusType;
  } else if (summary) {
    mindMapStatus = summary.status as MindMapJobStatusType;
  }

  const { deleteMindMap, isDeleting } = useDeleteMindMapMutation(
    spaceId,
    mindMapId,
    setStatusOverride,
  );
  const { regenerateMindMap, isGenerating } = useRegenerateMindMapMutation(
    spaceId,
    mindMapId,
    setStatusOverride,
  );

  const mindMap = useMemo(() => {
    if (isListEmpty) {
      return new MindMap([]);
    }
    if (
      mindMapData?.status === 200 &&
      isMindMapDto(mindMapData.data) &&
      mindMapData.data.status === MindMapJobStatus.DONE
    ) {
      return mapMindMap(mindMapData.data);
    }
    return new MindMap([]);
  }, [isListEmpty, mindMapData]);

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
