import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080',
});

instance.interceptors.request.use((config) => {
    const stored = localStorage.getItem('user');
    if (stored) {
        const { token } = JSON.parse(stored);
        if (token) config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const customInstance = <T>(
    url: string,
    options?: RequestInit
): Promise<T> => {
    const { method = 'GET', headers, body } = options ?? {};

    return instance.request<T>({
        url,
        method,
        headers: headers as Record<string, string>,
        data: body,
    }).then((res) => res.data);
};