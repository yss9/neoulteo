import { reactive } from "vue";
import { clearAuthToken, setAuthToken } from "@/api/http";
import { getProfile, login, logout } from "@/api/userApi";

export const authStore = reactive({
  user: null,
  loading: false,

  get isLoggedIn() {
    return Boolean(this.user);
  },

  async loadProfile() {
    this.loading = true;
    try {
      const data = await getProfile();
      this.user = data.success ? { id: data.id, name: data.name, email: data.email } : null;
      if (!data.success) {
        clearAuthToken();
      }
    } catch {
      clearAuthToken();
      this.user = null;
    } finally {
      this.loading = false;
    }
  },

  async login(email, password, rememberMe = false) {
    const data = await login(email, password, rememberMe);
    if (data.success) {
      setAuthToken(data.token, rememberMe);
      this.user = { id: data.id, name: data.name, email: data.email };
    }
    return data;
  },

  async logout() {
    try {
      return await logout();
    } finally {
      clearAuthToken();
      this.user = null;
    }
  }
});
