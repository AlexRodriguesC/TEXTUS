import {Card, CardContent, CardFooter} from "@/components/ui/card.tsx"
import {XIcon} from "lucide-react"
import {Button} from "@/components/ui/button.tsx"
import {Link} from "@tanstack/react-router"
import PathConstants from "@/routing/pathConstants.ts"
import {useTranslation} from "react-i18next";

type ErrorCardProps = {
    title: string
    goTo?: string
    reset?: () => void
    goToLabel?: string
    onClick?: () => void
}

function ErrorCard({ title, goTo, reset, goToLabel = "Go", onClick }: ErrorCardProps) {
    const {t} = useTranslation();

    return (
        <Card className="flex flex-col items-center">
            <CardContent className="flex flex-row items-center gap-8">
                <XIcon color="red" />
                <div>{title}</div>
            </CardContent>

            <CardFooter className="inline-flex flex-col items-stretch gap-2">
                {goTo ? (
                    reset ? <Button onClick={reset} asChild>
                        <Link to={goTo}>{goToLabel}</Link>
                    </Button> :
                        <Button asChild>
                            <Link to={goTo}>{goToLabel}</Link>
                        </Button>
                ) : onClick ? (
                    <Button onClick={onClick}>{t("general.try-again")}</Button>
                ) : null}

                <Button variant={"outline"} asChild>
                    <Link to={PathConstants.HOME}>{t("general.go-home")}</Link>
                </Button>
            </CardFooter>
        </Card>
    )
}

export default ErrorCard