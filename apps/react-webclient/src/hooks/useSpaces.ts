import { useReadSpaces } from "@/client/generated/spaces/spaces.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import Space from "@/classes/Space.ts";
import { mapSpace } from "@/hooks/dtoMappers.ts";

function useSpaces() {
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

  return { spaces, isLoading };
}

export default useSpaces;
