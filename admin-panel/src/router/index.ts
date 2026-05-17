import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import CinemaHallEditorView from '@/views/CinemaHallEditorView.vue'
import CinemaHallsListView from '@/views/CinemaHallsListView.vue'
import MoviesView from '@/views/MoviesView.vue'
import ScreeningsView from '@/views/ScreeningsView.vue'
import UsersView from '@/views/UsersView.vue'
import AdminLayout from '@/components/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardView
        },
        {
          path: 'halls',
          name: 'halls',
          component: CinemaHallsListView
        },
        {
          path: 'halls/new',
          name: 'halls-new',
          component: CinemaHallEditorView
        },
        {
          path: 'halls/:id',
          name: 'halls-edit',
          component: CinemaHallEditorView
        },
        {
          path: 'movies',
          name: 'movies',
          component: MoviesView
        },
        {
          path: 'screenings',
          name: 'screenings',
          component: ScreeningsView
        },
        {
          path: 'users',
          name: 'users',
          component: UsersView
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }
  if (to.name === 'login' && token) {
    return { name: 'dashboard' }
  }
})

export default router
