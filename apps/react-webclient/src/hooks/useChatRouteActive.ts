import PathConstants from "@/routing/pathConstants.ts";
import { useRouterState } from "@tanstack/react-router";
import { useSidebar } from "@/components/ui/sidebar.tsx";

function useChatRouteActive(): boolean {
  const { state, openMobile } = useSidebar();
  const sidebarOpen = state === "expanded" || openMobile;
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  const isChatRoute = PathConstants.SPACE_CHAT.startsWith("/spaces/")
    ? currentPath.match(/^\/spaces\/[^/]+\/chat$/)
    : false;

  return Boolean(isChatRoute && sidebarOpen);
}

export default useChatRouteActive;
