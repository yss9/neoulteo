import { createRouter, createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import AttractionSearchPage from "@/pages/AttractionSearchPage.vue";
import TravelPlanPage from "@/pages/TravelPlanPage.vue";
import HotplacePage from "@/pages/HotplacePage.vue";
import MyHotplacePage from "@/pages/MyHotplacePage.vue";
import ProfilePage from "@/pages/ProfilePage.vue";
import CommunityListPage from "@/pages/community/CommunityListPage.vue";
import CommunityDetailPage from "@/pages/community/CommunityDetailPage.vue";
import CommunityWritePage from "@/pages/community/CommunityWritePage.vue";

const routes = [
  { path: "/", name: "home", component: HomePage },
  { path: "/attractions", name: "attractions", component: AttractionSearchPage },
  { path: "/plans", name: "plans", component: TravelPlanPage },
  { path: "/hotplaces", name: "hotplaces", component: HotplacePage },
  { path: "/hotplaces/my", name: "my-hotplaces", component: MyHotplacePage },
  { path: "/profile", name: "profile", component: ProfilePage },
  { path: "/community", name: "community", component: CommunityListPage },
  { path: "/community/write", name: "community-write", component: CommunityWritePage },
  { path: "/community/:id", name: "community-detail", component: CommunityDetailPage }
];

export default createRouter({
  history: createWebHistory(),
  routes
});
