import ChatEmptyMessages from "@/pages/space/chat/ChatEmptyMessages.tsx";
import ChatInputFooter from "@/pages/space/chat/ChatInputFooter.tsx";
import { useTranslation } from "react-i18next";

// Placeholder UI shown when no chat is selected in the space chat page.
function SpaceChatNoSelection({
  models,
  defaultModel,
}: {
  models: { value: string; label: string }[];
  defaultModel?: string;
}) {
  const { t } = useTranslation();

  return (
    <>
      <ChatEmptyMessages title={t("chat.new")} />
      <ChatInputFooter
        onSubmit={() => undefined}
        models={models}
        defaultModel={defaultModel}
        disabled
      />
    </>
  );
}

export default SpaceChatNoSelection;
