import {DotLottieReact} from "@lottiefiles/dotlottie-react";
import {useTranslation} from "react-i18next";

function EmptySpaces() {
    const {t} = useTranslation();


    return (
        <div className="flex flex-col items-center">
            <div className={"flex flex-col items-center gap-4 w-sm"}>
                <div className={"md:w-xs xl:w-xl"}>
                    <DotLottieReact
                        src="/animations/think.lottie"
                        loop
                        autoplay
                    />
                </div>
                <p className={"text-center"}>{t("spaces.add-spaces")}</p>
            </div>
        </div>
    )
}

export default EmptySpaces;