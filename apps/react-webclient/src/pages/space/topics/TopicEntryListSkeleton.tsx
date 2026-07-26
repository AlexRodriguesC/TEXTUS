import { Skeleton } from "@/components/ui/skeleton.tsx";

function TopicEntryListSkeleton() {
  return (
    <div className="flex h-full w-full justify-center overflow-hidden">
      <div className="h-full w-full px-4 py-6 flex flex-col gap-6">
        <Skeleton className="h-10 w-full" />
        <div className="flex flex-col gap-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="border p-4">
              <div className="flex justify-between items-center">
                <Skeleton className="h-5 w-40" />
                <Skeleton className="size-4 rounded-full" />
              </div>
              <Skeleton className="mt-2 h-3 w-full" />
              <Skeleton className="mt-1 h-3 w-3/4" />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default TopicEntryListSkeleton;
