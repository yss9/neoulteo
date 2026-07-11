<script setup>
import { computed } from "vue";

const props = defineProps({
  hotplace: {
    type: Object,
    required: true
  }
});

const imageUrl = computed(() => props.hotplace.image || props.hotplace.imageUrl || props.hotplace.firstimage || "");
const metaText = computed(() => [props.hotplace.address, props.hotplace.type].filter(Boolean).join(" · "));
</script>

<template>
  <article class="region-hotplace-card">
    <img v-if="imageUrl" :src="imageUrl" :alt="hotplace.name" class="region-hotplace-image" />
    <div v-else class="region-hotplace-image placeholder">No image</div>

    <div class="region-hotplace-content">
      <strong>{{ hotplace.name }}</strong>
      <span>{{ metaText || "장소 정보 미정" }}</span>
      <p>{{ hotplace.description || "아직 설명이 없습니다." }}</p>
    </div>
  </article>
</template>
