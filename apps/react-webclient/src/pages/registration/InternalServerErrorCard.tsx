import { useTranslation } from "react-i18next";
import PathConstants from "@/routing/pathConstants.ts";
import ErrorCard from "@/pages/registration/ErrorCard.tsx";

function InternalServerErrorCard() {
  const { t } = useTranslation();

  return (
    <ErrorCard
      title={t("general.internal-server-error")}
      goTo={PathConstants.HOME}
      goToLabel={t("general.wrong")}
    ></ErrorCard>
  );
}

export default InternalServerErrorCard;
