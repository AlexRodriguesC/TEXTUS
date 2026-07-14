import {
    Stepper,
    StepperIndicator,
    StepperItem,
    StepperNav,
    StepperPanel,
    StepperSeparator,
    StepperTitle,
    StepperTrigger,
} from "@/components/reui/stepper"
import {CheckIcon, FileUp, Hand, LoaderCircleIcon, Play} from 'lucide-react'
import {useTranslation} from "react-i18next";
import {useMemo, useState} from "react";
import {Button} from "@/components/ui/button.tsx";
import * as React from "react";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useDocuments from "@/hooks/useDocuments.ts";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {cn} from "@/lib/utils.ts";
import Introduction from "@/pages/space/tutorial/step/Introduction.tsx";
import Upload from "@/pages/space/tutorial/step/Upload.tsx";
import Start from "@/pages/space/tutorial/step/Start.tsx";

interface Step {
    indicator: React.ReactNode,
    title: string,
    component: React.ReactNode
}

interface EmptySpace {
    onCompleted?: () => void;
}

function EmptySpace({ onCompleted }: EmptySpace) {
    const { t } = useTranslation();
    const [currentIndex, setCurrentIndex] = useState(1);
    const { spaceId } = useSpaceId();
    const { documents } = useDocuments(spaceId);

    const introduction = useMemo(() : Step => {
        return {indicator: <Hand size={18}/> , title: t("space.stepper.introduction.title"), component: <Introduction index={0}/>};
    }, []);

    const uploadFiles = useMemo(() : Step => {
        return {indicator: <FileUp size={18}/>,  title: t("space.stepper.upload.title"), component: <Upload index={1}/>};
    }, []);

    const start = useMemo(() : Step => {
        return {indicator: <Play size={18}/>, title: t("space.stepper.start.title"), component: <Start index={2}/>};
    }, []);

    const steps = useMemo(() => {
        return [introduction, uploadFiles, start]
    }, [introduction, uploadFiles, start])

    const isDisabled =
        (currentIndex === steps.length && !onCompleted) ||
        (currentIndex === steps.length && documents.length === 0);

    return (
        <Stepper
            defaultValue={currentIndex}
            value={currentIndex}
            onValueChange={setCurrentIndex}
            indicators={{
                completed: <CheckIcon className="size-3.5" />,
                loading: <LoaderCircleIcon className="size-3.5 animate-spin" />
            }}
            className="w-full h-125 space-y-8 flex flex-col overflow-hidden"
        >
            <StepperNav className="shrink-0">
                {steps.map((step, index) => (
                    <StepperItem key={index} step={index + 1} className="relative flex-1 items-start">
                        <StepperTrigger className="flex flex-col gap-2.5">
                            <StepperIndicator>{step.indicator}</StepperIndicator>
                            <StepperTitle>{step.title}</StepperTitle>
                        </StepperTrigger>
                        {steps.length > index + 1 && (
                            <StepperSeparator className="group-data-[state=completed]/step:bg-primary absolute inset-x-0 top-2.5 left-[calc(50%+0.875rem)] m-0 group-data-[orientation=horizontal]/stepper-nav:w-[calc(100%-2rem+0.225rem)] group-data-[orientation=horizontal]/stepper-nav:flex-none" />
                        )}
                    </StepperItem>
                ))}
            </StepperNav>

            <StepperPanel className="text-sm flex-1 min-h-0 min-w-0 flex flex-col overflow-hidden">
                <div className="relative flex-1 min-h-0 overflow-auto">
                    {steps.map((step, index) => (
                        <div
                            key={index}
                            className={cn(
                                "absolute inset-0 flex items-center justify-center",
                                currentIndex === index + 1 ? "opacity-100 z-10" : "opacity-0 z-0 pointer-events-none"
                            )}
                        >
                            {step.component}
                        </div>
                    ))}
                </div>

                <div className="flex flex-row gap-16 place-content-center shrink-0 pt-4">
                    <Button
                        variant="outline"
                        onClick={() => setCurrentIndex(Math.max(currentIndex - 1, 1))}
                        disabled={currentIndex === 1}
                        className="shrink-0"
                    >
                        {t("button.back")}
                    </Button>
                    <Tooltip>
                        <TooltipTrigger asChild>
                    <span className="inline-block">
                        <Button
                            onClick={() => {
                                if (isDisabled) return;
                                if (currentIndex !== steps.length) {
                                    setCurrentIndex(Math.min(currentIndex + 1, steps.length));
                                    return;
                                }
                                if (onCompleted) {
                                    onCompleted();
                                }
                            }}
                            aria-disabled={isDisabled}
                            className={cn("shrink-0", isDisabled && "opacity-50 pointer-events-none")}
                            tabIndex={isDisabled ? -1 : 0}
                        >
                            {currentIndex === steps.length ? t("button.start") : t("button.next")}
                        </Button>
                    </span>
                        </TooltipTrigger>
                        {isDisabled && (
                            <TooltipContent>{t("space.stepper.prerequisite")}</TooltipContent>
                        )}
                    </Tooltip>
                </div>
            </StepperPanel>
        </Stepper>
    )
}

export default EmptySpace;
