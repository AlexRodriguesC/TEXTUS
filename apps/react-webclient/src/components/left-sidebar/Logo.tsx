import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

function Logo() {
  return (
    <Link
      className={`flex items-center gap-2 flex-row`}
      to={PathConstants.HOME}
    >
      <img
        src="/book.svg"
        alt="logo"
        className={`h-8 w-8 object-contain`}
      />
      <div className="font-playfair font-bold text-muted-foreground">
        TEXTUS
      </div>
    </Link>
  );
}

export default Logo;
