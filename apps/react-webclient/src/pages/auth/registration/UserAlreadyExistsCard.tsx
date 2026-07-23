import { useTranslation } from "react-i18next";
import PathConstants from "@/routing/pathConstants.ts";
import { ErrorCard } from "@/components/ErrorCard.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useNavigate } from "@tanstack/react-router";

interface UserAlreadyExistsCardProps {
  reset?: () => void;
}

function UserAlreadyExistsCard({ reset }: UserAlreadyExistsCardProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <ErrorCard messageKey={t("general.user-already-exists")}>
      <Button variant="default" className={"w-full"} onClick={reset}>
        {t("general.try-another-username")}
      </Button>
      <Button
        className="w-full"
        variant="secondary"
        onClick={() => navigate({ to: PathConstants.HOME })}
      >
        {t("general.go-home")}
      </Button>
    </ErrorCard>
  );
}

export default UserAlreadyExistsCard;
