import MainLayout from "@/MainLayout.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import { Button } from "@/components/ui/button.tsx";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu.tsx";
import PathConstants from "@/routing/pathConstants.ts";
import { useTranslation } from "react-i18next";
import { Link } from "@tanstack/react-router";

function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }

  if (error != null) {
    return String(error);
  }

  return "Unknown error";
}

function ErrorFallback({ error }: { error: unknown }) {
  const { t } = useTranslation();
  const errorMessage = getErrorMessage(error);

  return (
    <MainLayout sidebarEnabled={false}>
      <CenteredPage>
        <IllustratedAside lottieSrc="/animations/not_found.lottie">
          <AsideHeader>
            <p className="text-center">{t("errorBoundary.message")}</p>
          </AsideHeader>
          <AsideFooter>
            <div className="flex flex-col w-full max-w-[90%] md:w-sm gap-2">
              <Button asChild>
                <Link to={PathConstants.HOME}>{t("general.go-home")}</Link>
              </Button>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline">
                    {t("errorBoundary.cause")}
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="max-w-sm flex place-content-center">
                  <DropdownMenuLabel className="normal-case font-normal tracking-normal whitespace-pre-wrap wrap-break-word">
                    {errorMessage}
                  </DropdownMenuLabel>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </AsideFooter>
        </IllustratedAside>
      </CenteredPage>
    </MainLayout>
  );
}

export default ErrorFallback;
