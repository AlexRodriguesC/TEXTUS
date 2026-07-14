const PathConstants = {
  NOT_FOUND: "/not-found",
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  SPACES: "/spaces",
  SPACE_MENU: "/spaces/$spaceId/menu",
  SPACE_CHAT: "/spaces/$spaceId/chat",
  SPACE_MIND_MAP: "/spaces/$spaceId/mind_map",
  SPACE_LEARN: "/spaces/$spaceId/learn",
  SPACE_LEARN_GUIDE: "/spaces/$spaceId/learn/$chapterId/guide",
} as const;

export default PathConstants;
