import {
  Avatar,
  AvatarBadge,
  AvatarFallback,
} from "@/components/ui/avatar.tsx";
import { UserRound } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu.tsx";
import { Link } from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { useTranslation } from "react-i18next";

function Profile() {
  const { t } = useTranslation();
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <div>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Avatar>
            <AvatarFallback>
              <UserRound />
            </AvatarFallback>
            <AvatarBadge
              className={
                isAuthenticated
                  ? "bg-green-600 dark:bg-green-800"
                  : "bg-red-600 dark:bg-red-800"
              }
            />
          </Avatar>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-40" align="start">
          {isAuthenticated && user ? (
            <DropdownMenuGroup>
              <DropdownMenuLabel>
                {t("profile.logged-as", { username: user.username })}
              </DropdownMenuLabel>
              <DropdownMenuItem onClick={logout}>
                {t("logout")}
              </DropdownMenuItem>
            </DropdownMenuGroup>
          ) : (
            <DropdownMenuGroup>
              <DropdownMenuLabel>
                {t("profile.not-logged-in")}
              </DropdownMenuLabel>
              <DropdownMenuItem asChild>
                <Link to={PathConstants.LOGIN}>{t("login.login")}</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link to={PathConstants.REGISTER}>
                  {t("registration.register")}
                </Link>
              </DropdownMenuItem>
            </DropdownMenuGroup>
          )}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}

export default Profile;
