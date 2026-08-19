import { createPortal } from "react-dom";
import { useSidebar } from "@/components/ui/sidebar.tsx";
import { Button } from "@/components/ui/button.tsx";
import { PanelRightIcon } from "lucide-react";
import * as React from "react";

function RightSidebarToggle({
  containerRef,
}: {
  containerRef: React.RefObject<HTMLDivElement | null>;
}) {
  const { isMobile, open, setOpen, openMobile, setOpenMobile } = useSidebar();

  const toggle = () => {
    if (isMobile) {
      setOpenMobile(!openMobile);
    } else {
      setOpen(!open);
    }
  };

  if (!containerRef.current) return null;

  return createPortal(
    <Button className="shrink-0" variant="ghost" size="icon" onClick={toggle}>
      <PanelRightIcon />
    </Button>,
    containerRef.current,
  );
}

export default RightSidebarToggle;
