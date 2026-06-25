import {Avatar, AvatarBadge, AvatarFallback} from "@/components/ui/avatar.tsx";
import {UserRound} from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger
} from "@/components/ui/dropdown-menu.tsx";
import {Link} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";
import {useAuth} from "@/hooks/useAuth.tsx";

function Profile() {
    const {user, logout} = useAuth()

    return (
        <div>
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Avatar>
                        <AvatarFallback><UserRound/></AvatarFallback>
                        <AvatarBadge className={user ? "bg-green-600 dark:bg-green-800" : "bg-red-600 dark:bg-red-800"} />
                    </Avatar>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-40" align="start">
                    {user ? (
                        <DropdownMenuGroup>
                            <DropdownMenuLabel>Logged as: {user.username}</DropdownMenuLabel>
                            <DropdownMenuItem onClick={logout}>
                                Logout
                            </DropdownMenuItem>
                        </DropdownMenuGroup>
                    ) : (
                        <DropdownMenuGroup>
                            <DropdownMenuLabel>Not logged</DropdownMenuLabel>
                            <DropdownMenuItem asChild>
                                <Link to={PathConstants.LOGIN}>Login</Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem asChild>
                                <Link to={PathConstants.REGISTER}>Register</Link>
                            </DropdownMenuItem>
                        </DropdownMenuGroup>
                    )}
                </DropdownMenuContent>
            </DropdownMenu>
        </div>
    )
}

export default Profile;