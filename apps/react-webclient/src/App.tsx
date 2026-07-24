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
import AuthenticationProvider from "@/provider/AuthenticationProvider.tsx";
import ErrorFallback from "@/components/ErrorFallback.tsx";

const router = createRouter({ routeTree, defaultErrorComponent: ErrorFallback });

const queryClient = new QueryClient({
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
  })
});

function App() {
  return (
      <QueryClientContext value={queryClient}>
        <AuthenticationProvider>
          <SidebarOpenProvider>
            <RightSidebarOpenProvider>
              <TutorialContextProvider>
                <RouterProvider router={router} />
              </TutorialContextProvider>
            </RightSidebarOpenProvider>
          </SidebarOpenProvider>
        </AuthenticationProvider>
      </QueryClientContext>
  );
}

export default App;
