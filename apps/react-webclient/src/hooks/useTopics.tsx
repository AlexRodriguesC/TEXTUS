import {useGetTopics} from "@/client/generated/topics/topics.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import type {ErrorResponseDTO, TopicsResponseDTO} from "@/client/model";

function useTopics() {
    const { spaceId } = useSpaceId();

    const { data: topicsData } = useGetTopics(spaceId ?? 0, {
        query: {
            enabled: spaceId !== undefined,
        },
    });

    let topics: TopicsResponseDTO["topics"] = [];

    if (topicsData?.data && isTopicsResponse(topicsData.data)) {
        topics = topicsData.data.topics;
    }

    return {
        topics,
    };
}

export default useTopics;

function isTopicsResponse(data: ErrorResponseDTO | TopicsResponseDTO): data is TopicsResponseDTO {
    return "topics" in data;
}
