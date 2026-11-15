import "server-only";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { cache } from "react";
import { api, SESSION_COOKIE } from "./api";
import type { AuthResponse, User } from "./types";

export async function saveSession(auth: AuthResponse) {
  (await cookies()).set(SESSION_COOKIE, auth.accessToken, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: auth.expiresInSeconds,
  });
}

export async function clearSession() {
  (await cookies()).delete(SESSION_COOKIE);
}

export async function hasSession() {
  return (await cookies()).has(SESSION_COOKIE);
}

export const getCurrentUser = cache(async (): Promise<User | null> => {
  if (!(await hasSession())) return null;
  return api.get<User>("/users/me");
});

export async function requireUser(returnTo?: string): Promise<User> {
  const user = await getCurrentUser();
  if (!user) redirect(returnTo ? `/login?next=${encodeURIComponent(returnTo)}` : "/login");
  return user;
}

export async function requireAdmin(): Promise<User> {
  const user = await requireUser();
  if (user.role !== "ADMIN") redirect("/");
  return user;
}

export function safeRedirectPath(value: FormDataEntryValue | string | null | undefined): string {
  const path = typeof value === "string" ? value : "";
  return path.startsWith("/") && !path.startsWith("//") ? path : "/";
}
