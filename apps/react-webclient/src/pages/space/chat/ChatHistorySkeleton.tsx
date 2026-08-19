import { Skeleton } from "@/components/ui/skeleton.tsx";

function ChatHistorySkeleton() {
  return (
    <div className="flex flex-col gap-4 w-full py-12">
      <Skeleton className="h-16 w-2/3 rounded-lg" />
      <Skeleton className="h-20 w-3/4 rounded-lg self-end" />
      <Skeleton className="h-12 w-1/2 rounded-lg" />
    </div>
  );
}

export default ChatHistorySkeleton;
