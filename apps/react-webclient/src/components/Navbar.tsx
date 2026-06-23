import Logo from "@/components/Logo.tsx";
import { ModeToggle } from "@/components/ModeToggle.tsx";
import { LanguageComboBox } from "@/components/LanguageComboBox.tsx";
import type { ReactNode } from "react";

type NavbarProps = {
    children?: ReactNode;
};

function Navbar({ children }: NavbarProps) {
    return (
        <div className="flex shrink-0 items-center justify-between px-6 py-4 md:px-16">
            <Logo />
            {children}
            <div className="flex flex-row gap-2">
                <ModeToggle />
                <LanguageComboBox />
            </div>
        </div>
    );
}

export default Navbar;
