
import * as React from "react";
import Navbar from "@/components/Navbar.tsx";
import {MeshGradientBackground} from "@/components/ui/mesh-gradient.tsx";
import {useTheme} from "@/hooks/useTheme.tsx";

export default function MainLayout({ children }: { children: React.ReactNode }) {
    const { theme } = useTheme();

    return (
        <MeshGradientBackground colors={
            theme === "dark"
                ? ["#7c3aed", "#2563eb", "#4338ca", "#5b21b6"]
                : ["#a78bfa", "#93c5fd", "#818cf8", "#c4b5fd"]
        }
                                backgroundColor={theme === "dark" ? "#1e1b26" : "#ffffff"}
                                speed={1}>
            <div className="flex min-h-screen flex-col py-2 px-16">
                <Navbar />
                <main className="flex flex-1 items-center justify-center">{children}</main>
            </div>
        </MeshGradientBackground>
    );
}