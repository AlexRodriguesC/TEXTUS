import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080',
});

export const customInstance = <T>(config: {
    url: string;
    method: string;
    params?: unknown;
    data?: unknown;
    headers?: Record<string, string>;
}): Promise<T> => {
    return instance.request<T>(config).then((res) => res.data);
};