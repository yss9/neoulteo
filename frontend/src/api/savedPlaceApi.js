import { apiFetch } from "@/api/http";

export function fetchSavedPlaces() {
  return apiFetch("/api/saved-places");
}

export function savePlace(attractionContentId, sourceType = "ATTRACTION") {
  return apiFetch("/api/saved-places", {
    method: "POST",
    body: JSON.stringify({ attractionContentId, sourceType })
  });
}

export function deleteSavedPlace(id) {
  return apiFetch(`/api/saved-places/${id}`, {
    method: "DELETE"
  });
}
