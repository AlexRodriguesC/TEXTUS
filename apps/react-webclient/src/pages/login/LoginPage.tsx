import {Card, CardAction, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {Label } from "@/components/ui/label";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import * as React from "react";
import { Eye, EyeClosed } from 'lucide-react';
import {useTranslation} from "react-i18next";

function LoginPage() {
    const {t} = useTranslation();

    const [isPasswordShown, setIsPasswordShown] = React.useState(false);
    const [password, setPassword] = React.useState("");


    return (
        <Card className="w-full max-w-sm">
            <CardHeader>
                <CardTitle>{t("login.info")}</CardTitle>
                <CardDescription>
                    {t("login.sub")}
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form>
                    <div className="flex flex-col gap-6">
                        <div className="grid gap-2">
                            <Label htmlFor="email">Email</Label>
                            <Input
                                id="email"
                                type="email"
                                placeholder="john.doe@textus.com"
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
                </form>
            </CardContent>
            <CardFooter className="flex-col gap-2">
                <Button type="submit" className="w-full">
                    Login
                </Button>
                <Button variant="outline" className="w-full">
                    {t('login.sign-up')}
                </Button>
            </CardFooter>
        </Card>
    )
}

export default LoginPage;