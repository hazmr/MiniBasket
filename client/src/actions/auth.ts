"use server";

import { redirect } from "next/navigation";
import { api, attempt } from "@/lib/api";
import type { ActionState } from "@/lib/action-state";
import { text } from "@/lib/format";
import { clearSession, safeRedirectPath, saveSession } from "@/lib/session";
import type { AuthResponse } from "@/lib/types";

export async function login(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post<AuthResponse>("/auth/login", {
      email: text(formData, "email"),
      password: text(formData, "password"),
    }),
  );
  if (!result.ok) return { error: result.error };

  await saveSession(result.data);
  redirect(safeRedirectPath(formData.get("next")));
}

export async function register(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post("/auth/register", {
      email: text(formData, "email"),
      password: text(formData, "password"),
    }),
  );
  if (!result.ok) return { error: result.error };

  redirect("/verify-email?registered=1");
}

export async function verifyEmail(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() => api.post("/auth/verify-email", { token: text(formData, "token") }));
  if (!result.ok) return { error: result.error };

  redirect("/login?verified=1");
}

export async function forgotPassword(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() => api.post("/auth/forgot-password", { email: text(formData, "email") }));
  if (!result.ok) return { error: result.error };

  return { message: "If that email has an account, we've emailed it a link to reset the password." };
}

export async function resetPassword(_: ActionState, formData: FormData): Promise<ActionState> {
  const result = await attempt(() =>
    api.post("/auth/reset-password", {
      token: text(formData, "token"),
      newPassword: text(formData, "newPassword"),
    }),
  );
  if (!result.ok) return { error: result.error };

  redirect("/login?reset=1");
}

export async function logout() {
  await clearSession();
  redirect("/");
}
