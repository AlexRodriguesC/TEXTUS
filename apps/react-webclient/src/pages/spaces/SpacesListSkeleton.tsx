import { Skeleton } from "@/components/ui/skeleton.tsx";

function SpacesListItemSkeleton() {
  return (
    <div className="flex flex-row items-center justify-between border border-border p-4">
      <div className="flex flex-1 flex-row items-center gap-4">
        <Skeleton className="size-8 shrink-0" />
        <div className="flex flex-col gap-1.5 flex-1">
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-4 w-64" />
        </div>
      </div>
      <div className="flex flex-row gap-2 items-center">
        <Skeleton className="size-4" />
        <Skeleton className="size-9 rounded-md" />
      </div>
    </div>
  );
}

function SpacesListSkeleton() {
  return (
    <div className="flex flex-col max-h-[60vh] min-h-[60vh] min-w-full overflow-y-auto gap-2">
      {Array.from({ length: 4 }).map((_, i) => (
        <SpacesListItemSkeleton key={i} />
      ))}
    </div>
  );
}

export default SpacesListSkeleton;
