import { type ReactNode } from "react";
import * as React from "react";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import type { DotLottieReactProps } from "@lottiefiles/dotlottie-react";
import { cn } from "@/lib/utils.ts";

interface IllustratedAsideProps {
  lottieSrc?: string;
  lottieClassName?: string;
  className?: string;
  contentClassName?: string;
  lottieProps?: Omit<DotLottieReactProps, "src">;
  lottieSide?: "left" | "right";
  aside?: ReactNode;
  children: ReactNode;
}

function IllustratedAside({
  lottieSrc,
  lottieClassName,
  className,
  contentClassName,
  lottieProps,
  lottieSide = "left",
  aside,
  children,
}: IllustratedAsideProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center gap-2 md:flex-row md:items-stretch",
        lottieSide === "right" && "md:flex-row-reverse",
        className,
      )}
    >
      {aside ?? (
        <ResponsiveLottie
          src={lottieSrc!}
          className={lottieClassName}
          {...lottieProps}
        />
      )}
      <div
        className={cn(
          "flex w-full md:w-md flex-col justify-between gap-4",
          contentClassName,
        )}
      >
        {children}
      </div>
    </div>
  );
}

function AsideHeader({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn(
        "flex flex-col h-full items-center justify-center gap-2",
        className,
      )}
      {...props}
    />
  );
}

function AsideFooter({ className, ...props }: React.ComponentProps<"div">) {
  return <div className={cn("h-fit w-full", className)} {...props} />;
}

export { AsideHeader, AsideFooter };
export default IllustratedAside;
