import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import * as React from "react";
import { Eye, EyeClosed } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {
  type loginResponse,
  type loginResponseSuccess,
  useLogin,
} from "@/client/generated/authentication/authentication.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { successToast } from "@/lib/toasts.ts";
import type { ApiError } from "@/client/mutator.ts";
import CenteredPage from "@/components/CenteredPage.tsx";
import { ErrorCard } from "@/components/ErrorCard.tsx";
import HomeLink from "@/components/HomeLink.tsx";

function isSuccessResponse(resp: loginResponse): resp is loginResponseSuccess {
  return "data" in resp && "token" in resp.data;
}

function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { mutate } = useLogin<ApiError>();
  const { login } = useAuth();

  const [isPasswordShown, setIsPasswordShown] = React.useState(false);
  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [error, setError] = React.useState<number | undefined>(undefined);

  const handleLogin = () => {
    mutate(
      { data: { username: username, password: password } },
      {
        onSuccess: (resp) => {
          if (!isSuccessResponse(resp)) {
            return;
          }
          login({ username, token: resp.data.token });
          navigate({ to: PathConstants.HOME }).then(() => {
            successToast(t("login.success"));
          });
        },
        onError: (resp) => {
          setError(resp.status);
        },
      },
    );
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
    <CenteredPage>
      <div className="flex w-full h-full justify-center items-center">
        <Card className="w-full max-w-sm">
          <CardHeader>
            <div className={"flex justify-end pt-0"}>
              <HomeLink/>
            </div>
            <div>
              <CardTitle>{t("login.info")}</CardTitle>
              <CardDescription>{t("login.sub")}</CardDescription>
            </div>
          </CardHeader>
          <CardContent>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleLogin();
              }}
            >
              <div className="flex flex-col gap-8">
                <div className="flex flex-col gap-6">
                  <div className="grid gap-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="john.doe@textus.com"
                      onChange={(e) => setUsername(e.target.value)}
                      required
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="password">Password</Label>
                    <div className="flex flex-row items-center relative">
                      <Input
                        id="password"
                        type={isPasswordShown ? "text" : "password"}
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="pr-10 p-2"
                      />
                      <button
                        type="button"
                        onClick={() => setIsPasswordShown((prev) => !prev)}
                        className="absolute right-3 text-muted-foreground"
                      >
                        {isPasswordShown ? (
                          <Eye size={16} />
                        ) : (
                          <EyeClosed size={16} />
                        )}
                      </button>
                    </div>
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  <Button type="submit" className="w-full">
                    {t("login.login")}
                  </Button>
                  <Button asChild variant="outline" className="w-full">
                    <Link to={PathConstants.REGISTER}>
                      {t("login.sign-up")}
                    </Link>
                  </Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </CenteredPage>
  );
}

export default LoginPage;
