<script setup>
import { onMounted } from "vue";
import AppHeader from "@/components/AppHeader.vue";
import Chatbot from "@/components/Chatbot.vue";
import { authStore } from "@/stores/authStore";

onMounted(() => {
  authStore.loadProfile();
});
</script>

<template>
  <div class="app-layout">
    <AppHeader />
    <main class="app-main page-shell">
      <RouterView />
    </main>
    <Chatbot />
  </div>
</template>
