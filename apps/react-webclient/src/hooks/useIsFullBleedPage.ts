import PathConstants from "@/routing/pathConstants.ts";
import { useRouterState } from "@tanstack/react-router";

const FULL_BLEED_PATH_PATTERNS = [
  /^\/spaces\/[^/]+\/mind_map$/,
] as const;

function useIsFullBleedPage(): boolean {
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  if (!PathConstants.SPACE_MIND_MAP.startsWith("/spaces/")) {
    return false;
  }

  return FULL_BLEED_PATH_PATTERNS.some((pattern) => pattern.test(currentPath));
}

export default useIsFullBleedPage;
