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
import { useMemo } from "react";

type MindMapStatus = MindMapJobDTOStatus;

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
        const done = raw.status === "DONE";
        const failed = raw.status === "FAILED";
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
        jobData.data.status === "DONE",
    },
  });

  let mindMapStatus: MindMapStatus = "NOT_CREATED";
  if (jobData && isMindMapJobDTO(jobData.data)) {
    mindMapStatus = jobData.data.status;
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
    if (spaceId === undefined) return undefined;

    const maybeMindMap = mindMapData?.data;
    const base =
      maybeMindMap && MindMap.isMindMapDTO(maybeMindMap)
        ? MindMap.from(maybeMindMap)
        : new MindMap([]);

    return base.withRuntime(
      spaceId,
      deleteMutation,
      generateMutation,
      isDeleting,
      isGenerating,
    );
  }, [
    mindMapData,
    spaceId,
    deleteMutation,
    generateMutation,
    isDeleting,
    isGenerating,
  ]);

  return {
    mindMap,
    isMindMapLoading: isLoading,
    status: mindMapStatus,
  };
}

function isMindMapJobDTO(
  data: ErrorResponseDTO | MindMapJobDTO,
): data is MindMapJobDTO {
  return "status" in data;
}

export default useMindMap;
