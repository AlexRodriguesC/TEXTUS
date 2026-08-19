import { PanelLeftIcon } from "lucide-react";
import Logo from "@/components/Logo.tsx";
import SidebarToggleButton from "@/SidebarToggleButton.tsx";
import * as React from "react";
import { useSidebar } from "@/components/ui/sidebar.tsx";

function LayoutHeader({
  showRightToggle,
  rightTogglePortalRef,
}: {
  showRightToggle: boolean;
  rightTogglePortalRef: React.RefObject<HTMLDivElement | null>;
}) {
  const { isMobile } = useSidebar();

  return (
    <div
      className={
        isMobile
          ? "grid grid-cols-3 items-center gap-2 p-1 border-b bg-transparent"
          : "grid grid-cols-1 items-center gap-2 p-1 border-b bg-transparent"
      }
    >
      {isMobile && (
        <div className="flex justify-start">
          <SidebarToggleButton icon={<PanelLeftIcon />} />
        </div>
      )}
      <div className="flex justify-center">
        <Logo />
      </div>
      {showRightToggle && isMobile && (
        <div className="flex justify-end" ref={rightTogglePortalRef} />
      )}
    </div>
  );
}

export default LayoutHeader;
