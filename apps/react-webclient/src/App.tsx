import {ThemeProvider} from "@/provider/ThemeProvider.tsx";
import {createRouter, RouterProvider} from "@tanstack/react-router";
import {routeTree} from "@/routing/routeTree.tsx";
import {TooltipProvider} from "@/components/ui/tooltip.tsx";
import {AuthContext, type User} from "@/context/AuthContext.tsx";
import {QueryClient, QueryClientContext} from "@tanstack/react-query";
import * as React from "react";
import {useCallback, useMemo} from "react";
import {Toaster} from "@/components/ui/sonner.tsx";

const router = createRouter({routeTree})

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5, // 5 mins
        }
    }
});

function App() {
    const [user, setUser] = React.useState<User | null>(() => {
        const storedUser = localStorage.getItem("user");
        return storedUser ? JSON.parse(storedUser) : null;
    });

    const login = useCallback((usr: User) => {
        setUser(usr)
        localStorage.setItem('user', JSON.stringify(usr))
    }, [])

    const logout = useCallback(() => {
        setUser(null)
        localStorage.removeItem('user')
    }, [])

    const contextValue = useMemo(() => ({
        user,
        login,
        logout
    }), [user, login, logout]);



    return (
        <AuthContext.Provider value={contextValue}>
            <QueryClientContext value={queryClient}>
                <TooltipProvider>
                    <ThemeProvider>
                        <Toaster/>
                        <RouterProvider router={router}/>
                    </ThemeProvider>
                </TooltipProvider>
            </QueryClientContext>
        </AuthContext.Provider>
    )
}

export default App;