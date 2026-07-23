import { Button } from "@/components/ui/button.tsx";
import useChatContext from "@/hooks/context/useChatContext.ts";
import useChats from "@/hooks/useChats.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useTopics from "@/hooks/useTopics.tsx";
import { buildExerciseHelpMessage } from "@/pages/space/learn/exerciseHelpMessage.ts";
import PathConstants from "@/routing/pathConstants.ts";
import type { Exercise } from "@/classes/Exercise.ts";
import { useNavigate } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Spinner } from "@/components/ui/spinner.tsx";

interface ExerciseHelpButtonProps {
  exercise: Exercise;
  userAnswer: unknown;
  topicId: number;
}

function ExerciseHelpButton({
  exercise,
  userAnswer,
  topicId,
}: ExerciseHelpButtonProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { strSpaceId } = useSpaceId();
  const { createChat, isCreating } = useChats();
  const { setSelectedChatId, setPendingMessage } = useChatContext();
  const { topics } = useTopics();
  const [isNavigating, setIsNavigating] = useState(false);

  const topic = useMemo(
    () => topics.find((item) => item.id === topicId),
    [topics, topicId],
  );

  const handleClick = async () => {
    if (isCreating || isNavigating) return;

    setIsNavigating(true);
    try {
      const message = buildExerciseHelpMessage(t, exercise, userAnswer, topic);
      const chat = await createChat(t("exercise.help.title"));
      setSelectedChatId(chat.id);
      setPendingMessage({ chatId: chat.id, message });
      navigate({
        to: PathConstants.SPACE_CHAT,
        params: { spaceId: strSpaceId },
      });
    } finally {
      setIsNavigating(false);
    }
  };

  return (
    <Button
      variant="outline"
      disabled={isCreating || isNavigating}
      onClick={() => void handleClick()}
    >
      {(isCreating || isNavigating) && <Spinner />}
      {t("exercise.help.button")}
    </Button>
  );
}

export default ExerciseHelpButton;
