import { apiFetch } from "@/api/http";

export function fetchHotplaces() {
  return apiFetch("/api/hotplaces");
}

export function getHotplaces(region, areaCode) {
  const params = new URLSearchParams();
  if (region) {
    params.set("region", region);
  }
  if (areaCode) {
    params.set("areaCode", String(areaCode));
  }
  const query = params.toString();
  return apiFetch(`/api/hotplaces${query ? `?${query}` : ""}`);
}

export function getHotplacesByRegion(region, areaCode) {
  return getHotplaces(region, areaCode);
}

export function getPopularHotplaces(region, limit = 5, areaCode) {
  const params = new URLSearchParams();
  if (region) {
    params.set("region", region);
  }
  if (areaCode) {
    params.set("areaCode", String(areaCode));
  }
  params.set("limit", String(limit));
  return apiFetch(`/api/hotplaces/popular?${params.toString()}`);
}

export function getMyHotplaces() {
  return apiFetch("/api/hotplaces/me");
}

export function createHotplace(payload) {
  return apiFetch("/api/hotplaces", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function updateHotplace(id, payload) {
  return apiFetch(`/api/hotplaces/${id}`, {
    method: "PATCH",
    body: JSON.stringify(payload)
  });
}

export function deleteHotplace(id) {
  return apiFetch(`/api/hotplaces/${id}`, {
    method: "DELETE"
  });
}
