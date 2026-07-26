import { useTranslation } from "react-i18next";
import { useState } from "react";
import PathConstants from "@/routing/pathConstants.ts";
import { useNavigate } from "@tanstack/react-router";
import useRegisterMutation from "@/hooks/useRegisterMutation.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { successToast } from "@/lib/toasts.tsx";
import UserAlreadyExistsCard from "@/pages/auth/registration/UserAlreadyExistsCard.tsx";
import InternalServerErrorCard from "@/pages/auth/registration/InternalServerErrorCard.tsx";
import AuthFormCard from "@/components/auth/AuthFormCard.tsx";

function RegistrationPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { register: registerMutation } = useRegisterMutation();
  const { login } = useAuth();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<number | undefined>(undefined);

  const register = () => {
    registerMutation(username, password, {
      onSuccess: (token) => {
        login({ username, token });

        navigate({ to: PathConstants.HOME }).then(() => {
          successToast(t("registration.success"));
        });
      },
      onError: (status) => {
        setError(status);
      },
    });
  };

  if (error == 409) {
    return (
      <UserAlreadyExistsCard
        reset={() => {
          setError(undefined);
        }}
      />
    );
  } else if (error) {
    return <InternalServerErrorCard />;
  }

  return (
    <AuthFormCard
      title={t("registration.info")}
      description={t("registration.sub")}
      submitLabel={t("registration.register")}
      secondaryLink={{ to: PathConstants.LOGIN, label: t("registration.sign-in") }}
      username={username}
      password={password}
      onUsernameChange={setUsername}
      onPasswordChange={setPassword}
      onSubmit={register}
    />
  );
}

export default RegistrationPage;
