<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { clearAuthToken } from "@/api/http";
import { authStore } from "@/stores/authStore";
import { runTourBatch } from "@/api/batchApi";
import { deleteAccount, resetPassword, updateProfile } from "@/api/userApi";

const router = useRouter();

const nameForm = reactive({
  name: ""
});
const passwordForm = reactive({
  password: ""
});
const resetForm = reactive({
  email: "",
  password: ""
});
const deleteForm = reactive({
  password: ""
});

const nameMessage = ref("");
const passwordMessage = ref("");
const resetMessage = ref("");
const deleteMessage = ref("");
const batchMessage = ref("");
const batchResult = ref(null);
const confirmDelete = ref(false);
const loadingAction = ref("");

const displayName = computed(() => authStore.user?.name || "이름 없음");
const displayEmail = computed(() => authStore.user?.email || "-");
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase());

function clearMessages() {
  nameMessage.value = "";
  passwordMessage.value = "";
  resetMessage.value = "";
  deleteMessage.value = "";
  batchMessage.value = "";
}

async function updateName() {
  clearMessages();
  const nextName = nameForm.name.trim();
  if (!nextName) {
    nameMessage.value = "새 이름을 입력해주세요.";
    return;
  }

  loadingAction.value = "name";
  try {
    const data = await updateProfile({ name: nextName, password: "" });
    authStore.user = { id: data.id, name: data.name, email: data.email };
    nameMessage.value = data.message || "이름이 변경되었습니다.";
    nameForm.name = "";
  } catch (error) {
    nameMessage.value = error.message;
  } finally {
    loadingAction.value = "";
  }
}

async function updatePassword() {
  clearMessages();
  const nextPassword = passwordForm.password.trim();
  if (!nextPassword) {
    passwordMessage.value = "새 비밀번호를 입력해주세요.";
    return;
  }

  loadingAction.value = "password";
  try {
    const data = await updateProfile({ name: "", password: nextPassword });
    authStore.user = { id: data.id, name: data.name, email: data.email };
    passwordMessage.value = data.message || "비밀번호가 변경되었습니다.";
    passwordForm.password = "";
  } catch (error) {
    passwordMessage.value = error.message;
  } finally {
    loadingAction.value = "";
  }
}

async function resetPw() {
  clearMessages();
  if (!resetForm.email.trim() || !resetForm.password.trim()) {
    resetMessage.value = "이메일과 새 비밀번호를 입력해주세요.";
    return;
  }

  loadingAction.value = "reset";
  try {
    const data = await resetPassword(resetForm);
    resetMessage.value = data.message || "비밀번호가 재설정되었습니다.";
    resetForm.email = "";
    resetForm.password = "";
  } catch (error) {
    resetMessage.value = error.message;
  } finally {
    loadingAction.value = "";
  }
}

function requestDelete() {
  clearMessages();
  if (!deleteForm.password.trim()) {
    deleteMessage.value = "현재 비밀번호를 입력해주세요.";
    return;
  }

  confirmDelete.value = true;
}

async function removeAccount() {
  clearMessages();
  loadingAction.value = "delete";
  try {
    const data = await deleteAccount(deleteForm.password);
    deleteMessage.value = data.message || "회원 탈퇴가 완료되었습니다.";
    clearAuthToken();
    authStore.user = null;
    deleteForm.password = "";
    confirmDelete.value = false;
    router.push("/");
  } catch (error) {
    deleteMessage.value = error.message;
    confirmDelete.value = false;
  } finally {
    loadingAction.value = "";
  }
}

async function executeTourBatch() {
  clearMessages();
  batchResult.value = null;
  loadingAction.value = "batch";
  try {
    const data = await runTourBatch();
    batchResult.value = data;
    batchMessage.value = data.message || "배치 실행이 완료되었습니다.";
  } catch (error) {
    batchMessage.value = error.message;
  } finally {
    loadingAction.value = "";
  }
}
</script>

<template>
  <section class="profile-page">
    <header class="profile-page-header">
      <div>
        <p class="eyebrow">Account</p>
        <h1>마이페이지</h1>
        <p>
          로그인 정보와 계정 설정을 관리합니다.
        </p>
      </div>
      <span v-if="authStore.isLoggedIn" class="badge badge-soft">로그인 중</span>
    </header>

    <section v-if="!authStore.isLoggedIn" class="empty-state profile-login-required">
      <p class="empty-state-title">로그인이 필요합니다</p>
      <p class="empty-state-description">
        상단 로그인 영역에서 로그인하면 프로필을 확인할 수 있습니다.
      </p>
    </section>

    <section v-else class="profile-layout">
      <aside class="profile-summary-card">
        <div class="profile-avatar" aria-hidden="true">{{ avatarText }}</div>
        <div>
          <h2>{{ displayName }}</h2>
          <p>{{ displayEmail }}</p>
        </div>
        <dl class="profile-meta-list">
          <div>
            <dt>계정 이름</dt>
            <dd>{{ displayName }}</dd>
          </div>
          <div>
            <dt>이메일</dt>
            <dd>{{ displayEmail }}</dd>
          </div>
        </dl>
      </aside>

      <div class="profile-settings">
        <form class="profile-section app-card" @submit.prevent="updateName">
          <div class="app-card-header">
            <h2 class="app-card-title">이름 변경</h2>
            <p class="app-card-description">서비스에 표시되는 이름을 변경합니다.</p>
          </div>
          <label class="form-group">
            <span class="form-label">새 이름</span>
            <input
              v-model="nameForm.name"
              class="form-input"
              placeholder="새 이름"
              autocomplete="name"
            />
          </label>
          <button class="btn btn-primary" type="submit" :disabled="loadingAction === 'name'">
            {{ loadingAction === "name" ? "저장 중" : "이름 저장" }}
          </button>
          <p v-if="nameMessage" class="notice">{{ nameMessage }}</p>
        </form>

        <form class="profile-section app-card" @submit.prevent="updatePassword">
          <div class="app-card-header">
            <h2 class="app-card-title">비밀번호 변경</h2>
            <p class="app-card-description">로그인에 사용할 새 비밀번호를 저장합니다.</p>
          </div>
          <label class="form-group">
            <span class="form-label">새 비밀번호</span>
            <input
              v-model="passwordForm.password"
              class="form-input"
              placeholder="새 비밀번호"
              type="password"
              autocomplete="new-password"
            />
          </label>
          <button class="btn btn-primary" type="submit" :disabled="loadingAction === 'password'">
            {{ loadingAction === "password" ? "저장 중" : "비밀번호 저장" }}
          </button>
          <p v-if="passwordMessage" class="notice">{{ passwordMessage }}</p>
        </form>

        <form class="profile-section app-card" @submit.prevent="resetPw">
          <div class="app-card-header">
            <h2 class="app-card-title">비밀번호 재설정</h2>
            <p class="app-card-description">이메일 기준으로 비밀번호를 다시 설정합니다.</p>
          </div>
          <div class="profile-form-grid">
            <label class="form-group">
              <span class="form-label">이메일</span>
              <input
                v-model="resetForm.email"
                class="form-input"
                placeholder="이메일"
                type="email"
                autocomplete="email"
              />
            </label>
            <label class="form-group">
              <span class="form-label">새 비밀번호</span>
              <input
                v-model="resetForm.password"
                class="form-input"
                placeholder="새 비밀번호"
                type="password"
                autocomplete="new-password"
              />
            </label>
          </div>
          <button class="btn btn-secondary" type="submit" :disabled="loadingAction === 'reset'">
            {{ loadingAction === "reset" ? "재설정 중" : "비밀번호 재설정" }}
          </button>
          <p v-if="resetMessage" class="notice">{{ resetMessage }}</p>
        </form>

        <form class="profile-section app-card danger-zone" @submit.prevent="requestDelete">
          <div class="app-card-header">
            <h2 class="app-card-title">회원 탈퇴</h2>
            <p class="app-card-description">현재 비밀번호를 확인한 뒤 계정을 삭제합니다.</p>
          </div>
          <label class="form-group">
            <span class="form-label">현재 비밀번호</span>
            <input
              v-model="deleteForm.password"
              class="form-input"
              placeholder="현재 비밀번호"
              type="password"
              autocomplete="current-password"
            />
          </label>
          <button class="btn btn-danger" type="submit">회원 탈퇴</button>
          <p v-if="deleteMessage" class="notice">{{ deleteMessage }}</p>
        </form>

        <section class="profile-section app-card batch-zone">
          <div class="app-card-header">
            <h2 class="app-card-title">TourAPI 배치 테스트</h2>
            <p class="app-card-description">
              로그인 세션으로 관광지 전체 수집, DB 비교, AI 요약, PDF 생성을 실행합니다.
            </p>
          </div>
          <button class="btn btn-primary" type="button" :disabled="loadingAction === 'batch'" @click="executeTourBatch">
            {{ loadingAction === "batch" ? "배치 실행 중" : "배치 실행" }}
          </button>
          <p v-if="batchMessage" class="notice">{{ batchMessage }}</p>
          <div v-if="batchResult" class="batch-result">
            <div>
              <span>상태</span>
              <strong>{{ batchResult.jobStatus || "-" }}</strong>
            </div>
            <div>
              <span>수집</span>
              <strong>{{ batchResult.apiItemCount ?? "-" }}</strong>
            </div>
            <div>
              <span>신규</span>
              <strong>{{ batchResult.newItemCount ?? "-" }}</strong>
            </div>
            <div>
              <span>변경</span>
              <strong>{{ batchResult.changedItemCount ?? "-" }}</strong>
            </div>
            <div>
              <span>삭제 후보</span>
              <strong>{{ batchResult.missingItemCount ?? "-" }}</strong>
            </div>
            <div class="batch-result-path">
              <span>PDF 경로</span>
              <strong>{{ batchResult.pdfPath || "-" }}</strong>
            </div>
          </div>
        </section>
      </div>
    </section>

    <div v-if="confirmDelete" class="modal-backdrop" role="presentation">
      <section class="modal-panel confirm-modal-card" role="dialog" aria-modal="true">
        <header class="modal-header">
          <h2>정말 탈퇴할까요?</h2>
          <p>탈퇴하면 현재 계정 정보가 삭제됩니다.</p>
        </header>
        <div class="modal-body">
          <p>계속하려면 아래 버튼을 눌러 탈퇴를 완료하세요.</p>
        </div>
        <footer class="modal-footer">
          <button class="btn btn-ghost" type="button" @click="confirmDelete = false">
            취소
          </button>
          <button class="btn btn-danger" type="button" :disabled="loadingAction === 'delete'" @click="removeAccount">
            {{ loadingAction === "delete" ? "처리 중" : "탈퇴하기" }}
          </button>
        </footer>
      </section>
    </div>
  </section>
</template>
