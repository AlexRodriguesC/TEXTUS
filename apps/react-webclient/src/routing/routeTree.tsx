import { createRootRoute, createRoute, Outlet } from "@tanstack/react-router";
import NotFoundPage from "../pages/not-found/NotFoundPage";
import LoginPage from "../pages/login/LoginPage";
import RegistrationPage from "../pages/registration/RegistrationPage";
import PathConstants from "./pathConstants";
import MainLayout from "../MainLayout";
import HomePage from "@/pages/HomePage.tsx";
import SpacesPage from "@/pages/spaces/SpacesPage.tsx";
import ProtectedRoute from "@/pages/ProtectedRoute.tsx";
import SpaceMenu from "@/pages/space/SpaceMenu.tsx";
import SpaceChat from "@/pages/space/SpaceChat.tsx";
import SpaceMap from "@/pages/space/SpaceMap.tsx";
import SpaceLearn from "@/pages/space/learn/SpaceLearn.tsx";

const rootRoute = createRootRoute({
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
});

const spaceMenuRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_MENU,
  component: SpaceMenu
})

const spaceChatRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_CHAT,
  component: SpaceChat
})

const spaceMapRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_MIND_MAP,
  component: SpaceMap
})

const spaceLearnRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.SPACE_LEARN,
  component: SpaceLearn
})

const notFoundRoute = createRoute({
  getParentRoute: () => protectedRoute,
  path: PathConstants.NOT_FOUND,
  component: NotFoundPage,
});

export const routeTree = rootRoute.addChildren([
  publicRoute.addChildren([homeRoute, notFoundRoute]),
  authRoute.addChildren([loginRoute, registerRoute]),
  protectedRoute.addChildren([spacesRoute, spaceMenuRoute, spaceChatRoute, spaceMapRoute, spaceLearnRoute]),
]);
