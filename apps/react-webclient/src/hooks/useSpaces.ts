import {getReadSpacesQueryKey, useReadSpaces} from "@/client/generated/spaces/spaces.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import Space from "@/classes/Space.ts";
import { mapSpace } from "@/hooks/dtoMappers.ts";
import {useQueryClient} from "@tanstack/react-query";

function useSpaces() {
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { data, isLoading } = useReadSpaces({
    query: {
      enabled: isAuthenticated,
    },
  });
  let spaces: Space[] = [];

  if (data && data.data) {
    spaces = data.data.map(mapSpace);
  }

  const refetchSpaces = async (onSuccess?: () => void) => {
    await queryClient.invalidateQueries({
      queryKey: getReadSpacesQueryKey()
    });
    onSuccess?.();
  }

  return { spaces, refetchSpaces, isLoading };
}

export default useSpaces;
