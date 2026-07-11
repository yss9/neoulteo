<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { fetchGuguns, fetchSidos, searchAttractions } from "@/api/attractionApi";
import { deleteSavedPlace, fetchSavedPlaces, savePlace } from "@/api/savedPlaceApi";
import KakaoMap from "@/components/KakaoMap.vue";
import { KOREA_REGIONS } from "@/constants/regions";
import { SIGUNGU_BY_AREA_CODE } from "@/constants/sigungus";

const route = useRoute();
const router = useRouter();
const sidos = ref([]);
const guguns = ref([]);
const items = ref([]);
const savedPlaces = ref([]);
const selectedItem = ref(null);
const message = ref("");
const errorMessage = ref("");
const loading = ref(false);
const routeReady = ref(false);
const filterOpen = ref(false);
const targetAttractionId = ref("");
const savingPlaceId = ref("");
const saveMessage = ref("");
const savedPlacesLoading = ref(false);
const deletingSavedPlaceId = ref("");
const form = reactive({
  areaCode: "",
  gugunCode: "",
  contentTypeId: "",
  keyword: ""
});

const contentTypes = [
  { value: "12", label: "관광지" },
  { value: "14", label: "문화시설" },
  { value: "15", label: "축제/공연" },
  { value: "39", label: "음식점" }
];

const resultTitle = computed(() => (items.value.length === 0 ? "검색 결과" : `검색 결과 ${items.value.length}개`));

const selectedAreaName = computed(() => {
  const selected = sidos.value.find((sido) => String(sido.code) === String(form.areaCode));
  return selected?.name || "";
});

const selectedGugunName = computed(() => {
  const selected = guguns.value.find((gugun) => String(gugun.code) === String(form.gugunCode));
  return selected?.name || "";
});

const selectedContentTypeName = computed(() => {
  const selected = contentTypes.find((type) => String(type.value) === String(form.contentTypeId));
  return selected?.label || "";
});

const activeQueryBadges = computed(() => {
  const badges = [];
  if (selectedAreaName.value) badges.push(`지역 ${selectedAreaName.value}`);
  if (selectedGugunName.value) badges.push(`구군 ${selectedGugunName.value}`);
  if (selectedContentTypeName.value) badges.push(`분류 ${selectedContentTypeName.value}`);
  if (form.keyword) badges.push(`키워드 ${form.keyword}`);
  return badges;
});

const selectedItemTitle = computed(() => selectedItem.value?.title || "");
const hasSearched = computed(() => loading.value || message.value || errorMessage.value || items.value.length > 0);

onMounted(async () => {
  try {
    const data = await fetchSidos();
    sidos.value = mergeSidos(data.items || []);
  } catch {
    sidos.value = mergeSidos([]);
  }
  await loadSavedPlaces();
  await applyRouteQueryAndSearch();
  routeReady.value = true;
});

async function loadSavedPlaces() {
  savedPlacesLoading.value = true;
  try {
    const data = await fetchSavedPlaces();
    savedPlaces.value = data.items || [];
  } catch {
    savedPlaces.value = [];
  } finally {
    savedPlacesLoading.value = false;
  }
}

function mergeSidos(apiSidos) {
  const apiMap = new Map((apiSidos || []).map((sido) => [String(sido.code), sido]));
  const regionSidos = KOREA_REGIONS.map((region) => {
    const apiSido = apiMap.get(String(region.areaCode));
    return {
      code: String(region.areaCode),
      name: region.displayName || region.name || apiSido?.name || String(region.areaCode)
    };
  });
  const regionCodes = new Set(regionSidos.map((sido) => String(sido.code)));
  const extraSidos = (apiSidos || [])
    .filter((sido) => !regionCodes.has(String(sido.code)))
    .map((sido) => ({ ...sido, code: String(sido.code) }));
  return [...regionSidos, ...extraSidos];
}

function mergeGuguns(sidoCode, apiGuguns) {
  const fallback = SIGUNGU_BY_AREA_CODE[String(sidoCode)] || [];
  const apiMap = new Map((apiGuguns || []).map((gugun) => [String(gugun.code), gugun]));
  const mergedFallback = fallback.map((gugun) => {
    const apiGugun = apiMap.get(String(gugun.code));
    return {
      code: String(gugun.code),
      name: apiGugun?.name || gugun.name
    };
  });
  const fallbackCodes = new Set(mergedFallback.map((gugun) => String(gugun.code)));
  const extraGuguns = (apiGuguns || [])
    .filter((gugun) => !fallbackCodes.has(String(gugun.code)))
    .map((gugun) => ({ ...gugun, code: String(gugun.code) }));
  return [...mergedFallback, ...extraGuguns];
}

function normalize(value) {
  return String(value || "").replace(/\s/g, "").toLowerCase();
}

function findAreaCode(area) {
  if (!area) return "";

  const normalizedArea = normalize(area);
  const region = KOREA_REGIONS.find((item) =>
    item.name === area || item.aliases.some((alias) => normalize(alias) === normalizedArea)
  );

  const matchedSido = sidos.value.find((sido) => {
    const sidoName = normalize(sido.name);
    const regionAliases = region?.aliases.map(normalize) || [];
    return sidoName === normalizedArea || regionAliases.includes(sidoName);
  });

  if (matchedSido) {
    return String(matchedSido.code);
  }

  if (region) {
    return region.areaCode;
  }

  console.warn(`Unknown area query: ${area}`);
  return "";
}

async function applyRouteQueryAndSearch() {
  const area = route.query.area ? String(route.query.area) : "";
  const keyword = route.query.keyword ? String(route.query.keyword) : "";
  targetAttractionId.value = route.query.attractionId ? String(route.query.attractionId) : "";

  form.areaCode = findAreaCode(area);
  form.gugunCode = "";
  form.keyword = keyword;

  if (form.areaCode || form.keyword) {
    await search();
  }
}

watch(
  () => [route.query.area, route.query.keyword, route.query.attractionId],
  async () => {
    if (!routeReady.value) return;
    await applyRouteQueryAndSearch();
  }
);

watch(
  () => form.areaCode,
  async (sidoCode) => {
    form.gugunCode = "";
    guguns.value = [];
    if (!sidoCode) return;
    try {
      const data = await fetchGuguns(sidoCode);
      guguns.value = mergeGuguns(sidoCode, data.items || []);
    } catch {
      guguns.value = mergeGuguns(sidoCode, []);
    }
  }
);

async function search() {
  message.value = "";
  errorMessage.value = "";
  saveMessage.value = "";
  loading.value = true;
  try {
    const data = await searchAttractions(form);
    items.value = data.items || [];
    selectedItem.value =
      items.value.find((item) => String(item.contentid) === String(targetAttractionId.value)) || items.value[0] || null;
    if (items.value.length === 0) {
      message.value = "검색 결과가 없습니다. 다른 지역이나 키워드로 다시 검색해보세요.";
    }
    filterOpen.value = false;
  } catch (error) {
    items.value = [];
    selectedItem.value = null;
    errorMessage.value = error.message || "관광지 정보를 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  form.areaCode = "";
  form.gugunCode = "";
  form.contentTypeId = "";
  form.keyword = "";
  guguns.value = [];
  items.value = [];
  selectedItem.value = null;
  message.value = "";
  errorMessage.value = "";
  saveMessage.value = "";
}

function selectItem(item) {
  selectedItem.value = item;
}

async function saveAttraction(item) {
  saveMessage.value = "";
  const contentId = item?.contentid || item?.contentId;
  if (!contentId) {
    saveMessage.value = "저장할 관광지 정보를 확인할 수 없습니다.";
    return;
  }

  savingPlaceId.value = String(contentId);
  try {
    const data = await savePlace(contentId, "ATTRACTION");
    saveMessage.value = data.message || "관광지를 저장했습니다.";
    await loadSavedPlaces();
  } catch (error) {
    saveMessage.value = error.message || "관광지 저장에 실패했습니다.";
  } finally {
    savingPlaceId.value = "";
  }
}

async function removeSavedPlace(item) {
  saveMessage.value = "";
  deletingSavedPlaceId.value = item.id;
  try {
    const data = await deleteSavedPlace(item.id);
    saveMessage.value = data.message || "저장한 관광지를 삭제했습니다.";
    savedPlaces.value = savedPlaces.value.filter((place) => place.id !== item.id);
  } catch (error) {
    saveMessage.value = error.message || "저장한 관광지 삭제에 실패했습니다.";
  } finally {
    deletingSavedPlaceId.value = "";
  }
}

function createPlanWithSavedPlaces() {
  if (savedPlaces.value.length === 0) return;

  router.push({
    path: "/plans",
    query: {
      builder: "1",
      savedPlaces: "all"
    }
  });
}
</script>

<template>
  <section class="attraction-page">
    <button
      class="btn btn-secondary attraction-filter-toggle"
      type="button"
      :aria-expanded="filterOpen"
      @click="filterOpen = !filterOpen"
    >
      필터 {{ filterOpen ? "닫기" : "열기" }}
    </button>

    <section class="attraction-search-layout">
      <form class="attraction-filter-panel" :class="{ 'attraction-filter-panel-open': filterOpen }" @submit.prevent="search">
        <div class="app-card-header">
          <h2 class="app-card-title">검색 필터</h2>
          <p class="app-card-description">원하는 조건을 선택해 관광지를 찾아보세요.</p>
        </div>

        <label class="attraction-filter-group">
          <span>지역</span>
          <select v-model="form.areaCode">
            <option value="">전체 지역</option>
            <option v-for="sido in sidos" :key="sido.code" :value="String(sido.code)">{{ sido.name }}</option>
          </select>
        </label>

        <label class="attraction-filter-group">
          <span>구군</span>
          <select v-model="form.gugunCode" :disabled="!form.areaCode">
            <option value="">전체 구군</option>
            <option v-for="gugun in guguns" :key="gugun.code" :value="gugun.code">{{ gugun.name }}</option>
          </select>
        </label>

        <label class="attraction-filter-group">
          <span>카테고리</span>
          <select v-model="form.contentTypeId">
            <option value="">전체 카테고리</option>
            <option v-for="type in contentTypes" :key="type.value" :value="type.value">{{ type.label }}</option>
          </select>
        </label>

        <label class="attraction-filter-group">
          <span>키워드</span>
          <input v-model="form.keyword" placeholder="예: 바다, 한옥, 축제" />
        </label>

        <div v-if="activeQueryBadges.length" class="attraction-active-filters compact">
          <span v-for="badge in activeQueryBadges" :key="`panel-${badge}`" class="badge badge-soft">{{ badge }}</span>
        </div>

        <div class="filter-actions">
          <button class="btn btn-primary" type="submit" :disabled="loading">{{ loading ? "검색 중" : "검색" }}</button>
          <button class="btn btn-ghost" type="button" @click="resetFilters">초기화</button>
        </div>

        <button class="btn btn-primary attraction-create-plan-button" type="button" :disabled="savedPlaces.length === 0" @click="createPlanWithSavedPlaces">
          코스 만들기
        </button>
      </form>

      <section class="attraction-map-panel">
        <div class="attraction-map-toolbar">
          <div>
            <strong>{{ resultTitle }}</strong>
            <span v-if="selectedItemTitle">선택: {{ selectedItemTitle }}</span>
            <span v-else>리스트를 클릭하면 지도가 해당 장소로 이동합니다.</span>
          </div>
        </div>
        <div class="attraction-map-shell">
          <KakaoMap :spots="items" :selected-spot="selectedItem" />
        </div>
      </section>

      <aside class="attraction-results-panel">
        <div class="attraction-results-header">
          <div>
            <strong>{{ resultTitle }}</strong>
            <span>관광지 카드를 눌러 지도 위치를 확인하세요.</span>
          </div>
        </div>
        <p v-if="saveMessage" class="notice attraction-save-message">{{ saveMessage }}</p>

        <div v-if="loading" class="attraction-results-list">
          <div class="loading-skeleton attraction-result-skeleton"></div>
          <div class="loading-skeleton attraction-result-skeleton"></div>
          <div class="loading-skeleton attraction-result-skeleton"></div>
        </div>

        <p v-else-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>

        <div v-else-if="items.length === 0" class="attraction-empty-state">
          <strong>{{ hasSearched ? "검색 결과가 없습니다" : "검색을 시작해보세요" }}</strong>
          <p>{{ message || "지역이나 키워드를 선택한 뒤 검색 버튼을 눌러보세요." }}</p>
        </div>

        <div v-else class="attraction-results-list">
          <article
            v-for="item in items"
            :key="item.contentid"
            class="attraction-result-card"
            :class="{ 'attraction-result-card-active': selectedItem?.contentid === item.contentid }"
            :aria-current="selectedItem?.contentid === item.contentid ? 'true' : undefined"
            tabindex="0"
            @click="selectItem(item)"
            @keydown.enter.prevent="selectItem(item)"
            @keydown.space.prevent="selectItem(item)"
          >
            <img v-if="item.firstimage" :src="item.firstimage" :alt="item.title" class="attraction-result-thumb" />
            <div v-else class="attraction-result-thumb placeholder">No image</div>

            <div class="attraction-result-body">
              <strong class="attraction-result-title">{{ item.title }}</strong>
              <span class="attraction-result-address">{{ item.addr1 || "주소 정보 없음" }}</span>
              <p v-if="item.overview">{{ item.overview }}</p>
              <div class="attraction-result-meta">
                <span>Area {{ item.areacode || "-" }}</span>
                <span>District {{ item.sigungucode || "-" }}</span>
              </div>
              <div class="attraction-result-actions">
                <button
                  class="btn btn-primary btn-sm"
                  type="button"
                  :disabled="savingPlaceId === String(item.contentid)"
                  @click.stop="saveAttraction(item)"
                >
                  {{ savingPlaceId === String(item.contentid) ? "저장 중" : "저장" }}
                </button>
              </div>
            </div>
          </article>
        </div>
      </aside>

      <section class="attraction-saved-panel">
        <div class="plan-list-header compact">
          <button class="btn btn-primary attraction-create-plan-button" type="button" :disabled="savedPlaces.length === 0" @click="createPlanWithSavedPlaces">
            코스 만들기
          </button>
          <div>
            <strong>내가 저장한 관광지</strong>
            <p>저장한 장소를 한 번에 여행 코스로 만들 수 있습니다.</p>
          </div>
        </div>

        <div v-if="savedPlacesLoading" class="saved-place-list saved-place-list-cards">
          <div class="loading-skeleton saved-place-skeleton"></div>
          <div class="loading-skeleton saved-place-skeleton"></div>
          <div class="loading-skeleton saved-place-skeleton"></div>
        </div>

        <div v-else-if="savedPlaces.length === 0" class="plan-empty-state compact">
          <strong>저장한 관광지가 없습니다</strong>
          <p>검색 결과에서 저장 버튼을 눌러보세요.</p>
        </div>

        <div v-else class="saved-place-list saved-place-list-cards">
          <article v-for="item in savedPlaces" :key="item.id" class="saved-place-card saved-place-card-large">
            <img v-if="item.imageUrl || item.firstimage" :src="item.imageUrl || item.firstimage" :alt="item.name" />
            <span v-else class="saved-place-thumb">No image</span>
            <div class="saved-place-main">
              <strong>{{ item.name }}</strong>
              <small>{{ item.address || "주소 정보 없음" }}</small>
            </div>
            <div class="saved-place-actions">
              <button class="btn btn-ghost btn-sm" type="button" :disabled="deletingSavedPlaceId === item.id" @click="removeSavedPlace(item)">
                {{ deletingSavedPlaceId === item.id ? "삭제 중" : "삭제" }}
              </button>
            </div>
          </article>
        </div>
      </section>
    </section>
  </section>
</template>
