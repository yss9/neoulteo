<script setup>
import { computed } from "vue";
import RegionHotplaceCard from "@/components/home/RegionHotplaceCard.vue";
import { KOREA_REGIONS } from "@/constants/regions";

const props = defineProps({
  region: {
    type: String,
    default: ""
  },
  hotplaces: {
    type: Array,
    default: () => []
  },
  totalCount: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  errorMessage: {
    type: String,
    default: ""
  }
});

const emit = defineEmits(["open-attractions", "open-hotplaces"]);

const displayRegionName = computed(() => {
  const found = KOREA_REGIONS.find((region) => region.name === props.region);
  return found?.displayName || props.region;
});
</script>

<template>
  <aside class="region-info-panel">
    <div v-if="!region" class="region-panel-empty">
      <span class="badge badge-soft">지역 선택</span>
      <h2>지도를 클릭해 지역을 선택해보세요</h2>
      <p>
        시도별 핫플레이스 개수를 보고, 지역을 선택하면 관련 장소를 바로 확인할 수 있습니다.
      </p>
    </div>

    <template v-else>
      <div class="region-info-header">
        <span class="badge badge-soft">선택 지역</span>
        <h2>{{ displayRegionName }}</h2>
        <p>등록된 핫플레이스 {{ totalCount }}개</p>
      </div>

      <div v-if="loading" class="region-hotplace-list">
        <div class="loading-skeleton region-card-skeleton"></div>
        <div class="loading-skeleton region-card-skeleton"></div>
        <div class="loading-skeleton region-card-skeleton"></div>
      </div>

      <p v-else-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <div v-else-if="hotplaces.length === 0" class="region-panel-empty compact">
        <h3>아직 등록된 핫플레이스가 없습니다</h3>
        <p>이 지역의 숨은 장소를 처음으로 등록해보세요.</p>
      </div>

      <div v-else class="region-hotplace-list">
        <RegionHotplaceCard v-for="hotplace in hotplaces" :key="hotplace.id" :hotplace="hotplace" />
      </div>

      <div class="region-info-actions">
        <button type="button" class="btn btn-primary" @click="emit('open-attractions')">
          관광지 검색하기
        </button>
        <button type="button" class="btn btn-secondary" @click="emit('open-hotplaces')">
          핫플레이스 더보기
        </button>
      </div>
    </template>
  </aside>
</template>
