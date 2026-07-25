import type { QueryClient } from "@tanstack/react-query";
import { getListChatsQueryOptions } from "@/client/generated/chats/chats.ts";
import { getGetDocumentsQueryOptions } from "@/client/generated/documents/documents.ts";
import {
  getGetLessonQueryOptions,
  getGetNextExerciseQueryOptions,
} from "@/client/generated/lessons/lessons.ts";
import {
  getGetMindMapQueryOptions,
  getListMindMapsQueryOptions,
} from "@/client/generated/mind-maps/mind-maps.ts";
import { getGetModelsQueryOptions } from "@/client/generated/models/models.ts";
import { getGetPagesQueryOptions } from "@/client/generated/pages/pages.ts";
import { getGetLearningPathQueryOptions } from "@/client/generated/paths/paths.ts";
import { getReadSpacesQueryOptions } from "@/client/generated/spaces/spaces.ts";
import { getGetTopicsQueryOptions } from "@/client/generated/topics/topics.ts";
import type { MindMapsResponseDTO } from "@/client/model";
import { ApiError } from "@/client/mutator.ts";
import { mapLesson } from "@/hooks/dtoMappers.ts";

function isMindMapsResponseDto(data: unknown): data is MindMapsResponseDTO {
  return (
    typeof data === "object" &&
    data !== null &&
    "mindMaps" in data &&
    Array.isArray((data as MindMapsResponseDTO).mindMaps)
  );
}

export async function ensureLearningPath(
  queryClient: QueryClient,
  spaceId: number,
) {
  await queryClient.ensureQueryData(getGetLearningPathQueryOptions(spaceId));
}

async function ensureMindMapData(queryClient: QueryClient, spaceId: number) {
  const listResponse = await queryClient.ensureQueryData(
    getListMindMapsQueryOptions(spaceId),
  );
  const list = listResponse?.data;
  if (!isMindMapsResponseDto(list) || list.mindMaps.length === 0) {
    return;
  }
  const mindMapId = list.mindMaps[0].id;
  try {
    await queryClient.ensureQueryData(
      getGetMindMapQueryOptions(spaceId, mindMapId, {
        query: {
          retry: (failureCount, error) => {
            if (error instanceof ApiError && error.status === 404) return false;
            return failureCount < 2;
          },
        },
      }),
    );
  } catch (error) {
    if (!(error instanceof ApiError && error.status === 404)) {
      throw error;
    }
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
    queryClient.ensureQueryData(getListMindMapsQueryOptions(spaceId)),
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

export async function ensureSpaceTopicsData(
  queryClient: QueryClient,
  spaceId: number,
) {
  await queryClient.ensureQueryData(getGetTopicsQueryOptions(spaceId));
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
