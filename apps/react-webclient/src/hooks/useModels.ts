import { useGetModels } from "@/client/generated/models/models.ts";
import { useMemo } from "react";

function useModels() {
  const { data, isLoading } = useGetModels();

  const models = useMemo(() => {
    if (!data || !("status" in data) || data.status !== 200) return [];
    return data.data.models.map((model) => ({
      value: model.id,
      label: model.name || model.id,
    }));
  }, [data]);

  return {
    models,
    isLoading,
    defaultModel: models[0]?.value,
  };
}

export default useModels;
