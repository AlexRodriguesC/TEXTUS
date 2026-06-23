import * as React from "react";
import Navbar from "@/components/Navbar.tsx";

function AuthLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col px-4 sm:px-6 lg:px-8 py-2">
            <Navbar/>
            <main className="flex flex-1 items-center justify-center">
                {children}
            </main>
        </div>
    )
}

export default AuthLayout;