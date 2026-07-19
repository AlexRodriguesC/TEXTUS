import { useTranslation } from "react-i18next";
import CenteredPage from "@/components/CenteredPage.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import type { ReactNode } from "react";

interface LoadingScreenProps {
    title?: string;
    description?: string;
    actions?: ReactNode;
}

function LoadingScreen({
                           title = "loading.title",
                           description = "loading.description",
                           actions,
                       }: LoadingScreenProps) {
    const { t } = useTranslation();

    return (
        <CenteredPage>
            <div className="flex flex-col md:flex-row items-center gap-4">
                <ResponsiveLottie src="/animations/loading.lottie" />
                <div className="flex flex-col items-center md:items-start gap-2 text-center md:text-left">
                    <h2 className="text-xl font-semibold loading-dots">{t(title)}</h2>
                    <p className="text-muted-foreground text-sm">{t(description)}</p>
                    {actions && <div className="flex items-center gap-3 pt-2">{actions}</div>}
                </div>
            </div>
        </CenteredPage>
    );
}

export default LoadingScreen;