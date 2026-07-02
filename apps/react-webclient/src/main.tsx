import "./i18n/i18n";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { ThemeProvider } from "@/provider/ThemeProvider.tsx";
import { Toaster } from "@/components/ui/sonner.tsx";
import { TooltipProvider } from "@/components/ui/tooltip.tsx";
import AuthenticationProvider from "@/provider/AuthenticationProvider.tsx";
import { ReactFlowProvider } from "@xyflow/react";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <TooltipProvider>
      <ThemeProvider>
        <AuthenticationProvider>
          <ReactFlowProvider>
            <Toaster />
            <App />
          </ReactFlowProvider>
        </AuthenticationProvider>
      </ThemeProvider>
    </TooltipProvider>
  </StrictMode>,
);
