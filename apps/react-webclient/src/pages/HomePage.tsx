import {Trans, useTranslation} from "react-i18next";
import {Button} from "@/components/ui/button.tsx";
import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

function HomePage() {
    const {t} = useTranslation();

    return (
        <div className="flex flex-row items-center gap-8">
            <img src="./book_pile.png" alt="Pile of books" width={400} height={400} />
            <div className="flex flex-col items-center gap-8">
                <Trans
                    i18nKey="home.tagline"
                    components={{
                        bold: <span className="font-bold" />,
                    }}
                />
                <div className="flex w-full flex-col items-center gap-2">
                    <Button asChild className="w-full" variant="default">
                        <Link to="/pippo">{t("home.start")}</Link>
                    </Button>

                    <Button asChild className="w-full" variant="secondary">
                        <Link to={PathConstants.LOGIN}>{t("home.account")}</Link>
                    </Button>
                </div>
            </div>
        </div>
    );
}


export default HomePage;