import { apiFetch } from "@/api/http";

function toRoutingPlace(place) {
  return {
    key: String(place.attractionContentId ?? place.contentid ?? place.name),
    name: place.name || place.title,
    latitude: Number(place.latitude ?? place.mapy),
    longitude: Number(place.longitude ?? place.mapx)
  };
}

export function buildRoadRoute(places, mode = "CAR", optimize = false) {
  return apiFetch("/api/routes/plan", {
    method: "POST",
    body: JSON.stringify({
      mode,
      optimize,
      places: (places || []).map(toRoutingPlace)
    })
  });
}
