import {createRootRoute, createRoute, Outlet, redirect} from '@tanstack/react-router'
import NotFoundPage from '../pages/not-found/NotFoundPage'
import LoginPage from '../pages/login/LoginPage'
import RegistrationPage from '../pages/registration/RegistrationPage'
import PathConstants from './pathConstants'
import MainLayout from '../MainLayout'
import HomePage from "@/pages/HomePage.tsx";
import SpacesPage from "@/pages/spaces/SpacesPage.tsx";
import SpacePage from "@/pages/space/SpacePage.tsx";

// Single root route - no layout here
const rootRoute = createRootRoute({
    notFoundComponent: NotFoundPage,
})

// MainLayout route for auth pages (login, register)
const publicRoute = createRoute({
    getParentRoute: () => rootRoute,
    id: 'auth',
    component: () => (
        <MainLayout>
            <Outlet />
        </MainLayout>
    ),
})

// MainLayout route for main app pages
const protectedRoute = createRoute({
    getParentRoute: () => rootRoute,
    id: 'main',
    beforeLoad: ({context, location}) => {
        if (!context.auth.isAuthenticated) {
            throw redirect({
                to: PathConstants.LOGIN,
                search: {
                    redirect: location.href,
                },
            })
        }
    },
    component: () => (
        <MainLayout>
            <Outlet />
        </MainLayout>
    ),
})

const loginRoute = createRoute({
    getParentRoute: () => publicRoute,
    path: PathConstants.LOGIN,
    component: LoginPage,
})

const registerRoute = createRoute({
    getParentRoute: () => publicRoute,
    path: PathConstants.REGISTER,
    component: RegistrationPage,
})

const homeRoute = createRoute({
    getParentRoute: () => publicRoute,
    path: PathConstants.HOME,
    component: HomePage,
})

const spacesRoute = createRoute({
    getParentRoute: () => protectedRoute,
    path: PathConstants.SPACES,
    component: SpacesPage
})

const spaceRoute = createRoute({
    getParentRoute: () => protectedRoute,
    path: PathConstants.SPACE,
    component: SpacePage
})

export const routeTree = rootRoute.addChildren([
    publicRoute.addChildren([loginRoute, registerRoute, homeRoute]),
    protectedRoute.addChildren([spacesRoute, spaceRoute]),
])