import MainLayout from "@/MainLayout.tsx";
import CenteredPage from "@/components/CenteredPage.tsx";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import { Link } from "@tanstack/react-router";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import PathConstants from "@/routing/pathConstants.ts";

function NotFoundPage() {
  const { t } = useTranslation();

  return (
    <MainLayout sidebarEnabled={false}>
      <CenteredPage>
        <div className={"flex flex-col items-center gap-2 md:flex-row"}>
          <ResponsiveLottie src={"/animations/not_found.lottie"} />
          <div className={"flex flex-col items-center gap-4"}>
            <p>{t("notfound")}</p>
            <Link to={PathConstants.HOME}>
              <Button>{t("general.go-home")}</Button>
            </Link>
          </div>
        </div>
      </CenteredPage>
    </MainLayout>
  );
}

export default NotFoundPage;
