import { MeshGradientBackground } from "@/components/ui/mesh-gradient.tsx";
import { useTheme } from "@/hooks/useTheme.ts";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger, useSidebar,
} from "@/components/ui/sidebar.tsx";
import { AppSidebar } from "@/components/left-sidebar/Sidebar.tsx";
import {type PropsWithChildren} from "react";
import useSidebarOpen from "@/hooks/useSidebarOpen.tsx";
import { useMatches } from "@tanstack/react-router"
import {RightSidebar} from "@/components/right-sidebar/RightSidebar.tsx";
import {PanelLeftIcon, PanelRightIcon} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import Logo from "@/components/left-sidebar/Logo.tsx";
import * as React from "react";
import useTutorial from "@/hooks/useTutorial.tsx";

interface MainLayoutProps extends PropsWithChildren {
  sidebarEnabled: boolean;
}

export default function MainLayout({
  children,
  sidebarEnabled = true,
}: MainLayoutProps) {
  const { theme } = useTheme();
  const { open, setOpen } = useSidebarOpen();

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
        <LayoutInner open={open} setOpen={setOpen}>
          {children}
        </LayoutInner>
      </SidebarProvider>
      ) : (
      <main className="flex justify-center items-center h-full w-full">{children}</main>
      )}
    </MeshGradientBackground>
  );
}

function LayoutInner({ open, setOpen, children }: {
  open: boolean
  setOpen: (v: boolean) => void
  children: React.ReactNode
}) {
  const matches = useMatches();
  const isOnSpaceRoute = matches.some(
      (m) => m.routeId === "/main/spaces/$spaceId"
  )
  const { isMobile, setOpenMobile, openMobile } = useSidebar();
  const { tutorialEnabled } = useTutorial();

  const toggleLeftSidebar = () => {
    if (isMobile) {
      setOpenMobile(!openMobile);
    } else {
      setOpen(!open);
    }
  };

  return (
      <>
        <AppSidebar />
        {isOnSpaceRoute && !tutorialEnabled ? (
            <SidebarProvider>
              <SidebarInset className="bg-transparent">
                <div className="grid grid-cols-3 items-center gap-2 p-1 border-b bg-transparent">
                  <div className="flex justify-start">
                    {isMobile && (
                        <Button className="shrink-0" variant="ghost" size="icon" onClick={toggleLeftSidebar}>
                          <PanelLeftIcon />
                        </Button>
                    )}
                  </div>

                  <div className="flex justify-center">
                    <Logo />
                  </div>

                  <div className="flex justify-end">
                    {isMobile && (
                        <SidebarTrigger className="shrink-0">
                          <PanelRightIcon />
                        </SidebarTrigger>
                    )}
                  </div>
                </div>
                <main className="flex justify-center items-center h-full w-full">
                  {children}
                </main>
              </SidebarInset>
              <RightSidebar />
            </SidebarProvider>
        ) : (
            <SidebarInset className="bg-transparent">
              <div className="grid grid-cols-3 items-center gap-2 p-1 border-b bg-transparent">
                <div className="flex justify-start">
                  {isMobile && (
                      <Button className="shrink-0" variant="ghost" size="icon" onClick={toggleLeftSidebar}>
                        <PanelLeftIcon />
                      </Button>
                  )}
                </div>

                <div className="flex justify-center">
                  <Logo />
                </div>
              </div>
              <main className="flex justify-center items-center h-full w-full">
                {children}
              </main>
            </SidebarInset>
        )}
      </>
  )
}
