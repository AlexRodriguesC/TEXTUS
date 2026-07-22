import useSpaceId from "@/hooks/useSpaceId.ts";
import {
  getGetMindMapJobQueryKey,
  getGetMindMapQueryKey,
  useDeleteMindMap,
  useExtractMindMap,
  useGetMindMap,
  useGetMindMapJob,
} from "@/client/generated/mind-maps/mind-maps.ts";
import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import type {
  ErrorResponseDTO,
  MindMapJobDTO,
  MindMapJobDTOStatus,
} from "@/client/model";
import MindMap from "@/classes/MindMap.ts";
import MindMapJobStatus, {
  type MindMapJobStatusType,
} from "@/classes/MindMapJobStatus.ts";
import { isMindMapDto, mapMindMap } from "@/hooks/dtoMappers.ts";
import { useCallback, useMemo } from "react";

function mapJobStatus(status: MindMapJobDTOStatus): MindMapJobStatusType {
  return status as MindMapJobStatusType;
}

function useMindMap() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const { data: jobData } = useGetMindMapJob(spaceId ?? -1, {
    query: {
      enabled: spaceId !== undefined,
      refetchIntervalInBackground: true,
      refetchInterval: (query) => {
        const raw = query.state.data?.data;
        if (!raw || !isMindMapJobDTO(raw)) return 2000;
        const done = raw.status === MindMapJobStatus.DONE;
        const failed = raw.status === MindMapJobStatus.FAILED;
        return done || failed ? false : 2000;
      },
    },
  });

  const { data: mindMapData, isLoading } = useGetMindMap(spaceId ?? -1, {
    query: {
      enabled:
        spaceId !== undefined &&
        !!jobData &&
        isMindMapJobDTO(jobData.data) &&
        jobData.data.status === MindMapJobStatus.DONE,
    },
  });

  let mindMapStatus: MindMapJobStatusType = MindMapJobStatus.NOT_CREATED;
  if (jobData && isMindMapJobDTO(jobData.data)) {
    mindMapStatus = mapJobStatus(jobData.data.status);
  }

  const { mutate: deleteMutation, isPending: isDeleting } = useDeleteMindMap({
    mutation: {
      onSuccess: async () => {
        await queryClient.resetQueries({
          queryKey: getGetMindMapQueryKey(spaceId),
        });
        await queryClient.resetQueries({
          queryKey: getGetMindMapJobQueryKey(spaceId),
        });
      },
    },
  });

  const { mutate: generateMutation, isPending: isGenerating } =
    useExtractMindMap({
      mutation: {
        onSuccess: () => {
          successToast(t("mindmap.creation.success"));
        },
        onError: () => {
          failureToast(t("mindmap.creation.error"));
        },
      },
    });

  const mindMap = useMemo(() => {
    const maybeMindMap = mindMapData?.data;
    if (maybeMindMap && isMindMapDto(maybeMindMap)) {
      return mapMindMap(maybeMindMap);
    }
    return new MindMap([]);
  }, [mindMapData]);

  const deleteMindMap = useCallback(() => {
    if (spaceId === undefined) return;
    deleteMutation({ spaceId });
  }, [spaceId, deleteMutation]);

  const regenerateMindMap = useCallback(() => {
    if (spaceId === undefined) return;
    generateMutation({ spaceId });
  }, [spaceId, generateMutation]);

  return {
    mindMap,
    isMindMapLoading: isLoading,
    status: mindMapStatus,
    deleteMindMap,
    regenerateMindMap,
    isDeleting,
    isGenerating,
  };
}

function isMindMapJobDTO(
  data: ErrorResponseDTO | MindMapJobDTO,
): data is MindMapJobDTO {
  return "status" in data;
}

export default useMindMap;
