import {Button} from "@/components/ui/button.tsx";
import {useTranslation} from "react-i18next";

interface LessonFooterProps {
  onCancel: () => void;
  onConfirm: () => void;
  confirmationEnabled?: boolean;
}

function LessonFooter({onCancel, onConfirm, confirmationEnabled = false}: LessonFooterProps) {
  const { t } = useTranslation();

  return (
      <div className={"flex flex-row items-center justify-between w-full"}>
        <Button onClick={onCancel} variant={"outline"}>{t("button.skip")}</Button>
        <Button disabled={!confirmationEnabled} onClick={onConfirm}>{t("button.submit")}</Button>
      </div>
  )
}

export default LessonFooter;