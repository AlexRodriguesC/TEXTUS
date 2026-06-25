import {createRouter} from "@tanstack/react-router";
import {routeTree} from "@/routing/routeTree.tsx";

export const router = createRouter({
    routeTree,
    context: {
        auth: undefined!,
    },
});

declare module "@tanstack/react-router" {
    interface Register {
        router: typeof router;
    }
}