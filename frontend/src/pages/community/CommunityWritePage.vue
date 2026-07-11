<template>
  <div class="community-write page-shell">
    <header class="section-header">
      <h1>글쓰기</h1>
      <p>여행 이야기와 코스를 커뮤니티에 공유해보세요.</p>
    </header>

    <form class="panel" @submit.prevent="handleSubmit">
      <div class="form-container">
        <div class="form-group">
          <label class="form-label">분류</label>
          <select v-model="form.category" class="form-select">
            <option v-for="category in writableCategories" :key="category.value" :value="category.value">
              {{ category.label }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">제목</label>
          <input v-model="form.title" type="text" class="form-input" placeholder="제목을 입력하세요" required />
        </div>

        <div v-if="form.category === 'PLAN_SHARE'" class="form-group">
          <label class="form-label">여행 계획 선택</label>
          <select v-model="form.travelPlanId" class="form-select" required>
            <option value="" disabled>공유할 계획을 선택하세요</option>
            <option v-for="plan in myPlans" :key="plan.id" :value="plan.id">
              {{ plan.title || plan.places[0]?.name || "여행 계획" }}
            </option>
          </select>
          <p v-if="myPlans.length === 0" class="form-error mt-xs">저장된 여행 계획이 없습니다.</p>
        </div>

        <div class="form-group">
          <label class="form-label">대표 이미지</label>
          <input class="form-input" type="file" accept="image/*" @change="handleImageChange" />
          <p class="form-help">선택한 이미지는 백엔드의 <code>uploads/community</code> 폴더에 저장되고, 저장 경로가 게시글에 연결됩니다.</p>
          <img v-if="imagePreviewUrl" class="community-image-preview" :src="imagePreviewUrl" alt="첨부 이미지 미리보기" />
          <p v-if="uploadMessage" class="form-help">{{ uploadMessage }}</p>
        </div>

        <div class="form-group">
          <label class="form-label">내용</label>
          <textarea v-model="form.content" class="form-textarea" placeholder="내용을 입력하세요" required></textarea>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-ghost" @click="$router.back()">취소</button>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? "등록 중" : "등록하기" }}
          </button>
        </div>
        <p v-if="message" class="notice">{{ message }}</p>
      </div>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { uploadPostImage, writePost } from "@/api/postApi";
import { fetchPlans } from "@/api/travelPlanApi";
import { authStore } from "@/stores/authStore";

const route = useRoute();
const router = useRouter();
const submitting = ref(false);
const message = ref("");
const uploadMessage = ref("");
const selectedImageFile = ref(null);
const imagePreviewUrl = ref("");
const form = ref({
  category: "FREE",
  title: "",
  content: "",
  imageUrl: "",
  travelPlanId: ""
});

const myPlans = ref([]);
const adminEmails = ["a@a"];
const isAdmin = computed(() => authStore.user?.email && adminEmails.includes(authStore.user.email));
const writableCategories = computed(() => [
  ...(isAdmin.value ? [{ label: "공지사항", value: "NOTICE" }] : []),
  { label: "자유게시판", value: "FREE" },
  { label: "여행지 후기", value: "REVIEW" },
  { label: "질의응답", value: "QNA" },
  { label: "여행 계획 공유", value: "PLAN_SHARE" }
]);

const fetchMyPlans = async () => {
  try {
    const data = await fetchPlans();
    myPlans.value = data.items || [];
  } catch (error) {
    console.error("Failed to fetch my plans:", error);
  }
};


const durationLabel = (days) => {
  const dayCount = Math.max(Number(days) || 1, 1);
  return dayCount === 1 ? "당일치기" : `${dayCount - 1}박 ${dayCount}일`;
};

function handleImageChange(event) {
  const file = event.target.files?.[0] || null;
  selectedImageFile.value = file;
  uploadMessage.value = "";

  if (!file) {
    imagePreviewUrl.value = form.value.imageUrl || "";
    return;
  }

  if (!file.type.startsWith("image/")) {
    selectedImageFile.value = null;
    imagePreviewUrl.value = form.value.imageUrl || "";
    uploadMessage.value = "이미지 파일만 업로드할 수 있습니다.";
    return;
  }

  imagePreviewUrl.value = URL.createObjectURL(file);
}

watch(
  () => form.value.category,
  (newCategory) => {
    if (newCategory === "PLAN_SHARE" && myPlans.value.length === 0) {
      fetchMyPlans();
    }
  }
);

onMounted(() => {
  const category = String(route.query.category || "");
  if (category && writableCategories.value.some((item) => item.value === category)) {
    form.value.category = category;
  }
  if (route.query.title) {
    form.value.title = String(route.query.title);
  }
  if (route.query.content) {
    form.value.content = String(route.query.content);
  }
  if (route.query.imageUrl) {
    form.value.imageUrl = String(route.query.imageUrl);
    imagePreviewUrl.value = form.value.imageUrl;
  }
  if (route.query.travelPlanId) {
    form.value.category = "PLAN_SHARE";
    form.value.travelPlanId = String(route.query.travelPlanId);
    fetchMyPlans();
  }
});

const handleSubmit = async () => {
  submitting.value = true;
  message.value = "";
  uploadMessage.value = "";
  try {
    let imageUrl = form.value.imageUrl || null;
    if (selectedImageFile.value) {
      uploadMessage.value = "이미지를 업로드하는 중입니다.";
      const uploadResult = await uploadPostImage(selectedImageFile.value);
      imageUrl = uploadResult.imageUrl;
      form.value.imageUrl = imageUrl;
      uploadMessage.value = "이미지가 업로드되었습니다.";
    }

    const payload = {
      category: form.value.category,
      title: form.value.title,
      content: form.value.content,
      imageUrl,
      travelPlanId: form.value.category === "PLAN_SHARE" ? form.value.travelPlanId : null
    };
    await writePost(payload);
    alert("글이 등록되었습니다.");
    router.push("/community");
  } catch (error) {
    console.error(error);
    message.value = error.message || "오류가 발생했습니다.";
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.form-container {
  display: grid;
  gap: var(--space-lg);
  padding: var(--space-lg);
}

.form-actions {
  display: flex;
  gap: var(--space-sm);
  justify-content: flex-end;
  margin-top: var(--space-md);
}

.community-image-preview {
  aspect-ratio: 16 / 9;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  display: block;
  max-height: 280px;
  object-fit: cover;
  width: 100%;
}

.mt-xs {
  margin-top: 0.25rem;
}
</style>
