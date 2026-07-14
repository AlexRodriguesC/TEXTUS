import { useParams } from "@tanstack/react-router";

function useSpaceId() {
  const params = useParams({
    strict: false,
  });

  const numericSpaceId = params?.spaceId ? Number(params.spaceId) : undefined;
  const strSpaceId = params?.spaceId ? params.spaceId : undefined;

  return { spaceId: numericSpaceId, strSpaceId };
}

export default useSpaceId;
