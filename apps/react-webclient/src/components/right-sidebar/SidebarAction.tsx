import * as React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button.tsx";
import { HintOverlay } from "@/components/HintOverlay.tsx";
import {ChevronRight, type LucideIcon, Plus} from "lucide-react";
import { useSidebar } from "@/components/ui/sidebar.tsx";
import { Link } from "@tanstack/react-router";
import HoverResponseIcon from "@/components/HoverResponseIcon.tsx";

interface SidebarActionProps {
  icon: LucideIcon;
  labelKey: string;
  tooltipKey: string;
  to: string;
}

function SidebarAction({ icon, labelKey, tooltipKey, to }: SidebarActionProps) {
  const { t } = useTranslation();
  const { state, openMobile, setOpen, setOpenMobile, isMobile } = useSidebar();
  const open = state === "expanded" || openMobile;

  if (!open) {
    return (
      <HintOverlay
        side="left"
        trigger={
          <Button
            variant="secondary"
            className="relative h-10 w-10 flex items-center justify-center rounded-xl bg-muted/40 p-0"
            asChild
          >
            <Link to={to}>
                <HoverResponseIcon lucideIcon={icon}/>
              <span className="absolute -bottom-1 -right-1 flex items-center justify-center rounded-full bg-background border h-4 w-4">
                <Plus className="h-2.5 w-2.5" />
              </span>
            </Link>
          </Button>
        }
        content={t(tooltipKey)}
      />
    );
  }

  return (
    <HintOverlay
      side="left"
      trigger={
        <Button
          onClick={() => isMobile ? setOpenMobile(false) : setOpen(false)}
          variant="secondary"
          className="h-auto w-full flex flex-row items-center justify-between gap-3 rounded-xl px-4 py-3 bg-muted/40"
          asChild
        >
          <Link to={to}>
            <div className="flex flex-row items-center gap-3">
                <HoverResponseIcon lucideIcon={icon}/>
              <span className="font-medium">{t(labelKey)}</span>
            </div>
            <span className="flex items-center justify-center rounded-full bg-background/60 h-7 w-7 shrink-0">
              <ChevronRight className="h-4 w-4" />
            </span>
          </Link>
        </Button>
      }
      content={t(tooltipKey)}
    />
  );
}

export default SidebarAction;
