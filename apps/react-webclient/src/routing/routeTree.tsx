import {
  createRootRouteWithContext,
  createRoute,
  Outlet,
} from "@tanstack/react-router";
import type { QueryClient } from "@tanstack/react-query";
import NotFoundPage from "../pages/not-found/NotFoundPage";
import LoginPage from "@/pages/auth/LoginPage.tsx";
import RegistrationPage from "@/pages/auth/registration/RegistrationPage";
import PathConstants from "./pathConstants";
import MainLayout from "../MainLayout";
import HomePage from "@/pages/HomePage.tsx";
import SpacesPage from "@/pages/spaces/SpacesPage.tsx";
import ProtectedRoute from "@/pages/ProtectedRoute.tsx";
import SpaceMenu from "@/pages/space/menu/SpaceMenu.tsx";
import SpaceChat from "@/pages/space/chat/SpaceChat.tsx";
import SpaceMap from "@/pages/space/SpaceMap.tsx";
import SpaceLearn from "@/pages/space/learn/SpaceLearn.tsx";
import ChapterGuide from "@/pages/space/learn/ChapterGuide.tsx";
import Lesson from "@/pages/space/learn/lesson/Lesson.tsx";
import TopicsPage from "@/pages/space/topics/TopicsPage.tsx";
import {
  ensureChapterGuideData,
  ensureLessonData,
  ensureSpaceChatData,
  ensureSpaceLearnData,
  ensureSpaceMapData,
  ensureSpaceMenuData,
  ensureSpaceTopicsData,
  ensureSpaces,
} from "@/routing/loaders/routeLoaders.ts";

const rootRoute = createRootRouteWithContext<{ queryClient: QueryClient }>()({
  notFoundComponent: NotFoundPage,
});

// MainLayout route for auth pages (login, register)
const publicRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: "public",
  component: () => (
    <MainLayout sidebarEnabled={true}>
      <Outlet />
    </MainLayout>
  ),
});

const authRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: "auth",
  component: () => (
    <MainLayout sidebarEnabled={false}>
      <Outlet />
    </MainLayout>
  ),
});

// MainLayout route for main app pages
const protectedRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: "main",
  component: () => (
    <ProtectedRoute>
      <MainLayout sidebarEnabled={true}>
        <Outlet />
      </MainLayout>
    </ProtectedRoute>
  ),
});

const loginRoute = createRoute({
  getParentRoute: () => authRoute,
  path: PathConstants.LOGIN,
  component: LoginPage,
});

const registerRoute = createRoute({
  getParentRoute: () => authRoute,
  path: PathConstants.REGISTER,
  component: RegistrationPage,
});

const homeRoute = createRoute({
  getParentRoute: () => publicRoute,
  path: PathConstants.HOME,
  component: HomePage,
});

const spacesRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACES,
  component: SpacesPage,
  loader: ({ context: { queryClient } }) => {
    void ensureSpaces(queryClient);
  },
});

const spaceMenuRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_MENU,
  component: SpaceMenu,
  loader: ({ context: { queryClient }, params }) => {
    void ensureSpaceMenuData(queryClient, Number(params.spaceId));
  },
});

const spaceChatRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_CHAT,
  component: SpaceChat,
  loader: ({ context: { queryClient }, params }) => {
    void ensureSpaceChatData(queryClient, Number(params.spaceId));
  },
});

const spaceTopicsRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_TOPICS,
  component: TopicsPage,
  validateSearch: (search: Record<string, unknown>): { q?: string } => ({
    q: typeof search.q === "string" ? search.q : undefined,
  }),
  loader: ({ context: { queryClient }, params }) => {
    void ensureSpaceTopicsData(queryClient, Number(params.spaceId));
  },
});

const spaceMapRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_MIND_MAP,
  component: SpaceMap,
  loader: ({ context: { queryClient }, params }) => {
    void ensureSpaceMapData(queryClient, Number(params.spaceId));
  },
});

const spaceLearnRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_LEARN,
  component: SpaceLearn,
  loader: ({ context: { queryClient }, params }) => {
    void ensureSpaceLearnData(queryClient, Number(params.spaceId));
  },
});

const spaceChapterGuideRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_LEARN_GUIDE,
  component: ChapterGuide,
  loader: ({ context: { queryClient }, params }) => {
    void ensureChapterGuideData(queryClient, Number(params.spaceId));
  },
});

const lessonRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.LESSON,
  component: Lesson,
  loader: ({ context: { queryClient }, params }) => {
    void ensureLessonData(
      queryClient,
      Number(params.spaceId),
      Number(params.lessonId),
    );
  },
});

const notFoundRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.NOT_FOUND,
  component: NotFoundPage,
});

export const routeTree = rootRoute.addChildren([
  publicRoute.addChildren([homeRoute, notFoundRoute]),
  authRoute.addChildren([loginRoute, registerRoute]),
  protectedRoute.addChildren([
    spacesRoute,
    spaceMenuRoute,
    spaceChatRoute,
    spaceTopicsRoute,
    spaceMapRoute,
    spaceLearnRoute,
    spaceChapterGuideRoute,
    lessonRoute,
  ]),
]);
