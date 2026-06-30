import { useReadSpaces } from "@/api/generated/spaces/spaces.ts";

export interface ISpace {
  spaceId: number;
  icon: string;
  name: string;
  description: string;
}

function useSpaces() {
  const { data } = useReadSpaces();
  let spaces: ISpace[] = [];

  if (data && data.data) {
    spaces = data.data;
  }

  return { spaces };
}

export default useSpaces;
