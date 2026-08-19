import { createRouter } from "@tanstack/react-router";
import { routeTree } from "@/routing/routeTree.tsx";
import { queryClient } from "@/routing/queryClient.ts";
import ErrorFallback from "@/components/ErrorFallback.tsx";

export const router = createRouter({
  routeTree,
  context: {
    queryClient,
  },
  defaultPreloadStaleTime: 0,
  defaultErrorComponent: ErrorFallback,
});

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}
