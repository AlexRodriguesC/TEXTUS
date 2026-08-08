import {
  getGetDocumentsQueryKey,
  useMoveDocument,
} from "@/client/generated/documents/documents.ts";
import type { DocumentsResponseDTO } from "@/client/model";
import { useReorderDocumentsMutation } from "@/hooks/useReorderDocumentsMutation.ts";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act, renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

const moveMutate = vi.fn();

vi.mock("@/client/generated/documents/documents.ts", async () => {
  const actual = await vi.importActual<
    typeof import("@/client/generated/documents/documents.ts")
  >("@/client/generated/documents/documents.ts");
  return {
    ...actual,
    useMoveDocument: vi.fn(),
  };
});

describe("useReorderDocumentsMutation", () => {
  beforeEach(() => {
    moveMutate.mockReset();
    vi.mocked(useMoveDocument).mockImplementation((options) => {
      return {
        mutateAsync: async (variables: {
          spaceId: number;
          documentId: number;
          data: { orderIndex: number };
        }) => {
          const context = await options?.mutation?.onMutate?.(variables);
          try {
            const result = await moveMutate(variables);
            await options?.mutation?.onSettled?.(
              result,
              null,
              variables,
              context,
            );
            return result;
          } catch (error) {
            options?.mutation?.onError?.(error as Error, variables, context);
            await options?.mutation?.onSettled?.(
              undefined,
              error as Error,
              variables,
              context,
            );
            throw error;
          }
        },
        isPending: false,
      } as never;
    });
  });

  it("should optimistically reorder and roll back when the move fails", async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    const spaceId = 7;
    const queryKey = getGetDocumentsQueryKey(spaceId);
    const initial: DocumentsResponseDTO = {
      documents: [
        { id: 1, name: "a.pdf", orderIndex: 0, tasks: [] },
        { id: 2, name: "b.pdf", orderIndex: 1, tasks: [] },
      ],
    };
    queryClient.setQueryData(queryKey, {
      data: initial,
      status: 200,
      headers: new Headers(),
    });
    moveMutate.mockRejectedValue(new Error("network"));

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useReorderDocumentsMutation(), {
      wrapper,
    });

    await act(async () => {
      await expect(
        result.current.mutateAsync({
          spaceId,
          documentId: 1,
          data: { orderIndex: 1 },
        }),
      ).rejects.toThrow("network");
    });

    await waitFor(() => {
      const cached = queryClient.getQueryData<{ data: DocumentsResponseDTO }>(
        queryKey,
      );
      expect(cached?.data.documents.map((document) => document.id)).toEqual([
        1, 2,
      ]);
    });
  });

  it("should optimistically reorder on successful move", async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    const spaceId = 7;
    const queryKey = getGetDocumentsQueryKey(spaceId);
    queryClient.setQueryData(queryKey, {
      data: {
        documents: [
          { id: 1, name: "a.pdf", orderIndex: 0, tasks: [] },
          { id: 2, name: "b.pdf", orderIndex: 1, tasks: [] },
        ],
      },
      status: 200,
      headers: new Headers(),
    });
    moveMutate.mockResolvedValue({
      data: undefined,
      status: 204,
      headers: new Headers(),
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useReorderDocumentsMutation(), {
      wrapper,
    });

    await act(async () => {
      await result.current.mutateAsync({
        spaceId,
        documentId: 1,
        data: { orderIndex: 1 },
      });
    });

    const cached = queryClient.getQueryData<{ data: DocumentsResponseDTO }>(
      queryKey,
    );
    expect(cached?.data.documents.map((document) => document.id)).toEqual([
      2, 1,
    ]);
    expect(cached?.data.documents.map((document) => document.orderIndex)).toEqual([
      0, 1,
    ]);
  });
});
