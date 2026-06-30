import {
  useExtractMindMap,
  useGetMindMap,
  useGetMindMapStatus,
} from "@/api/generated/spaces/spaces.ts";
import { useTranslation } from "react-i18next";
import { failureToast, successToast } from "@/lib/toasts";

function useMindMap(spaceId: number) {
  const { t } = useTranslation();

  const { data: mindMapData } = useGetMindMap(spaceId);

  const mindMap = mindMapData?.data ?? undefined;

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

  const { data } = useGetMindMapStatus(spaceId, {
    query: {
      refetchInterval: (query) => {
        const status = query.state.data?.data.status ?? "NOT_CREATED";
        const done = status == "DONE";
        const canFetch =
          (mindMap != undefined && mindMap.nodes && mindMap.nodes.length > 0) ??
          false;
        return done || !canFetch ? false : 2000;
      },
      refetchIntervalInBackground: true,
    },
  });

  const status = data?.data.status ?? "NOT_CREATED";

  return {
    extractMindMap,
    status,
    mindMap,
  };
}

export default useMindMap;
