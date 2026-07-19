import { useSidebar } from "@/components/ui/sidebar.tsx";
import { Button } from "@/components/ui/button.tsx";

function SidebarToggleButton({ icon }: { icon: React.ReactNode }) {
  const { isMobile, open, setOpen, openMobile, setOpenMobile } = useSidebar();

  const toggle = () => {
    if (isMobile) {
      setOpenMobile(!openMobile);
    } else {
      setOpen(!open);
    }
  };

  return (
    <Button className="shrink-0" variant="ghost" size="icon" onClick={toggle}>
      {icon}
    </Button>
  );
}

export default SidebarToggleButton;
