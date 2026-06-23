import './i18n/i18n'
import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import {createRouter, RouterProvider} from "@tanstack/react-router";
import {routeTree} from "./routing/routeTree.tsx";
import {TooltipProvider} from "@/components/ui/tooltip.tsx";

const router = createRouter({routeTree})

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <TooltipProvider>
            <RouterProvider router={router}/>
        </TooltipProvider>
    </StrictMode>,
)
