import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import { useSidebar } from "@/components/ui/sidebar.tsx";

const sizeMap = {
  open: "h-8 w-8",
  closed: "h-6 w-6",
};

function SidebarLogo() {
  const { state, openMobile } = useSidebar();
  const open = state === "expanded" || openMobile;

  return (
    <Link
      className={`flex items-center gap-2 ${open ? "flex-row" : "flex-col justify-center"}`}
      to={PathConstants.HOME}
    >
      <img
        src="/book.svg"
        alt="logo"
        className={`${open ? sizeMap.open : sizeMap.closed} object-contain`}
      />
      {open && (
        <div className="font-playfair font-bold text-muted-foreground">
          TEXTUS
        </div>
      )}
    </Link>
  );
}

export default SidebarLogo;
