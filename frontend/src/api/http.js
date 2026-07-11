const JSON_HEADERS = {
  "Content-Type": "application/json"
};

const TOKEN_KEY = "neoulteo.jwt";

let authToken = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || "";

export function getAuthToken() {
  return authToken;
}

export function setAuthToken(token, remember = false) {
  authToken = token || "";
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
  if (!authToken) return;
  const storage = remember ? localStorage : sessionStorage;
  storage.setItem(TOKEN_KEY, authToken);
}

export function clearAuthToken() {
  setAuthToken("");
}

export async function apiFetch(path, options = {}) {
  const headers = options.body instanceof FormData ? { ...options.headers } : { ...JSON_HEADERS, ...options.headers };
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  const response = await fetch(path, {
    credentials: "include",
    headers,
    ...options
  });

  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof data === "object" && data !== null ? data.message : data || "Request failed.";
    throw new Error(message);
  }
  return data;
}
