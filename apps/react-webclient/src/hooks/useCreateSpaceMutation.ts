import {
  type createSpaceResponse,
  getReadSpacesQueryKey,
  useCreateSpace,
} from "@/client/generated/spaces/spaces.ts";
import { successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useCallback } from "react";

type CreateSpaceParams = {
  icon: string;
  name: string;
  description: string;
};

type CreateSpaceCallbacks = {
  onSuccess?: (spaceId: number) => void;
  onError?: () => void;
};

function useCreateSpaceMutation() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const mutation = useCreateSpace();

  const createSpace = useCallback(
    (params: CreateSpaceParams, callbacks?: CreateSpaceCallbacks) => {
      mutation.mutate(
        { data: params },
        {
          onSuccess: async (resp: createSpaceResponse) => {
            await queryClient.invalidateQueries({
              queryKey: getReadSpacesQueryKey(),
            });
            successToast(t("spaces.add.success"));
            callbacks?.onSuccess?.(resp.data.id);
          },
          onError: callbacks?.onError,
        },
      );
    },
    [mutation, queryClient, t],
  );

  return {
    createSpace,
    isPending: mutation.isPending,
  };
}

export default useCreateSpaceMutation;
