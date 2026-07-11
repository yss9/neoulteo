<script setup>
import { markRaw, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from "vue";

const KAKAO_MAP_API_KEY = import.meta.env.VITE_KAKAO_MAP_APP_KEY;

const props = defineProps({
  spots: {
    type: Array,
    default: () => []
  },
  selectedSpot: {
    type: Object,
    default: null
  },
  showRoute: {
    type: Boolean,
    default: false
  },
  routePath: {
    type: Array,
    default: null
  }
});

const mapEl = ref(null);
const panelEl = ref(null);
// Kakao Map is a stateful third-party class instance. Vue's deep reactive Proxy
// breaks SDK internals during zoom animations, so keep the instance raw.
const map = shallowRef(null);
const errorMessage = ref("");
const markers = [];
const overlays = [];
const markerById = new Map();
const overlayById = new Map();
let routeLine = null;
let sdkPromise;
let resizeObserver = null;

function loadKakaoSdk() {
  if (!KAKAO_MAP_API_KEY) {
    return Promise.reject(new Error("Missing VITE_KAKAO_MAP_APP_KEY. Create frontend/.env from frontend/.env.example."));
  }

  if (window.kakao?.maps) {
    return Promise.resolve(window.kakao);
  }

  if (sdkPromise) {
    return sdkPromise;
  }

  sdkPromise = new Promise((resolve, reject) => {
    const script = document.createElement("script");
    const timeoutId = window.setTimeout(() => {
      reject(new Error("Kakao Map SDK did not respond. Check the JavaScript key and exact Web platform domain."));
    }, 8000);

    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_MAP_API_KEY}&autoload=false`;
    script.async = true;
    script.onload = () => {
      window.clearTimeout(timeoutId);
      window.kakao.maps.load(() => resolve(window.kakao));
    };
    script.onerror = () => {
      window.clearTimeout(timeoutId);
      reject(new Error("Failed to load Kakao Map SDK. Check network access to dapi.kakao.com and the Web platform domain."));
    };
    document.head.appendChild(script);
  });

  return sdkPromise;
}

function spotId(spot) {
  return String(spot?.contentid ?? spot?.attractionContentId ?? "");
}

function clearMarkers() {
  markers.splice(0).forEach((marker) => marker.setMap(null));
  overlays.splice(0).forEach((overlay) => overlay.close());
  markerById.clear();
  overlayById.clear();
}

function clearRoute() {
  if (routeLine) {
    routeLine.setMap(null);
    routeLine = null;
  }
}

function getCoordinate(spot) {
  const lat = Number(spot?.mapy ?? spot?.latitude);
  const lng = Number(spot?.mapx ?? spot?.longitude);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
    return null;
  }
  return { lat, lng };
}

function relayoutMap() {
  if (!map.value) return;
  map.value.relayout();
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function spotTitle(spot) {
  return spot?.title || spot?.name || "";
}

function spotAddress(spot) {
  return spot?.addr1 || spot?.address || "No address";
}

function openSpot(spot, shouldPan = true) {
  if (!map.value || !spot) return;

  const id = spotId(spot);
  const marker = markerById.get(id);
  const overlay = overlayById.get(id);
  const coordinate = getCoordinate(spot);

  if (!marker || !overlay || !coordinate) return;

  overlays.forEach((item) => item.close());
  if (shouldPan) {
    map.value.panTo(new window.kakao.maps.LatLng(coordinate.lat, coordinate.lng));
  }
  overlay.open(map.value, marker);
}

function renderRoute(kakao = window.kakao) {
  if (!map.value || !kakao?.maps) return;

  clearRoute();
  if (!props.showRoute || props.spots.length < 2) return;

  const routeCoordinates = (Array.isArray(props.routePath) ? props.routePath : props.spots)
    .map((point) => getCoordinate(point))
    .filter(Boolean)
    .map((coordinate) => new kakao.maps.LatLng(coordinate.lat, coordinate.lng));

  if (routeCoordinates.length < 2) return;
  routeLine = new kakao.maps.Polyline({
    map: map.value,
    path: routeCoordinates,
    strokeWeight: 5,
    strokeColor: "#2563eb",
    strokeOpacity: 0.82,
    strokeStyle: "solid"
  });
}

async function renderMarkers() {
  let kakao;
  try {
    kakao = await loadKakaoSdk();
    errorMessage.value = "";
    await nextTick();
  } catch (error) {
    errorMessage.value = error.message;
    return;
  }

  if (!map.value && mapEl.value) {
    map.value = markRaw(new kakao.maps.Map(mapEl.value, {
      center: new kakao.maps.LatLng(37.5665, 126.978),
      level: 8
    }));
  }

  if (!map.value) return;

  clearMarkers();
  clearRoute();

  const bounds = new kakao.maps.LatLngBounds();
  let markerCount = 0;

  props.spots.forEach((spot) => {
    const coordinate = getCoordinate(spot);
    const id = spotId(spot);
    if (!coordinate || !id) return;

    const position = new kakao.maps.LatLng(coordinate.lat, coordinate.lng);
    const markerImage = new kakao.maps.MarkerImage(
      "/map-pin.svg",
      new kakao.maps.Size(34, 42),
      { offset: new kakao.maps.Point(17, 42) }
    );
    const marker = new kakao.maps.Marker({ map: map.value, position, image: markerImage });
    const infoWindow = new kakao.maps.InfoWindow({
      content: `<div class="map-info"><strong>${escapeHtml(spotTitle(spot))}</strong><span>${escapeHtml(spotAddress(spot))}</span></div>`,
      removable: true
    });

    kakao.maps.event.addListener(marker, "click", () => {
      overlays.forEach((overlay) => overlay.close());
      infoWindow.open(map.value, marker);
    });

    markers.push(marker);
    overlays.push(infoWindow);
    markerById.set(id, marker);
    overlayById.set(id, infoWindow);
    bounds.extend(position);
    markerCount += 1;
  });

  renderRoute(kakao);

  if (markerCount > 0) {
    map.value.relayout();
    map.value.setBounds(bounds);
    openSpot(props.selectedSpot, false);
  } else {
    map.value.relayout();
  }
}

onMounted(() => {
  renderMarkers();
  if (window.ResizeObserver && panelEl.value) {
    resizeObserver = new ResizeObserver(() => {
      window.requestAnimationFrame(relayoutMap);
    });
    resizeObserver.observe(panelEl.value);
  }
});

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
  clearMarkers();
  clearRoute();
  map.value = null;
});

watch(
  () => props.spots,
  () => {
    renderMarkers();
  },
  { deep: true }
);

watch(
  () => props.selectedSpot,
  (spot) => {
    openSpot(spot);
  }
);

watch(
  () => props.routePath,
  () => {
    renderRoute();
  },
  { deep: true }
);

watch(
  () => props.showRoute,
  () => {
    renderRoute();
  }
);
</script>

<template>
  <section ref="panelEl" class="map-panel">
    <div ref="mapEl" class="kakao-map"></div>
    <p v-if="errorMessage" class="map-empty error">{{ errorMessage }}</p>
    <p v-else-if="spots.length === 0" class="map-empty">Search attractions to show markers.</p>
  </section>
</template>
