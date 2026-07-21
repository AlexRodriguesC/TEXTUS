import { Button } from "@/components/ui/button.tsx";
import useChatContext from "@/hooks/useChatContext.ts";
import useChats from "@/hooks/useChats.ts";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useTopics from "@/hooks/useTopics.tsx";
import { buildExerciseHelpMessage } from "@/pages/space/learn/exerciseHelpMessage.ts";
import PathConstants from "@/routing/pathConstants.ts";
import type {
  GroupResponseDTO,
  ListResponseDTO,
  PairResponseDTO,
  QuestionResponseDTO,
} from "@/client/model";
import { useNavigate } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Spinner } from "@/components/ui/spinner.tsx";

type Exercise =
  | QuestionResponseDTO
  | ListResponseDTO
  | GroupResponseDTO
  | PairResponseDTO;

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
      navigate({ to: PathConstants.SPACE_CHAT, params: { spaceId: strSpaceId } });
    } finally {
      setIsNavigating(false);
    }
  };

  return (
    <div className="flex justify-center pt-4">
      <Button
        variant="outline"
        disabled={isCreating || isNavigating}
        onClick={() => void handleClick()}
      >
        {(isCreating || isNavigating) && <Spinner className="h-4 w-4 mr-2" />}
        {t("exercise.help.button")}
      </Button>
    </div>
  );
}

export default ExerciseHelpButton;
