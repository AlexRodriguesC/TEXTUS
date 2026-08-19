import axios, { type AxiosRequestConfig, type Method } from "axios";

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem("TOKEN");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export class ApiError<T = unknown> extends Error {
  status: number;
  data: T;

  constructor(status: number, data: T, message?: string) {
    super(message ?? `HTTP ${status}`);
    this.status = status;
    this.data = data;
  }
}

export async function customInstance<T>(
  url: string,
  options?: RequestInit,
): Promise<T> {
  try {
    const response = await instance.request<T>({
      url,
      method: (options?.method as Method | undefined) ?? "GET",
      data: options?.body,
      headers: options?.headers as AxiosRequestConfig["headers"],
    });

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
        error.message,
      );
    }

    throw error;
  }
}
