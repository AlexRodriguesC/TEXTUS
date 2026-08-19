import CenteredPage from "@/components/CenteredPage.tsx";
import { Skeleton } from "@/components/ui/skeleton.tsx";

function SpaceMenuSkeleton() {
  return (
    <CenteredPage>
      <div className="place-content-center w-full min-w-[90%]">
        <div className="flex flex-col gap-4">
          <Skeleton className="h-32 w-full rounded-none border-2 border-dashed" />
          <div className="flex flex-col w-full items-center gap-1">
            {Array.from({ length: 3 }).map((_, index) => (
              <div
                key={index}
                className="flex w-full items-center justify-between gap-4 border p-3"
              >
                <Skeleton className="h-4 w-48" />
                <Skeleton className="h-8 w-20 rounded-md" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </CenteredPage>
  );
}

export default SpaceMenuSkeleton;
