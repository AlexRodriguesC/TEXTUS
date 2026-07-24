import {
  MutationCache,
  QueryCache,
  QueryClient,
} from "@tanstack/react-query";
import { ApiError } from "@/client/mutator.ts";

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      refetchOnWindowFocus: false,
    },
  },
  queryCache: new QueryCache({
    onError: (error) => {
      if (error instanceof ApiError && error.status === 401) {
        window.dispatchEvent(new CustomEvent("unauthorized"));
      }
    },
  }),
  mutationCache: new MutationCache({
    onError: (error) => {
      if (error instanceof ApiError && error.status === 401) {
        window.dispatchEvent(new CustomEvent("unauthorized"));
      }
    },
  }),
});
