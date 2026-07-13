import { useReadSpaces } from "@/client/generated/spaces/spaces.ts";
import {useAuth} from "@/hooks/useAuth.ts";

export interface ISpace {
  spaceId: number;
  icon: string;
  name: string;
  description: string;
}

function useSpaces() {
  const { isAuthenticated } = useAuth();
  const { data, isLoading } = useReadSpaces({
    query: {
      enabled: isAuthenticated
    }
  });
  let spaces: ISpace[] = [];

  if (data && data.data) {
    spaces = data.data;
  }

  return { spaces, isLoading };
}

export default useSpaces;
