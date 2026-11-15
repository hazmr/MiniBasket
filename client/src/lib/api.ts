import "server-only";
import { cookies } from "next/headers";
import { notFound, redirect } from "next/navigation";
import { messageFor, type ErrorCode, type Problem } from "./error-messages";

const API_URL = process.env.API_URL ?? "http://localhost:8080";

export const SESSION_COOKIE = "minibasket_token";

class ApiError extends Error {
  readonly status: number;
  readonly errorCode: ErrorCode;
  readonly params: Record<string, unknown>;
  readonly traceId: string | undefined;

  constructor(problem: Problem) {
    super(messageFor(problem));
    this.status = problem.status;
    this.errorCode = problem.errorCode;
    this.params = problem.params ?? {};
    this.traceId = problem.traceId;
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const token = (await cookies()).get(SESSION_COOKIE)?.value;

  const headers: Record<string, string> = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(`${API_URL}/api/v1${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (response.status === 401 && token) redirect("/logout");

  if (response.status === 204) return undefined as T;

  const data = await response.json().catch(() => null);
  if (!response.ok) throw toApiError(response, data);
  return data as T;
}

function toApiError(response: Response, body: unknown): ApiError {
  const problem = body as Problem | null;
  if (problem?.errorCode) return new ApiError(problem);
  return new ApiError({
    status: response.status,
    errorCode: response.status >= 500 ? "INTERNAL_ERROR" : "BAD_REQUEST",
    traceId: response.headers.get("X-Request-Id") ?? undefined,
  });
}

export const api = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T = void>(path: string, body?: unknown) => request<T>("POST", path, body ?? {}),
  put: <T = void>(path: string, body: unknown) => request<T>("PUT", path, body),
  patch: <T = void>(path: string, body: unknown) => request<T>("PATCH", path, body),
  delete: (path: string) => request<void>("DELETE", path),
};

export type Result<T> = { ok: true; data: T } | { ok: false; error: string };

export async function attempt<T>(call: () => Promise<T>): Promise<Result<T>> {
  try {
    return { ok: true, data: await call() };
  } catch (error) {
    if (error instanceof ApiError) return { ok: false, error: error.message };
    throw error;
  }
}

export async function getOrNotFound<T>(path: string): Promise<T> {
  try {
    return await api.get<T>(path);
  } catch (error) {
    if (error instanceof ApiError && (error.status === 404 || error.status === 400)) notFound();
    throw error;
  }
}
