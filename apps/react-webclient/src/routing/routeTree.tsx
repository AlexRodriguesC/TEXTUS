import { createRootRoute, createRoute, Outlet } from '@tanstack/react-router'
import NotFoundPage from '../pages/not-found/NotFoundPage'
import LoginPage from '../pages/login/LoginPage'
import RegistrationPage from '../pages/registration/RegistrationPage'
import PathConstants from './pathConstants'
import MainLayout from '../MainLayout'
import AuthLayout from '../AuthLayout'

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

// Add your future main app pages under mainLayoutRoute
const homeRoute = createRoute({
    getParentRoute: () => mainLayoutRoute,
    path: '/',
    component: () => <div>Home</div>,
})

export const routeTree = rootRoute.addChildren([
    authLayoutRoute.addChildren([loginRoute, registerRoute]),
    mainLayoutRoute.addChildren([homeRoute]),
])