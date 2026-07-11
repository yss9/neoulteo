import { apiFetch } from "@/api/http";

export function evaluatePlanWithAi(payload) {
  return apiFetch("/app/ai/evaluate-plan", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
