import { AuthContext, type User } from "@/context/AuthContext.tsx";
import * as React from "react";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { jwtDecode } from "jwt-decode";

const USERNAME = "USERNAME";
const TOKEN = "TOKEN";
const EXPIRES_AT = "EXPIRES_AT";

function AuthenticationProvider({ children }: React.PropsWithChildren) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const logoutTimerRef = useRef<number | null>(null);

  const clearLogoutTimer = useCallback(() => {
    if (logoutTimerRef.current) {
      window.clearTimeout(logoutTimerRef.current);
      logoutTimerRef.current = null;
    }
  }, []);

  const logout = useCallback(() => {
    clearLogoutTimer();
    setUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem(TOKEN);
    localStorage.removeItem(EXPIRES_AT);
    localStorage.removeItem(USERNAME);
  }, [clearLogoutTimer]);

  const scheduleLogout = useCallback(
    (expiresAtMs: number) => {
      clearLogoutTimer();

      const delay = expiresAtMs - Date.now();

      if (delay <= 0) {
        logout();
        return;
      }

      logoutTimerRef.current = window.setTimeout(() => {
        logout();
      }, delay);
    },
    [clearLogoutTimer, logout],
  );

  const login = useCallback(
    (usr: User) => {
      if (!usr.token) {
        throw new Error(
          "Tried to set invalid user. User is missing authentication token!",
        );
      }

      const decodedToken: { exp?: number } = jwtDecode(usr.token);
      const expiresAtMs = decodedToken.exp
        ? decodedToken.exp * 1000
        : Date.now();

      setUser(usr);
      setIsAuthenticated(true);

      localStorage.setItem(TOKEN, usr.token);
      localStorage.setItem(EXPIRES_AT, String(expiresAtMs));
      localStorage.setItem(USERNAME, usr.username);

      scheduleLogout(expiresAtMs);
    },
    [scheduleLogout],
  );

  useEffect(() => {
    const token = localStorage.getItem(TOKEN);
    const expiresAtMs = Number(localStorage.getItem(EXPIRES_AT) ?? 0);

    if (!token || !expiresAtMs) {
      logout();
      return;
    }

    if (expiresAtMs <= Date.now()) {
      logout();
      return;
    }

    setIsAuthenticated(true);
    scheduleLogout(expiresAtMs);
  }, [logout, scheduleLogout]);

  useEffect(() => {
    return () => clearLogoutTimer();
  }, [clearLogoutTimer]);

  const value = useMemo(
    () => ({ user, isAuthenticated, login, logout }),
    [user, isAuthenticated, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthenticationProvider;
