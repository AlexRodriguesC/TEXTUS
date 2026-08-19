import {
  getGetMindMapQueryKey,
  getListMindMapsQueryKey,
  type getMindMapResponse,
  type listMindMapsResponse,
} from "@/client/generated/mind-maps/mind-maps.ts";
import type { MindMapResponseDTOStatus } from "@/client/model";
import MindMapJobStatus from "@/classes/MindMapJobStatus.ts";
import { isMindMapDto } from "@/hooks/dtoMappers.ts";
import type { QueryClient } from "@tanstack/react-query";

function isMindMapsResponseDto(
  data: unknown,
): data is { mindMaps: { id: number; status: string }[] } {
  return (
    typeof data === "object" &&
    data !== null &&
    "mindMaps" in data &&
    Array.isArray((data as { mindMaps: unknown }).mindMaps)
  );
}

export function isMindMapListEmpty(
  listResponse: listMindMapsResponse | undefined,
): boolean {
  return (
    listResponse?.status === 200 &&
    isMindMapsResponseDto(listResponse.data) &&
    listResponse.data.mindMaps.length === 0
  );
}

export function setMindMapStatus(
  queryClient: QueryClient,
  spaceId: number,
  mindMapId: number | undefined,
  status: MindMapResponseDTOStatus,
) {
  const listKey = getListMindMapsQueryKey(spaceId);
  queryClient.setQueryData<listMindMapsResponse>(listKey, (old) => {
    if (old?.status === 200 && isMindMapsResponseDto(old.data)) {
      if (status === MindMapJobStatus.NOT_CREATED) {
        return {
          ...old,
          data: { mindMaps: [] },
        };
      }
      const existing = old.data.mindMaps[0];
      if (existing !== undefined) {
        return {
          ...old,
          data: {
            mindMaps: [{ ...existing, status }],
          },
        };
      }
    }
    return old;
  });

  if (mindMapId === undefined) {
    return;
  }

  const getKey = getGetMindMapQueryKey(spaceId, mindMapId);
  queryClient.setQueryData<getMindMapResponse>(getKey, (old) => {
    if (old?.status === 200 && isMindMapDto(old.data)) {
      return {
        ...old,
        data: {
          ...old.data,
          status,
          nodes:
            status === MindMapJobStatus.GENERATING ||
            status === MindMapJobStatus.NOT_CREATED
              ? []
              : old.data.nodes,
        },
      };
    }
    return old;
  });
}
