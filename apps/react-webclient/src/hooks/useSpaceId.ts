import { useParams } from "@tanstack/react-router";

function useSpaceId() {
  const { spaceId } = useParams({
    from: "/main/spaces/$spaceId",
    select: (val) => {
      const num = Number(val.spaceId);
      if (isNaN(num)) throw new Error("Invalid spaceId");
      return { spaceId: num };
    },
  });

  return { spaceId };
}

export default useSpaceId;
