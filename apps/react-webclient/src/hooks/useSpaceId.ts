import { useParams } from "@tanstack/react-router";

function useSpaceId() {
  const params = useParams({
    strict: false,
  });

  const numericSpaceId = params?.spaceId ? Number(params.spaceId) : undefined;
  const strSpaceId = params?.spaceId ? params.spaceId : undefined;

  if (!numericSpaceId && strSpaceId) throw new Error(
    "useSpaceId must be used within a valid path containing a spaceId"
  )

  return { spaceId: numericSpaceId!, strSpaceId: strSpaceId! };
}

export default useSpaceId;
