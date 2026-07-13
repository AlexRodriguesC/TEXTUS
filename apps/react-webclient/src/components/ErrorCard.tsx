import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { XIcon } from "lucide-react";
import { useTranslation } from "react-i18next";
import CenteredPage from "@/components/CenteredPage.tsx";
import type {ReactNode} from "react";

interface ErrorCardProps {
    messageKey: string;
    className?: string;
    children: ReactNode;
}

export function ErrorCard({ messageKey, className = "max-w-md", children }: ErrorCardProps) {
    const { t } = useTranslation();

    return (
        <CenteredPage>
            <Card className={className}>
                <CardContent className="flex flex-row items-center place-content-center gap-8 w-full">
                    <XIcon color="red" />
                    <div>{t(messageKey)}</div>
                </CardContent>
                <CardFooter className="flex flex-col gap-2 w-full">{children}</CardFooter>
            </Card>
        </CenteredPage>
    );
}