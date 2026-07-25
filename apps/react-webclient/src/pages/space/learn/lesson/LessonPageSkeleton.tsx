import CenteredPage from "@/components/CenteredPage.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import LessonFooterSkeleton from "@/pages/space/learn/lesson/LessonFooterSkeleton.tsx";
import LessonHeaderSkeleton from "@/pages/space/learn/lesson/LessonHeaderSkeleton.tsx";
import LessonQuestionSkeleton from "@/pages/space/learn/LessonQuestionSkeleton.tsx";

function LessonPageSkeleton() {
  return (
    <CenteredPage>
      <div className="flex flex-col items-center gap-4 h-full w-[90%] md:w-3/5">
        <div className="sticky top-4 flex items-center gap-3 w-full">
          <LessonHeaderSkeleton />
        </div>
        <div className="flex-1 min-h-0 overflow-y-auto w-full place-content-center">
          <LessonQuestionSkeleton />
        </div>
        <Separator />
        <div className="sticky bottom-4 pt-4 flex items-center gap-3 w-full">
          <LessonFooterSkeleton />
        </div>
      </div>
    </CenteredPage>
  );
}

export default LessonPageSkeleton;
