import { Skeleton } from "@/components/ui/skeleton.tsx";

function LessonQuestionSkeleton() {
  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-col gap-2">
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-6 w-full max-w-lg" />
      </div>
      <div className="grid grid-cols-2 grid-rows-2 gap-4">
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className="h-14 w-full rounded-md" />
        ))}
      </div>
    </div>
  );
}

export default LessonQuestionSkeleton;
