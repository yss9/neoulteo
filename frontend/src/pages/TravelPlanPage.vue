<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { searchAttractions } from "@/api/attractionApi";
import { deleteSavedPlace, fetchSavedPlaces, savePlace } from "@/api/savedPlaceApi";
import { evaluatePlanWithAi } from "@/api/planAiApi";
import { buildRoadRoute } from "@/api/routingApi";
import {
  copySharedPlan,
  deletePlan,
  fetchPlans,
  fetchSharedPlan,
  savePlan,
  updatePlan,
  updatePlanSharing
} from "@/api/travelPlanApi";
import KakaoMap from "@/components/KakaoMap.vue";
import { authStore } from "@/stores/authStore";
import { evaluateTravelPlan } from "@/utils/planEvaluation";

const route = useRoute();
const plans = ref([]);
const draftPlaces = ref([]);
const draftTitle = ref("");
const draftDayCount = ref(1);
const activeDraftDay = ref(1);
const savedPlaces = ref([]);
const searchKeyword = ref("");
const searchResults = ref([]);
const selectedSpot = ref(null);
const selectedPlanId = ref("");
const selectedPlanDay = ref(1);
const editingPlanId = ref("");
const shareCode = ref("");
const sharedPlan = ref(null);
const message = ref("");
const listError = ref("");
const savedPlaceMessage = ref("");
const shareMessage = ref("");
const planEvaluation = ref(null);
const loading = ref(false);
const searching = ref(false);
const saving = ref(false);
const shareLoading = ref(false);
const sharingPlanId = ref("");
const savedPlacesLoading = ref(false);
const savingPlaceId = ref("");
const deletingId = ref("");
const deletingSavedPlaceId = ref("");
const evaluatingPlan = ref(false);
const isBuilderOpen = ref(false);
const routeMode = ref("CAR");
const activeRoutePath = ref([]);
const routeSummary = ref(null);
const routeLoading = ref(false);
const routeError = ref("");
const draggedPlace = ref(null);
const dragOverPlace = ref(null);
const planPage = ref(1);
const planPageSize = 4;
const handledIncomingPlaceKey = ref("");
let routeRequestId = 0;

const selectedPlan = computed(() => plans.value.find((plan) => plan.id === selectedPlanId.value) || null);
const draftDays = computed(() => Array.from({ length: draftDayCount.value }, (_, index) => index + 1));
const activeDraftPlaces = computed(() => draftPlaces.value.filter((place) => getDayNo(place) === activeDraftDay.value));
const selectedPlanDays = computed(() => getPlanDays(sharedPlan.value?.places || selectedPlan.value?.places || []));
const selectedPlanPlaces = computed(() =>
  (sharedPlan.value?.places || selectedPlan.value?.places || []).filter(
    (place) => getDayNo(place) === selectedPlanDay.value
  )
);
const activeRoutePlaces = computed(() => {
  if (sharedPlan.value || selectedPlan.value) return selectedPlanPlaces.value;
  return activeDraftPlaces.value;
});
const evaluationTargetPlaces = computed(() => {
  if (sharedPlan.value?.places?.length) return sharedPlan.value.places;
  if (selectedPlan.value?.places?.length) return selectedPlan.value.places;
  return draftPlaces.value;
});
const isEditing = computed(() => Boolean(editingPlanId.value));
const planTotalPages = computed(() => Math.max(Math.ceil(plans.value.length / planPageSize), 1));
const paginatedPlans = computed(() => {
  const start = (planPage.value - 1) * planPageSize;
  return plans.value.slice(start, start + planPageSize);
});
const activeRouteTitle = computed(() => {
  if (sharedPlan.value) return planTitle(sharedPlan.value);
  if (selectedPlan.value) return planTitle(selectedPlan.value);
  return draftTitle.value.trim() || "작성 중인 코스";
});
const activeRouteTotalPlaces = computed(() => evaluationTargetPlaces.value.length);
const activeRouteDays = computed(() => getPlanDays(evaluationTargetPlaces.value));
const activeRouteDaySummaries = computed(() =>
  activeRouteDays.value.map((day) => ({
    dayNo: day,
    places: evaluationTargetPlaces.value.filter((place) => getDayNo(place) === day)
  }))
);
const activeRouteMapKey = computed(() => {
  const routeId = selectedPlan.value?.id || sharedPlan.value?.id || "draft";
  const dayNo = selectedPlan.value || sharedPlan.value ? selectedPlanDay.value : activeDraftDay.value;
  const placeKey = activeRoutePlaces.value
    .map((place) => `${getDayNo(place)}-${place.attractionContentId || place.name}`)
    .join("|");
  return `${routeId}-${dayNo}-${placeKey}`;
});

function getDayNo(place) {
  const dayNo = Number(place?.dayNo || 1);
  return Number.isFinite(dayNo) && dayNo > 0 ? dayNo : 1;
}

function getPlanDays(places) {
  const days = [...new Set((places || []).map(getDayNo))].sort((a, b) => a - b);
  return days.length ? days : [1];
}

function planTitle(plan) {
  return plan?.title || plan?.places?.[0]?.name || "여행 계획";
}

function durationLabel(days) {
  const dayCount = Math.max(Number(days) || 1, 1);
  return dayCount === 1 ? "당일치기" : `${dayCount - 1}박 ${dayCount}일`;
}

function openBuilder() {
  isBuilderOpen.value = true;
  selectedPlanId.value = "";
  sharedPlan.value = null;
  selectedSpot.value = activeDraftPlaces.value[0] || null;
  loadSavedPlaces();
}

function closeBuilder() {
  isBuilderOpen.value = false;
}

function setDraftDay(day) {
  activeDraftDay.value = day;
  selectedSpot.value = activeDraftPlaces.value[0] || null;
}

function addDraftDay() {
  draftDayCount.value += 1;
  setDraftDay(draftDayCount.value);
}

function setDraftDayCount(value) {
  const nextCount = Math.max(1, Math.min(Number(value) || 1, 10));
  draftDayCount.value = nextCount;
  draftPlaces.value = draftPlaces.value.map((place) => ({
    ...place,
    dayNo: getDayNo(place) > nextCount ? nextCount : getDayNo(place)
  }));
  if (activeDraftDay.value > nextCount) {
    activeDraftDay.value = nextCount;
  }
  selectedSpot.value = activeDraftPlaces.value[0] || null;
  planEvaluation.value = null;
}

function setSelectedPlanDay(day) {
  selectedPlanDay.value = day;
  selectedSpot.value = selectedPlanPlaces.value[0] || null;
}

async function load() {
  if (!authStore.isLoggedIn) {
    plans.value = [];
    selectedPlanId.value = "";
    return;
  }

  listError.value = "";
  loading.value = true;
  try {
    const data = await fetchPlans();
    plans.value = data.items || [];
    if (planPage.value > planTotalPages.value) {
      planPage.value = planTotalPages.value;
    }
    if (!selectedPlanId.value && plans.value.length > 0) {
      selectPlan(plans.value[0]);
    } else if (selectedPlan.value) {
      selectedPlanDay.value = getPlanDays(selectedPlan.value.places || [])[0];
      selectedSpot.value = selectedPlanPlaces.value[0] || null;
    }
  } catch (error) {
    listError.value = error.message || "저장된 여행 계획을 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

async function loadSavedPlaces() {
  if (!authStore.isLoggedIn) {
    savedPlaces.value = [];
    return [];
  }

  savedPlacesLoading.value = true;
  try {
    const data = await fetchSavedPlaces();
    savedPlaces.value = data.items || [];
    return savedPlaces.value;
  } catch (error) {
    savedPlaceMessage.value = error.message || "저장한 관광지를 불러오지 못했습니다.";
    return [];
  } finally {
    savedPlacesLoading.value = false;
  }
}

async function addIncomingSavedPlaceToDraft() {
  const addAllSavedPlaces = route.query.savedPlaces === "all";
  const savedPlaceId = route.query.savedPlaceId ? String(route.query.savedPlaceId) : "";
  const attractionContentId = route.query.attractionContentId ? String(route.query.attractionContentId) : "";
  const shouldOpenBuilder = route.query.builder === "1" || addAllSavedPlaces || savedPlaceId || attractionContentId;

  if (!shouldOpenBuilder) return;

  isBuilderOpen.value = true;
  selectedPlanId.value = "";
  sharedPlan.value = null;

  const incomingKey = addAllSavedPlaces ? "all-saved-places" : `${savedPlaceId}:${attractionContentId}`;
  if (handledIncomingPlaceKey.value === incomingKey) return;

  const places = savedPlaces.value.length ? savedPlaces.value : await loadSavedPlaces();
  if (addAllSavedPlaces) {
    let addedCount = 0;
    places.forEach((item) => {
      const place = toPlanPlace(item);
      if (!place.attractionContentId || !place.name) return;
      if (draftPlaces.value.some((draftPlace) => draftPlace.attractionContentId === place.attractionContentId)) return;
      draftPlaces.value.push(place);
      addedCount += 1;
    });

    if (addedCount > 0) {
      selectedSpot.value = activeDraftPlaces.value[0] || draftPlaces.value[0] || null;
      planEvaluation.value = null;
      message.value = `저장한 관광지 ${addedCount}곳을 코스에 추가했습니다.`;
    } else {
      message.value = places.length ? "저장한 관광지가 이미 코스에 추가되어 있습니다." : "저장한 관광지가 없습니다.";
    }

    handledIncomingPlaceKey.value = incomingKey;
    return;
  }

  if (!savedPlaceId && !attractionContentId) return;

  const place = places.find(
    (item) =>
      (savedPlaceId && String(item.id) === savedPlaceId) ||
      (attractionContentId && String(item.attractionContentId) === attractionContentId)
  );

  if (!place) {
    savedPlaceMessage.value = "저장한 관광지를 찾을 수 없습니다.";
    return;
  }

  addAttraction(place);
  handledIncomingPlaceKey.value = incomingKey;
  message.value = "저장한 관광지를 코스에 추가했습니다.";
}

async function searchPlaces() {
  message.value = "";
  searchResults.value = [];

  if (!searchKeyword.value.trim()) {
    message.value = "검색어를 입력해주세요.";
    return;
  }

  searching.value = true;
  try {
    const data = await searchAttractions({ keyword: searchKeyword.value.trim() });
    searchResults.value = data.items || [];
    if (searchResults.value.length === 0) {
      message.value = "검색 결과가 없습니다. 다른 검색어로 다시 찾아보세요.";
    }
  } catch (error) {
    message.value = error.message || "관광지 검색에 실패했습니다.";
  } finally {
    searching.value = false;
  }
}

function toPlanPlace(attraction, dayNo = activeDraftDay.value) {
  return {
    attractionContentId: Number(attraction.contentid ?? attraction.attractionContentId),
    dayNo,
    contentTypeId: attraction.contenttypeid ?? attraction.contentTypeId,
    contentTypeName: attraction.contentTypeName || attraction.type,
    name: attraction.title || attraction.name,
    address: attraction.addr1 || attraction.address,
    imageUrl: attraction.firstimage || attraction.imageUrl,
    firstimage: attraction.firstimage || attraction.imageUrl,
    latitude: attraction.mapy || attraction.latitude,
    longitude: attraction.mapx || attraction.longitude,
    mapy: attraction.mapy || attraction.latitude,
    mapx: attraction.mapx || attraction.longitude
  };
}

function addAttraction(attraction) {
  const place = toPlanPlace(attraction);
  if (!place.attractionContentId || !place.name) {
    message.value = "장소 정보를 확인할 수 없습니다.";
    return;
  }

  if (draftPlaces.value.some((item) => item.attractionContentId === place.attractionContentId)) {
    message.value = "이미 코스에 추가한 장소입니다.";
    return;
  }

  draftPlaces.value.push(place);
  selectedSpot.value = place;
  planEvaluation.value = null;
  message.value = "";
}

async function saveAttractionForLater(attraction, sourceType = "ATTRACTION") {
  savedPlaceMessage.value = "";
  const place = toPlanPlace(attraction);
  if (!place.attractionContentId) {
    savedPlaceMessage.value = "장소 정보를 확인할 수 없습니다.";
    return;
  }

  savingPlaceId.value = String(place.attractionContentId);
  try {
    const data = await savePlace(place.attractionContentId, sourceType);
    savedPlaceMessage.value = data.message || "관광지를 저장했습니다.";
    await loadSavedPlaces();
  } catch (error) {
    savedPlaceMessage.value = error.message || "관광지 저장에 실패했습니다.";
  } finally {
    savingPlaceId.value = "";
  }
}

async function removeSavedPlace(item) {
  savedPlaceMessage.value = "";
  deletingSavedPlaceId.value = item.id;
  try {
    const data = await deleteSavedPlace(item.id);
    savedPlaceMessage.value = data.message || "저장한 관광지를 삭제했습니다.";
    savedPlaces.value = savedPlaces.value.filter((place) => place.id !== item.id);
  } catch (error) {
    savedPlaceMessage.value = error.message || "저장한 관광지 삭제에 실패했습니다.";
  } finally {
    deletingSavedPlaceId.value = "";
  }
}

function removePlace(place) {
  draftPlaces.value = draftPlaces.value.filter((item) => item !== place);
  selectedSpot.value = activeDraftPlaces.value[0] || null;
  planEvaluation.value = null;
}

function movePlace(place, direction) {
  const dayPlaces = activeDraftPlaces.value;
  const index = dayPlaces.indexOf(place);
  const targetIndex = index + direction;
  if (index < 0 || targetIndex < 0 || targetIndex >= dayPlaces.length) return;

  const places = [...draftPlaces.value];
  const sourceGlobalIndex = places.indexOf(dayPlaces[index]);
  const targetGlobalIndex = places.indexOf(dayPlaces[targetIndex]);
  [places[sourceGlobalIndex], places[targetGlobalIndex]] = [places[targetGlobalIndex], places[sourceGlobalIndex]];
  draftPlaces.value = places;
  planEvaluation.value = null;
}

function reorderDayPlaces(dayPlaces) {
  const queue = [...dayPlaces];
  draftPlaces.value = draftPlaces.value.map((place) =>
    getDayNo(place) === activeDraftDay.value ? queue.shift() : place
  );
  planEvaluation.value = null;
}

function startPlaceDrag(place, event) {
  draggedPlace.value = place;
  dragOverPlace.value = place;
  if (event?.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/plain", String(place.attractionContentId));
  }
}

function enterPlaceDropZone(place) {
  if (draggedPlace.value && place !== draggedPlace.value) {
    dragOverPlace.value = place;
  }
}

function dropPlace(targetPlace) {
  const sourcePlace = draggedPlace.value;
  if (!sourcePlace || !targetPlace || sourcePlace === targetPlace) {
    finishPlaceDrag();
    return;
  }

  const reordered = [...activeDraftPlaces.value];
  const sourceIndex = reordered.indexOf(sourcePlace);
  const targetIndex = reordered.indexOf(targetPlace);
  if (sourceIndex < 0 || targetIndex < 0) {
    finishPlaceDrag();
    return;
  }

  reordered.splice(sourceIndex, 1);
  reordered.splice(targetIndex, 0, sourcePlace);
  reorderDayPlaces(reordered);
  selectedSpot.value = sourcePlace;
  finishPlaceDrag();
}

function finishPlaceDrag() {
  draggedPlace.value = null;
  dragOverPlace.value = null;
}

function routePlaceKey(place) {
  return String(place.attractionContentId ?? place.contentid ?? place.name);
}

async function refreshActiveRoadRoute() {
  const requestId = ++routeRequestId;
  routeError.value = "";
  routeSummary.value = null;
  if (activeRoutePlaces.value.length < 2) {
    activeRoutePath.value = [];
    routeLoading.value = false;
    return;
  }

  routeLoading.value = true;
  activeRoutePath.value = [];
  try {
    const data = await buildRoadRoute(activeRoutePlaces.value, routeMode.value, false);
    if (requestId !== routeRequestId) return;
    activeRoutePath.value = data.path || [];
    routeSummary.value = {
      distanceMeters: Number(data.distanceMeters) || 0,
      durationSeconds: Number(data.durationSeconds) || 0
    };
  } catch (error) {
    if (requestId !== routeRequestId) return;
    routeError.value = error.message || "실제 이동 경로를 불러오지 못했습니다.";
    activeRoutePath.value = [];
  } finally {
    if (requestId === routeRequestId) routeLoading.value = false;
  }
}

function selectPlan(plan) {
  selectedPlanId.value = plan.id;
  sharedPlan.value = null;
  planEvaluation.value = null;
  selectedPlanDay.value = getPlanDays(plan.places || [])[0];
  selectedSpot.value = selectedPlanPlaces.value[0] || null;
}

function copyPlace(place) {
  return {
    attractionContentId: Number(place.attractionContentId),
    dayNo: getDayNo(place),
    contentTypeId: place.contentTypeId ?? place.contenttypeid,
    contentTypeName: place.contentTypeName || place.type,
    name: place.name,
    address: place.address,
    imageUrl: place.imageUrl,
    firstimage: place.firstimage,
    latitude: place.latitude,
    longitude: place.longitude,
    mapy: place.mapy,
    mapx: place.mapx
  };
}

function startEditPlan(plan) {
  editingPlanId.value = plan.id;
  selectedPlanId.value = "";
  sharedPlan.value = null;
  draftPlaces.value = (plan.places || []).map(copyPlace);
  draftTitle.value = plan.title || planTitle(plan);
  planEvaluation.value = null;
  draftDayCount.value = Math.max(...getPlanDays(draftPlaces.value), Number(plan.durationDays) || 1);
  activeDraftDay.value = getPlanDays(draftPlaces.value)[0];
  selectedSpot.value = activeDraftPlaces.value[0] || null;
  message.value = "저장된 코스를 수정 중입니다.";
  openBuilder();
}

function cancelEditPlan() {
  editingPlanId.value = "";
  draftPlaces.value = [];
  draftTitle.value = "";
  draftDayCount.value = 1;
  activeDraftDay.value = 1;
  selectedSpot.value = selectedPlan.value?.places?.[0] || null;
  message.value = "";
  planEvaluation.value = null;
}

function clearDraft() {
  draftPlaces.value = [];
  draftTitle.value = "";
  draftDayCount.value = 1;
  activeDraftDay.value = 1;
  editingPlanId.value = "";
  selectedSpot.value = selectedPlan.value?.places?.[0] || null;
  planEvaluation.value = null;
}

function setPlanPage(page) {
  planPage.value = Math.min(Math.max(page, 1), planTotalPages.value);
}

function toAiEvaluationPlace(place) {
  return {
    dayNo: getDayNo(place),
    name: place.name || place.title,
    address: place.address || place.addr1,
    contentTypeName: place.contentTypeName || place.type,
    contentTypeId: String(place.contentTypeId ?? place.contenttypeid ?? ""),
    latitude: Number(place.latitude ?? place.mapy) || null,
    longitude: Number(place.longitude ?? place.mapx) || null
  };
}

function currentEvaluationTitle() {
  if (sharedPlan.value) return sharedPlan.value.title || planTitle(sharedPlan.value);
  if (selectedPlan.value) return selectedPlan.value.title || planTitle(selectedPlan.value);
  return draftTitle.value.trim() || "작성 중인 여행 코스";
}

function currentEvaluationDurationDays() {
  if (sharedPlan.value?.durationDays) return sharedPlan.value.durationDays;
  if (selectedPlan.value?.durationDays) return selectedPlan.value.durationDays;
  return draftDayCount.value;
}

function scoreLevel(score) {
  const normalizedScore = Math.max(0, Math.min(100, Math.round(Number(score) || 0)));
  if (normalizedScore >= 82) return "good";
  if (normalizedScore >= 62) return "check";
  return "tight";
}

function extractJsonObject(text) {
  if (!text) return null;
  const raw = String(text).trim();
  const fenced = raw.match(/```(?:json)?\s*([\s\S]*?)```/i);
  const candidate = fenced?.[1]?.trim() || raw;

  try {
    return JSON.parse(candidate);
  } catch {
    const start = candidate.indexOf("{");
    const end = candidate.lastIndexOf("}");
    if (start < 0 || end <= start) return null;
    try {
      return JSON.parse(candidate.slice(start, end + 1));
    } catch {
      return null;
    }
  }
}

function normalizeAiEvaluation(ruleEvaluation, answer) {
  const parsed = extractJsonObject(answer);
  if (!parsed || typeof parsed !== "object") {
    return {
      ...ruleEvaluation,
      aiAdvice: answer || "AI 평가 결과를 불러오지 못했습니다.",
      aiSource: "spring-ai"
    };
  }

  const aiScore = Math.max(0, Math.min(100, Math.round(Number(parsed.score ?? ruleEvaluation.score))));
  const feedback = Array.isArray(parsed.feedback)
    ? parsed.feedback.map((item) => String(item).trim()).filter(Boolean).slice(0, 5)
    : ruleEvaluation.feedback;

  return {
    ...ruleEvaluation,
    score: aiScore,
    level: scoreLevel(aiScore),
    summary: String(parsed.summary || ruleEvaluation.summary),
    feedback: feedback.length ? feedback : ruleEvaluation.feedback,
    aiAdvice: String(parsed.advice || parsed.reason || answer || ""),
    aiSource: "spring-ai"
  };
}

async function evaluateCurrentPlan() {
  const ruleEvaluation = evaluateTravelPlan(evaluationTargetPlaces.value);

  if (evaluationTargetPlaces.value.length === 0) {
    planEvaluation.value = {
      ...ruleEvaluation,
      aiAdvice: "",
      aiSource: "rule"
    };
    return;
  }

  planEvaluation.value = null;
  evaluatingPlan.value = true;
  try {
    const data = await evaluatePlanWithAi({
      title: currentEvaluationTitle(),
      durationDays: currentEvaluationDurationDays(),
      score: ruleEvaluation.score,
      ruleSummary: ruleEvaluation.summary,
      ruleFeedback: ruleEvaluation.feedback,
      places: evaluationTargetPlaces.value.map(toAiEvaluationPlace)
    });
    planEvaluation.value = normalizeAiEvaluation(
      ruleEvaluation,
      data?.data?.answer || data?.message || "AI 평가 결과를 불러오지 못했습니다."
    );
  } catch (error) {
    planEvaluation.value = {
      ...ruleEvaluation,
      aiAdvice: `AI 코스 분석 호출에 실패했습니다. 기본 평가만 표시합니다. (${error.message})`,
      aiSource: "fallback"
    };
  } finally {
    evaluatingPlan.value = false;
  }
}

async function applyAiSuggestionToDraft() {
  if (!draftPlaces.value.length) return;
  routeLoading.value = true;
  routeError.value = "";
  try {
    const optimizedDays = [];
    for (const day of draftDays.value) {
      const dayPlaces = draftPlaces.value.filter((place) => getDayNo(place) === day);
      if (dayPlaces.length < 2) {
        optimizedDays.push(...dayPlaces);
        continue;
      }

      const data = await buildRoadRoute(dayPlaces, routeMode.value, true);
      const byKey = new Map(dayPlaces.map((place) => [routePlaceKey(place), place]));
      const ordered = (data.orderedKeys || []).map((key) => byKey.get(String(key))).filter(Boolean);
      optimizedDays.push(...(ordered.length === dayPlaces.length ? ordered : dayPlaces));
      if (day === activeDraftDay.value) {
        activeRoutePath.value = data.path || [];
        routeSummary.value = {
          distanceMeters: Number(data.distanceMeters) || 0,
          durationSeconds: Number(data.durationSeconds) || 0
        };
      }
    }

    draftPlaces.value = optimizedDays;
    selectedSpot.value = activeDraftPlaces.value[0] || null;
    planEvaluation.value = {
      ...evaluateTravelPlan(draftPlaces.value),
      aiAdvice: `${routeMode.value === "WALK" ? "도보" : "자동차"} 실제 이동 거리를 기준으로 일차별 방문 순서를 정렬했습니다.`,
      aiSource: "rule"
    };
    message.value = `${routeMode.value === "WALK" ? "도보" : "자동차"} 도로 경로 기준 정렬을 반영했습니다.`;
  } catch (error) {
    routeError.value = error.message || "실제 이동 거리 기준 정렬에 실패했습니다.";
    message.value = routeError.value;
  } finally {
    routeLoading.value = false;
  }
}

async function removePlan(plan) {
  if (!window.confirm(`'${planTitle(plan)}' 계획을 삭제할까요?`)) return;

  deletingId.value = plan.id;
  message.value = "";
  try {
    const data = await deletePlan(plan.id);
    message.value = data.message || "여행 계획을 삭제했습니다.";
    if (selectedPlanId.value === plan.id) selectedPlanId.value = "";
    if (editingPlanId.value === plan.id) cancelEditPlan();
    await load();
  } catch (error) {
    message.value = error.message || "여행 계획 삭제에 실패했습니다.";
  } finally {
    deletingId.value = "";
  }
}

async function togglePlanSharing(plan) {
  sharingPlanId.value = plan.id;
  shareMessage.value = "";
  try {
    const data = await updatePlanSharing(plan.id, !plan.shared);
    plan.shared = data.shared;
    shareMessage.value = data.shared ? `공유 코드가 생성되었습니다. ${data.shareCode}` : "공유가 해제되었습니다.";
  } catch (error) {
    shareMessage.value = error.message || "공유 상태 변경에 실패했습니다.";
  } finally {
    sharingPlanId.value = "";
  }
}

async function copyShareCode(plan) {
  shareMessage.value = "";
  try {
    await navigator.clipboard.writeText(plan.id);
    shareMessage.value = "공유 코드를 복사했습니다.";
  } catch {
    shareMessage.value = `공유 코드: ${plan.id}`;
  }
}

async function loadSharedPlanByCode() {
  const code = shareCode.value.trim();
  shareMessage.value = "";
  sharedPlan.value = null;
  if (!code) {
    shareMessage.value = "공유 코드를 입력해주세요.";
    return;
  }

  shareLoading.value = true;
  try {
    sharedPlan.value = await fetchSharedPlan(code);
    selectedPlanId.value = "";
    selectedPlanDay.value = getPlanDays(sharedPlan.value?.places || [])[0];
    selectedSpot.value = selectedPlanPlaces.value[0] || null;
    planEvaluation.value = null;
    shareMessage.value = "공유된 여행 코스를 불러왔습니다.";
  } catch (error) {
    shareMessage.value = error.message || "공유 코스를 불러오지 못했습니다.";
  } finally {
    shareLoading.value = false;
  }
}

async function copySharedPlanToMine() {
  if (!sharedPlan.value?.id) {
    shareMessage.value = "먼저 공유 코스를 불러와주세요.";
    return;
  }

  shareLoading.value = true;
  try {
    const data = await copySharedPlan(sharedPlan.value.id);
    shareMessage.value = data.message || "공유 코스를 내 계획으로 복사했습니다.";
    shareCode.value = "";
    sharedPlan.value = null;
    await load();
  } catch (error) {
    shareMessage.value = error.message || "공유 코스 복사에 실패했습니다.";
  } finally {
    shareLoading.value = false;
  }
}

async function save() {
  message.value = "";
  if (!authStore.isLoggedIn) {
    message.value = "로그인이 필요합니다.";
    return;
  }

  if (draftPlaces.value.length === 0) {
    message.value = "저장할 관광지를 먼저 추가해주세요.";
    return;
  }

  saving.value = true;
  try {
    const payload = {
      title: draftTitle.value.trim() || null,
      durationDays: draftDayCount.value,
      places: draftPlaces.value.map((place) => ({
        attractionContentId: place.attractionContentId,
        dayNo: getDayNo(place),
        name: place.name
      }))
    };
    const data = isEditing.value ? await updatePlan(editingPlanId.value, payload) : await savePlan(payload);
    message.value = data.message || (isEditing.value ? "여행 코스를 수정했습니다." : "여행 코스를 저장했습니다.");
    clearDraft();
    closeBuilder();
    await load();
  } catch (error) {
    message.value = error.message || "여행 코스 저장에 실패했습니다.";
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await load();
  await loadSavedPlaces();
  await addIncomingSavedPlaceToDraft();
});

watch(
  () => authStore.isLoggedIn,
  () => {
    clearDraft();
    selectedPlanId.value = "";
    sharedPlan.value = null;
    load();
    loadSavedPlaces();
  }
);

watch(
  () => [activeRouteMapKey.value, routeMode.value],
  () => refreshActiveRoadRoute(),
  { immediate: true }
);

watch(
  () => [route.query.builder, route.query.savedPlaces, route.query.savedPlaceId, route.query.attractionContentId],
  () => addIncomingSavedPlaceToDraft()
);
</script>

<template>
  <section class="plan-page">
    <div v-if="!authStore.isLoggedIn" class="empty-state">
      <strong class="empty-state-title">로그인이 필요합니다</strong>
      <p class="empty-state-description">여행 계획 저장과 목록 조회는 로그인 후 사용할 수 있습니다.</p>
    </div>

    <section v-else class="plan-route-layout plan-route-layout-view">
      <section class="plan-map-panel">
        <div class="plan-list-header">
          <div>
            <h2>{{ selectedPlan ? "저장된 코스 보기" : sharedPlan ? "공유 코스 보기" : "코스 미리보기" }}</h2>
            <p>선택한 일차의 장소를 지도에서 확인할 수 있습니다.</p>
          </div>
          <button class="btn btn-primary btn-sm" type="button" @click="openBuilder">코스 만들기</button>
        </div>
        <div v-if="selectedPlan || sharedPlan" class="plan-day-tabs compact" aria-label="여행 일차">
          <button
            v-for="day in selectedPlanDays"
            :key="`selected-day-${day}`"
            class="plan-day-tab"
            :class="{ 'plan-day-tab-active': selectedPlanDay === day }"
            type="button"
            @click="setSelectedPlanDay(day)"
          >
            {{ day }}일차
          </button>
        </div>
        <div class="route-mode-toolbar">
          <strong>이동 경로</strong>
          <label><input v-model="routeMode" type="radio" value="CAR" /> 자동차</label>
          <label><input v-model="routeMode" type="radio" value="WALK" /> 도보</label>
          <span v-if="routeLoading">실제 경로 계산 중...</span>
          <span v-else-if="routeSummary">
            {{ (routeSummary.distanceMeters / 1000).toFixed(1) }}km · 약 {{ Math.max(1, Math.round(routeSummary.durationSeconds / 60)) }}분
          </span>
          <span v-if="routeError" class="route-error">{{ routeError }}</span>
        </div>
        <div class="plan-map-dashboard">
          <KakaoMap
            :spots="activeRoutePlaces"
            :selected-spot="selectedSpot"
            :route-path="activeRoutePath"
            show-route
          />
          <aside class="plan-route-dashboard">
            <div class="plan-route-dashboard-header">
              <span class="badge badge-soft">{{ selectedPlan ? "저장된 코스" : sharedPlan ? "공유 코스" : "작성 중" }}</span>
              <h3>{{ activeRouteTitle }}</h3>
              <p>{{ durationLabel(currentEvaluationDurationDays()) }} · {{ activeRouteTotalPlaces }}곳</p>
            </div>

            <div class="plan-route-day-list">
              <article v-for="day in activeRouteDaySummaries" :key="`route-summary-${day.dayNo}`" class="plan-route-day-card">
                <button
                  v-if="selectedPlan || sharedPlan"
                  class="plan-route-day-title"
                  type="button"
                  :class="{ 'plan-route-day-title-active': selectedPlanDay === day.dayNo }"
                  @click="setSelectedPlanDay(day.dayNo)"
                >
                  {{ day.dayNo }}일차
                  <span>{{ day.places.length }}곳</span>
                </button>
                <strong v-else class="plan-route-day-title">{{ day.dayNo }}일차 <span>{{ day.places.length }}곳</span></strong>
                <ol>
                  <li v-for="(place, index) in day.places" :key="`${day.dayNo}-${place.attractionContentId}-${index}`">
                    <button type="button" @click="selectedSpot = place">
                      <span>{{ index + 1 }}</span>
                      {{ place.name }}
                    </button>
                  </li>
                </ol>
              </article>
            </div>

            <section class="plan-ai-panel plan-ai-panel-compact">
              <div class="plan-ai-header">
                <div>
                  <span class="badge badge-accent">AI 코스 평가</span>
                  <h3>동선 분석</h3>
                  <p>선택한 전체 코스를 Spring AI 중심으로 평가합니다.</p>
                </div>
                <button
                  class="btn btn-secondary btn-sm"
                  type="button"
                  :disabled="evaluationTargetPlaces.length === 0 || evaluatingPlan"
                  @click="evaluateCurrentPlan"
                >
                  {{ evaluatingPlan ? "분석 중" : "평가하기" }}
                </button>
              </div>

              <div v-if="evaluatingPlan" class="plan-ai-loading">분석중</div>

              <div v-else-if="planEvaluation" class="plan-ai-result" :class="`plan-ai-result-${planEvaluation.level}`">
                <div class="plan-ai-score">
                  <strong>{{ planEvaluation.score }}</strong>
                  <span>/ 100</span>
                </div>
                <div>
                  <h4>{{ planEvaluation.summary }}</h4>
                  <ul>
                    <li v-for="item in planEvaluation.feedback" :key="item">{{ item }}</li>
                  </ul>
                </div>
              </div>

              <article v-if="!evaluatingPlan && planEvaluation?.aiAdvice" class="plan-ai-advice">
                <span class="badge badge-primary">
                  {{ planEvaluation.aiSource === "spring-ai" ? "Spring AI 분석" : "기본 분석" }}
                </span>
                <p>{{ planEvaluation.aiAdvice }}</p>
              </article>
            </section>
          </aside>
        </div>
        <section class="plan-ai-panel plan-ai-panel-wide">
          <div class="plan-ai-header">
            <div>
              <span class="badge badge-accent">AI 코스 평가</span>
              <h3>동선 분석</h3>
              <p>선택한 전체 코스를 Spring AI 중심으로 평가합니다.</p>
            </div>
            <button
              class="btn btn-secondary"
              type="button"
              :disabled="evaluationTargetPlaces.length === 0 || evaluatingPlan"
              @click="evaluateCurrentPlan"
            >
              {{ evaluatingPlan ? "분석 중" : "평가하기" }}
            </button>
          </div>

          <div v-if="evaluatingPlan" class="plan-ai-loading">분석중</div>

          <div v-else-if="planEvaluation" class="plan-ai-result" :class="`plan-ai-result-${planEvaluation.level}`">
            <div class="plan-ai-score">
              <strong>{{ planEvaluation.score }}</strong>
              <span>/ 100</span>
            </div>
            <div>
              <h4>{{ planEvaluation.summary }}</h4>
              <ul>
                <li v-for="item in planEvaluation.feedback" :key="item">{{ item }}</li>
              </ul>
            </div>
          </div>

          <div v-if="!evaluatingPlan && planEvaluation?.daySummaries?.length" class="plan-ai-days">
            <article v-for="day in planEvaluation.daySummaries" :key="day.dayNo">
              <strong>{{ day.dayNo }}일차</strong>
              <span>{{ day.placeCount }}곳</span>
              <span>약 {{ day.distanceKm.toFixed(1) }}km</span>
            </article>
          </div>

          <article v-if="!evaluatingPlan && planEvaluation?.aiAdvice" class="plan-ai-advice">
            <span class="badge badge-primary">
              {{ planEvaluation.aiSource === "spring-ai" ? "Spring AI 분석" : "기본 분석" }}
            </span>
            <p>{{ planEvaluation.aiAdvice }}</p>
          </article>
        </section>
      </section>

      <section class="plan-list-section plan-saved-panel">
        <div class="plan-list-header">
          <div>
            <h2>저장된 계획</h2>
            <p>계획을 선택하면 지도에 일차별 코스가 표시됩니다.</p>
          </div>
          <button class="btn btn-ghost btn-sm refresh-icon-button" type="button" aria-label="저장된 계획 새로고침" title="새로고침" @click="load">
            <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
              <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.96-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8Zm-6.76 3.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3c-3.31 0-6-2.69-6-6 0-1.01.25-1.96.7-2.8L5.24 7.74Z" />
            </svg>
          </button>
        </div>

        <div class="plan-share-loader">
          <label class="form-group">
            <span class="form-label">공유 코드로 코스 불러오기</span>
            <div class="inline-form">
              <input v-model="shareCode" class="form-input" placeholder="여행 코스 공유 코드" @keydown.enter.prevent="loadSharedPlanByCode" />
              <button class="btn btn-secondary" type="button" :disabled="shareLoading" @click="loadSharedPlanByCode">
                {{ shareLoading ? "확인 중" : "불러오기" }}
              </button>
            </div>
          </label>

          <article v-if="sharedPlan" class="shared-plan-preview">
            <div>
              <span class="badge badge-accent">공유 코스</span>
              <h3>{{ planTitle(sharedPlan) }}</h3>
              <p>{{ sharedPlan.writerEmail }} · {{ sharedPlan.places?.length || 0 }}곳</p>
            </div>
            <button class="btn btn-primary" type="button" :disabled="shareLoading" @click="copySharedPlanToMine">
              내 계획으로 복사
            </button>
          </article>

          <p v-if="shareMessage" class="notice">{{ shareMessage }}</p>
        </div>

        <div v-if="loading" class="plan-grid">
          <div class="loading-skeleton plan-card-skeleton"></div>
          <div class="loading-skeleton plan-card-skeleton"></div>
        </div>

        <p v-else-if="listError" class="error-message">{{ listError }}</p>

        <div v-else-if="plans.length === 0" class="plan-empty-state">
          <strong>저장된 여행 계획이 없습니다</strong>
          <p>코스 만들기 버튼을 눌러 첫 여행 코스를 저장해보세요.</p>
        </div>

        <div v-else class="plan-grid">
          <article v-for="plan in paginatedPlans" :key="plan.id" class="plan-card" :class="{ 'plan-card-active': selectedPlanId === plan.id }">
            <button class="plan-card-button" type="button" @click="selectPlan(plan)">
              <div class="plan-card-header">
                <strong class="plan-card-title">{{ planTitle(plan) }}</strong>
                <span class="plan-card-meta">{{ plan.createdAt }}</span>
              </div>
              <p class="plan-card-description">{{ durationLabel(plan.durationDays) }} · {{ plan.places?.length || 0 }}곳 코스</p>
              <ol class="plan-place-list readonly">
                <li v-for="day in getPlanDays(plan.places)" :key="`${plan.id}-day-${day}`">
                  <span class="plan-place-badge">{{ day }}일차</span>
                  <span>{{ (plan.places || []).filter((place) => getDayNo(place) === day).map((place) => place.name).join(" → ") }}</span>
                </li>
              </ol>
            </button>

            <div class="plan-card-actions">
              <button class="btn btn-secondary btn-sm" type="button" @click="startEditPlan(plan)">수정</button>
              <button class="btn btn-ghost btn-sm" type="button" :disabled="sharingPlanId === plan.id" @click="togglePlanSharing(plan)">
                {{ sharingPlanId === plan.id ? "변경 중" : plan.shared ? "공유 해제" : "공유 켜기" }}
              </button>
              <button v-if="plan.shared" class="btn btn-ghost btn-sm" type="button" @click="copyShareCode(plan)">코드 복사</button>
              <button class="btn btn-danger btn-sm" type="button" :disabled="deletingId === plan.id" @click="removePlan(plan)">
                {{ deletingId === plan.id ? "삭제 중" : "삭제" }}
              </button>
            </div>
            <p v-if="plan.shared" class="plan-share-code">공유 코드 {{ plan.id }}</p>
          </article>
        </div>

        <nav v-if="planTotalPages > 1" class="pagination" aria-label="여행 계획 페이지">
          <button class="btn btn-ghost btn-sm" type="button" :disabled="planPage === 1" @click="setPlanPage(planPage - 1)">이전</button>
          <button
            v-for="page in planTotalPages"
            :key="`plan-page-${page}`"
            class="btn btn-sm"
            :class="page === planPage ? 'btn-primary' : 'btn-ghost'"
            type="button"
            @click="setPlanPage(page)"
          >
            {{ page }}
          </button>
          <button class="btn btn-ghost btn-sm" type="button" :disabled="planPage === planTotalPages" @click="setPlanPage(planPage + 1)">다음</button>
        </nav>
      </section>

    </section>

    <div v-if="isBuilderOpen" class="modal-backdrop plan-builder-backdrop">
      <section class="modal-panel plan-builder-modal">
        <div class="modal-body plan-builder-modal-body">
          <section class="plan-builder-panel">
            <div v-if="isEditing" class="plan-edit-banner">
              <strong>수정 모드</strong>
              <span>저장된 계획을 불러왔습니다. 저장하면 기존 계획에 반영됩니다.</span>
              <button class="btn btn-ghost btn-sm" type="button" @click="cancelEditPlan">수정 취소</button>
            </div>

            <label class="form-group">
              <span class="form-label">여행 계획 이름</span>
              <input v-model="draftTitle" class="form-input" maxlength="100" placeholder="예: 부산 2박 3일 바다 코스" />
            </label>

            <div class="plan-day-control">
              <label class="form-group">
                <span class="form-label">여행 일수</span>
                <input class="form-input" type="number" min="1" max="10" :value="draftDayCount" @change="setDraftDayCount($event.target.value)" />
              </label>
              <button class="btn btn-secondary" type="button" @click="addDraftDay">하루 추가</button>
            </div>

            <div class="plan-day-tabs" aria-label="작성 중인 여행 일차">
              <button
                v-for="day in draftDays"
                :key="`draft-day-${day}`"
                class="plan-day-tab"
                :class="{ 'plan-day-tab-active': activeDraftDay === day }"
                type="button"
                @click="setDraftDay(day)"
              >
                {{ day }}일차
              </button>
            </div>

            <label class="form-group">
              <span class="form-label">관광지 검색</span>
              <div class="inline-form">
                <input v-model="searchKeyword" class="form-input" placeholder="예: 경복궁, 감천문화마을" @keydown.enter.prevent="searchPlaces" />
                <button class="btn btn-secondary" type="button" :disabled="searching" @click="searchPlaces">
                  {{ searching ? "검색 중" : "검색" }}
                </button>
              </div>
            </label>

            <div v-if="searchResults.length" class="plan-search-results">
              <article v-for="attraction in searchResults" :key="attraction.contentid" class="plan-search-result" @mouseenter="selectedSpot = toPlanPlace(attraction)">
                <img v-if="attraction.firstimage" :src="attraction.firstimage" :alt="attraction.title" />
                <span v-else class="plan-search-thumb">No image</span>
                <span>
                  <strong>{{ attraction.title }}</strong>
                  <small>{{ attraction.addr1 || "주소 정보 없음" }}</small>
                </span>
                <div class="plan-search-actions">
                  <button class="btn btn-primary btn-sm" type="button" @click="addAttraction(attraction)">코스 추가</button>
                  <button class="btn btn-ghost btn-sm" type="button" :disabled="savingPlaceId === String(attraction.contentid)" @click="saveAttractionForLater(attraction)">
                    {{ savingPlaceId === String(attraction.contentid) ? "저장 중" : "저장" }}
                  </button>
                </div>
              </article>
            </div>

            <section class="plan-saved-picker">
              <div class="plan-list-header compact">
                <div>
                  <strong>내가 저장한 관광지</strong>
                  <p>관광지 검색에서 저장한 장소를 현재 코스에 추가할 수 있습니다.</p>
                </div>
                <button class="btn btn-ghost btn-sm refresh-icon-button" type="button" aria-label="저장한 관광지 새로고침" title="새로고침" @click="loadSavedPlaces">
                  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.96-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8Zm-6.76 3.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3c-3.31 0-6-2.69-6-6 0-1.01.25-1.96.7-2.8L5.24 7.74Z" />
                  </svg>
                </button>
              </div>

              <div v-if="savedPlacesLoading" class="saved-place-list saved-place-list-compact">
                <div class="loading-skeleton saved-place-skeleton"></div>
                <div class="loading-skeleton saved-place-skeleton"></div>
              </div>

              <div v-else-if="savedPlaces.length === 0" class="plan-empty-state compact">
                <strong>저장한 관광지가 없습니다</strong>
                <p>관광지 검색 결과에서 저장 버튼을 눌러보세요.</p>
              </div>

              <div v-else class="saved-place-list saved-place-list-compact">
                <article v-for="item in savedPlaces" :key="item.id" class="saved-place-card saved-place-card-compact">
                  <img v-if="item.imageUrl || item.firstimage" :src="item.imageUrl || item.firstimage" :alt="item.name" />
                  <span v-else class="saved-place-thumb">No image</span>
                  <div class="saved-place-main">
                    <strong>{{ item.name }}</strong>
                    <small>{{ item.address || "주소 정보 없음" }}</small>
                  </div>
                  <div class="saved-place-actions">
                    <button class="btn btn-primary btn-sm" type="button" @click="addAttraction(item)">코스 추가</button>
                    <button class="btn btn-ghost btn-sm" type="button" :disabled="deletingSavedPlaceId === item.id" @click="removeSavedPlace(item)">
                      삭제
                    </button>
                  </div>
                </article>
              </div>

              <p v-if="savedPlaceMessage" class="notice">{{ savedPlaceMessage }}</p>
            </section>

            <div class="plan-draft-box">
              <div class="plan-list-header compact">
                <div>
                  <strong>작성 중인 코스</strong>
                  <p>{{ activeDraftDay }}일차 {{ activeDraftPlaces.length }}곳 · 전체 {{ draftPlaces.length }}곳</p>
                  <p>{{ durationLabel(draftDayCount) }}</p>
                </div>
                <button class="btn btn-ghost btn-sm" type="button" :disabled="draftPlaces.length === 0" @click="clearDraft">비우기</button>
              </div>

              <ol v-if="activeDraftPlaces.length" class="plan-place-list">
                <li
                  v-for="(place, index) in activeDraftPlaces"
                  :key="`${place.dayNo}-${place.attractionContentId}`"
                  class="plan-place-draggable"
                  :class="{
                    'plan-place-dragging': draggedPlace === place,
                    'plan-place-drag-over': dragOverPlace === place && draggedPlace !== place
                  }"
                  draggable="true"
                  @dragstart="startPlaceDrag(place, $event)"
                  @dragenter.prevent="enterPlaceDropZone(place)"
                  @dragover.prevent
                  @drop.prevent="dropPlace(place)"
                  @dragend="finishPlaceDrag"
                >
                  <button class="plan-place-main" type="button" @click="selectedSpot = place">
                    <span class="plan-place-badge">{{ index + 1 }}</span>
                    <span>
                      <strong>{{ place.name }}</strong>
                      <small>{{ place.address || "주소 정보 없음" }}</small>
                    </span>
                  </button>
                  <div class="plan-place-actions">
                    <button class="btn btn-ghost btn-sm" type="button" :disabled="index === 0" @click="movePlace(place, -1)">위</button>
                    <button class="btn btn-ghost btn-sm" type="button" :disabled="index === activeDraftPlaces.length - 1" @click="movePlace(place, 1)">아래</button>
                    <button class="btn btn-ghost btn-sm" type="button" @click="removePlace(place)">삭제</button>
                  </div>
                </li>
              </ol>

              <div v-else class="plan-empty-state compact">
                <strong>아직 추가한 관광지가 없습니다</strong>
                <p>검색 결과에서 장소를 클릭해 코스에 추가해보세요.</p>
              </div>
            </div>
          </section>

          <section class="plan-builder-map-column">
            <div class="route-mode-toolbar builder-route-mode-toolbar">
              <strong>실제 이동 경로</strong>
              <label><input v-model="routeMode" type="radio" value="CAR" /> 자동차</label>
              <label><input v-model="routeMode" type="radio" value="WALK" /> 도보</label>
              <span v-if="routeLoading">계산 중...</span>
              <span v-else-if="routeSummary">
                {{ (routeSummary.distanceMeters / 1000).toFixed(1) }}km · 약 {{ Math.max(1, Math.round(routeSummary.durationSeconds / 60)) }}분
              </span>
            </div>
            <p v-if="routeError" class="route-error route-error-block">{{ routeError }}</p>
            <KakaoMap
              class="plan-builder-route-map"
              :spots="activeDraftPlaces"
              :selected-spot="selectedSpot"
              :route-path="activeRoutePath"
              show-route
            />
            <section class="plan-ai-panel">
              <div class="plan-ai-header">
                <div>
                  <span class="badge badge-accent">AI 코스 평가</span>
                  <h3>일정 강도와 동선 체크</h3>
                  <p>Spring AI 분석을 확인하고, 버튼으로는 좌표 기준 가까운 장소 순서 정렬을 반영합니다.</p>
                </div>
                <div class="plan-ai-actions">
                  <button class="btn btn-secondary" type="button" :disabled="draftPlaces.length === 0 || evaluatingPlan" @click="evaluateCurrentPlan">
                    {{ evaluatingPlan ? "분석 중" : "코스 평가" }}
                  </button>
                  <button class="btn btn-primary" type="button" :disabled="draftPlaces.length < 2 || routeLoading" @click="applyAiSuggestionToDraft">
                    {{ routeLoading ? "경로 계산 중" : "실제 경로로 정렬" }}
                  </button>
                </div>
              </div>

              <div v-if="evaluatingPlan" class="plan-ai-loading">분석중</div>

              <div v-else-if="planEvaluation" class="plan-ai-result" :class="`plan-ai-result-${planEvaluation.level}`">
                <div class="plan-ai-score">
                  <strong>{{ planEvaluation.score }}</strong>
                  <span>/ 100</span>
                </div>
                <div>
                  <h4>{{ planEvaluation.summary }}</h4>
                  <ul>
                    <li v-for="item in planEvaluation.feedback" :key="item">{{ item }}</li>
                  </ul>
                </div>
              </div>

              <div v-if="!evaluatingPlan && planEvaluation?.daySummaries?.length" class="plan-ai-days">
                <article v-for="day in planEvaluation.daySummaries" :key="day.dayNo">
                  <strong>{{ day.dayNo }}일차</strong>
                  <span>{{ day.placeCount }}곳</span>
                  <span>약 {{ day.distanceKm.toFixed(1) }}km</span>
                </article>
              </div>

              <article v-if="!evaluatingPlan && planEvaluation?.aiAdvice" class="plan-ai-advice">
                <span class="badge badge-primary">
                  {{ planEvaluation.aiSource === "spring-ai" ? "Spring AI 분석" : "기본 분석" }}
                </span>
                <p>{{ planEvaluation.aiAdvice }}</p>
              </article>
            </section>
          </section>
        </div>

        <footer class="modal-footer">
          <p v-if="message" class="notice plan-builder-message">{{ message }}</p>
          <button class="btn btn-ghost" type="button" @click="closeBuilder">닫기</button>
          <button class="btn btn-secondary" type="button" :disabled="draftPlaces.length === 0 && !draftTitle.trim() && draftDayCount === 1" @click="clearDraft">
            삭제
          </button>
          <button class="btn btn-primary" type="button" :disabled="draftPlaces.length === 0 || saving" @click="save">
            {{ saving ? "저장 중" : isEditing ? "여행 코스 수정" : "여행 코스 저장" }}
          </button>
        </footer>
      </section>
    </div>
  </section>
</template>
