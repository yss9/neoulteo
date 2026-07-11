<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const keyword = ref("");

function submit() {
  const value = keyword.value.trim();
  router.push(value ? { path: "/attractions", query: { keyword: value } } : { path: "/attractions" });
}
</script>

<template>
  <form class="home-search" @submit.prevent="submit">
    <input v-model="keyword" placeholder="어디로 떠나고 싶나요?" />
    <button type="submit">관광지 검색</button>
  </form>
</template>
