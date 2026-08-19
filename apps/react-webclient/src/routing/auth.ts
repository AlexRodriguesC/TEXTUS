import PathConstants from "@/routing/pathConstants.ts";
import { redirect } from "@tanstack/react-router";

const TOKEN = "TOKEN";
const EXPIRES_AT = "EXPIRES_AT";
const USERNAME = "USERNAME";

export function readStoredAuth(): boolean {
  const token = localStorage.getItem(TOKEN);
  const expiresAtMs = Number(localStorage.getItem(EXPIRES_AT) ?? 0);
  const username = localStorage.getItem(USERNAME);

  if (!token || !expiresAtMs || !username) {
    return false;
  }

  return expiresAtMs > Date.now();
}

export function requireAuth() {
  if (!readStoredAuth()) {
    throw redirect({ to: PathConstants.LOGIN });
  }
}
