import Navbar from "./Navbar.tsx";
import * as React from "react";

export default function Layout({ children }: { children: React.ReactNode }) {
    return (
        <>
            <Navbar />
            <main>{children}</main>
        </>
    )
}