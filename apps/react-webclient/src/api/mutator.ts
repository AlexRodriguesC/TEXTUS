import axios from 'axios';
import {getStoredUser, logout2} from "@/App.tsx";

const instance = axios.create({
    baseURL: 'http://localhost:8080',
});

function isTokenExpired(token: string) {
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        return payload.exp * 1000 <= Date.now();
    } catch {
        return true;
    }
}

instance.interceptors.request.use((config) => {
    const user = getStoredUser();
    const token = user?.token;

    if (token) {
        if (isTokenExpired(token)) {
            logout2();
            return Promise.reject(new Error("Token expired"));
        }
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            logout2();
        }
        return Promise.reject(error);
    }
);

export const customInstance = <T>(
    url: string,
    options?: RequestInit
): Promise<T> => {
    const {method = 'GET', headers, body} = options ?? {};

    return instance.request<T>({
        url,
        method,
        headers: headers as Record<string, string>,
        data: body,
    }).then((res) => {
        return {
            data: res.data,
            status: res.status,
            headers: res.headers,
        } as T;
    });
};