import * as React from "react";
import {useTranslation} from "react-i18next";
import {Button} from "@/components/ui/button.tsx";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {ChevronRight, Plus} from "lucide-react";
import {useSidebar} from "@/components/ui/sidebar.tsx";

interface SidebarActionProps {
    icon: React.ReactNode;
    labelKey: string;
    tooltipKey: string;
    onClick?: () => void;
    backgroundColor?: string;
}

function SidebarAction({ icon, labelKey, tooltipKey, onClick, backgroundColor }: SidebarActionProps) {
    const { t } = useTranslation();
    const { state, openMobile } = useSidebar();
    const open = state === "expanded" || openMobile;

    if (!open) {
        return (
            <Tooltip>
                <TooltipTrigger asChild>
                    <Button
                        variant="secondary"
                        onClick={onClick}
                        style={{ backgroundColor }}
                        className="relative h-10 w-10 flex items-center justify-center rounded-xl bg-muted/40 p-0"
                    >
                        <span>{icon}</span>
                        <span className="absolute -bottom-1 -right-1 flex items-center justify-center rounded-full bg-background border h-4 w-4">
              <Plus className="h-2.5 w-2.5" />
            </span>
                    </Button>
                </TooltipTrigger>
                <TooltipContent>{t(tooltipKey)}</TooltipContent>
            </Tooltip>
        );
    }

    return (
        <Tooltip>
            <TooltipTrigger asChild>
                <Button
                    variant="secondary"
                    onClick={onClick}
                    style={{ backgroundColor }}
                    className="h-auto w-full flex flex-row items-center justify-between gap-3 rounded-xl px-4 py-3 bg-muted/40"
                >
                    <div className="flex flex-row items-center gap-3">
                        <span>{icon}</span>
                        <span className="font-medium">{t(labelKey)}</span>
                    </div>
                    <span className="flex items-center justify-center rounded-full bg-background/60 h-7 w-7 shrink-0">
            <ChevronRight className="h-4 w-4" />
          </span>
                </Button>
            </TooltipTrigger>
            <TooltipContent>{t(tooltipKey)}</TooltipContent>
        </Tooltip>
    );
}

export default SidebarAction;
