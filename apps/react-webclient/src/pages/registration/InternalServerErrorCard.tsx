import { useTranslation } from "react-i18next";
import PathConstants from "@/routing/pathConstants.ts";
import {ErrorCard} from "@/components/ErrorCard.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Link} from "@tanstack/react-router";

function InternalServerErrorCard() {
  const { t } = useTranslation();

  return (
    <ErrorCard
        messageKey={"general.wrong"}>
      <Button variant={"default"} className={"w-full"} asChild>
        <Link to={PathConstants.HOME}>
          {t("general.go-home")}
        </Link>
      </Button>
    </ErrorCard>
  );
}

export default InternalServerErrorCard;
