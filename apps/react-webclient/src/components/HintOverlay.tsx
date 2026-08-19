import {
    Tooltip,
    TooltipTrigger,
    TooltipContent,
} from "@/components/ui/tooltip";
import {
    Popover,
    PopoverTrigger,
    PopoverContent,
} from "@/components/ui/popover";
import * as React from "react";
import {useIsMobile} from "@/hooks/shadcn/useIsMobile.ts";

interface HintOverlayProps {
    trigger: React.ReactNode;
    content?: React.ReactNode;
    className?: string;
    variant?: "hint" | "panel";
    side?: "top" | "right" | "bottom" | "left";
    align?: "start" | "center" | "end";
    open?: boolean;
    onOpenChange?: (open: boolean) => void;
}

export function HintOverlay({
                                trigger,
                                content,
                                className,
                                variant = "hint",
                                side,
                                align,
                                open,
                                onOpenChange,
                            }: HintOverlayProps) {
    const isMobile = useIsMobile();

    if (!content) {
        return <>{trigger}</>;
    }

    if (isMobile) {
        return (
            <Popover open={open} onOpenChange={onOpenChange}>
                <PopoverTrigger asChild>{trigger}</PopoverTrigger>
                <PopoverContent className={className} side={side} align={align}>
                    {content}
                </PopoverContent>
            </Popover>
        );
    }

    return (
        <Tooltip open={open} onOpenChange={onOpenChange}>
            <TooltipTrigger asChild>{trigger}</TooltipTrigger>
            <TooltipContent
                variant={variant === "panel" ? "popover" : "default"}
                className={className}
                side={side}
                align={align}
            >
                {content}
            </TooltipContent>
        </Tooltip>
    );
}
