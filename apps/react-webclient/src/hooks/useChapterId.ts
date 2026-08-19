import { useParams } from "@tanstack/react-router";

function useChapterId() {
  const params = useParams({
    strict: false,
  });

  const numericChapterId = params?.chapterId
    ? Number(params.chapterId)
    : undefined;
  const strChapterId = params?.chapterId ? params.chapterId : undefined;

  return { chapterId: numericChapterId, strChapterId };
}

export default useChapterId;
