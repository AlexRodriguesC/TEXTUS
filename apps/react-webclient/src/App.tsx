import { createRouter, RouterProvider } from "@tanstack/react-router";
import { routeTree } from "@/routing/routeTree.tsx";
import { QueryClient, QueryClientContext } from "@tanstack/react-query";

const router = createRouter({ routeTree });

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 mins
    },
  },
});

function App() {
  return (
    <QueryClientContext value={queryClient}>
      <RouterProvider router={router} />
    </QueryClientContext>
  );
}

export default App;
