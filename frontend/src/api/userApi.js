import { apiFetch } from "@/api/http";

export function getProfile() {
  return apiFetch("/api/users/me");
}

export function signup(payload) {
  return apiFetch("/api/users/signup", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function login(email, password, rememberMe) {
  const body = new URLSearchParams();
  body.set("email", email);
  body.set("pw", password);
  if (rememberMe) {
    body.set("remember-me", "on");
  }

  return apiFetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });
}

export function logout() {
  return apiFetch("/api/auth/logout", {
    method: "POST"
  });
}

export function updateProfile(payload) {
  return apiFetch("/api/users/me", {
    method: "PATCH",
    body: JSON.stringify(payload)
  });
}

export function deleteAccount(password) {
  return apiFetch("/api/users/me", {
    method: "DELETE",
    body: JSON.stringify({ password })
  });
}

export function resetPassword(payload) {
  return apiFetch("/api/users/password-reset", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
