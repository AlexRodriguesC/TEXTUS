import CenteredPage from "@/components/CenteredPage.tsx";
import IllustratedAside, {
  AsideFooter,
  AsideHeader,
} from "@/components/IllustratedAside.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Spinner } from "@/components/ui/spinner.tsx";
import useChatContext from "@/hooks/context/useChatContext.ts";
import useChats from "@/hooks/useChats.ts";
import { useCallback } from "react";
import { useTranslation } from "react-i18next";

function SpaceChatEmpty() {
  const { t } = useTranslation();
  const { createChat, isCreating } = useChats();
  const { setSelectedChatId } = useChatContext();

  const handleCreate = useCallback(async () => {
    const chat = await createChat();
    setSelectedChatId(chat.id);
  }, [createChat, setSelectedChatId]);

  return (
    <CenteredPage>
      <IllustratedAside
        className="max-w-[80%]"
        lottieSrc="/animations/think.lottie"
        lottieProps={{ loop: true, autoplay: true }}
      >
        <AsideHeader>
          <p className="font-bold uppercase text-center">
            {t("chat.empty.page.title")}
          </p>
          <p className="text-center">{t("chat.empty.page.description")}</p>
        </AsideHeader>
        <AsideFooter>
          <Button
            className="w-full"
            disabled={isCreating}
            onClick={() => void handleCreate()}
          >
            {isCreating && <Spinner />}
            {t("chat.empty.page.cta")}
          </Button>
        </AsideFooter>
      </IllustratedAside>
    </CenteredPage>
  );
}

export default SpaceChatEmpty;
