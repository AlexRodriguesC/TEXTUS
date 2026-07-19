import { useTranslation } from "react-i18next";
import { type SubmitEvent, useState } from "react";
import * as React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card.tsx";
import { Label } from "@/components/ui/label.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Eye, EyeClosed } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import PathConstants from "@/routing/pathConstants.ts";
import { Link, useNavigate } from "@tanstack/react-router";
import {
  type registerResponse,
  type registerResponseSuccess,
  useRegister,
} from "@/client/generated/authentication/authentication.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { successToast } from "@/lib/toasts.ts";
import type { ApiError } from "@/client/mutator.ts";
import UserAlreadyExistsCard from "@/pages/registration/UserAlreadyExistsCard.tsx";
import InternalServerErrorCard from "@/pages/registration/InternalServerErrorCard.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import HomeLink from "@/components/HomeLink.tsx";

function hasToken(resp: registerResponse): resp is registerResponseSuccess {
  return "data" in resp && "token" in resp.data;
}

function RegistrationPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { mutate } = useRegister<ApiError>();
  const { login } = useAuth();

  const [isPasswordShown, setIsPasswordShown] = React.useState(false);
  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [error, setError] = useState<number | undefined>(undefined);

  const register = () => {
    mutate(
      { data: { username, password } },
      {
        onSuccess: (resp) => {
          if (!hasToken(resp)) {
            console.error("no token in registration response");
            return;
          }

          login({ username, token: resp.data.token });

          navigate({ to: PathConstants.HOME }).then(() => {
            successToast(t("registration.success"));
          });
        },
        onError: (err) => {
          setError(err.status);
        },
      },
    );
  };

  const handleSubmit = (e: SubmitEvent) => {
    e.preventDefault();
    register();
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
    <CenteredPage>
      <div className="flex w-full h-full justify-center items-center">
        <Card className="w-full max-w-sm h-fit">
          <CardHeader>
            <div className={"flex justify-end pt-0"}>
              <HomeLink/>
            </div>
            <div>
              <CardTitle>{t("registration.info")}</CardTitle>
              <CardDescription>{t("registration.sub")}</CardDescription>
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit}>
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
                        className="pr-10"
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
                    {t("registration.register")}
                  </Button>
                  <Button asChild variant="outline" className="w-full">
                    <Link to={PathConstants.LOGIN}>
                      {t("registration.sign-in")}
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

export default RegistrationPage;
