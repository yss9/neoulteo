import { apiFetch } from "@/api/http";

export function runTourBatch() {
  return apiFetch("/api/batch/tour", {
    method: "POST"
  });
}
