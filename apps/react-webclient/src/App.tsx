import {createRouter, RouterProvider} from "@tanstack/react-router";
import {routeTree} from "@/routing/routeTree.tsx";
import {AuthContext, type User} from "@/context/AuthContext.tsx";
import {QueryClient, QueryClientContext} from "@tanstack/react-query";
import * as React from "react";
import {useCallback, useEffect, useMemo, useState} from "react";

const router = createRouter({routeTree})

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5, // 5 mins
        }
    }
});

export function getStoredUser() {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
}

export function clearStoredUser() {
    localStorage.removeItem("user");
}

export function logout2() {
    clearStoredUser();
}

function App() {
    const [user, setUser] = React.useState<User | null>(getStoredUser());
    const [isAuthenticated, setIsAuthenticated] = useState(user != null);

    const login = useCallback((usr: User) => {
        setUser(usr)
        setIsAuthenticated(true);
        localStorage.setItem('user', JSON.stringify(usr))
    }, [])

    const logout = useCallback(() => {
        setUser(null)
        setIsAuthenticated(false);
        clearStoredUser()
    }, [])

    useEffect(() => {
        if (localStorage.getItem('user') == null) logout();
    }, [localStorage.getItem('user')]);

    const contextValue = useMemo(() => ({
        user,
        isAuthenticated,
        login,
        logout
    }), [user, isAuthenticated, login, logout]);

    return (
        <AuthContext.Provider value={contextValue}>
            <QueryClientContext value={queryClient}>
                <RouterProvider router={router} context={{auth: contextValue}}/>
            </QueryClientContext>
        </AuthContext.Provider>
    )
}

export default App;