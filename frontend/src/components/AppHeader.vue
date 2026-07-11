<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { authStore } from "@/stores/authStore";
import { signup } from "@/api/userApi";

const route = useRoute();
const router = useRouter();
const authOpen = ref(false);
const mobileOpen = ref(false);
const authMode = ref("login");
const message = ref("");
const submitting = ref(false);

const navItems = [
  { label: "홈", to: "/" },
  { label: "관광지 검색", to: "/attractions" },
  { label: "여행 계획", to: "/plans" },
  { label: "핫플레이스", to: "/hotplaces" },
  { label: "커뮤니티", to: "/community" }
];

const loginForm = reactive({
  email: "",
  password: "",
  rememberMe: false
});
const signupForm = reactive({
  name: "",
  email: "",
  password: ""
});

const authTitle = computed(() => (authMode.value === "login" ? "로그인" : "회원가입"));

function isActive(path) {
  return path === "/" ? route.path === "/" : route.path.startsWith(path);
}

function openAuth(mode = "login") {
  authMode.value = mode;
  authOpen.value = true;
  mobileOpen.value = false;
  message.value = "";
}

function closeAuth() {
  authOpen.value = false;
  message.value = "";
}

function resetSignupForm() {
  signupForm.name = "";
  signupForm.email = "";
  signupForm.password = "";
}

async function submitLogin() {
  message.value = "";
  submitting.value = true;
  try {
    const data = await authStore.login(loginForm.email, loginForm.password, loginForm.rememberMe);
    message.value = data.message || "로그인되었습니다.";
    loginForm.password = "";
    closeAuth();
    router.push("/");
  } catch (error) {
    message.value = error.message;
  } finally {
    submitting.value = false;
  }
}

async function submitSignup() {
  message.value = "";
  submitting.value = true;
  try {
    const data = await signup({
      name: signupForm.name,
      email: signupForm.email,
      password: signupForm.password
    });
    message.value = data.message || "회원가입이 완료되었습니다.";
    loginForm.email = signupForm.email;
    loginForm.password = "";
    resetSignupForm();
    authMode.value = "login";
  } catch (error) {
    message.value = error.message;
  } finally {
    submitting.value = false;
  }
}

async function logout() {
  await authStore.logout();
  loginForm.email = "";
  loginForm.password = "";
  loginForm.rememberMe = false;
  resetSignupForm();
  authOpen.value = false;
  mobileOpen.value = false;
  message.value = "";
  router.push("/");
}

function closeMenus() {
  mobileOpen.value = false;
  authOpen.value = false;
}

function handleKeydown(event) {
  if (event.key === "Escape") {
    closeMenus();
  }
}

watch(
  () => route.fullPath,
  () => {
    mobileOpen.value = false;
  }
);

onMounted(() => {
  window.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  window.removeEventListener("keydown", handleKeydown);
});
</script>

<template>
  <header class="app-header">
    <div class="app-header-inner">
      <RouterLink class="app-logo" to="/" aria-label="Neoulteo 홈으로 이동" @click="closeMenus">
        <img class="app-logo-image" src="/logo-neoul.png" alt="너울터" />
      </RouterLink>

      <nav class="app-nav" aria-label="주요 메뉴">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          class="app-nav-link"
          :class="{ 'app-nav-link-active': isActive(item.to) }"
          :to="item.to"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="app-header-actions">
        <template v-if="authStore.isLoggedIn">
          <RouterLink class="header-account-pill" to="/profile">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="8" r="3.5" />
              <path d="M5.5 20c.7-4 2.8-6 6.5-6s5.8 2 6.5 6" />
            </svg>
            <span>{{ authStore.user.name }}</span>
          </RouterLink>
          <button class="header-logout-button" type="button" @click="logout" aria-label="로그아웃">↗</button>
        </template>

        <template v-else>
          <button class="header-account-pill" type="button" @click="openAuth('login')">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="8" r="3.5" />
              <path d="M5.5 20c.7-4 2.8-6 6.5-6s5.8 2 6.5 6" />
            </svg>
            <span>로그인</span>
          </button>
        </template>

        <button
          class="mobile-menu-button"
          type="button"
          aria-label="모바일 메뉴 열기"
          :aria-expanded="mobileOpen"
          @click="mobileOpen = !mobileOpen; authOpen = false"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <section v-if="authOpen && !authStore.isLoggedIn" class="auth-panel auth-panel-open" :aria-label="authTitle">
      <div class="auth-panel-header">
        <strong>{{ authTitle }}</strong>
        <button class="auth-close" type="button" aria-label="로그인 패널 닫기" @click="closeAuth">×</button>
      </div>

      <div class="auth-tabs" role="tablist" aria-label="인증 모드">
        <button
          type="button"
          :class="{ active: authMode === 'login' }"
          @click="authMode = 'login'; message = ''"
        >
          로그인
        </button>
        <button
          type="button"
          :class="{ active: authMode === 'signup' }"
          @click="authMode = 'signup'; message = ''"
        >
          회원가입
        </button>
      </div>

      <form v-if="authMode === 'login'" class="auth-form" @submit.prevent="submitLogin">
        <input v-model="loginForm.email" placeholder="이메일" type="email" autocomplete="email" required />
        <input v-model="loginForm.password" placeholder="비밀번호" type="password" autocomplete="current-password" required />
        <label class="remember">
          <input v-model="loginForm.rememberMe" type="checkbox" />
          <span class="remember-check" aria-hidden="true"></span>
          <span>로그인 상태 유지</span>
        </label>
        <button class="btn btn-primary" type="submit" :disabled="submitting">로그인</button>
      </form>

      <form v-else class="auth-form" @submit.prevent="submitSignup">
        <input v-model="signupForm.name" placeholder="이름" autocomplete="name" required />
        <input v-model="signupForm.email" placeholder="이메일" type="email" autocomplete="email" required />
        <input v-model="signupForm.password" placeholder="비밀번호" type="password" autocomplete="new-password" required />
        <button class="btn btn-primary" type="submit" :disabled="submitting">계정 만들기</button>
      </form>

      <p v-if="message" class="auth-message">{{ message }}</p>
    </section>

    <nav v-if="mobileOpen" class="mobile-nav mobile-nav-open" aria-label="모바일 메뉴">
      <RouterLink
        v-for="item in navItems"
        :key="`mobile-${item.to}`"
        class="mobile-nav-link"
        :class="{ 'app-nav-link-active': isActive(item.to) }"
        :to="item.to"
      >
        {{ item.label }}
      </RouterLink>
      <RouterLink v-if="authStore.isLoggedIn" class="mobile-nav-link" to="/profile">마이페이지</RouterLink>
      <button v-if="authStore.isLoggedIn" class="mobile-nav-link mobile-nav-button" type="button" @click="logout">로그아웃</button>
      <button v-else class="mobile-nav-link mobile-nav-button" type="button" @click="openAuth('login')">로그인</button>
    </nav>
  </header>
</template>
