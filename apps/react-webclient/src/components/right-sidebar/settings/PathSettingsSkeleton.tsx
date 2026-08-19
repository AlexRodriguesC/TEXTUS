import { Skeleton } from "@/components/ui/skeleton.tsx";

function PathSettingsSkeleton() {
  return (
    <>
      <div className="p-4">
        <Skeleton className="h-6 w-48" />
      </div>
      <div className="flex flex-col gap-6 p-4">
        <div className="flex items-center justify-between">
          <Skeleton className="h-5 w-36" />
          <Skeleton className="size-4 rounded-full" />
        </div>
        <div className="flex flex-col gap-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="flex items-center gap-4">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-2 flex-1 rounded-full" />
              <Skeleton className="h-4 w-10" />
            </div>
          ))}
        </div>
      </div>
      <div className="flex flex-row w-full justify-between p-4">
        <Skeleton className="h-9 w-24 rounded-md" />
        <Skeleton className="h-9 w-24 rounded-md" />
      </div>
    </>
  );
}

export default PathSettingsSkeleton;
