import ReviewTopicsButton from "@/components/ReviewTopicsButton.tsx";
import RetryLessonButton from "@/components/RetryLessonButton.tsx";

interface ExhaustedFooterProps {
  strSpaceId: string;
  chapterId: number;
  onRetry: () => void;
  isDeleting: boolean;
}

function ExhaustedFooter({
  strSpaceId,
  chapterId,
  onRetry,
  isDeleting,
}: ExhaustedFooterProps) {
  return (
    <>
      <ReviewTopicsButton
        strSpaceId={strSpaceId}
        chapterId={chapterId}
        onCancel={() => undefined}
      />
      <RetryLessonButton onRetry={onRetry} isDeleting={isDeleting} />
    </>
  );
}

export default ExhaustedFooter;
