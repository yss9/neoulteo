<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { RouterLink } from "vue-router";
import { searchAttractions } from "@/api/attractionApi";
import { createHotplace, deleteHotplace, getMyHotplaces, updateHotplace } from "@/api/hotplaceApi";
import { authStore } from "@/stores/authStore";

const hotplaces = ref([]);
const attractionResults = ref([]);
const selectedAttraction = ref(null);
const message = ref("");
const loading = ref(false);
const searching = ref(false);
const saving = ref(false);
const updatingId = ref(null);
const deletingId = ref(null);
const editingId = ref(null);
const errorMessage = ref("");
const searchKeyword = ref("");
const form = reactive({
  date: "",
  description: ""
});
const editForm = reactive({
  date: "",
  description: ""
});

const selectedAttractionAddress = computed(() => selectedAttraction.value?.addr1 || "주소 정보 없음");

async function load() {
  if (!authStore.isLoggedIn) {
    hotplaces.value = [];
    return;
  }

  loading.value = true;
  errorMessage.value = "";
  try {
    const data = await getMyHotplaces();
    hotplaces.value = data.items || [];
  } catch (error) {
    errorMessage.value = error.message || "내 핫플레이스를 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

async function searchPlaces() {
  message.value = "";
  attractionResults.value = [];
  selectedAttraction.value = null;

  if (!searchKeyword.value.trim()) {
    message.value = "검색어를 입력해주세요.";
    return;
  }

  searching.value = true;
  try {
    const data = await searchAttractions({
      keyword: searchKeyword.value.trim()
    });
    attractionResults.value = data.items || [];
    if (attractionResults.value.length === 0) {
      message.value = "검색 결과가 없습니다. 다른 검색어로 다시 찾아보세요.";
    }
  } catch (error) {
    message.value = error.message || "관광지 검색에 실패했습니다.";
  } finally {
    searching.value = false;
  }
}

function selectAttraction(attraction) {
  selectedAttraction.value = attraction;
  message.value = "";
}

async function submit() {
  message.value = "";
  if (!selectedAttraction.value) {
    message.value = "핫플레이스로 등록할 관광지를 먼저 선택해주세요.";
    return;
  }

  saving.value = true;
  try {
    const data = await createHotplace({
      attractionContentId: Number(selectedAttraction.value.contentid),
      date: form.date,
      description: form.description
    });
    message.value = data.message || "핫플레이스가 등록되었습니다.";
    Object.assign(form, { date: "", description: "" });
    selectedAttraction.value = null;
    attractionResults.value = [];
    searchKeyword.value = "";
    await load();
  } catch (error) {
    message.value = error.message;
  } finally {
    saving.value = false;
  }
}

function startEdit(item) {
  editingId.value = item.id;
  editForm.date = item.date || "";
  editForm.description = item.description || "";
  message.value = "";
}

function cancelEdit() {
  editingId.value = null;
  editForm.date = "";
  editForm.description = "";
}

async function submitEdit(item) {
  updatingId.value = item.id;
  message.value = "";
  try {
    const data = await updateHotplace(item.id, {
      date: editForm.date,
      description: editForm.description
    });
    message.value = data.message || "핫플레이스가 수정되었습니다.";
    cancelEdit();
    await load();
  } catch (error) {
    message.value = error.message || "핫플레이스 수정에 실패했습니다.";
  } finally {
    updatingId.value = null;
  }
}

async function removeHotplace(item) {
  if (!window.confirm(`'${item.name}' 핫플을 삭제할까요?`)) {
    return;
  }

  deletingId.value = item.id;
  message.value = "";
  try {
    const data = await deleteHotplace(item.id);
    message.value = data.message || "핫플레이스가 삭제되었습니다.";
    if (editingId.value === item.id) {
      cancelEdit();
    }
    await load();
  } catch (error) {
    message.value = error.message || "핫플레이스 삭제에 실패했습니다.";
  } finally {
    deletingId.value = null;
  }
}

onMounted(load);

watch(
  () => authStore.isLoggedIn,
  () => {
    cancelEdit();
    load();
  }
);
</script>

<template>
  <section class="hotplace-page">
    <section class="hotplace-page-header">
      <div>
        <h1>내 핫플레이스</h1>
        <p>공공 관광지 데이터에서 장소를 선택하고, 내가 직접 추천하는 이유만 추가해서 등록합니다.</p>
      </div>

      <div class="hotplace-toolbar">
        <RouterLink class="btn btn-secondary" to="/hotplaces">공개 핫플 보기</RouterLink>
        <RouterLink class="btn btn-primary" to="/attractions">관광지 검색</RouterLink>
      </div>
    </section>

    <div v-if="!authStore.isLoggedIn" class="empty-state">
      <strong class="empty-state-title">로그인이 필요합니다</strong>
      <p class="empty-state-description">내 핫플 등록과 목록 관리는 로그인 후 사용할 수 있습니다.</p>
    </div>

    <section v-else class="hotplace-layout">
      <form class="hotplace-form-card" @submit.prevent="submit">
        <div class="app-card-header">
          <h2 class="app-card-title">검증된 장소로 핫플 등록</h2>
          <p class="app-card-description">
            없는 장소를 직접 만드는 대신, 기존 관광지 정보를 선택해서 악의적인 등록을 줄입니다.
          </p>
        </div>

        <div class="hotplace-form">
          <label class="form-group">
            <span class="form-label">관광지 검색</span>
            <div class="inline-form">
              <input
                v-model="searchKeyword"
                class="form-input"
                placeholder="예: 감천문화마을, 석촌호수"
                @keydown.enter.prevent="searchPlaces"
              />
              <button class="btn btn-secondary" type="button" :disabled="searching" @click="searchPlaces">
                {{ searching ? "검색 중" : "검색" }}
              </button>
            </div>
          </label>

          <div v-if="attractionResults.length" class="hotplace-search-results">
            <button
              v-for="attraction in attractionResults"
              :key="attraction.contentid"
              class="hotplace-search-result"
              :class="{ 'hotplace-search-result-active': selectedAttraction?.contentid === attraction.contentid }"
              type="button"
              @click="selectAttraction(attraction)"
            >
              <img v-if="attraction.firstimage" :src="attraction.firstimage" :alt="attraction.title" />
              <span v-else class="hotplace-search-thumb">No image</span>
              <span>
                <strong>{{ attraction.title }}</strong>
                <small>{{ attraction.addr1 || "주소 정보 없음" }}</small>
              </span>
            </button>
          </div>

          <div v-if="selectedAttraction" class="selected-attraction-card">
            <span class="badge badge-accent">선택한 장소</span>
            <strong>{{ selectedAttraction.title }}</strong>
            <span>{{ selectedAttractionAddress }}</span>
          </div>

          <label class="form-group">
            <span class="form-label">방문일</span>
            <input v-model="form.date" class="form-input" type="date" />
          </label>

          <label class="form-group">
            <span class="form-label">추천 이유</span>
            <textarea
              v-model="form.description"
              class="form-textarea"
              placeholder="이 장소를 추천하는 이유나 방문 팁을 적어주세요."
            />
          </label>

          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? "등록 중" : "핫플레이스 등록" }}
          </button>

          <p v-if="message" class="notice">{{ message }}</p>
        </div>
      </form>

      <section class="hotplace-list-section">
        <div class="hotplace-list-header">
          <div>
            <h2>내가 등록한 핫플</h2>
            <p>{{ hotplaces.length }}개의 장소를 등록했습니다.</p>
          </div>
          <button class="btn btn-ghost btn-sm" type="button" @click="load">다시 불러오기</button>
        </div>

        <div v-if="loading" class="hotplace-grid">
          <div class="loading-skeleton hotplace-card-skeleton"></div>
          <div class="loading-skeleton hotplace-card-skeleton"></div>
        </div>

        <p v-else-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div v-else-if="hotplaces.length === 0" class="hotplace-empty-state">
          <strong>아직 등록한 핫플이 없습니다</strong>
          <p>관광지를 검색해서 첫 핫플을 등록해보세요.</p>
        </div>

        <div v-else class="hotplace-grid">
          <article v-for="item in hotplaces" :key="item.id" class="hotplace-card">
            <img
              v-if="item.imageUrl || item.firstimage"
              :src="item.imageUrl || item.firstimage"
              :alt="item.name"
              class="hotplace-card-image"
            />
            <div v-else class="hotplace-card-image placeholder">No image</div>
            <div class="hotplace-card-header">
              <span class="badge badge-soft">{{ item.type || "핫플레이스" }}</span>
              <span class="hotplace-card-meta">{{ item.date || "방문일 미정" }}</span>
            </div>
            <strong class="hotplace-card-title">{{ item.name }}</strong>
            <span class="hotplace-card-meta">{{ item.address || "주소 정보 없음" }}</span>
            <p class="hotplace-card-description">{{ item.description || "아직 설명이 없습니다." }}</p>

            <form v-if="editingId === item.id" class="hotplace-edit-form" @submit.prevent="submitEdit(item)">
              <label class="form-group">
                <span class="form-label">방문일</span>
                <input v-model="editForm.date" class="form-input" type="date" />
              </label>
              <label class="form-group">
                <span class="form-label">추천 이유</span>
                <textarea v-model="editForm.description" class="form-textarea" />
              </label>
              <div class="hotplace-card-actions">
                <button class="btn btn-primary btn-sm" type="submit" :disabled="updatingId === item.id">
                  {{ updatingId === item.id ? "저장 중" : "저장" }}
                </button>
                <button class="btn btn-ghost btn-sm" type="button" @click="cancelEdit">취소</button>
              </div>
            </form>

            <div v-else class="hotplace-card-actions">
              <button class="btn btn-secondary btn-sm" type="button" @click="startEdit(item)">수정</button>
              <button
                class="btn btn-danger btn-sm"
                type="button"
                :disabled="deletingId === item.id"
                @click="removeHotplace(item)"
              >
                {{ deletingId === item.id ? "삭제 중" : "삭제" }}
              </button>
            </div>
          </article>
        </div>
      </section>
    </section>
  </section>
</template>
