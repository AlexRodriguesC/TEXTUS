import {useTranslation} from "react-i18next";
import PathConstants from "@/routing/pathConstants.ts";
import ErrorCard from "@/pages/registration/ErrorCard.tsx";

interface UserAlreadyExistsCardProps {
    reset?: () => void
}

function UserAlreadyExistsCard({reset}: UserAlreadyExistsCardProps) {
    const {t} = useTranslation();

    return <ErrorCard title={t("general.user-already-exists")} goTo={PathConstants.REGISTER} reset={reset}
                      goToLabel={t("general.try-another-username")}></ErrorCard>
}

export default UserAlreadyExistsCard;