import { Skeleton } from "@/components/ui/skeleton.tsx";

const OFFSETS = [0, 60, 0, -60, 0, 60];

function SpaceLearnSkeleton() {
  return (
    <div className="flex h-full flex-col items-center pt-4 gap-6 w-full">
      {OFFSETS.map((offset, index) => (
        <div
          key={index}
          className="flex justify-center"
          style={{ transform: `translateX(${offset}px)` }}
        >
          <Skeleton className="size-14 rounded-full" />
        </div>
      ))}
    </div>
  );
}

export default SpaceLearnSkeleton;
