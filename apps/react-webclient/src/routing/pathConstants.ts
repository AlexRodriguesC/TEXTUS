const PathConstants = {
  NOT_FOUND: "/not-found",
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  SPACES: "/spaces",
  SPACE: "/spaces/$spaceId",
  SPACE_MENU: "/spaces/$spaceId/menu",
  SPACE_CHAT: "/spaces/$spaceId/chat",
  SPACE_MIND_MAP: "/spaces/$spaceId/mind_map"
} as const;

export default PathConstants;
