import { cn } from "@/lib/utils.ts";
import type { ReactNode } from "react";

export type PageContainerVariant = "default" | "full";

interface PageContainerProps {
  variant?: PageContainerVariant;
  children: ReactNode;
  className?: string;
}

function PageContainer({
  variant = "default",
  children,
  className,
}: PageContainerProps) {
  return (
    <div
      className={cn(
        "flex h-full w-full min-h-0 min-w-0 flex-col",
        variant === "default" && "mx-auto max-w-page-mobile lg:max-w-page-desktop",
        className,
      )}
    >
      {children}
    </div>
  );
}

export { PageContainer };
