import {
  getReadSpacesQueryKey,
  useDeleteSpace,
} from "@/client/generated/spaces/spaces.ts";
import { successToast } from "@/lib/toasts.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useCallback } from "react";

type DeleteSpaceCallbacks = {
  onError?: () => void;
};

function useDeleteSpaceMutation() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const mutation = useDeleteSpace();

  const deleteSpace = useCallback(
    (spaceId: number, callbacks?: DeleteSpaceCallbacks) => {
      mutation.mutate(
        { spaceId },
        {
          onSuccess: async () => {
            await queryClient.invalidateQueries({
              queryKey: getReadSpacesQueryKey(),
            });
            successToast(t("spaces.delete.success"));
          },
          onError: callbacks?.onError,
        },
      );
    },
    [mutation, queryClient, t],
  );

  return {
    deleteSpace,
    isPending: mutation.isPending,
  };
}

export default useDeleteSpaceMutation;
