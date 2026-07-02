import { MeshGradientBackground } from "@/components/ui/mesh-gradient.tsx";
import { useTheme } from "@/hooks/useTheme.ts";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar.tsx";
import { AppSidebar } from "@/components/sidebar/Sidebar.tsx";
import { type PropsWithChildren } from "react";
import useSidebarOpen from "@/hooks/useSidebarOpen.tsx";

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
          <AppSidebar />
          <SidebarInset className="bg-transparent">
            <div className="flex items-center gap-2 p-1 border-b bg-transparent">
              <SidebarTrigger />
            </div>
            <main className="flex justify-center items-center h-full w-full">
              {children}
            </main>
          </SidebarInset>
        </SidebarProvider>
      ) : (
        <main className="flex justify-center items-center h-full w-full">
          {children}
        </main>
      )}
    </MeshGradientBackground>
  );
}
