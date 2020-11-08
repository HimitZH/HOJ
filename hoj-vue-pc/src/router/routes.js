import Home from '@/views/Home.vue'
import SetNewPassword from "@/views/user/SetNewPassword.vue"
import UserHome from "@/views/user/UserHome.vue"
import Setting from "@/views/user/Setting.vue"
import ProblemLIst from "@/views/problem/ProblemList.vue"
import Logout from "@/views/user/Logout.vue"
import SubmissionList from "@/views/status/SubmissionList.vue"
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/problem',
    name: 'ProblemList',
    component: ProblemLIst
  },
  {
    path: '/status',
    name: 'SubmissionList',
    component: SubmissionList
  },
  {
    path: '/reset-password',
    name: 'SetNewPassword',
    component: SetNewPassword
  },
  {
    name: 'UserHome',
    path: '/user-home',
    component: UserHome,
    meta: {requireAuth: true, title: 'User Home'}
  },
  {
    name: 'Setting',
    path: '/setting',
    component: Setting,
    meta: {requireAuth: true, title: 'Setting'}
  },
  {
    name: 'Logout',
    path: '/logout',
    component: Logout,
    meta: {requireAuth: true, title: 'Logout'}
  },
]
export default routes
