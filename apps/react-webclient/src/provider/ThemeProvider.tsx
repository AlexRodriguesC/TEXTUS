import { type ReactNode, useCallback, useMemo, useState } from "react";
import { ThemeContext, type Theme } from "@/context/ThemeContext.tsx";

function applyThemeToDocument(next: Theme) {
  document.documentElement.classList.toggle("dark", next === "dark");
  localStorage.setItem("theme", next);
}

function readInitialTheme(): Theme {
  const stored = localStorage.getItem("theme");
  if (stored === "light" || stored === "dark") {
    return stored;
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(() => {
    const initial = readInitialTheme();
    applyThemeToDocument(initial);
    return initial;
  });

  const setTheme = useCallback((next: Theme) => {
    applyThemeToDocument(next);
    setThemeState(next);
  }, []);

  const toggleTheme = useCallback(() => {
    setThemeState((current) => {
      const next = current === "light" ? "dark" : "light";
      applyThemeToDocument(next);
      return next;
    });
  }, []);

  const value = useMemo(
    () => ({ theme, setTheme, toggleTheme }),
    [theme, setTheme, toggleTheme],
  );

  return (
    <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
  );
}
