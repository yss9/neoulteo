<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { getHotplaces, getPopularHotplaces } from "@/api/hotplaceApi";
import { savePlace } from "@/api/savedPlaceApi";
import { KOREA_REGIONS } from "@/constants/regions";
import { authStore } from "@/stores/authStore";

const route = useRoute();
const router = useRouter();

const hotplaces = ref([]);
const popularHotplaces = ref([]);
const loading = ref(false);
const popularLoading = ref(false);
const errorMessage = ref("");
const saveMessage = ref("");
const savingPlaceId = ref("");
const hotplacePage = ref(1);
const selectedHotplaceGroup = ref(null);
const selectedReviewIndex = ref(0);
const hotplacePageSize = 6;

const activeRegion = computed(() => (route.query.region ? String(route.query.region) : ""));
const activeRegionMeta = computed(() => KOREA_REGIONS.find((region) => region.name === activeRegion.value));
const groupedHotplaces = computed(() => {
  const groups = new Map();
  hotplaces.value.forEach((item) => {
    const key = String(item.attractionContentId || item.name || item.id);
    const current = groups.get(key);
    if (!current) {
      groups.set(key, {
        ...item,
        reviewCount: 1,
        reviews: [item]
      });
      return;
    }
    current.reviewCount += 1;
    current.reviews.push(item);
  });

  return [...groups.values()]
    .map((group) => ({
      ...group,
      reviews: group.reviews.sort((left, right) => reviewTime(right) - reviewTime(left)),
      latestReview: group.reviews.sort((left, right) => reviewTime(right) - reviewTime(left))[0]
    }))
    .sort((left, right) => {
      if (right.reviewCount !== left.reviewCount) return right.reviewCount - left.reviewCount;
      return reviewTime(right.latestReview) - reviewTime(left.latestReview);
    });
});
const hotplaceTotalPages = computed(() => Math.max(Math.ceil(groupedHotplaces.value.length / hotplacePageSize), 1));
const paginatedHotplaces = computed(() => {
  const start = (hotplacePage.value - 1) * hotplacePageSize;
  return groupedHotplaces.value.slice(start, start + hotplacePageSize);
});
const selectedReview = computed(() => selectedHotplaceGroup.value?.reviews?.[selectedReviewIndex.value] || null);

async function loadPublicHotplaces() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const data = await getHotplaces(activeRegion.value, activeRegionMeta.value?.areaCode);
    hotplaces.value = data.items || [];
    hotplacePage.value = 1;
  } catch (error) {
    errorMessage.value = error.message || "핫플레이스를 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

async function loadPopularHotplaces() {
  popularLoading.value = true;
  try {
    const data = await getPopularHotplaces(activeRegion.value, 5, activeRegionMeta.value?.areaCode);
    popularHotplaces.value = data.items || [];
  } catch {
    popularHotplaces.value = [];
  } finally {
    popularLoading.value = false;
  }
}

async function load() {
  await Promise.all([loadPublicHotplaces(), loadPopularHotplaces()]);
}

function clearRegionFilter() {
  router.push({ path: "/hotplaces" });
}

function imageUrlOf(item) {
  return item.imageUrl || item.firstimage || "";
}

function reviewTime(item) {
  return new Date(item?.date || item?.createdAt || 0).getTime() || 0;
}

function openHotplaceReviews(group) {
  selectedHotplaceGroup.value = group;
  selectedReviewIndex.value = 0;
}

function closeHotplaceReviews() {
  selectedHotplaceGroup.value = null;
  selectedReviewIndex.value = 0;
}

function moveReview(direction) {
  if (!selectedHotplaceGroup.value?.reviews?.length) return;
  const count = selectedHotplaceGroup.value.reviews.length;
  selectedReviewIndex.value = (selectedReviewIndex.value + direction + count) % count;
}

function setHotplacePage(page) {
  hotplacePage.value = Math.min(Math.max(page, 1), hotplaceTotalPages.value);
}

async function saveHotplacePlace(item) {
  saveMessage.value = "";
  if (!authStore.isLoggedIn) {
    saveMessage.value = "로그인이 필요합니다.";
    return;
  }
  if (!item.attractionContentId) {
    saveMessage.value = "장소 정보를 확인할 수 없습니다.";
    return;
  }

  savingPlaceId.value = String(item.attractionContentId);
  try {
    const data = await savePlace(item.attractionContentId, "HOTPLACE");
    saveMessage.value = data.message || "관광지를 저장했습니다.";
  } catch (error) {
    saveMessage.value = error.message || "관광지 저장에 실패했습니다.";
  } finally {
    savingPlaceId.value = "";
  }
}

function shareHotplaceToCommunity(item) {
  const title = `[핫플 추천] ${item.name || "추천 장소"}`;
  const lines = [
    `${item.name || "추천 장소"}를 소개합니다.`,
    "",
    `주소: ${item.address || "주소 정보 없음"}`,
    `방문일: ${item.date || "방문일 미정"}`,
    item.type ? `분류: ${item.type}` : "",
    item.writerName || item.writerEmail ? `등록자: ${item.writerName || item.writerEmail}` : "",
    "",
    item.description || "이 장소를 추천합니다."
  ].filter(Boolean);

  router.push({
    path: "/community/write",
    query: {
      category: "REVIEW",
      title,
      content: lines.join("\n"),
      imageUrl: imageUrlOf(item)
    }
  });
}

onMounted(load);

watch(
  () => route.query.region,
  () => {
    hotplacePage.value = 1;
    load();
  }
);
</script>

<template>
  <section class="hotplace-page">
    <div class="hotplace-toolbar hotplace-page-actions">
      <div v-if="activeRegionMeta" class="filter-badge-row">
        <span class="badge badge-soft">지역 필터 {{ activeRegionMeta.displayName || activeRegion }}</span>
        <button type="button" class="btn btn-ghost btn-sm" @click="clearRegionFilter">전체 보기</button>
      </div>
      <RouterLink class="btn btn-secondary" to="/hotplaces/my">내 핫플 관리</RouterLink>
      <RouterLink class="btn btn-primary" to="/attractions">관광지 검색</RouterLink>
    </div>

    <section class="hotplace-public-layout">
      <section class="hotplace-list-section">
        <div class="hotplace-list-header">
          <div>
            <h2>인기 핫플 TOP 5</h2>
            <p>같은 관광지를 많이 등록한 순서로 보여줍니다.</p>
          </div>
        </div>

        <div v-if="popularLoading" class="hotplace-grid">
          <div class="loading-skeleton hotplace-card-skeleton"></div>
          <div class="loading-skeleton hotplace-card-skeleton"></div>
        </div>

        <div v-else-if="popularHotplaces.length === 0" class="hotplace-empty-state compact">
          <strong>아직 집계된 인기 핫플이 없습니다</strong>
          <p>사용자 등록이 쌓이면 상위 장소가 표시됩니다.</p>
        </div>

        <div v-else class="hotplace-grid">
          <article v-for="(item, index) in popularHotplaces" :key="`popular-${item.id}`" class="hotplace-card">
            <img v-if="imageUrlOf(item)" :src="imageUrlOf(item)" :alt="item.name" class="hotplace-card-image" />
            <div v-else class="hotplace-card-image placeholder">No image</div>
            <div class="hotplace-card-header">
              <span class="badge badge-accent">TOP {{ index + 1 }}</span>
              <span class="hotplace-card-meta">{{ item.hotplaceCount || 1 }}명 등록</span>
            </div>
            <strong class="hotplace-card-title">{{ item.name }}</strong>
            <span class="hotplace-card-meta">{{ item.address || "주소 정보 없음" }}</span>
            <p class="hotplace-card-description">{{ item.description || "사용자 추천이 쌓이고 있는 장소입니다." }}</p>
            <div class="hotplace-card-actions">
              <button class="btn btn-ghost btn-sm" type="button" :disabled="savingPlaceId === String(item.attractionContentId)" @click="saveHotplacePlace(item)">
                {{ savingPlaceId === String(item.attractionContentId) ? "저장 중" : "저장" }}
              </button>
              <button class="btn btn-secondary btn-sm" type="button" @click="shareHotplaceToCommunity(item)">
                커뮤니티 공유
              </button>
            </div>
          </article>
        </div>
      </section>

      <section class="hotplace-list-section">
        <div class="hotplace-list-header">
          <div>
            <h2>{{ activeRegion ? `${activeRegion} 공개 핫플` : "전체 공개 핫플" }}</h2>
            <p>{{ groupedHotplaces.length }}개의 장소가 표시됩니다. 같은 장소의 후기는 하나로 묶어 보여줍니다.</p>
          </div>
          <button class="btn btn-ghost btn-sm refresh-icon-button" type="button" aria-label="핫플레이스 새로고침" title="새로고침" @click="load">
            <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
              <path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.96-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8Zm-6.76 3.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3c-3.31 0-6-2.69-6-6 0-1.01.25-1.96.7-2.8L5.24 7.74Z" />
            </svg>
          </button>
        </div>

        <p v-if="saveMessage" class="notice">{{ saveMessage }}</p>

        <div v-if="loading" class="hotplace-grid">
          <div class="loading-skeleton hotplace-card-skeleton"></div>
          <div class="loading-skeleton hotplace-card-skeleton"></div>
          <div class="loading-skeleton hotplace-card-skeleton"></div>
        </div>

        <p v-else-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div v-else-if="groupedHotplaces.length === 0" class="hotplace-empty-state">
          <strong>{{ activeRegion ? `${activeRegion}에 등록된 핫플이 없습니다` : "아직 등록된 핫플이 없습니다" }}</strong>
          <p>관광지 검색에서 장소를 고른 뒤 내 핫플로 등록해보세요.</p>
          <RouterLink class="btn btn-secondary" to="/hotplaces/my">내 핫플 등록하기</RouterLink>
        </div>

        <div v-else class="hotplace-grid">
          <article v-for="item in paginatedHotplaces" :key="item.attractionContentId || item.id" class="hotplace-card hotplace-card-clickable" @click="openHotplaceReviews(item)">
            <img v-if="imageUrlOf(item)" :src="imageUrlOf(item)" :alt="item.name" class="hotplace-card-image" />
            <div v-else class="hotplace-card-image placeholder">No image</div>
            <div class="hotplace-card-header">
              <span class="badge badge-soft">{{ item.type || "핫플레이스" }}</span>
              <span class="hotplace-card-meta">후기 {{ item.reviewCount }}개</span>
            </div>
            <strong class="hotplace-card-title">{{ item.name }}</strong>
            <span class="hotplace-card-meta">{{ item.address || "주소 정보 없음" }}</span>
            <p class="hotplace-card-description">{{ item.latestReview?.description || "아직 설명이 없습니다." }}</p>
            <span class="hotplace-card-meta">
              최근 후기 {{ item.latestReview?.writerName || item.latestReview?.writerEmail || "작성자" }} · {{ item.latestReview?.date || "방문일 미정" }}
            </span>
            <div class="hotplace-card-actions">
              <button class="btn btn-ghost btn-sm" type="button" :disabled="savingPlaceId === String(item.attractionContentId)" @click.stop="saveHotplacePlace(item)">
                {{ savingPlaceId === String(item.attractionContentId) ? "저장 중" : "저장" }}
              </button>
              <button class="btn btn-secondary btn-sm" type="button" @click.stop="openHotplaceReviews(item)">
                후기 보기
              </button>
            </div>
          </article>
        </div>

        <nav v-if="hotplaceTotalPages > 1" class="pagination" aria-label="핫플레이스 페이지">
          <button class="btn btn-ghost btn-sm" type="button" :disabled="hotplacePage === 1" @click="setHotplacePage(hotplacePage - 1)">이전</button>
          <button
            v-for="page in hotplaceTotalPages"
            :key="`hotplace-page-${page}`"
            class="btn btn-sm"
            :class="page === hotplacePage ? 'btn-primary' : 'btn-ghost'"
            type="button"
            @click="setHotplacePage(page)"
          >
            {{ page }}
          </button>
          <button class="btn btn-ghost btn-sm" type="button" :disabled="hotplacePage === hotplaceTotalPages" @click="setHotplacePage(hotplacePage + 1)">다음</button>
        </nav>
      </section>
    </section>

    <div v-if="selectedHotplaceGroup" class="modal-backdrop" role="presentation">
      <section class="modal-panel hotplace-review-modal" role="dialog" aria-modal="true">
        <button class="hotplace-review-close" type="button" aria-label="핫플 후기 닫기" @click="closeHotplaceReviews">×</button>
        <header class="modal-header hotplace-review-header">
          <div>
            <span class="badge badge-accent">핫플 후기 {{ selectedHotplaceGroup.reviewCount }}개</span>
            <h2>{{ selectedHotplaceGroup.name }}</h2>
            <p>{{ selectedHotplaceGroup.address || "주소 정보 없음" }}</p>
          </div>
        </header>

        <div class="hotplace-review-body">
          <div class="hotplace-review-media">
            <img v-if="imageUrlOf(selectedHotplaceGroup)" :src="imageUrlOf(selectedHotplaceGroup)" :alt="selectedHotplaceGroup.name" />
            <div v-else class="hotplace-review-placeholder">No image</div>
            <div class="hotplace-review-media-caption">
              <span class="badge badge-soft">{{ selectedHotplaceGroup.type || "핫플레이스" }}</span>
              <strong>{{ selectedHotplaceGroup.reviewCount }}명이 남긴 장소</strong>
            </div>
          </div>

          <article v-if="selectedReview" class="hotplace-review-content">
            <div class="hotplace-review-meta">
              <span class="badge badge-primary">최근 후기순</span>
              <strong>{{ selectedReview.writerName || selectedReview.writerEmail || "작성자" }}</strong>
              <small>{{ selectedReview.date || "방문일 미정" }}</small>
            </div>
            <blockquote>{{ selectedReview.description || "작성된 후기가 없습니다." }}</blockquote>
            <div class="hotplace-review-counter">
              <button class="hotplace-review-nav" type="button" :disabled="selectedHotplaceGroup.reviewCount <= 1" @click="moveReview(-1)">‹</button>
              <span>{{ selectedReviewIndex + 1 }} / {{ selectedHotplaceGroup.reviewCount }}</span>
              <button class="hotplace-review-nav" type="button" :disabled="selectedHotplaceGroup.reviewCount <= 1" @click="moveReview(1)">›</button>
            </div>
          </article>
        </div>

        <footer class="modal-footer hotplace-review-footer">
          <button class="btn btn-primary" type="button" :disabled="savingPlaceId === String(selectedHotplaceGroup.attractionContentId)" @click="saveHotplacePlace(selectedHotplaceGroup)">
            저장
          </button>
          <button class="btn btn-secondary" type="button" @click="shareHotplaceToCommunity(selectedReview || selectedHotplaceGroup)">
            커뮤니티 공유
          </button>
        </footer>
      </section>
    </div>
  </section>
</template>
