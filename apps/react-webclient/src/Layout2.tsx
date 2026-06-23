import Logo from "./components/Logo.tsx";
import * as React from "react";

function Layout2({ children }: { children: React.ReactNode }) {
    return <>
        <main>{children}</main>
    </>
}

export default Layout2;