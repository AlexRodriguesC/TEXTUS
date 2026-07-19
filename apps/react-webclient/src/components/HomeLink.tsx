import PathConstants from "@/routing/pathConstants.ts";
import {HomeIcon} from "lucide-react";
import {Link} from "@tanstack/react-router";

function HomeLink() {
    return (
        <Link to={PathConstants.HOME}>
            <HomeIcon
                size={20}
                style={{ color: "var(--muted-foreground)" }}
            />
        </Link>
    )
}

export default HomeLink;
