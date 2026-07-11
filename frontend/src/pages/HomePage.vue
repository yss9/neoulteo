<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import KoreaRegionMap from "@/components/home/KoreaRegionMap.vue";
import { fetchHotplaces, getPopularHotplaces } from "@/api/hotplaceApi";
import { KOREA_REGIONS } from "@/constants/regions";

const router = useRouter();
const allHotplaces = ref([]);
const selectedHotplaces = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const selectedRegion = ref("");
const previewRegion = ref("");
const introActiveIndex = ref(0);
const introComplete = ref(false);

const introCards = [
  {
    kicker: "관광지 검색",
    title: "조건에 맞는 관광지를 지도에서 바로 찾기",
    description: "지역, 구군, 카테고리, 키워드를 조합해 관광지를 검색하고 지도 핀과 결과 목록으로 위치를 한눈에 확인할 수 있습니다.",
    image: "/intro-attractions.png"
  },
  {
    kicker: "여행 계획",
    title: "저장한 코스를 일차별 지도와 함께 보기",
    description: "내가 만든 여행 코스를 날짜별로 나누어 확인하고, 자동차와 도보 이동 경로를 지도에서 비교하며 일정을 점검합니다.",
    image: "/intro-plan-map.png"
  },
  {
    kicker: "AI 코스 평가",
    title: "AI가 여행 동선을 분석하고 개선점 제안",
    description: "선택한 코스를 점수와 일차별 거리로 분석하고, Spring AI 기반 조언으로 더 균형 잡힌 여행 동선을 만들어갑니다.",
    image: "/intro-ai-evaluation.png"
  },
  {
    kicker: "핫플레이스",
    title: "사람들이 많이 저장한 인기 장소 둘러보기",
    description: "사용자들이 등록한 핫플레이스를 순위로 확인하고, 마음에 드는 장소를 저장하거나 커뮤니티에 공유할 수 있습니다.",
    image: "/intro-hotplaces.png"
  },
  {
    kicker: "커뮤니티",
    title: "여행 코스와 후기를 함께 나누기",
    description: "여행자들이 공유한 코스와 후기를 살펴보고, 마음에 드는 계획을 참고해 나만의 여행으로 이어갈 수 있습니다.",
    image: "/intro-community.png"
  }
];

const currentIntro = computed(() => introCards[introActiveIndex.value]);

const hotplacesByRegion = computed(() => {
  const grouped = Object.fromEntries(KOREA_REGIONS.map((region) => [region.name, []]));

  allHotplaces.value.forEach((hotplace) => {
    const areaCode = String(hotplace.areaCode || "");
    const regionByCode = KOREA_REGIONS.find((item) => item.areaCode === areaCode);
    if (regionByCode) {
      grouped[regionByCode.name].push(hotplace);
      return;
    }

    const searchable = `${hotplace.name || ""} ${hotplace.type || ""} ${hotplace.description || ""} ${hotplace.address || ""}`;
    const region = KOREA_REGIONS.find((item) => item.aliases.some((alias) => searchable.includes(alias)));
    if (region) {
      grouped[region.name].push(hotplace);
    }
  });

  return grouped;
});

const regionCounts = computed(() =>
  Object.fromEntries(KOREA_REGIONS.map((region) => [region.name, hotplacesByRegion.value[region.name]?.length || 0]))
);

const panelHotplaces = computed(() => selectedHotplaces.value.slice(0, 5));
const panelCount = computed(() => regionCounts.value[selectedRegion.value] || 0);

async function loadAllHotplaces() {
  try {
    const data = await fetchHotplaces();
    allHotplaces.value = data.items || [];
  } catch {
    allHotplaces.value = [];
  }
}

async function selectRegion(region) {
  selectedRegion.value = region;
  previewRegion.value = "";
  loading.value = true;
  errorMessage.value = "";
  try {
    const regionMeta = KOREA_REGIONS.find((item) => item.name === region);
    const data = await getPopularHotplaces(region, 5, regionMeta?.areaCode);
    selectedHotplaces.value = data.items || [];
  } catch (error) {
    selectedHotplaces.value = hotplacesByRegion.value[region] || [];
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

function closeRegion() {
  selectedRegion.value = "";
  selectedHotplaces.value = [];
  errorMessage.value = "";
  loading.value = false;
}

function openAttractions() {
  if (!selectedRegion.value) return;
  router.push({ path: "/attractions", query: { area: selectedRegion.value } });
}

function openHotplaces() {
  if (!selectedRegion.value) return;
  router.push({ path: "/hotplaces", query: { region: selectedRegion.value } });
}

function openHotplaceAttraction(hotplace) {
  if (!hotplace) return;
  router.push({
    path: "/attractions",
    query: {
      area: selectedRegion.value || undefined,
      keyword: hotplace.name || undefined,
      attractionId: hotplace.attractionContentId ? String(hotplace.attractionContentId) : undefined
    }
  });
}

function moveIntro(direction) {
  const next = introActiveIndex.value + direction;
  if (next < 0 || next >= introCards.length) return;
  introActiveIndex.value = next;
}

function startExperience() {
  introComplete.value = true;
}

onMounted(loadAllHotplaces);
</script>

<template>
  <section class="home-page">
    <section v-if="!introComplete" class="home-intro-screen">
      <article class="home-intro-card">
        <div class="home-intro-image">
          <img :src="currentIntro.image" :alt="currentIntro.title" />
        </div>

        <div class="home-intro-copy">
          <span class="home-intro-kicker">{{ currentIntro.kicker }}</span>
          <h1>{{ currentIntro.title }}</h1>
          <p>{{ currentIntro.description }}</p>

          <div class="home-intro-dots" aria-label="인트로 카드">
            <button
              v-for="(_, index) in introCards"
              :key="`intro-dot-${index}`"
              type="button"
              :class="{ active: introActiveIndex === index }"
              :aria-label="`${index + 1}번째 소개 보기`"
              @click="introActiveIndex = index"
            ></button>
          </div>

          <div class="home-intro-actions">
            <button class="btn btn-ghost" type="button" :disabled="introActiveIndex === 0" @click="moveIntro(-1)">
              이전
            </button>
            <button
              v-if="introActiveIndex < introCards.length - 1"
              class="btn btn-secondary"
              type="button"
              @click="moveIntro(1)"
            >
              다음
            </button>
            <button v-else class="btn btn-secondary" type="button" @click="introActiveIndex = 0">처음</button>
            <button class="btn btn-primary" type="button" @click="startExperience">시작하기</button>
          </div>
        </div>
      </article>
    </section>

    <section v-else class="home-landing">
      <div class="home-landing-map">
        <KoreaRegionMap
          :selected-region="selectedRegion"
          :preview-region="previewRegion"
          :counts="regionCounts"
          :hotplaces="panelHotplaces"
          :total-count="panelCount"
          :loading="loading"
          :error-message="errorMessage"
          @hover="previewRegion = $event"
          @leave="previewRegion = ''"
          @select-region="selectRegion"
          @close-region="closeRegion"
          @open-attractions="openAttractions"
          @open-hotplaces="openHotplaces"
          @open-hotplace-attraction="openHotplaceAttraction"
        />
      </div>

      <div class="home-landing-copy">
        <p class="home-landing-kicker">당신의 이야기가 머무는 곳</p>
        <h1>당신의 이야기를 담는 여행, 너울터</h1>
        <p>
          초자님, 다음 여행지를 지도에서 골라보세요.<br />
          지역의 결을 따라 걷고, 오래 기억할 나만의 여정을 만들어보세요.
        </p>
        <div class="home-landing-actions">
          <RouterLink to="/attractions">지역별 탐색</RouterLink>
          <RouterLink to="/plans">테마별 여행</RouterLink>
          <RouterLink to="/community">커뮤니티 추천</RouterLink>
        </div>
      </div>
    </section>
  </section>
</template>
