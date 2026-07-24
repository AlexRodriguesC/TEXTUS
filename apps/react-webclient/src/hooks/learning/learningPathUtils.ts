import {
  getGetLearningPathQueryKey,
  type getLearningPathResponse,
} from "@/client/generated/paths/paths.ts";
import type { LearningPathResponseDTOStatus } from "@/client/model";
import { ApiError } from "@/client/mutator.ts";
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

export function isLearningPathNotFound(error: unknown): boolean {
  return error instanceof ApiError && error.status === 404;
}

export function setPathStatus(
  queryClient: QueryClient,
  spaceId: number,
  status: LearningPathResponseDTOStatus,
) {
  const queryKey = getGetLearningPathQueryKey(spaceId);
  queryClient.setQueryData<getLearningPathResponse>(queryKey, (old) => {
    if (old?.status === 200 && isLearningPathDto(old.data)) {
      return {
        ...old,
        data: {
          ...old.data,
          status,
          chapters:
            status === LearningPathJobStatus.GENERATING ||
            status === LearningPathJobStatus.NOT_CREATED
              ? []
              : old.data.chapters,
        },
      };
    }
    return old;
  });
}
