import IllustratedAside from "@/components/IllustratedAside.tsx";
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
    <div className="flex flex-1 items-center justify-center px-4">
      <IllustratedAside
        className="gap-4 max-w-lg"
        lottieSrc="/animations/think.lottie"
        lottieProps={{ loop: true, autoplay: true }}
      >
        <div className="flex flex-col items-center gap-3 text-center">
          <h2 className="text-lg font-medium">{t("chat.empty.page.title")}</h2>
          <p className="text-sm text-muted-foreground">
            {t("chat.empty.page.description")}
          </p>
          <Button
            disabled={isCreating}
            onClick={() => void handleCreate()}
          >
            {isCreating && <Spinner />}
            {t("chat.empty.page.cta")}
          </Button>
        </div>
      </IllustratedAside>
    </div>
  );
}

export default SpaceChatEmpty;
