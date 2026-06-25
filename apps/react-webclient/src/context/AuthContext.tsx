import { createContext } from "react";

export type User = {
    username: string,
    token?: string
}

interface AuthContext {
    user: User | null,
    login: (user: User) => void,
    logout: () => void
}

export const AuthContext = createContext<AuthContext>({
    user: null,
    login: () => null,
    logout: () => null
});
