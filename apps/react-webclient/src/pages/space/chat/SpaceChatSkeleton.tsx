import { Skeleton } from "@/components/ui/skeleton.tsx";

function SpaceChatSkeleton() {
  return (
    <div className="flex flex-col flex-1 min-h-0 items-center bg-transparent">
      <div className="relative flex flex-1 min-h-0 w-full max-w-3xl px-4 pt-3 gap-4">
        <Skeleton className="h-16 w-2/3 rounded-lg self-start" />
        <Skeleton className="h-20 w-3/4 rounded-lg self-end" />
        <Skeleton className="h-12 w-1/2 rounded-lg self-start" />
      </div>
      <div className="w-full max-w-3xl px-4 py-3">
        <Skeleton className="h-12 w-full rounded-lg" />
      </div>
    </div>
  );
}

export default SpaceChatSkeleton;
