import {
  getReadSpacesQueryKey,
  useCreateSpace,
  type readSpacesResponse,
} from "@/client/generated/spaces/spaces.ts";
import type { CreateSpaceResponseDTO, ReadSpaceResponseDTO } from "@/client/model";
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

type CreateSpaceMutationContext = {
  previousSpaces: readSpacesResponse | undefined;
};

function useCreateSpaceMutation() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const mutation = useCreateSpace<CreateSpaceResponseDTO, CreateSpaceMutationContext>({
    mutation: {
      onMutate: async ({ data }) => {
        const queryKey = getReadSpacesQueryKey();
        await queryClient.cancelQueries({ queryKey });

        const previousSpaces =
          queryClient.getQueryData<readSpacesResponse>(queryKey);

        const optimisticSpace: ReadSpaceResponseDTO = {
          spaceId: -Date.now(),
          icon: data.icon,
          name: data.name,
          description: data.description,
        };

        queryClient.setQueryData<readSpacesResponse>(queryKey, (old) => {
          if (!old) {
            return {
              data: [optimisticSpace],
              status: 200,
              headers: new Headers(),
            };
          }
          return { ...old, data: [...old.data, optimisticSpace] };
        });

        return { previousSpaces };
      },
      onError: (_err, _variables, context) => {
        if (context?.previousSpaces !== undefined) {
          queryClient.setQueryData(
            getReadSpacesQueryKey(),
            context.previousSpaces,
          );
        }
      },
      onSettled: () => {
        void queryClient.invalidateQueries({
          queryKey: getReadSpacesQueryKey(),
        });
      },
    },
  });

  const createSpace = useCallback(
    (params: CreateSpaceParams, callbacks?: CreateSpaceCallbacks) => {
      mutation.mutate(
        { data: params },
        {
          onSuccess: (resp) => {
            successToast(t("spaces.add.success"));
            callbacks?.onSuccess?.(resp.data.id);
          },
          onError: callbacks?.onError,
        },
      );
    },
    [mutation, t],
  );

  return {
    createSpace,
    isPending: mutation.isPending,
  };
}

export default useCreateSpaceMutation;
