<template>
  <div class="community-list">
    <div class="community-list-actions">
      <router-link to="/community/write" class="btn btn-primary">글쓰기</router-link>
    </div>

    <section class="community-category-bar" aria-label="게시판 카테고리">
      <button
        v-for="cat in categories"
        :key="cat.value"
        type="button"
        class="community-category-button"
        :class="{ 'community-category-button-active': selectedCategory === cat.value }"
        @click="selectedCategory = cat.value"
      >
        <span>{{ cat.label }}</span>
        <small>{{ cat.description }}</small>
      </button>
    </section>

    <section class="community-filter-panel">
      <input
        v-model="keyword"
        class="form-input"
        placeholder="제목, 내용, 작성자로 검색"
        @keydown.enter.prevent="fetchPosts"
      />
      <select v-model="sort" class="form-select" @change="fetchPosts">
        <option value="latest">최신순</option>
        <option value="views">조회순</option>
        <option value="likes">좋아요순</option>
      </select>
      <button class="btn btn-secondary" type="button" @click="fetchPosts">검색</button>
    </section>

    <section class="community-post-grid">
      <article
        v-for="post in paginatedPosts"
        :key="post.id"
        class="community-post-card"
        :class="[`community-post-card-${post.category?.toLowerCase()}`]"
        @click="$router.push(`/community/${post.id}`)"
      >
        <div class="community-post-card-header">
          <span class="badge" :class="categoryMeta(post.category).badgeClass">
            {{ categoryMeta(post.category).label }}
          </span>
          <span class="community-post-date">{{ formatDate(post.createdAt) }}</span>
        </div>

        <h2>{{ post.title }}</h2>
        <p class="community-post-excerpt">{{ excerpt(post.content) }}</p>

        <div v-if="post.category === 'PLAN_SHARE'" class="community-plan-highlight">
          <strong>공유 여행 코스</strong>
          <span>코드 {{ post.travelPlanId || '확인 필요' }}</span>
        </div>

        <div v-else-if="post.category === 'REVIEW'" class="community-review-highlight">
          <strong>여행지 후기</strong>
          <span>{{ reviewPlaceName(post) }}</span>
        </div>

        <footer class="community-post-meta">
          <span>{{ post.userName || '작성자' }}</span>
          <span>조회 {{ post.views || 0 }}</span>
          <span>좋아요 {{ post.likeCount || 0 }}</span>
          <span>댓글 {{ post.commentCount || 0 }}</span>
        </footer>
      </article>

      <div v-if="posts.length === 0" class="empty-state community-empty">
        <strong class="empty-state-title">게시글이 없습니다.</strong>
        <p class="empty-state-description">선택한 게시판에 아직 등록된 글이 없습니다.</p>
      </div>
    </section>

    <nav v-if="communityTotalPages > 1" class="pagination" aria-label="커뮤니티 페이지">
      <button class="btn btn-ghost btn-sm" type="button" :disabled="communityPage === 1" @click="setCommunityPage(communityPage - 1)">이전</button>
      <button
        v-for="page in communityTotalPages"
        :key="`community-page-${page}`"
        class="btn btn-sm"
        :class="page === communityPage ? 'btn-primary' : 'btn-ghost'"
        type="button"
        @click="setCommunityPage(page)"
      >
        {{ page }}
      </button>
      <button class="btn btn-ghost btn-sm" type="button" :disabled="communityPage === communityTotalPages" @click="setCommunityPage(communityPage + 1)">다음</button>
    </nav>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { getPosts } from "@/api/postApi";

const categories = [
  { label: "전체", value: "ALL", description: "모든 글" },
  { label: "공지사항", value: "NOTICE", description: "운영 안내" },
  { label: "자유게시판", value: "FREE", description: "가벼운 이야기" },
  { label: "여행지 후기", value: "REVIEW", description: "장소 경험 공유" },
  { label: "질의응답", value: "QNA", description: "궁금한 점" },
  { label: "여행 계획 공유", value: "PLAN_SHARE", description: "코스 공유" }
];

const categoryMap = {
  NOTICE: { label: "공지", badgeClass: "badge-primary" },
  FREE: { label: "자유", badgeClass: "badge-soft" },
  REVIEW: { label: "후기", badgeClass: "badge-accent" },
  QNA: { label: "Q&A", badgeClass: "badge-soft" },
  PLAN_SHARE: { label: "코스 공유", badgeClass: "badge-primary" }
};

const selectedCategory = ref("ALL");
const posts = ref([]);
const keyword = ref("");
const sort = ref("latest");
const communityPage = ref(1);
const communityPageSize = 8;
const communityTotalPages = computed(() => Math.max(Math.ceil(posts.value.length / communityPageSize), 1));
const paginatedPosts = computed(() => {
  const start = (communityPage.value - 1) * communityPageSize;
  return posts.value.slice(start, start + communityPageSize);
});

const fetchPosts = async () => {
  try {
    const data = await getPosts({
      category: selectedCategory.value,
      keyword: keyword.value.trim(),
      sort: sort.value
    });
    posts.value = data.posts || [];
    communityPage.value = 1;
  } catch (error) {
    posts.value = [];
  }
};

const setCommunityPage = (page) => {
  communityPage.value = Math.min(Math.max(page, 1), communityTotalPages.value);
};

const categoryMeta = (category) => categoryMap[category] || { label: category || "기타", badgeClass: "badge-soft" };

const stripHtml = (value) => String(value || "").replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim();

const excerpt = (content) => {
  const text = stripHtml(content);
  return text.length > 96 ? `${text.slice(0, 96)}...` : text || "내용 미리보기가 없습니다.";
};

const reviewPlaceName = (post) => {
  const title = post.title || "";
  return title.replace(/^(\[[^\]]+\]\s*)?/, "") || "장소 정보 확인";
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleDateString();
};

onMounted(fetchPosts);
watch(selectedCategory, fetchPosts);
</script>

<style scoped>
.community-list {
  display: grid;
  gap: var(--space-lg);
}

.community-list-actions {
  display: flex;
  justify-content: flex-end;
}

.community-hero {
  align-items: end;
  background: linear-gradient(135deg, #ecfeff 0%, #f8fafc 55%, #eef2ff 100%);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-xl);
  display: flex;
  gap: var(--space-md);
  justify-content: space-between;
  padding: var(--space-xl);
}

.community-hero > div {
  display: grid;
  gap: var(--space-xs);
}

.community-hero h1 {
  color: var(--color-text-strong);
  font-size: clamp(2rem, 4vw, 3.2rem);
  line-height: 1.05;
  margin: 0;
}

.community-hero p {
  color: var(--color-text-muted);
  margin: 0;
}

.community-category-bar {
  display: grid;
  gap: var(--space-sm);
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.community-filter-panel {
  align-items: center;
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  display: grid;
  gap: var(--space-sm);
  grid-template-columns: minmax(0, 1fr) 150px auto;
  padding: var(--space-md);
}

.community-category-button {
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  color: var(--color-text);
  cursor: pointer;
  display: grid;
  gap: 0.2rem;
  min-height: 76px;
  padding: var(--space-sm);
  text-align: left;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.community-category-button span {
  color: var(--color-text-strong);
  font-weight: 900;
}

.community-category-button small {
  color: var(--color-text-muted);
}

.community-category-button:hover,
.community-category-button-active {
  border-color: var(--color-primary);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}

.community-post-grid {
  display: grid;
  gap: var(--space-md);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.community-post-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  display: grid;
  gap: var(--space-sm);
  min-height: 220px;
  padding: var(--space-md);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.community-post-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.community-post-card-plan_share {
  background: linear-gradient(135deg, #f0fdfa 0%, #ffffff 60%);
  border-color: rgba(122, 111, 93, 0.28);
}

.community-post-card-review {
  background: linear-gradient(135deg, #f2eee8 0%, #ffffff 62%);
}

.community-post-card-header,
.community-post-meta {
  align-items: center;
  display: flex;
  gap: var(--space-sm);
  justify-content: space-between;
}

.community-post-date,
.community-post-meta {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.community-post-card h2 {
  color: var(--color-text-strong);
  font-size: var(--font-size-xl);
  line-height: 1.25;
  margin: 0;
}

.community-post-excerpt {
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
}

.community-plan-highlight,
.community-review-highlight {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  display: grid;
  gap: 0.2rem;
  margin-top: auto;
  padding: var(--space-sm);
}

.community-plan-highlight strong,
.community-review-highlight strong {
  color: var(--color-primary-dark);
}

.community-plan-highlight span,
.community-review-highlight span {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.community-empty {
  grid-column: 1 / -1;
}

@media (max-width: 1023px) {
  .community-category-bar {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .community-hero {
    align-items: stretch;
    display: grid;
  }

  .community-category-bar,
  .community-filter-panel,
  .community-post-grid {
    grid-template-columns: 1fr;
  }
}
</style>
