import type { ReactNode } from "react";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import type { DotLottieReactProps } from "@lottiefiles/dotlottie-react";
import { cn } from "@/lib/utils.ts";

interface IllustratedAsideProps {
  lottieSrc: string;
  lottieClassName?: string;
  className?: string;
  lottieProps?: Omit<DotLottieReactProps, "src">;
  children: ReactNode;
}

function IllustratedAside({
  lottieSrc,
  lottieClassName,
  className,
  lottieProps,
  children,
}: IllustratedAsideProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center gap-2 md:flex-row",
        className,
      )}
    >
      <ResponsiveLottie
        src={lottieSrc}
        className={lottieClassName}
        {...lottieProps}
      />
      {children}
    </div>
  );
}

export default IllustratedAside;
