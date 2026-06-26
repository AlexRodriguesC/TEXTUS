import {Trans, useTranslation} from "react-i18next";
import {Button} from "@/components/ui/button.tsx";
import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {useAuth} from "@/hooks/useAuth.tsx";

function HomePage() {
    const {t} = useTranslation();
    const {user} = useAuth()

    return (
        <div className="flex flex-col items-center gap-6 md:flex-row md:gap-8">
            <img
                src="/book_pile.png"
                alt="Pile of books"
                className="aspect-square size-48 md:size-80 lg:size-128"
            />
            <div className="flex flex-col items-center gap-4">
                <div className="text-center text-2xl md:text-3xl lg:text-4xl">
                    <Trans
                        i18nKey="home.tagline"
                        components={{
                            bold: <span className="font-bold" />,
                        }}
                    />
                </div>
                <div className="flex w-full max-w-xs flex-col gap-2 md:max-w-sm">
                    <Button asChild className="w-full" variant="default">
                        <Link to={PathConstants.SPACES}>{t("home.start")}</Link>
                    </Button>
                    {!user &&
                        <Button asChild className="w-full" variant="secondary">
                        <Link to={PathConstants.LOGIN}>{t("home.account")}</Link>
                        </Button>}
                </div>
            </div>
        </div>
    );
}


export default HomePage;