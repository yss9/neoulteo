<script setup>
import { ref } from "vue";
import { chat, travelAssistant, uploadDocument } from "@/api/aiApi";

const open = ref(false);
const useSpringAi = ref(true);
const useRag = ref(true);
const input = ref("");
const messages = ref([]);
const file = ref(null);
const loading = ref(false);

function formatSpringAiSource(data) {
  const sources = data?.data?.ragSources || [];
  const tools = data?.data?.toolResults || [];
  const sourceNames = sources.slice(0, 3).map((source) => source.title).filter(Boolean);
  const toolNames = tools.map((tool) => tool.toolName).filter(Boolean);
  const parts = [];
  if (sourceNames.length) {
    parts.push(`RAG: ${sourceNames.join(", ")}`);
  }
  if (toolNames.length) {
    parts.push(`Tools: ${toolNames.join(", ")}`);
  }
  return parts.join(" / ");
}

async function send() {
  const text = input.value.trim();
  if (!text) return;

  messages.value.push({ type: "user", text });
  input.value = "";
  loading.value = true;
  try {
    if (useSpringAi.value) {
      const data = await travelAssistant(text);
      messages.value.push({
        type: "bot",
        text: data?.data?.answer || data?.message || "응답을 생성하지 못했습니다.",
        source: formatSpringAiSource(data)
      });
      return;
    }

    const data = await chat(text, useRag.value);
    messages.value.push({ type: "bot", text: data.answer, source: data.source });
  } catch (error) {
    const message =
      error.name === "AbortError"
        ? "응답 시간이 너무 오래 걸려 요청을 중단했습니다. 잠시 후 다시 시도해주세요."
        : error.message;
    messages.value.push({ type: "bot", text: message });
  } finally {
    loading.value = false;
  }
}

async function upload() {
  if (!file.value) return;

  loading.value = true;
  try {
    const data = await uploadDocument(file.value);
    messages.value.push({ type: "bot", text: data.message || "문서 업로드가 완료되었습니다." });
    file.value = null;
  } catch (error) {
    messages.value.push({ type: "bot", text: error.message || "문서 업로드에 실패했습니다." });
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <aside class="chatbot" :class="{ open }">
    <button
      class="chatbot-button"
      type="button"
      :aria-expanded="open"
      aria-controls="neoulteo-ai-panel"
      @click="open = !open"
    >
      {{ open ? "닫기" : "AI" }}
    </button>

    <section v-if="open" id="neoulteo-ai-panel" class="chatbot-panel" aria-label="Neoulteo AI">
      <header class="chatbot-header">
        <div>
          <strong>Neoulteo AI</strong>
          <span>여행지 검색, RAG, Tool Calling 기반 질문을 도와줍니다.</span>
        </div>
        <button class="chatbot-close" type="button" aria-label="챗봇 닫기" @click="open = false">
          x
        </button>
      </header>

      <div class="chatbot-options">
        <label>
          <input v-model="useSpringAi" type="checkbox" />
          Spring AI RAG+Tool
        </label>
        <label v-if="!useSpringAi">
          <input v-model="useRag" type="checkbox" />
          FastAPI RAG
        </label>
      </div>

      <div class="chatbot-messages" aria-live="polite">
        <div v-if="messages.length === 0 && !loading" class="chatbot-empty">
          <strong>무엇을 도와드릴까요?</strong>
          <span>여행지, 일정, 업로드한 문서에 대해 질문할 수 있습니다.</span>
        </div>

        <p
          v-for="(message, index) in messages"
          :key="index"
          class="chatbot-message"
          :class="`chatbot-message-${message.type}`"
        >
          <span>{{ message.text }}</span>
          <small v-if="message.source">source: {{ message.source }}</small>
        </p>

        <p v-if="loading" class="chatbot-message chatbot-message-bot">
          <span>답변을 준비하고 있어요.</span>
        </p>
      </div>

      <form class="chatbot-input-row" @submit.prevent="send">
        <input v-model="input" placeholder="여행에 대해 질문하기" />
        <button class="btn btn-primary" type="submit" :disabled="loading">전송</button>
      </form>

      <div class="chatbot-upload-row">
        <input type="file" @change="file = $event.target.files?.[0] || null" />
        <button class="btn btn-secondary" type="button" :disabled="loading || !file" @click="upload">
          업로드
        </button>
      </div>
    </section>
  </aside>
</template>
