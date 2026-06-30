import { AuthContext } from "@/context/AuthContext.tsx";
import * as React from "react";

export function useAuth() {
  const ctx = React.useContext(AuthContext);
  if (ctx === undefined) {
    throw new Error("useUser() must be used within a UserProvider");
  }
  return ctx;
}
