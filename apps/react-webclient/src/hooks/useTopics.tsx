import {
  getGetTopicsQueryOptions,
  useGetTopics,
} from "@/client/generated/topics/topics.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type { ErrorResponseDTO, TopicsResponseDTO } from "@/client/model";
import Topic from "@/classes/Topic.ts";
import { mapTopic } from "@/hooks/dtoMappers.ts";
import {useQueryClient} from "@tanstack/react-query";

function useTopics() {
  const { spaceId } = useSpaceId();
  const queryClient = useQueryClient();

  const { data: topicsData } = useGetTopics(spaceId ?? 0, {
    query: {
      enabled: spaceId !== undefined,
    },
  });

  const prefetchTopics = async (targetSpaceId?: number) => {
    const id = targetSpaceId ?? spaceId;
    if (id === undefined) return;
    await queryClient.prefetchQuery(getGetTopicsQueryOptions(id));
  };

  let topics: Topic[] = [];

  if (topicsData?.data && isTopicsResponse(topicsData.data)) {
    topics = topicsData.data.topics.map(mapTopic);
  }

  return {
    topics, prefetchTopics,
  };
}

export default useTopics;

function isTopicsResponse(
  data: ErrorResponseDTO | TopicsResponseDTO,
): data is TopicsResponseDTO {
  return "topics" in data;
}
