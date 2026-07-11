<template>
  <div v-if="post" class="community-detail page-shell">
    <div class="page-section">
      <router-link to="/community" class="btn btn-ghost btn-sm">목록으로</router-link>
    </div>

    <article class="community-detail-card">
      <header class="community-detail-header">
        <span class="badge" :class="categoryMeta.badgeClass">{{ categoryMeta.label }}</span>
        <h1>{{ post.title }}</h1>
        <div class="community-detail-meta">
          <span>{{ post.userName || "작성자" }}</span>
          <span>조회 {{ post.views || 0 }}</span>
          <span>{{ formatDate(post.createdAt) }}</span>
        </div>
        <div v-if="authStore.isLoggedIn" class="post-actions">
          <button class="btn btn-secondary btn-sm" type="button" :disabled="likingPost" @click="likePost">
            좋아요 {{ post.likeCount || 0 }}
          </button>
          <button v-if="canManagePost" class="btn btn-danger btn-sm" type="button" :disabled="deletingPost" @click="removePost">
            {{ deletingPost ? "삭제 중" : "삭제" }}
          </button>
        </div>
        <p v-if="postMessage" class="notice">{{ postMessage }}</p>
      </header>

      <img v-if="post.imageUrl" class="community-detail-image" :src="post.imageUrl" :alt="post.title" />

      <section v-if="post.category === 'REVIEW'" class="review-place-summary">
        <span class="badge badge-accent">여행지 후기</span>
        <strong>{{ reviewPlaceName }}</strong>
        <p>핫플레이스나 관광지 경험을 공유한 글입니다.</p>
      </section>

      <section v-if="post.category === 'PLAN_SHARE' && post.travelPlanId" class="plan-share-section">
        <div class="plan-share-header">
          <div>
            <span class="badge badge-primary">공유 코스</span>
            <h2>{{ sharedPlanTitle }}</h2>
            <p class="plan-code">코스 코드 <code>{{ post.travelPlanId }}</code></p>
          </div>
          <button class="btn btn-primary" type="button" :disabled="copyingPlan" @click="copyPlanToMine">
            {{ copyingPlan ? "복사 중" : "내 계획으로 복사" }}
          </button>
        </div>
        <p v-if="planMessage" class="notice mt-md">{{ planMessage }}</p>

        <div v-if="planDays.length" class="community-plan-tabs mt-md" aria-label="공유 여행 일차">
          <button
            v-for="day in planDays"
            :key="`shared-day-${day.dayNo}`"
            class="plan-day-tab"
            :class="{ 'plan-day-tab-active': selectedShareDay === day.dayNo }"
            type="button"
            @click="selectedShareDay = day.dayNo"
          >
            {{ day.dayNo }}일차
          </button>
        </div>

        <div class="community-plan-layout mt-md">
          <div class="plan-map-shell">
            <KakaoMap :spots="selectedDaySpots" :selected-spot="selectedShareSpot" :show-route="true" />
          </div>

          <div class="plan-list-panel">
            <h3>{{ selectedShareDay }}일차 코스</h3>
            <ol v-if="selectedDaySpots.length" class="plan-order-list">
              <li
                v-for="(spot, index) in selectedDaySpots"
                :key="`${selectedShareDay}-${spot.attractionContentId}-${index}`"
                class="place-card"
                :class="{ 'place-card-active': selectedShareSpot === spot }"
                tabindex="0"
                @click="selectShareSpot(spot)"
                @keydown.enter.prevent="selectShareSpot(spot)"
                @keydown.space.prevent="selectShareSpot(spot)"
              >
                <span class="plan-place-badge">{{ index + 1 }}</span>
                <img v-if="spot.imageUrl || spot.firstimage" class="community-plan-thumb" :src="spot.imageUrl || spot.firstimage" :alt="spot.name" />
                <div class="place-info">
                  <strong>{{ spot.name }}</strong>
                  <small>{{ spot.address || "주소 정보 없음" }}</small>
                </div>
              </li>
            </ol>
            <p v-else class="form-help">선택한 일차에 표시할 코스가 없습니다.</p>
          </div>
        </div>
      </section>

      <div class="post-content">{{ post.content }}</div>

      <section class="comment-section">
        <div class="comment-header">
          <h2>댓글 {{ comments.length }}</h2>
          <p>여행 코스와 장소에 대한 의견을 남겨보세요.</p>
        </div>

        <form v-if="authStore.isLoggedIn" class="comment-form" @submit.prevent="submitComment">
          <textarea v-model="commentContent" class="form-textarea" rows="3" placeholder="댓글을 입력하세요"></textarea>
          <button class="btn btn-primary" type="submit" :disabled="submittingComment">댓글 등록</button>
        </form>
        <p v-else class="form-help">댓글 작성은 로그인 후 사용할 수 있습니다.</p>

        <p v-if="commentMessage" class="notice">{{ commentMessage }}</p>

        <div v-if="comments.length" class="comment-list">
          <article v-for="comment in comments" :key="comment.id" class="comment-card">
            <div>
              <strong>{{ comment.userName || "작성자" }}</strong>
              <span>{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p>{{ comment.content }}</p>
            <button v-if="canManageComment(comment)" class="btn btn-ghost btn-sm" type="button" @click="removeComment(comment.id)">
              삭제
            </button>
          </article>
        </div>
      </section>
    </article>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { deleteComment, deletePost, getComments, getPost, togglePostLike, writeComment } from "@/api/postApi";
import { copySharedPlan, fetchSharedPlan } from "@/api/travelPlanApi";
import KakaoMap from "@/components/KakaoMap.vue";
import { authStore } from "@/stores/authStore";

const route = useRoute();
const router = useRouter();
const post = ref(null);
const sharedPlan = ref(null);
const planSpots = ref([]);
const selectedShareDay = ref(1);
const selectedShareSpot = ref(null);
const planMessage = ref("");
const postMessage = ref("");
const copyingPlan = ref(false);
const deletingPost = ref(false);
const likingPost = ref(false);
const comments = ref([]);
const commentContent = ref("");
const commentMessage = ref("");
const submittingComment = ref(false);
const adminEmails = ["a@a"];

const categoryMap = {
  NOTICE: { label: "공지", badgeClass: "badge-primary" },
  FREE: { label: "자유", badgeClass: "badge-soft" },
  REVIEW: { label: "후기", badgeClass: "badge-accent" },
  QNA: { label: "Q&A", badgeClass: "badge-soft" },
  PLAN_SHARE: { label: "코스 공유", badgeClass: "badge-primary" }
};

const categoryMeta = computed(() => categoryMap[post.value?.category] || { label: post.value?.category || "기타", badgeClass: "badge-soft" });
const sharedPlanTitle = computed(() => sharedPlan.value?.title || sharedPlan.value?.places?.[0]?.name || "여행 계획 미리보기");
const isAdmin = computed(() => authStore.user?.email && adminEmails.includes(authStore.user.email));
const canManagePost = computed(() =>
  Boolean(authStore.isLoggedIn && post.value && (isAdmin.value || String(post.value.userId) === String(authStore.user?.id)))
);

const reviewPlaceName = computed(() => {
  const title = post.value?.title || "";
  return title.replace(/^(\[[^\]]+\]\s*)?/, "") || "장소 정보";
});

const planDays = computed(() => {
  const days = new Map();
  planSpots.value.forEach((spot) => {
    const dayNo = Number(spot.dayNo || 1);
    if (!days.has(dayNo)) {
      days.set(dayNo, []);
    }
    days.get(dayNo).push(spot);
  });
  return [...days.entries()]
    .sort(([left], [right]) => left - right)
    .map(([dayNo, places]) => ({ dayNo, places }));
});

const selectedDaySpots = computed(() => {
  const currentDay = planDays.value.find((day) => day.dayNo === selectedShareDay.value);
  return currentDay?.places || [];
});

watch(planDays, (days) => {
  if (days.length && !days.some((day) => day.dayNo === selectedShareDay.value)) {
    selectedShareDay.value = days[0].dayNo;
  }
});

watch(selectedDaySpots, (spots) => {
  selectedShareSpot.value = spots[0] || null;
});

function selectShareSpot(spot) {
  selectedShareSpot.value = spot;
}

function canManageComment(comment) {
  return Boolean(authStore.isLoggedIn && comment?.userId != null && String(comment.userId) === String(authStore.user?.id));
}

onMounted(async () => {
  try {
    const data = await getPost(route.params.id);
    post.value = data;
    await loadComments();

    if (data.category === "PLAN_SHARE" && data.travelPlanId) {
      const planData = await fetchSharedPlan(data.travelPlanId);
      sharedPlan.value = planData;
      planSpots.value = planData.places || [];
      selectedShareDay.value = planDays.value[0]?.dayNo || 1;
      selectedShareSpot.value = selectedDaySpots.value[0] || null;
    }
  } catch (error) {
    postMessage.value = error.message || "게시글을 불러오지 못했습니다.";
  }
});

const loadComments = async () => {
  const data = await getComments(route.params.id);
  comments.value = data.items || [];
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleString();
};

const copyPlanToMine = async () => {
  planMessage.value = "";
  if (!authStore.isLoggedIn) {
    planMessage.value = "로그인이 필요합니다.";
    return;
  }
  if (!post.value?.travelPlanId) {
    planMessage.value = "공유 코드를 확인할 수 없습니다.";
    return;
  }

  copyingPlan.value = true;
  try {
    await copySharedPlan(post.value.travelPlanId);
    planMessage.value = "내 여행 계획으로 복사했습니다.";
    router.push("/plans");
  } catch (error) {
    planMessage.value = error.message || "여행 코스 복사에 실패했습니다.";
  } finally {
    copyingPlan.value = false;
  }
};

const likePost = async () => {
  if (!post.value?.id) return;
  likingPost.value = true;
  try {
    const before = Number(post.value.likeCount || 0);
    const data = await togglePostLike(post.value.id);
    post.value.likeCount = data.liked ? before + 1 : Math.max(before - 1, 0);
  } catch (error) {
    postMessage.value = error.message || "좋아요 처리에 실패했습니다.";
  } finally {
    likingPost.value = false;
  }
};

const submitComment = async () => {
  commentMessage.value = "";
  if (!commentContent.value.trim()) {
    commentMessage.value = "댓글 내용을 입력해주세요.";
    return;
  }
  submittingComment.value = true;
  try {
    await writeComment(post.value.id, commentContent.value.trim());
    commentContent.value = "";
    await loadComments();
    post.value.commentCount = comments.value.length;
  } catch (error) {
    commentMessage.value = error.message || "댓글 등록에 실패했습니다.";
  } finally {
    submittingComment.value = false;
  }
};

const removeComment = async (commentId) => {
  try {
    await deleteComment(commentId);
    await loadComments();
    post.value.commentCount = comments.value.length;
  } catch (error) {
    commentMessage.value = error.message || "댓글 삭제에 실패했습니다.";
  }
};

const removePost = async () => {
  postMessage.value = "";
  if (!window.confirm("게시글을 삭제할까요?")) return;

  deletingPost.value = true;
  try {
    await deletePost(post.value.id);
    router.push("/community");
  } catch (error) {
    postMessage.value = error.message || "게시글 삭제에 실패했습니다.";
  } finally {
    deletingPost.value = false;
  }
};
</script>

<style scoped>
.community-detail-card {
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid var(--color-border-soft);
  border-radius: 28px;
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.community-detail-header {
  background:
    linear-gradient(135deg, rgba(239, 246, 255, 0.96) 0%, rgba(255, 255, 255, 0.96) 55%, rgba(236, 253, 245, 0.88) 100%);
  border-bottom: 1px solid var(--color-border-soft);
  display: grid;
  gap: var(--space-sm);
  padding: var(--space-xl);
}

.community-detail-header h1 {
  color: var(--color-text-strong);
  font-size: clamp(2rem, 4vw, 3.2rem);
  line-height: 1.1;
  margin: 0;
  max-width: 860px;
}

.community-detail-meta,
.post-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.community-detail-meta {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.post-actions {
  justify-content: flex-end;
}

.community-detail-image {
  aspect-ratio: 16 / 7;
  display: block;
  border-radius: var(--radius-xl);
  margin: var(--space-lg) auto 0;
  max-height: 520px;
  object-fit: cover;
  width: min(1040px, calc(100% - 3rem));
}

.review-place-summary {
  background: var(--color-accent-soft);
  border: 1px solid rgba(255, 122, 89, 0.28);
  border-radius: var(--radius-xl);
  display: grid;
  gap: 0.3rem;
  margin: var(--space-lg) var(--space-lg) 0;
  padding: var(--space-md);
}

.review-place-summary strong,
.plan-share-header h2,
.plan-list-panel h3,
.comment-header h2 {
  color: var(--color-text-strong);
  margin: 0;
}

.review-place-summary p,
.plan-code,
.comment-header p {
  color: var(--color-text-muted);
  margin: 0;
}

.post-content {
  color: var(--color-text);
  line-height: 1.75;
  min-height: 180px;
  padding: var(--space-xl);
  white-space: pre-line;
}

.plan-share-section {
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.88), rgba(255, 255, 255, 0.96)),
    var(--color-bg);
  border-top: 1px solid var(--color-border-soft);
  padding: var(--space-xl);
}

.plan-share-header {
  align-items: center;
  background:
    linear-gradient(135deg, rgba(239, 246, 255, 0.96), rgba(236, 253, 245, 0.86)),
    white;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-xl);
  display: flex;
  gap: var(--space-md);
  justify-content: space-between;
  padding: var(--space-lg);
}

.plan-share-header h2 {
  margin: var(--space-xs) 0;
}

.plan-code code {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  font-family: monospace;
  padding: 0.25rem 0.55rem;
}

.community-plan-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}

.community-plan-layout {
  align-items: start;
  display: grid;
  gap: var(--space-md);
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  min-height: 0;
}

.plan-map-shell {
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  height: 520px;
  overflow: hidden;
}

.plan-list-panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  display: grid;
  gap: var(--space-md);
  grid-template-rows: auto minmax(0, 1fr);
  height: 520px;
  min-height: 0;
  overflow: hidden;
  padding: var(--space-md);
}

.plan-order-list {
  align-content: start;
  display: grid;
  gap: var(--space-sm);
  list-style: none;
  margin: 0;
  min-height: 0;
  overflow: auto;
  padding: 0;
}

.plan-order-list .place-card {
  align-items: center;
  background: linear-gradient(135deg, #ffffff, #f8fafc);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  cursor: pointer;
  display: grid;
  gap: var(--space-sm);
  grid-template-columns: auto 72px minmax(0, 1fr);
  padding: var(--space-sm);
}

.plan-order-list .place-card:hover,
.plan-order-list .place-card:focus,
.plan-order-list .place-card-active {
  border-color: rgba(122, 111, 93, 0.48);
  box-shadow: 0 12px 26px rgba(122, 111, 93, 0.10);
  outline: none;
}

.community-plan-thumb {
  aspect-ratio: 1 / 1;
  border-radius: var(--radius-md);
  object-fit: cover;
  width: 72px;
}

.place-info {
  display: grid;
  gap: 0.1rem;
}

.place-info small {
  color: var(--color-text-muted);
}

.comment-section {
  border-top: 1px solid var(--color-border-soft);
  display: grid;
  gap: var(--space-md);
  padding: var(--space-xl);
}

.comment-form,
.comment-list,
.comment-card {
  display: grid;
  gap: var(--space-sm);
}

.comment-card {
  background: var(--color-bg);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  padding: var(--space-md);
}

.comment-card > div {
  align-items: center;
  display: flex;
  gap: var(--space-sm);
  justify-content: space-between;
}

.comment-card strong {
  color: var(--color-text-strong);
}

.comment-card span {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.comment-card p {
  margin: 0;
}

.mt-md {
  margin-top: var(--space-md);
}

@media (max-width: 1023px) {
  .community-plan-layout {
    grid-template-columns: 1fr;
  }

  .plan-map-shell,
  .plan-list-panel {
    height: 430px;
  }
}

@media (max-width: 767px) {
  .community-detail-header,
  .plan-share-section,
  .post-content {
    padding: var(--space-lg);
  }

  .plan-share-header {
    align-items: stretch;
    display: grid;
  }

  .plan-order-list .place-card {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .community-plan-thumb {
    display: none;
  }
}
</style>
