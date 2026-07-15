import {SidebarGroup} from "@/components/ui/sidebar.tsx";
import PathSettings from "@/components/right-sidebar/settings/PathSettings.tsx";
import {useRouterState} from "@tanstack/react-router";
import PathConstants from "@/routing/pathConstants.ts";

function RightSidebarFooter() {
    const routerState = useRouterState();
    const currentPath = routerState.location.pathname;

    // Match against the dynamic path patterns
    const isMindMap = PathConstants.SPACE_MIND_MAP.startsWith('/spaces/')
        ? currentPath.match(/^\/spaces\/[^/]+\/mind_map$/)
        : false;
    const isLearn = PathConstants.SPACE_LEARN.startsWith('/spaces/')
        ? currentPath.match(/^\/spaces\/[^/]+\/learn$/)
        : false;

    return (
        <SidebarGroup className="flex items-center">
            {isMindMap && (
                <div className="text-muted-foreground">Mind Map placeholder</div>
            )}
            {isLearn && <PathSettings />}
            {!isMindMap && !isLearn && null} {/* default fallback */}
        </SidebarGroup>
    );
}

export default RightSidebarFooter;
