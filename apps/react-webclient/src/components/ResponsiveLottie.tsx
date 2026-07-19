import {
  type DotLottie,
  DotLottieReact,
  type DotLottieReactProps,
} from "@lottiefiles/dotlottie-react";
import { useState } from "react";

interface ResponsiveLottieProps {
  src: string;
}

function ResponsiveLottie({
  src,
  loop = true,
  autoplay = true,
  ...props
}: ResponsiveLottieProps & DotLottieReactProps) {
  const [dotLottie, setDotLottie] = useState<DotLottie | null>(null);

  if (!autoplay) {
    return (
      <div className={"md:w-md xl:w-xl"}>
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
    <div className={"md:w-md xl:w-xl"}>
      <DotLottieReact src={src} loop={loop} autoplay={autoplay} {...props} />
    </div>
  );
}

export default ResponsiveLottie;
