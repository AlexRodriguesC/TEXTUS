import {
  getReadSpacesQueryKey,
  useUpdateSpace,
} from "@/client/generated/spaces/spaces.ts";
import { successToast } from "@/lib/toasts.tsx";
import { useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useCallback } from "react";

type UpdateSpaceParams = {
  spaceId: number;
  icon: string;
  name: string;
  description: string;
};

type UpdateSpaceCallbacks = {
  onError?: () => void;
};

function useUpdateSpaceMutation() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const mutation = useUpdateSpace();

  const updateSpace = useCallback(
    (params: UpdateSpaceParams, callbacks?: UpdateSpaceCallbacks) => {
      mutation.mutate(
        {
          spaceId: params.spaceId,
          data: {
            icon: params.icon,
            name: params.name,
            description: params.description,
          },
        },
        {
          onSuccess: async () => {
            await queryClient.invalidateQueries({
              queryKey: getReadSpacesQueryKey(),
            });
            successToast(t("spaces.update.success"));
          },
          onError: callbacks?.onError,
        },
      );
    },
    [mutation, queryClient, t],
  );

  return {
    updateSpace,
    isPending: mutation.isPending,
  };
}

export default useUpdateSpaceMutation;
