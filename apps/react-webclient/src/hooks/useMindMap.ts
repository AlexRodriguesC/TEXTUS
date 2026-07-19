import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts";
import {
  useExtractMindMap,
  useGetMindMap,
  useGetMindMapJob,
} from "@/client/generated/mind-maps/mind-maps.ts";
import type {
  ErrorResponseDTO,
  MindMapJobDTO,
  MindMapJobDTOStatus,
  MindMapResponseDTO,
  NodeDTO,
} from "@/client/model";

type MindMapStatus = MindMapJobDTOStatus;

function useMindMap(spaceId: number | undefined) {
  const { t } = useTranslation();

  const { data: mindMapData } = useGetMindMap(spaceId ?? -1, {
    query: {
      enabled: spaceId != undefined,
    },
  });

  const maybeData = mindMapData?.data;
  let mindMap: { nodes: NodeDTO[] } = { nodes: [] };

  if (maybeData && isMindMapDTO(maybeData) && maybeData.nodes) {
    mindMap = { nodes: maybeData.nodes };
  }

  const extractMindMap = useExtractMindMap({
    mutation: {
      onSuccess: () => {
        successToast(t("mindmap.creation.success"));
      },
      onError: () => {
        failureToast(t("mindmap.creation.error"));
      },
    },
  });

  const { data } = useGetMindMapJob(spaceId ?? -1, {
    query: {
      refetchInterval: (query) => {
        const jobData = query.state.data?.data;
        if (!jobData || !isMindMapJobDTO(jobData)) return 2000;
        const done = jobData.status === "DONE";
        const hasNodes = mindMap.nodes.length > 0;
        return done || hasNodes ? false : 2000;
      },
      refetchIntervalInBackground: true,
      enabled: spaceId != undefined,
    },
  });

  let status: MindMapStatus = "NOT_CREATED";

  if (data && isMindMapJobDTO(data.data)) {
    status = data.data.status;
  }

  return {
    extractMindMap,
    status,
    mindMap,
  };
}

function isMindMapJobDTO(
  data: ErrorResponseDTO | MindMapJobDTO,
): data is MindMapJobDTO {
  return "status" in data;
}

function isMindMapDTO(
  data: ErrorResponseDTO | MindMapResponseDTO,
): data is MindMapResponseDTO {
  return "nodes" in data;
}

export default useMindMap;
