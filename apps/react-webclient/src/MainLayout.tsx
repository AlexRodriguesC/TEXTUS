import Navbar from "./Navbar.tsx";
import * as React from "react";

export default function MainLayout({ children }: { children: React.ReactNode }) {
    return (
        <>
            <Navbar />
            <main>{children}</main>
        </>
    )
}