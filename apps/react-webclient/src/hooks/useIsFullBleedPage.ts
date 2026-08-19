import { useRouterState } from "@tanstack/react-router";

const FULL_BLEED_PATH_PATTERNS = [
  /^\/spaces\/[^/]+\/mind_map$/,
  /^\/spaces\/[^/]+\/menu$/,
] as const;

function useIsFullBleedPage(): boolean {
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  return FULL_BLEED_PATH_PATTERNS.some((pattern) => pattern.test(currentPath));
}

export default useIsFullBleedPage;
