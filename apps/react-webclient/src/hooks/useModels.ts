import { listModels } from "@/client/chatApi.ts";
import { useQuery } from "@tanstack/react-query";

export const getModelsQueryKey = () => ["models"] as const;

function useModels() {
  const { data, isLoading } = useQuery({
    queryKey: getModelsQueryKey(),
    queryFn: listModels,
  });

  const models =
    data?.models.map((model) => ({
      value: model.id,
      label: model.name || model.id,
    })) ?? [];

  return {
    models,
    isLoading,
    defaultModel: models[0]?.value,
  };
}

export default useModels;
