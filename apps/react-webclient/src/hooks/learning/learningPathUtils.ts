import {
  getGetLearningPathQueryKey,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import type { LearningPathResponseDTOStatus } from "@/client/model";
import type LearningPath from "@/classes/LearningPath.ts";
import type Topic from "@/classes/Topic.ts";
import LearningPathJobStatus from "@/classes/LearningPathJobStatus.ts";
import { isLearningPathDto } from "@/hooks/dtoMappers.ts";
import type { QueryClient } from "@tanstack/react-query";

function isTopicInPath(path: LearningPath, topicId: number): boolean {
  return path.chapters.some((chapter) =>
    chapter.lessons.some((lesson) => lesson.topics.includes(topicId)),
  );
}

export function isLearningPathsResponseDto(
  data: unknown,
): data is { paths: { status: string }[] } {
  return (
    typeof data === "object" &&
    data !== null &&
    "paths" in data &&
    Array.isArray((data as { paths: unknown }).paths)
  );
}

export function isTopicInMasteredLesson(
  path: LearningPath,
  topicId: number,
): boolean {
  if (path.chapters.length === 0) return false;
  return path.chapters.some((chapter) =>
    chapter.lessons.some(
      (lesson) => lesson.topics.includes(topicId) && lesson.isMastered(),
    ),
  );
}

export function isPageFullyMastered(
  path: LearningPath,
  pageId: number,
  topics: Topic[],
): boolean {
  if (path.chapters.length === 0) return false;

  const pageTopicsInPath = topics.filter(
    (topic) => topic.page === pageId && isTopicInPath(path, topic.id),
  );

  if (pageTopicsInPath.length === 0) return false;

  return pageTopicsInPath.every((topic) =>
    isTopicInMasteredLesson(path, topic.id),
  );
}

export function isLearningPathListEmpty(
  listResponse: getLearningPathResponse | undefined,
): boolean {
  return (
    listResponse?.status === 200 &&
    isLearningPathsResponseDto(listResponse.data) &&
    listResponse.data.paths.length === 0
  );
}

export function setPathStatus(
  queryClient: QueryClient,
  spaceId: number,
  status: LearningPathResponseDTOStatus,
) {
  const queryKey = getGetLearningPathQueryKey(spaceId);
  queryClient.setQueryData<getLearningPathResponse>(queryKey, (old) => {
    if (old?.status === 200 && isLearningPathsResponseDto(old.data)) {
      if (status === LearningPathJobStatus.NOT_CREATED) {
        return {
          ...old,
          data: { paths: [] },
        };
      }
      const existing = old.data.paths[0];
      if (existing !== undefined && isLearningPathDto(existing)) {
        return {
          ...old,
          data: {
            paths: [
              {
                ...existing,
                status,
                chapters:
                  status === LearningPathJobStatus.GENERATING ||
                  status === LearningPathJobStatus.NOT_CREATED
                    ? []
                    : existing.chapters,
              },
            ],
          },
        };
      }
    }
    return old;
  });
}
