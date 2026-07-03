import {DotLottieReact} from "@lottiefiles/dotlottie-react";

interface ResponsiveLottieProps {
    src: string
}

function ResponsiveLottie({src}: ResponsiveLottieProps) {

    return (
        <div className={"md:w-md xl:w-xl"}>
            <DotLottieReact src={src} loop autoplay />
        </div>
    )
}

export default ResponsiveLottie;
