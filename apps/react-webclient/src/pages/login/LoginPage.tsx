import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import * as React from "react";
import {Eye, EyeClosed, XIcon} from 'lucide-react';
import {useTranslation} from "react-i18next";
import {Link, useNavigate} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {useLogin} from "@/api/generated/authentication/authentication.ts";
import {Spinner} from "@/components/ui/spinner.tsx";
import {useAuth} from "@/hooks/useAuth.tsx";
import {successToast} from "@/lib/toasts.ts";
import type {ApiError} from "@/api/mutator.ts";

function LoginPage() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const {mutate, isPending, isError, isSuccess, data, error} = useLogin<ApiError>()
    const {login} = useAuth()

    const [isPasswordShown, setIsPasswordShown] = React.useState(false);
    const [username, setUsername] = React.useState("");
    const [password, setPassword] = React.useState("");

    const handleLogin = () => {
        mutate({data: {username: username, password: password}});
    }

    React.useEffect(() => {
        if (!isSuccess || !data) return;

        login({ username, token: data.data.token });

        const timer = setTimeout(() => {
            navigate({ to: PathConstants.HOME });
            successToast(t("login.success"));
        }, 2000);

        return () => clearTimeout(timer);
    }, [isSuccess, data, username, login, navigate]);

    if (isPending || isSuccess) {
        return (
            <Card>
                <CardContent className={"flex flex-row items-center gap-8"}>
                    <Spinner></Spinner><div>{t("login.loading")}</div>
                </CardContent>
            </Card>
        )
    }

    if (isError) {
        if (error.status == 404) {
            return (
                <Card className={"flex flex-col items-center"}>
                    <CardContent className={"flex flex-row items-center gap-8"}>
                        <XIcon color={"red"}/><div>{t("login.wrong-credentials")}</div>
                    </CardContent>
                    <CardFooter>
                        <Button asChild onClick={handleLogin}>
                            <Link to={PathConstants.REGISTER}>{t("login.register")}</Link>
                        </Button>
                    </CardFooter>
                </Card>
            )
        }
        return (
            <Card>
                <CardContent className={"flex flex-row items-center gap-8"}>
                    <XIcon color={"red"}/><div>{t("login.wrong")}</div>
                </CardContent>
                <CardFooter>
                    <Button onClick={handleLogin}>Try again</Button>
                </CardFooter>
            </Card>
        )
    }

    return (
        <Card className="w-full max-w-sm">
            <CardHeader>
                <CardTitle>{t("login.info")}</CardTitle>
                <CardDescription>
                    {t("login.sub")}
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={(e) => {
                    e.preventDefault();
                    handleLogin();
                }}>
                    <div className="flex flex-col gap-8">
                        <div className="flex flex-col gap-6">
                            <div className="grid gap-2">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="john.doe@textus.com"
                                    onChange={e => setUsername(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="password">Password</Label>
                                <div className="flex flex-row items-center relative">
                                    <Input
                                        id="password"
                                        type={isPasswordShown ? 'text' : 'password'}
                                        required
                                        value={password}
                                        onChange={e => setPassword(e.target.value)}
                                        className="pr-10"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setIsPasswordShown(prev => !prev)}
                                        className="absolute right-3 text-muted-foreground"
                                    >
                                        {isPasswordShown ? <Eye size={16} /> : <EyeClosed size={16} />}
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
                                    {t('login.sign-up')}
                                </Link>
                            </Button>
                        </div>
                    </div>
                </form>
            </CardContent>
        </Card>
    )
}

export default LoginPage;