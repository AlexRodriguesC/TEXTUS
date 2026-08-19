import PathConstants from "@/routing/pathConstants.ts";
import { Link } from "@tanstack/react-router";
import ResponsiveLottie from "@/components/ResponsiveLottie.tsx";
import type {DotLottie} from "@lottiefiles/dotlottie-react";
import {useState} from "react";

type LogoProps = {
  size?: "sm" | "md" | "lg";
};

const sizeMap = {
  sm: "h-6 w-6",
  md: "h-8 w-8",
  lg: "h-12 w-12",
};

function Logo({ size = "md" }: LogoProps) {
    const [dotLottie, setDotLottie] = useState<DotLottie | null>(null)

    return (
        <Link className="flex flex-row items-center gap-2" to={PathConstants.HOME}>
            <div className={`${sizeMap[size]} shrink-0 overflow-hidden flex items-center justify-center`}>
                <ResponsiveLottie
                    className="h-full w-auto max-h-full object-contain"
                    dotLottieRefCallback={(dotLottie) => {
                        dotLottie?.addEventListener("load", () => {
                            dotLottie.setFrame(dotLottie.totalFrames - 1);
                        });
                        setDotLottie(dotLottie);
                    }}
                    src="/animations/logo.lottie"
                    onMouseEnter={() => {
                        dotLottie?.setFrame(0);
                        dotLottie?.play();
                    }}
                    onMouseLeave={() => {
                        dotLottie?.setFrame(dotLottie?.totalFrames - 1);
                        dotLottie?.pause();
                    }}
                    useFrameInterpolation={true}
                    autoplay={false}
                />
            </div>
            <div className="font-playfair font-bold text-muted-foreground uppercase">
                Textus
            </div>
        </Link>
    );
}

export default Logo;
