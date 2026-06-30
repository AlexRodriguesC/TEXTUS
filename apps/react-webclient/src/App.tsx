import { createRouter, RouterProvider } from "@tanstack/react-router";
import { routeTree } from "@/routing/routeTree.tsx";
import {
  MutationCache,
  QueryCache,
  QueryClient,
  QueryClientContext,
} from "@tanstack/react-query";
import { ApiError } from "@/api/mutator.ts";

const router = createRouter({ routeTree });

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
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

function App() {
  return (
    <QueryClientContext value={queryClient}>
      <RouterProvider router={router} />
    </QueryClientContext>
  );
}

export default App;
