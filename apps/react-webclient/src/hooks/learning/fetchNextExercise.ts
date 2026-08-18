import {
  getGetNextExerciseQueryKey,
  getNextExercise,
  type getNextExerciseResponse,
} from "@/client/generated/lessons/lessons.ts";
import type { QueryClient } from "@tanstack/react-query";

const inflight = new Map<string, Promise<getNextExerciseResponse>>();

export function nextExerciseFlightKey(spaceId: number, lessonId: number) {
  return `${spaceId}:${lessonId}`;
}

export function fetchNextExercise(
  spaceId: number,
  lessonId: number,
  options?: RequestInit,
): Promise<getNextExerciseResponse> {
  const key = nextExerciseFlightKey(spaceId, lessonId);
  const existing = inflight.get(key);
  if (existing) {
    return existing;
  }

  const promise = getNextExercise(spaceId, lessonId, options).finally(() => {
    inflight.delete(key);
  });
  inflight.set(key, promise);
  return promise;
}

export function resetNextExerciseQuery(
  queryClient: QueryClient,
  spaceId: number,
  lessonId: number,
) {
  const queryKey = getGetNextExerciseQueryKey(spaceId, lessonId);
  if (queryClient.getQueryState(queryKey)?.fetchStatus === "fetching") {
    return;
  }
  return queryClient.resetQueries({ queryKey });
}

export async function clearNextExerciseQuery(
  queryClient: QueryClient,
  spaceId: number,
  lessonId: number,
) {
  const queryKey = getGetNextExerciseQueryKey(spaceId, lessonId);
  await queryClient.cancelQueries({ queryKey });
  queryClient.removeQueries({ queryKey });
  inflight.delete(nextExerciseFlightKey(spaceId, lessonId));
}

export function clearNextExerciseFlights() {
  inflight.clear();
}
