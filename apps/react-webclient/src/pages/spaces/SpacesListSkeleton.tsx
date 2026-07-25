import { Skeleton } from "@/components/ui/skeleton.tsx";

function SpacesListSkeleton() {
  return (
    <div className="flex flex-1 min-h-0 flex-col min-w-full overflow-y-auto gap-2">
      {Array.from({ length: 4 }).map((_, index) => (
        <div
          key={index}
          className="flex flex-row items-center justify-between border border-border p-4"
        >
          <div className="flex flex-1 flex-row items-center gap-4">
            <Skeleton className="size-8 rounded" />
            <div className="flex flex-col gap-2">
              <Skeleton className="h-4 w-40" />
              <Skeleton className="h-3 w-56" />
            </div>
          </div>
          <div className="flex flex-row gap-2 items-center">
            <Skeleton className="size-4 rounded" />
            <Skeleton className="size-9 rounded-md" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default SpacesListSkeleton;
