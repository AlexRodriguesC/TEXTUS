import type { QueryClient } from "@tanstack/react-query";
import { getListChatsQueryOptions } from "@/client/generated/chats/chats.ts";
import { getGetDocumentsQueryOptions } from "@/client/generated/documents/documents.ts";
import {
  getGetLessonQueryOptions,
  getGetNextExerciseQueryOptions,
} from "@/client/generated/lessons/lessons.ts";
import {
  getGetMindMapJobQueryOptions,
  getGetMindMapQueryOptions,
} from "@/client/generated/mind-maps/mind-maps.ts";
import { getGetModelsQueryOptions } from "@/client/generated/models/models.ts";
import { getGetPagesQueryOptions } from "@/client/generated/pages/pages.ts";
import { getGetLearningPathQueryOptions } from "@/client/generated/paths/paths.ts";
import { getReadSpacesQueryOptions } from "@/client/generated/spaces/spaces.ts";
import { getGetTopicsQueryOptions } from "@/client/generated/topics/topics.ts";
import type { MindMapJobDTO } from "@/client/model";
import { ApiError } from "@/client/mutator.ts";
import MindMapJobStatus from "@/classes/MindMapJobStatus.ts";
import { mapLesson } from "@/hooks/dtoMappers.ts";
import { isLearningPathNotFound } from "@/hooks/learning/learningPathUtils.ts";

function isMindMapJobDto(data: unknown): data is MindMapJobDTO {
  return typeof data === "object" && data !== null && "status" in data;
}

const learningPathRetry = (failureCount: number, error: unknown) => {
  if (error instanceof ApiError && error.status === 404) return false;
  return failureCount < 2;
};

export async function ensureLearningPath(
  queryClient: QueryClient,
  spaceId: number,
) {
  try {
    await queryClient.ensureQueryData(
      getGetLearningPathQueryOptions(spaceId, {
        query: { retry: learningPathRetry },
      }),
    );
  } catch (error) {
    if (!isLearningPathNotFound(error)) {
      throw error;
    }
  }
}

async function ensureMindMapData(queryClient: QueryClient, spaceId: number) {
  const jobResponse = await queryClient.ensureQueryData(
    getGetMindMapJobQueryOptions(spaceId),
  );
  const job = jobResponse?.data;
  if (isMindMapJobDto(job) && job.status === MindMapJobStatus.DONE) {
    await queryClient.ensureQueryData(getGetMindMapQueryOptions(spaceId));
  }
}

export async function ensureSpaces(queryClient: QueryClient) {
  await queryClient.ensureQueryData(getReadSpacesQueryOptions());
}

export async function ensureSpaceMenuData(
  queryClient: QueryClient,
  spaceId: number,
) {
  await Promise.all([
    queryClient.ensureQueryData(getReadSpacesQueryOptions()),
    queryClient.ensureQueryData(getGetDocumentsQueryOptions(spaceId)),
    queryClient.ensureQueryData(getGetMindMapJobQueryOptions(spaceId)),
    ensureLearningPath(queryClient, spaceId),
  ]);
}

export async function ensureSpaceChatData(
  queryClient: QueryClient,
  spaceId: number,
) {
  await Promise.all([
    queryClient.ensureQueryData(getListChatsQueryOptions(spaceId)),
    queryClient.ensureQueryData(getGetModelsQueryOptions()),
  ]);
}

export async function ensureSpaceMapData(
  queryClient: QueryClient,
  spaceId: number,
) {
  await Promise.all([
    ensureMindMapData(queryClient, spaceId),
    queryClient.ensureQueryData(getGetTopicsQueryOptions(spaceId)),
    queryClient.ensureQueryData(getGetPagesQueryOptions(spaceId)),
  ]);
}

export async function ensureSpaceLearnData(
  queryClient: QueryClient,
  spaceId: number,
) {
  await ensureLearningPath(queryClient, spaceId);
}

export async function ensureChapterGuideData(
  queryClient: QueryClient,
  spaceId: number,
) {
  await Promise.all([
    ensureLearningPath(queryClient, spaceId),
    queryClient.ensureQueryData(getGetTopicsQueryOptions(spaceId)),
  ]);
}

export async function ensureLessonData(
  queryClient: QueryClient,
  spaceId: number,
  lessonId: number,
) {
  const lessonResponse = await queryClient.ensureQueryData(
    getGetLessonQueryOptions(spaceId, lessonId),
  );

  if (
    lessonResponse &&
    "status" in lessonResponse &&
    lessonResponse.status === 200
  ) {
    const lesson = mapLesson(lessonResponse.data);
    if (lesson.shouldFetchNextExercise()) {
      await queryClient.ensureQueryData(
        getGetNextExerciseQueryOptions(spaceId, lessonId),
      );
    }
  }
}
