import { createRouter, RouterProvider } from "@tanstack/react-router";
import { routeTree } from "@/routing/routeTree.tsx";
import {
  MutationCache,
  QueryCache,
  QueryClient,
  QueryClientContext,
} from "@tanstack/react-query";
import { ApiError } from "@/client/mutator.ts";
import SidebarOpenProvider from "@/provider/SidebarOpenProvider.tsx";
import TutorialContextProvider from "@/provider/TutorialContextProvider.tsx";
import RightSidebarOpenProvider from "@/provider/RightSidebarOpenProvider.tsx";
import ViewModeProvider from "@/provider/ViewModeProvider.tsx";

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
      <SidebarOpenProvider>
        <RightSidebarOpenProvider>
          <TutorialContextProvider>
            <ViewModeProvider>
              <RouterProvider router={router} />
            </ViewModeProvider>
          </TutorialContextProvider>
        </RightSidebarOpenProvider>
      </SidebarOpenProvider>
    </QueryClientContext>
  );
}

export default App;
