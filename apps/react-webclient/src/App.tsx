import { RouterProvider } from "@tanstack/react-router";
import { QueryClientProvider } from "@tanstack/react-query";
import { router } from "@/routing/router.ts";
import { queryClient } from "@/routing/queryClient.ts";
import SidebarOpenProvider from "@/provider/SidebarOpenProvider.tsx";
import TutorialContextProvider from "@/provider/TutorialContextProvider.tsx";
import RightSidebarOpenProvider from "@/provider/RightSidebarOpenProvider.tsx";
import AuthenticationProvider from "@/provider/AuthenticationProvider.tsx";

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthenticationProvider>
        <SidebarOpenProvider>
          <RightSidebarOpenProvider>
            <TutorialContextProvider>
              <RouterProvider router={router} />
            </TutorialContextProvider>
          </RightSidebarOpenProvider>
        </SidebarOpenProvider>
      </AuthenticationProvider>
    </QueryClientProvider>
  );
}

export default App;
