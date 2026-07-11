import { apiFetch } from "@/api/http";

export async function chat(message, useRag) {
  const endpoint = useRag ? "/integrated-chat" : "/chat";
  const response = await fetch(`http://127.0.0.1:8000${endpoint}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message })
  });
  return response.json();
}

export async function travelAssistant(message) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), 45000);

  try {
    return await apiFetch("/app/ai/travel-assistant", {
      method: "POST",
      signal: controller.signal,
      body: JSON.stringify({
        message,
        useRag: true,
        useTools: true
      })
    });
  } finally {
    window.clearTimeout(timeoutId);
  }
}

export async function uploadDocument(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch("http://127.0.0.1:8000/upload", {
    method: "POST",
    body: formData
  });
  return response.json();
}
