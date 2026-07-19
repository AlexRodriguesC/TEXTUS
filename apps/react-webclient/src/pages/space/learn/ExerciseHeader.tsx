import { cn } from "@/lib/utils.ts";
import TopicInformationModal from "@/pages/space/learn/TopicInformationModal.tsx";
import useTopics from "@/hooks/useTopics.tsx";
import { useMemo } from "react";

interface ExerciseHeaderProps {
  question: string;
  topicId: number;
}

function ExerciseHeader({ question, topicId }: ExerciseHeaderProps) {
  const { topics } = useTopics();
  const topic = useMemo(
    () => topics.find((t) => t.id === topicId),
    [topics, topicId],
  );

  return (
    <div
      className={cn(
        "w-full",
        topic ? "flex flex-row gap-2 justify-between" : "",
      )}
    >
      <p>{question}</p>
      {topic && <TopicInformationModal topic={topic} />}
    </div>
  );
}

export default ExerciseHeader;
