
import {ThemeProvider} from "@/provider/ThemeProvider.tsx";
import {createRouter, RouterProvider} from "@tanstack/react-router";
import {routeTree} from "@/routing/routeTree.tsx";
import {TooltipProvider} from "@/components/ui/tooltip.tsx";
import {AuthContext} from "@/context/AuthContext.tsx";
import {useAuth} from "@/hooks/useAuth.tsx";
import {QueryClient, QueryClientContext} from "@tanstack/react-query";

const router = createRouter({routeTree})

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5, // 5 mins
        }
    }
});

function App() {
    const {user, setUser} = useAuth()

    return (
        <AuthContext.Provider value={{user, setUser }}>
            <QueryClientContext value={queryClient}>
                <TooltipProvider>
                    <ThemeProvider>
                        <RouterProvider router={router}/>
                    </ThemeProvider>
                </TooltipProvider>
            </QueryClientContext>
        </AuthContext.Provider>
    )
}

export default App;