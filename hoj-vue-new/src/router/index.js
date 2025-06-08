import { createRouter, createWebHistory } from 'vue-router'
// Make sure the path to Home.vue is correct
import HomeView from '../views/oj/Home.vue' // Changed from HomeView.vue to oj/Home.vue
import ProblemDetailView from '../views/oj/ProblemDetailView.vue';
import ContestDetailView from '../views/oj/ContestDetailView.vue';
import ContestListView from '../views/oj/ContestListView.vue';
import UserHomeView from '../views/oj/UserHomeView.vue'; // Added

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView // This should point to the new Home.vue
  },
  { // Placeholder for ProblemDetails
    path: '/problem/:problemID',
    name: 'ProblemDetails',
    component: ProblemDetailView, // Replace with actual component later
    props: true
  },
  { // Placeholder for ContestDetails
    path: '/contest/:contestID',
    name: 'ContestDetails',
    component: ContestDetailView, // Replace later
    props: true
  },
  { // Placeholder for ContestList
    path: '/contest',
    name: 'ContestList',
    component: ContestListView, // Replace later
    props: route => ({ query: route.query })
  },
  { // Placeholder for UserHome
    path: '/user-home',
    name: 'UserHome',
    component: UserHomeView, // Replace later
    props: route => ({ query: route.query })
  }
  // _TODO: Add other OJ routes here later
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
