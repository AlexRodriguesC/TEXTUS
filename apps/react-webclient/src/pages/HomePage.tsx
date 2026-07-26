import { Trans, useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";

function HomePage() {
  const { t } = useTranslation();
  const { isAuthenticated } = useAuth();

  return (
    <div className="flex flex-col min-h-screen min-w-100 justify-center items-center gap-6 md:flex-row md:gap-8 place-self-center">
      <div>
        <div className={"flex place-content-center"}>
          <ResponsiveLottie src="/animations/home.lottie" />
        </div>
        <div className="flex flex-col items-center gap-4 w-full">
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
              <Link to={isAuthenticated ? PathConstants.SPACES : PathConstants.LOGIN}>{t("home.start")}</Link>
            </Button>
            {!isAuthenticated && (
              <Button asChild className="w-full" variant="secondary">
                <Link to={PathConstants.LOGIN}>{t("home.account")}</Link>
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
