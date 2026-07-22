import {
  type loginResponse,
  type loginResponseSuccess,
  useLogin,
} from "@/client/generated/authentication/authentication.ts";
import type { ApiError } from "@/client/mutator.ts";

function isSuccessResponse(resp: loginResponse): resp is loginResponseSuccess {
  return "data" in resp && "token" in resp.data;
}

function useLoginMutation() {
  const { mutate, isPending } = useLogin<ApiError>();

  const login = (
    username: string,
    password: string,
    callbacks: {
      onSuccess: (token: string) => void;
      onError: (status: number) => void;
    },
  ) => {
    mutate(
      { data: { username, password } },
      {
        onSuccess: (resp) => {
          if (!isSuccessResponse(resp)) {
            return;
          }
          callbacks.onSuccess(resp.data.token);
        },
        onError: (resp) => {
          callbacks.onError(resp.status);
        },
      },
    );
  };

  return { login, isPending };
}

export default useLoginMutation;
