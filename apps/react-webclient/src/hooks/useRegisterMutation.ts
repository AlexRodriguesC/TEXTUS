import {
  type registerResponse,
  type registerResponseSuccess,
  useRegister,
} from "@/client/generated/authentication/authentication.ts";
import type { ApiError } from "@/client/mutator.ts";

function hasToken(resp: registerResponse): resp is registerResponseSuccess {
  return "data" in resp && "token" in resp.data;
}

function useRegisterMutation() {
  const { mutate, isPending } = useRegister<ApiError>();

  const register = (
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
          if (!hasToken(resp)) {
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

  return { register, isPending };
}

export default useRegisterMutation;
