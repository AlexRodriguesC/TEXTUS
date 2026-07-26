import MainLayout from "@/MainLayout.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import PathConstants from "@/routing/pathConstants.ts";

function NotFoundPage() {
  const { t } = useTranslation();

  return (
    <MainLayout sidebarEnabled={false}>
      <CenteredPage>
        <IllustratedAside lottieSrc="/animations/not_found.lottie">
          <AsideHeader>
            <p>{t("notfound")}</p>
          </AsideHeader>
          <AsideFooter className="flex justify-center">
            <Link to={PathConstants.HOME}>
              <Button>{t("general.go-home")}</Button>
            </Link>
          </AsideFooter>
        </IllustratedAside>
      </CenteredPage>
    </MainLayout>
  );
}

export default NotFoundPage;
