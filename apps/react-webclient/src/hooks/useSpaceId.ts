import { useParams } from "@tanstack/react-router";

function useSpaceId() {
  const params = useParams({
    strict: false,
  });

  const numericSpaceId = params?.spaceId ? Number(params.spaceId) : undefined;

  return { spaceId: numericSpaceId };
}

export default useSpaceId;
