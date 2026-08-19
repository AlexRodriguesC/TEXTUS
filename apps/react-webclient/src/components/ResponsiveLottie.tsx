import {
  type DotLottie,
  DotLottieReact,
  type DotLottieReactProps,
} from "@lottiefiles/dotlottie-react";
import { useState } from "react";
import { cn } from "@/lib/utils.ts";

interface ResponsiveLottieProps {
  src: string;
  className?: string;
}

function ResponsiveLottie({
  src,
  loop = true,
  autoplay = true,
  className,
  ...props
}: ResponsiveLottieProps & DotLottieReactProps) {
  const [dotLottie, setDotLottie] = useState<DotLottie | null>(null);

  const wrapperClass = cn("aspect-square w-40 md:w-64 xl:w-80", className);

  if (!autoplay) {
    return (
      <div className={wrapperClass}>
        <DotLottieReact
          src={src}
          loop={loop}
          autoplay={autoplay}
          dotLottieRefCallback={setDotLottie}
          onMouseEnter={() => dotLottie?.play()}
          onMouseLeave={() => dotLottie?.pause()}
          {...props}
        />
      </div>
    );
  }

  return (
    <div className={wrapperClass}>
      <DotLottieReact src={src} loop={loop} autoplay={autoplay} {...props} />
    </div>
  );
}

export default ResponsiveLottie;
