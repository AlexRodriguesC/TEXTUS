import { Skeleton } from "@/components/ui/skeleton.tsx";
import { cn } from "@/lib/utils.ts";

function LessonFooterSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("flex flex-row items-center justify-between w-full", className)}>
      <Skeleton className="h-9 w-20 rounded-md" />
      <Skeleton className="h-9 w-24 rounded-md" />
    </div>
  );
}

export default LessonFooterSkeleton;
