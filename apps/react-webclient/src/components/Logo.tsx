import PathConstants from "@/routing/pathConstants.ts";
import { Link } from "@tanstack/react-router";

type LogoProps = {
  size?: "sm" | "md" | "lg";
};

const sizeMap = {
  sm: "h-6 w-6",
  md: "h-8 w-8",
  lg: "h-12 w-12",
};

function Logo({ size = "md" }: LogoProps) {
  return (
    <Link className="flex flex-row items-center gap-2" to={PathConstants.HOME}>
      <img
        src="/book.svg"
        alt="logo"
        className={`${sizeMap[size]} object-contain`}
      />
      <div className="font-playfair font-bold text-muted-foreground">
        TEXTUS
      </div>
    </Link>
  );
}

export default Logo;
