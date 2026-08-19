import { Skeleton } from "@/components/ui/skeleton.tsx";

function SpaceMapSkeleton() {
  return (
    <div className="flex h-full w-full items-center justify-center p-8">
      <div className="flex flex-col items-center gap-4">
        <Skeleton className="h-10 w-48 rounded-md" />
        <div className="flex gap-8">
          <Skeleton className="h-9 w-28 rounded-md" />
          <Skeleton className="h-9 w-28 rounded-md" />
        </div>
        <div className="flex gap-6">
          <Skeleton className="h-7 w-24 rounded-md" />
          <Skeleton className="h-7 w-28 rounded-md" />
          <Skeleton className="h-7 w-24 rounded-md" />
        </div>
      </div>
    </div>
  );
}

export default SpaceMapSkeleton;
