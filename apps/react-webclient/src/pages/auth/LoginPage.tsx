import { Button } from "@/components/ui/button.tsx";
import * as React from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import useLoginMutation from "@/hooks/useLoginMutation.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ErrorCard } from "@/components/ErrorCard.tsx";
import { successToast } from "@/lib/toasts.tsx";
import AuthFormCard from "@/components/auth/AuthFormCard.tsx";

function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login: loginMutation } = useLoginMutation();
  const { login } = useAuth();

  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [error, setError] = React.useState<number | undefined>(undefined);

  const handleLogin = () => {
    loginMutation(username, password, {
      onSuccess: (token) => {
        login({ username, token });
        navigate({ to: PathConstants.HOME }).then(() => {
          successToast(t("login.success"));
        });
      },
      onError: (status) => {
        setError(status);
      },
    });
  };

  const reset = () => {
    setError(undefined);
    setUsername("");
    setPassword("");
  };

  if (error === 404) {
    return (
      <ErrorCard messageKey="login.wrong-credentials">
        <Button variant="default" asChild>
          <Link to={PathConstants.REGISTER}>{t("login.register")}</Link>
        </Button>
        <Button
          className="w-full"
          variant="secondary"
          onClick={() => navigate({ to: PathConstants.HOME })}
        >
          {t("general.go-home")}
        </Button>
      </ErrorCard>
    );
  } else if (error === 401) {
    return (
      <ErrorCard messageKey="login.wrong-credentials">
        <Button variant="default" onClick={() => reset()}>
          {t("login.try-with-other")}
        </Button>
        <Button variant="secondary" className="w-full" asChild>
          <Link to={PathConstants.REGISTER}>{t("login.register")}</Link>
        </Button>
      </ErrorCard>
    );
  } else if (error) {
    return (
      <ErrorCard messageKey="login.wrong">
        <Button className="w-full" onClick={handleLogin}>
          {t("button.retry")}
        </Button>
        <Button variant="secondary" onClick={() => reset()}>
          {t("login.try-with-other")}
        </Button>
      </ErrorCard>
    );
  }

  return (
    <AuthFormCard
      title={t("login.info")}
      description={t("login.sub")}
      submitLabel={t("login.login")}
      secondaryLink={{ to: PathConstants.REGISTER, label: t("login.sign-up") }}
      username={username}
      password={password}
      onUsernameChange={setUsername}
      onPasswordChange={setPassword}
      onSubmit={handleLogin}
    />
  );
}

export default LoginPage;
