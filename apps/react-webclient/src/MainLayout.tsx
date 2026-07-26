import { MeshGradientBackground } from "@/components/ui/mesh-gradient.tsx";
import { useTheme } from "@/hooks/context/useTheme.ts";
import { SidebarProvider } from "@/components/ui/sidebar.tsx";
import { type PropsWithChildren } from "react";
import useSidebarOpen from "@/hooks/context/useSidebarOpen.tsx";
import useRightSidebarOpen from "@/hooks/context/useRightSidebarOpen.tsx";
import { AppShell } from "@/layouts/AppShell.tsx";

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
          <AppShell
            leftOpen={leftOpen}
            setLeftOpen={setLeftOpen}
            rightOpen={rightOpen}
            setRightOpen={setRightOpen}
          >
            {children}
          </AppShell>
        </SidebarProvider>
      ) : (
        <main className="flex justify-center items-center h-full w-full">
          {children}
        </main>
      )}
    </MeshGradientBackground>
  );
}
