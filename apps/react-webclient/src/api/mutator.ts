import axios, {type AxiosRequestConfig, type Method} from 'axios';
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

export class ApiError<T = unknown> extends Error {
    status: number
    data: T

    constructor(status: number, data: T, message?: string) {
        super(message ?? `HTTP ${status}`)
        this.status = status
        this.data = data
    }
}

export async function customInstance<T>(
    url: string,
    options?: RequestInit
): Promise<T> {
    try {
        const response = await instance.request<T>({
            url,
            method: (options?.method as Method | undefined) ?? "GET",
            data: options?.body,
            headers: options?.headers as AxiosRequestConfig["headers"],
        })

        return {
            data: response.data,
            status: response.status,
            headers: response.headers,
        } as T;
    } catch (error) {
        if (axios.isAxiosError(error)) {
            throw new ApiError(
                error.response?.status ?? 500,
                error.response?.data,
                error.message
            )
        }

        throw error
    }
}