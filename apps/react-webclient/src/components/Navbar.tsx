import Logo from "@/components/Logo.tsx";
import { ModeToggle } from "@/components/ModeToggle.tsx";
import { LanguageComboBox } from "@/components/LanguageComboBox.tsx";
import type { ReactNode } from "react";
import Profile from "@/components/Profile.tsx";

type NavbarProps = {
  children?: ReactNode;
};

function Navbar({ children }: NavbarProps) {
  return (
    <div className="flex shrink-0 items-center justify-between flex-col md:flex-row">
      <Logo />
      {children}
      <div className="flex flex-row gap-4 items-center">
        <Profile />
        <div className={"flex flex-row"}>
          <LanguageComboBox />
          <ModeToggle />
        </div>
      </div>
    </div>
  );
}

export default Navbar;
