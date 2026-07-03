import {StepperContent} from "@/components/reui/stepper.tsx";
import * as React from "react";
import {DotLottieReact} from "@lottiefiles/dotlottie-react";
import {Trans, useTranslation} from "react-i18next";

interface StartProps {
    index: number
}

function Start({ index } : StartProps) {
    const { t } = useTranslation();

    return (
        <StepperContent
            key={index}
            value={index + 1}
            className="flex w-full items-center justify-center"
        >
            <div className={"flex flex-col md:flex-row w-full items-center gap-8"}>
                <div className={"w-xs md:w-md xl:w-xl shrink-0"}>
                    <DotLottieReact src="/animations/start.lottie" loop autoplay />
                </div>
                <div className={"flex flex-col items-center md:items-start justify-center gap-8 min-w-0 flex-1"}>
                    <h2 className="text-2xl font-semibold mb-2">
                        {t("start.title")}
                    </h2>
                    <p className="break-words">
                        <Trans
                            i18nKey="start.description"
                            components={{ bold: <strong /> }}
                        />
                    </p>
                </div>
            </div>
        </StepperContent>
    )
}

export default Start;
