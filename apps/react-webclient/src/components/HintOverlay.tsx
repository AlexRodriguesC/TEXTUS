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
    content: React.ReactNode;
    isMobile: boolean;
    className?: string;
    side?: "top" | "right" | "bottom" | "left";
    align?: "start" | "center" | "end";
}

export function HintOverlay({
                                trigger,
                                content,
                                className = "bg-muted/90 text-primary-foreground",
                                side,
                                align,
                            }: HintOverlayProps) {

    const isMobile = useIsMobile();
    if (isMobile) {
        return (
            <Popover>
                <PopoverTrigger asChild>{trigger}</PopoverTrigger>
                <PopoverContent className={className} side={side} align={align}>
                    {content}
                </PopoverContent>
            </Popover>
        );
    }

    return (
        <Tooltip>
            <TooltipTrigger asChild>{trigger}</TooltipTrigger>
            <TooltipContent className={className} side={side} align={align}>
                {content}
            </TooltipContent>
        </Tooltip>
    );
}