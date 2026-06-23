import { createRootRoute, createRoute, Outlet } from '@tanstack/react-router'
import NotFoundPage from '../pages/not-found/NotFoundPage'
import LoginPage from '../pages/login/LoginPage'
import RegistrationPage from '../pages/registration/RegistrationPage'
import PathConstants from './pathConstants'
import MainLayout from '../MainLayout'
import AuthLayout from '../AuthLayout'
import HomePage from "@/pages/HomePage.tsx";

// Single root route - no layout here
const rootRoute = createRootRoute({
    notFoundComponent: NotFoundPage,
})

// MainLayout route for auth pages (login, register)
const authLayoutRoute = createRoute({
    getParentRoute: () => rootRoute,
    id: 'auth',
    component: () => (
        <AuthLayout>
            <Outlet />
        </AuthLayout>
    ),
})

// MainLayout route for main app pages
const mainLayoutRoute = createRoute({
    getParentRoute: () => rootRoute,
    id: 'main',
    component: () => (
        <MainLayout>
            <Outlet />
        </MainLayout>
    ),
})

const loginRoute = createRoute({
    getParentRoute: () => authLayoutRoute,
    path: PathConstants.LOGIN,
    component: LoginPage,
})

const registerRoute = createRoute({
    getParentRoute: () => authLayoutRoute,
    path: PathConstants.REGISTER,
    component: RegistrationPage,
})

const homeRoute = createRoute({
    getParentRoute: () => mainLayoutRoute,
    path: '/',
    component: HomePage,
})

export const routeTree = rootRoute.addChildren([
    authLayoutRoute.addChildren([loginRoute, registerRoute]),
    mainLayoutRoute.addChildren([homeRoute]),
])