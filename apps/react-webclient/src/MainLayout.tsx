import { MeshGradientBackground } from "@/components/ui/mesh-gradient.tsx";
import { useTheme } from "@/hooks/useTheme.ts";
import {
  SidebarInset,
  SidebarProvider,
} from "@/components/ui/sidebar.tsx";
import { AppSidebar } from "@/components/left-sidebar/Sidebar.tsx";
import {type PropsWithChildren, useRef} from "react";
import useSidebarOpen from "@/hooks/useSidebarOpen.tsx";
import { useMatches } from "@tanstack/react-router"
import {RightSidebar} from "@/components/right-sidebar/RightSidebar.tsx";
import * as React from "react";
import useTutorial from "@/hooks/useTutorial.tsx";
import LayoutHeader from "@/LayoutHeader.tsx";
import useRightSidebarOpen from "@/hooks/useRightSidebarOpen.tsx";
import RightSidebarToggle from "@/RightSidebarToggle.tsx";

interface MainLayoutProps extends PropsWithChildren {
  sidebarEnabled: boolean;
}

export default function MainLayout({
  children,
  sidebarEnabled = true,
}: MainLayoutProps) {
  const { theme } = useTheme();
  const { open, setOpen } = useSidebarOpen();
  const { open: leftOpen, setOpen: setLeftOpen } = useSidebarOpen();
  const { open: rightOpen, setOpen: setRightOpen } = useRightSidebarOpen();

  return (
    <MeshGradientBackground
      colors={
        theme === "dark"
          ? ["#0c4a6e", "#0369a1", "#075985", "#0284c7"]
          : ["#bae6fd", "#7dd3fc", "#93c5fd", "#e0f2fe"]
      }
      backgroundColor={theme === "dark" ? "#0f172a" : "#f0f9ff"}
      speed={1}
    >
      {sidebarEnabled ? (
      <SidebarProvider open={open} onOpenChange={setOpen}>
        <LayoutInner
            leftOpen={leftOpen} setLeftOpen={setLeftOpen}
            rightOpen={rightOpen} setRightOpen={setRightOpen}
        >
          {children}
        </LayoutInner>
      </SidebarProvider>
      ) : (
      <main className="flex justify-center items-center h-full w-full">{children}</main>
      )}
    </MeshGradientBackground>
  );
}

function LayoutInner({
                       leftOpen, setLeftOpen,
                       rightOpen, setRightOpen,
                       children,
                     }: {
  leftOpen: boolean
  setLeftOpen: (v: boolean) => void
  rightOpen: boolean
  setRightOpen: (v: boolean) => void
  children: React.ReactNode
}) {
  const matches = useMatches();
  const isOnSpaceRoute = matches.some((m) => m.routeId === "/main/spaces/$spaceId");
  const { tutorialEnabled } = useTutorial();
  const showRightSidebar = isOnSpaceRoute && !tutorialEnabled;

  const rightTogglePortalRef = useRef<HTMLDivElement>(null);

  return (
      <SidebarProvider open={leftOpen} onOpenChange={setLeftOpen}>
        <AppSidebar />
        <SidebarInset className="bg-transparent flex flex-col flex-1 min-w-0 h-full">
          <LayoutHeader showRightToggle={showRightSidebar} rightTogglePortalRef={rightTogglePortalRef} />
          <main className="flex justify-center items-center h-full w-full min-w-0">
            {children}
          </main>
        </SidebarInset>

        {showRightSidebar && (
            <SidebarProvider open={rightOpen} onOpenChange={setRightOpen} className="w-auto shrink-0">
              <RightSidebarToggle containerRef={rightTogglePortalRef} />
              <RightSidebar />
            </SidebarProvider>
        )}
      </SidebarProvider>
  );
}
