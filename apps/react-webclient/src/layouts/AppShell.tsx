import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar.tsx";
import { AppSidebar } from "@/components/left-sidebar/AppSidebar.tsx";
import { RightSidebar } from "@/components/right-sidebar/RightSidebar.tsx";
import LayoutHeader from "@/LayoutHeader.tsx";
import RightSidebarToggle from "@/RightSidebarToggle.tsx";
import { ChatProvider } from "@/provider/ChatProvider.tsx";
import useSpaceId from "@/hooks/useSpaceId.ts";
import useTutorial from "@/hooks/context/useTutorial.tsx";
import useIsFullBleedPage from "@/hooks/useIsFullBleedPage.ts";
import { PageContainer } from "@/layouts/PageContainer.tsx";
import { useRef, type ReactNode } from "react";
import * as React from "react";

interface AppShellProps {
  leftOpen: boolean;
  setLeftOpen: (open: boolean) => void;
  rightOpen: boolean;
  setRightOpen: (open: boolean) => void;
  children: ReactNode;
}

function AppShell({
  leftOpen,
  setLeftOpen,
  rightOpen,
  setRightOpen,
  children,
}: AppShellProps) {
  const { spaceId } = useSpaceId();
  const { tutorialEnabled } = useTutorial();
  const isFullBleed = useIsFullBleedPage();
  const showRightSidebar = spaceId !== undefined && !tutorialEnabled;
  const chatEnabled = spaceId !== undefined;
  const rightTogglePortalRef = useRef<HTMLDivElement>(null);

  const layout = (
    <>
      <AppSidebar />
      <SidebarInset className="bg-transparent flex flex-col flex-1 min-w-0 h-full">
        <LayoutHeader
          showRightToggle={showRightSidebar}
          rightTogglePortalRef={rightTogglePortalRef}
        />
        <main className="flex justify-center h-full w-full min-w-0 min-h-0 overflow-hidden">
          <PageContainer variant={isFullBleed ? "full" : "default"}>
            {children}
          </PageContainer>
        </main>
      </SidebarInset>

      {showRightSidebar && (
        <RightSidebarShell
          rightOpen={rightOpen}
          setRightOpen={setRightOpen}
          togglePortalRef={rightTogglePortalRef}
        />
      )}
    </>
  );

  return (
    <SidebarProvider
      open={leftOpen}
      onOpenChange={setLeftOpen}
      className="h-svh min-h-0 overflow-hidden"
    >
      {chatEnabled ? <ChatProvider>{layout}</ChatProvider> : layout}
    </SidebarProvider>
  );
}

interface RightSidebarShellProps {
  rightOpen: boolean;
  setRightOpen: (open: boolean) => void;
  togglePortalRef: React.RefObject<HTMLDivElement | null>;
}

function RightSidebarShell({
  rightOpen,
  setRightOpen,
  togglePortalRef,
}: RightSidebarShellProps) {
  return (
    <SidebarProvider
      open={rightOpen}
      onOpenChange={setRightOpen}
      className="w-auto shrink-0 h-svh min-h-0 overflow-hidden"
    >
      <RightSidebarToggle containerRef={togglePortalRef} />
      <RightSidebar />
    </SidebarProvider>
  );
}

export { AppShell, RightSidebarShell };
