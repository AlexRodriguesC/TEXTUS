import { Skeleton } from "@/components/ui/skeleton.tsx";
import { cn } from "@/lib/utils.ts";

function LessonHeaderSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("flex items-center gap-3 w-full", className)}>
      <Skeleton className="size-9 rounded-md" />
      <Skeleton className="h-3 flex-1 rounded-full" />
      <Skeleton className="size-9 rounded-md" />
    </div>
  );
}

export default LessonHeaderSkeleton;
