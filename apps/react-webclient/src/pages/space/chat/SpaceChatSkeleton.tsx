import { Skeleton } from "@/components/ui/skeleton.tsx";

function SpaceChatSkeleton() {
  return (
    <div className="flex flex-col flex-1 min-h-0 items-center bg-transparent">
      <div className="relative flex flex-1 min-h-0 w-full max-w-3xl px-4 pt-3">
        <div className="flex flex-col flex-1 gap-4">
          <div className="flex justify-end">
            <Skeleton className="h-10 w-3/5 rounded-lg" />
          </div>
          <div className="flex justify-start">
            <Skeleton className="h-16 w-4/5 rounded-lg" />
          </div>
          <div className="flex justify-end">
            <Skeleton className="h-10 w-2/5 rounded-lg" />
          </div>
        </div>
      </div>
      <div className="w-full flex items-center justify-center relative">
        <div className="flex p-3 w-full shrink-0 place-content-center">
          <div className="w-full max-w-3xl rounded-md border bg-muted/30 p-3 flex flex-col gap-2">
            <Skeleton className="min-h-10 w-full rounded-md" />
            <div className="flex items-center gap-2 justify-end">
              <Skeleton className="h-8 w-28 rounded-md" />
              <Skeleton className="h-8 w-8 rounded-full" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SpaceChatSkeleton;
