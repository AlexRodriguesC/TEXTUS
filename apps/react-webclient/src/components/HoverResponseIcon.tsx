import type { CSSProperties } from "react";
import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

const GROUP_HOVER_CLASSES = {
    button: "group-hover/button:[&_*]:![stroke-width:var(--hover-stroke-width)]",
    "menu-button":
        "group-hover/menu-button:[&_*]:![stroke-width:var(--hover-stroke-width)]",
} as const;

type HoverGroupName = keyof typeof GROUP_HOVER_CLASSES;

interface HoverResponseIconProps {
    lucideIcon: LucideIcon;
    strokeWidth?: number;
    hoverStrokeWidth?: number;
    className?: string;
    groupName?: HoverGroupName;
}

function HoverResponseIcon({
    lucideIcon: Icon,
    strokeWidth = 2,
    hoverStrokeWidth = 2.5,
    className,
    groupName = "button",
}: HoverResponseIconProps) {
    return (
        <Icon
            className={cn(
                "[&_*]:transition-[stroke-width] [&_*]:duration-200",
                GROUP_HOVER_CLASSES[groupName],
                className,
            )}
            style={
                { "--hover-stroke-width": hoverStrokeWidth } as CSSProperties
            }
            strokeWidth={strokeWidth}
        />
    );
}

export default HoverResponseIcon;
