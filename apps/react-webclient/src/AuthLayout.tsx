import Logo from "./components/Logo.tsx";
import * as React from "react";
import {LanguageComboBox} from "./components/LanguageComboBox.tsx";
import {ModeToggle} from "@/components/ModeToggle.tsx";

function AuthLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="flex min-h-screen flex-col overflow-hidden overflow-y-hidden">
            <div className="flex shrink-0 items-center justify-between px-6 py-4 md:px-16">
                <Logo />
                <div className={"flex flex-row gap-2"}>
                    <ModeToggle />
                    <LanguageComboBox />
                </div>
            </div>
            <main className="flex flex-1 items-center justify-center">
                {children}
            </main>
        </div>
    )
}

export default AuthLayout;