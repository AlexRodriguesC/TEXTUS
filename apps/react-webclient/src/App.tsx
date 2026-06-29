import {createRouter, RouterProvider} from "@tanstack/react-router";
import {routeTree} from "@/routing/routeTree.tsx";
import {QueryClient, QueryClientContext} from "@tanstack/react-query";
import {useAuth} from "@/hooks/useAuth.tsx";

const router = createRouter({routeTree})

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5, // 5 mins
        }
    }
});

function App() {
    const { isAuthenticated } = useAuth();

    return (
        <QueryClientContext value={queryClient}>
            <RouterProvider router={router} context={{auth: isAuthenticated}}/>
        </QueryClientContext>
    )
}

export default App;