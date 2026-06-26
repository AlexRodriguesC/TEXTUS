import {useTranslation} from "react-i18next";
import type {SubmitEvent} from "react";
import * as React from "react";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Label} from "@/components/ui/label.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Eye, EyeClosed} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import PathConstants from "@/routing/pathConstants.ts";
import {Link, useNavigate} from "@tanstack/react-router";
import {useRegister} from "@/api/generated/authentication/authentication.ts";
import {Spinner} from "@/components/ui/spinner.tsx";
import {useAuth} from "@/hooks/useAuth.tsx";
import {successToast} from "@/lib/toasts.ts";
import type {ApiError} from "@/api/mutator.ts";
import UserAlreadyExistsCard from "@/pages/registration/UserAlreadyExistsCard.tsx";
import InternalServerErrorCard from "@/pages/registration/InternalServerErrorCard.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";

function RegistrationPage() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const {mutate, isPending, isError, isSuccess, data, error, reset} = useRegister<ApiError>();
    const { login } = useAuth();

    const [isPasswordShown, setIsPasswordShown] = React.useState(false);
    const [username, setUsername]= React.useState("");
    const [password, setPassword] = React.useState("");

    const register = () => {
        mutate({data:{username, password}})
    }

    const handleSubmit = (e: SubmitEvent) => {
        e.preventDefault();
        register();
    }

    React.useEffect(() => {
        if (!isSuccess || !data) return;

        login({ username, token: data.data.token });

        const timer = setTimeout(() => {
            navigate({ to: PathConstants.HOME });
            successToast(t("registration.success"));
        }, 2000);

        return () => clearTimeout(timer);
    }, [isSuccess, data, username, login]);

    if (isPending || isSuccess) {
        return (
            <CenteredPage>
                <Card>
                    <CardContent className={"flex flex-row items-center gap-8"}>
                        <Spinner></Spinner><div>{t("registration.loading")}</div>
                    </CardContent>
                </Card>
            </CenteredPage>
        )
    }

    if (isError) {
        if (error.status == 409) return <UserAlreadyExistsCard reset={() => reset()}/>
        return <InternalServerErrorCard/>
    }

    return (
        <CenteredPage>
        <Card className="w-full max-w-sm">
            <CardHeader>
                <CardTitle>{t("registration.info")}</CardTitle>
                <CardDescription>
                    {t("registration.sub")}
                </CardDescription>
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
                                {t("registration.register")}
                            </Button>
                            <Button asChild variant="outline" className="w-full">
                                <Link to={PathConstants.LOGIN}>
                                    {t('registration.sign-in')}
                                </Link>
                            </Button>
                        </div>
                    </div>
                </form>
            </CardContent>
        </Card>
        </CenteredPage>
    )

}

export default RegistrationPage;