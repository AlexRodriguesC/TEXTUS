import * as React from "react";
import Navbar from "@/components/Navbar.tsx";
import { MeshGradientBackground } from "@/components/ui/mesh-gradient.tsx";
import { useTheme } from "@/hooks/useTheme.ts";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { theme } = useTheme();

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
      <div className="flex flex-col h-full w-full py-4 px-16">
        <Navbar />
        <main className={"flex justify-center items-center h-full w-full"}>
          {children}
        </main>
      </div>
    </MeshGradientBackground>
  );
}
