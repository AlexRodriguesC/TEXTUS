import { Skeleton } from "@/components/ui/skeleton.tsx";

function QuestionSkeleton() {
  return (
    <div className="flex flex-col gap-8">
      <div className="w-full flex flex-row gap-2 justify-between">
        <div className="flex flex-col gap-2 flex-1">
          <Skeleton className="h-5 w-full" />
          <Skeleton className="h-5 w-4/5" />
        </div>
        <Skeleton className="h-9 w-9 shrink-0 rounded-md" />
      </div>
      <div className="grid grid-cols-2 grid-rows-2 gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-14 w-full rounded-md" />
        ))}
      </div>
    </div>
  );
}

export default QuestionSkeleton;
