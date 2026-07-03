import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts";
import {
  useExtractMindMap,
  useGetMindMap,
  useGetMindMapJob,
} from "@/api/generated/mind-maps/mind-maps.ts";
import type {ErrorResponseDTO, MindMapJobDTO, MindMapResponseDTO, NodeDTO} from "@/api/model";

function useMindMap(spaceId: number) {
  const { t } = useTranslation();

  const { data: mindMapData } = useGetMindMap(spaceId);

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

  const { data } = useGetMindMapJob(spaceId, {
    query: {
      refetchInterval: (query) => {
        const data = query.state.data?.data;
        if (!data || !isMindMapJobDTO(data)) {
          return 2000;
        }
        const status = data.status;
        const done = status == "DONE";
        const canFetch =
          (mindMap != undefined && mindMap.nodes && mindMap.nodes.length > 0) ??
          false;
        return done || !canFetch ? false : 2000;
      },
      refetchIntervalInBackground: true,
    },
  });

  let status = "NOT_CREATED";

  if (data && isMindMapJobDTO(data.data)) {
    status = data.data.status;
  }

  return {
    extractMindMap,
    status,
    mindMap,
  };
}

function isMindMapJobDTO(data: ErrorResponseDTO | MindMapJobDTO): data is MindMapJobDTO {
  return "status" in data;
}

function isMindMapDTO(data: ErrorResponseDTO | MindMapResponseDTO): data is MindMapResponseDTO {
  return "nodes" in data;
}

export default useMindMap;
