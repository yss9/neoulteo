<script setup>
import { computed, ref } from "vue";
import { KOREA_MAP_VIEW_BOX, KOREA_SIDO_PATHS } from "@/assets/maps/koreaSidoPaths";

const props = defineProps({
  selectedRegion: {
    type: String,
    default: ""
  },
  previewRegion: {
    type: String,
    default: ""
  },
  counts: {
    type: Object,
    default: () => ({})
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

const emit = defineEmits([
  "hover",
  "leave",
  "select",
  "select-region",
  "close-region",
  "open-attractions",
  "open-hotplaces",
  "open-hotplace-attraction"
]);

const mapWrap = ref(null);
const tooltip = ref({
  visible: false,
  x: 0,
  y: 0,
  label: ""
});
const bubble = ref({
  x: 0,
  y: 0,
  direction: "right"
});

const BUBBLE_WIDTH = 340;
const BUBBLE_HEIGHT = 430;
const BUBBLE_OFFSET = 24;
const BUBBLE_MARGIN = 16;

const regions = computed(() =>
  KOREA_SIDO_PATHS.map((region) => ({
    ...region,
    count: props.counts[region.name] || 0,
    active: props.selectedRegion === region.name,
    preview: props.previewRegion === region.name && props.selectedRegion !== region.name
  }))
);

const selectedRegionDetail = computed(() => regions.value.find((region) => region.name === props.selectedRegion));

function getRelativePoint(event) {
  const rect = mapWrap.value?.getBoundingClientRect();
  if (!rect) return null;

  if (typeof event.clientX !== "number" || typeof event.clientY !== "number") {
    return {
      x: rect.width / 2,
      y: rect.height / 2,
      width: rect.width,
      height: rect.height
    };
  }

  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top,
    width: rect.width,
    height: rect.height
  };
}

function updateTooltip(event, region) {
  const point = getRelativePoint(event);
  if (!point) return;

  tooltip.value = {
    visible: true,
    x: point.x,
    y: point.y,
    label: `${region.displayName} · 등록된 핫플레이스 ${region.count}개`
  };
}

function updateBubble(event) {
  const point = getRelativePoint(event);
  if (!point) return;

  const hasRoomRight = point.x + BUBBLE_OFFSET + BUBBLE_WIDTH <= point.width - BUBBLE_MARGIN;
  const direction = hasRoomRight ? "right" : "left";
  const horizontalOffset = direction === "right" ? BUBBLE_OFFSET : -(BUBBLE_WIDTH + BUBBLE_OFFSET);
  const maxX = Math.max(BUBBLE_MARGIN, point.width - BUBBLE_WIDTH - BUBBLE_MARGIN);
  const x = Math.min(Math.max(point.x + horizontalOffset, BUBBLE_MARGIN), maxX);
  const maxY = Math.max(BUBBLE_MARGIN, point.height - BUBBLE_HEIGHT - BUBBLE_MARGIN);
  const y = Math.min(Math.max(point.y - BUBBLE_HEIGHT / 2, BUBBLE_MARGIN), maxY);

  bubble.value = {
    x,
    y,
    direction
  };
}

function hoverRegion(event, region) {
  emit("hover", region.name);
  updateTooltip(event, region);
}

function moveTooltip(event, region) {
  updateTooltip(event, region);
}

function leaveRegion() {
  tooltip.value.visible = false;
  emit("leave");
}

function selectRegion(event, region) {
  tooltip.value.visible = false;
  updateBubble(event);
  emit("select", region.name);
  emit("select-region", region.name);
}

function closeBubble() {
  emit("close-region");
}
</script>

<template>
  <section ref="mapWrap" class="korea-region-map" aria-label="대한민국 시도 지도">
    <svg
      class="korea-region-svg"
      :viewBox="KOREA_MAP_VIEW_BOX"
      preserveAspectRatio="xMidYMid meet"
      role="img"
      aria-label="대한민국 시도 선택 지도"
    >
      <defs>
        <filter id="neoulteo-ink-wash" x="-12%" y="-12%" width="124%" height="124%">
          <feTurbulence type="fractalNoise" baseFrequency="0.008 0.085" numOctaves="4" seed="8" result="paperFibers" />
          <feTurbulence type="fractalNoise" baseFrequency="0.045" numOctaves="2" seed="13" result="paperGrain" />
          <feBlend in="paperFibers" in2="paperGrain" mode="multiply" result="paperNoise" />
          <feDisplacementMap in="SourceGraphic" in2="paperGrain" scale="0.9" xChannelSelector="R" yChannelSelector="G" result="displaced" />
          <feColorMatrix in="paperNoise" type="saturate" values="0" result="monoNoise" />
          <feComponentTransfer in="monoNoise" result="softNoise">
            <feFuncA type="table" tableValues="0 0.24" />
          </feComponentTransfer>
          <feComposite in="softNoise" in2="displaced" operator="in" result="clippedNoise" />
          <feBlend in="displaced" in2="clippedNoise" mode="multiply" />
        </filter>
      </defs>

      <g class="region-map-lands">
        <g v-for="region in regions" :key="region.code">
          <title>{{ region.displayName }}</title>
          <path
            class="region-map-path"
            :class="{
              'region-map-path-active': region.active,
              'region-map-path-preview': region.preview
            }"
            :d="region.d"
            tabindex="0"
            role="button"
            :aria-label="`${region.displayName} 선택, 등록된 핫플레이스 ${region.count}개`"
            @mouseenter="hoverRegion($event, region)"
            @mousemove="moveTooltip($event, region)"
            @mouseleave="leaveRegion"
            @focus="emit('hover', region.name)"
            @blur="emit('leave')"
            @click="selectRegion($event, region)"
            @keydown.enter.prevent="selectRegion($event, region)"
            @keydown.space.prevent="selectRegion($event, region)"
          />
        </g>
      </g>

    </svg>

    <div
      v-if="tooltip.visible"
      class="region-map-tooltip"
      :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }"
    >
      {{ tooltip.label }}
    </div>

    <aside
      v-if="selectedRegionDetail"
      class="region-map-bubble"
      :class="`region-map-bubble-${bubble.direction}`"
      :style="{ left: `${bubble.x}px`, top: `${bubble.y}px` }"
      aria-live="polite"
    >
      <button type="button" class="bubble-close" aria-label="지역 핫플레이스 닫기" @click="closeBubble">×</button>

      <div class="bubble-header">
        <span class="badge badge-soft">선택 지역</span>
        <strong>{{ selectedRegionDetail.displayName }}</strong>
        <p>등록된 핫플레이스 {{ totalCount }}개</p>
      </div>

      <div v-if="loading" class="bubble-list">
        <div class="loading-skeleton bubble-skeleton"></div>
        <div class="loading-skeleton bubble-skeleton"></div>
        <div class="loading-skeleton bubble-skeleton"></div>
      </div>

      <p v-else-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <div v-else-if="hotplaces.length === 0" class="bubble-empty">
        아직 등록된 핫플레이스가 없습니다.
      </div>

      <div v-else class="bubble-list">
        <button
          v-for="hotplace in hotplaces"
          :key="hotplace.id"
          class="bubble-place bubble-place-button"
          type="button"
          @click="emit('open-hotplace-attraction', hotplace)"
        >
          <strong>{{ hotplace.name }}</strong>
          <small v-if="hotplace.hotplaceCount" class="bubble-place-count">{{ hotplace.hotplaceCount }}명 등록</small>
          <span>{{ hotplace.address || hotplace.type || "장소 정보 미정" }}</span>
        </button>
      </div>

      <div class="bubble-actions">
        <button type="button" class="btn btn-primary btn-sm" @click="emit('open-attractions')">관광지 검색</button>
        <button type="button" class="btn btn-secondary btn-sm" @click="emit('open-hotplaces')">더보기</button>
      </div>
    </aside>
  </section>
</template>
