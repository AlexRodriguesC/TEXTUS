import type { ReactNode } from "react";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import type { DotLottieReactProps } from "@lottiefiles/dotlottie-react";
import { cn } from "@/lib/utils.ts";

interface IllustratedAsideProps {
  lottieSrc?: string;
  lottieClassName?: string;
  className?: string;
  lottieProps?: Omit<DotLottieReactProps, "src">;
  lottieSide?: "left" | "right";
  aside?: ReactNode;
  children: ReactNode;
}

function IllustratedAside({
                            lottieSrc,
                            lottieClassName,
                            className,
                            lottieProps,
                            lottieSide = "left",
                            aside,
                            children,
                          }: IllustratedAsideProps) {
  return (
      <div
          className={cn(
              "flex flex-col items-center gap-2 md:flex-row",
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
        {children}
      </div>
  );
}

export default IllustratedAside;
