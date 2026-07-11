import { apiFetch } from "@/api/http";

export function fetchSidos() {
  return apiFetch("/api/attractions/sidos");
}

export function fetchGuguns(sidoCode) {
  return apiFetch(`/api/attractions/guguns?sidoCode=${encodeURIComponent(sidoCode)}`);
}

export function searchAttractions(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value) query.set(key, value);
  });
  return apiFetch(`/api/attractions?${query.toString()}`);
}
