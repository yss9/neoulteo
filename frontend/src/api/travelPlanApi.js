import { apiFetch } from "@/api/http";

export function fetchPlans() {
  return apiFetch("/api/plans");
}

export function fetchPlanById(id) {
  return apiFetch(`/api/plans/${id}`);
}

export function fetchSharedPlan(id) {
  return apiFetch(`/api/plans/shared/${encodeURIComponent(id)}`);
}

function normalizePlanPayload(payload) {
  return Array.isArray(payload) ? { places: payload } : payload;
}

export function savePlan(payload) {
  return apiFetch("/api/plans", {
    method: "POST",
    body: JSON.stringify(normalizePlanPayload(payload))
  });
}

export function updatePlan(id, payload) {
  return apiFetch(`/api/plans/${id}`, {
    method: "PATCH",
    body: JSON.stringify(normalizePlanPayload(payload))
  });
}

export function deletePlan(id) {
  return apiFetch(`/api/plans/${id}`, {
    method: "DELETE"
  });
}

export function updatePlanSharing(id, shared) {
  return apiFetch(`/api/plans/${id}/share`, {
    method: "PATCH",
    body: JSON.stringify({ shared })
  });
}

export function copySharedPlan(id) {
  return apiFetch(`/api/plans/${id}/copy`, {
    method: "POST"
  });
}
