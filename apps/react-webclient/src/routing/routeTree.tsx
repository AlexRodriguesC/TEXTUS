import {createRootRoute, createRoute, Outlet} from '@tanstack/react-router'
import NotFoundPage from '../pages/not-found/NotFoundPage'
import LoginPage from "../pages/login/LoginPage.tsx";
import RegistrationPage from "../pages/registration/RegistrationPage.tsx";
import PathConstants from "./pathConstants.ts";
import Layout from "../Layout.tsx";
import Layout2 from "../Layout2.tsx";

const rootRoute = createRootRoute({
    component: () => (
        <Layout>
            <Outlet/>
        </Layout>
    ),
    notFoundComponent: NotFoundPage,
})

const rootRoutes2 = createRootRoute(({
    component: () => {
        <Layout2>
            <Outlet/>
        </Layout2>
    },
    notFoundComponent: NotFoundPage
}))

const loginRoute = createRoute({
    getParentRoute: () => rootRoutes2,
    path: PathConstants.LOGIN,
    component: LoginPage,
})

const registerRoute = createRoute({
    getParentRoute: () => rootRoutes2,
    path: PathConstants.REGISTER,
    component: RegistrationPage,
})

export const routeTree = rootRoutes2.addChildren([loginRoute, registerRoute])