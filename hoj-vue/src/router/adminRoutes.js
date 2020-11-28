
// 引入 view 组件
import Login from '@/views/admin/Login'
import Home from '@/views/admin/Home'
import Dashboard from '@/views/admin/Dashboard'
import User from '@/views/admin/general/User'
import Announcement from '@/views/admin/general/Announcement'
import SystemConfig from '@/views/admin/general/SystemConfig'
import PruneTestCase from '@/views/admin/general/PruneTestCase'
const adminRoutes= [
    {
      path: '/admin/login',
      name: 'login',
      component: Login
    },
    {
      path: '/admin/',
      component: Home,
      children: [
        {
          path: '',
          name: 'dashboard',
          component: Dashboard
        },
        {
          path: 'user',
          name: 'user',
          component: User
      },
        {
          path: 'announcement',
          name: 'announcement',
          component: Announcement
      },
        {
          path: 'conf',
          name: 'conf',
          component: SystemConfig
        },
        {
          path: 'prune-test-case',
          name: 'prune-test-case',
          component: PruneTestCase
        },
    //     {
    //       path: '/problems',
    //       name: 'problem-list',
    //       component: ProblemList
    //     },
    //     {
    //       path: '/problem/create',
    //       name: 'create-problem',
    //       component: Problem
    //     },
    //     {
    //       path: '/problem/edit/:problemId',
    //       name: 'edit-problem',
    //       component: Problem
    //     },
    //     {
    //       path: '/problem/batch_ops',
    //       name: 'problem_batch_ops',
    //       component: ProblemImportOrExport
    //     },
    //     {
    //       path: '/contest/create',
    //       name: 'create-contest',
    //       component: Contest
    //     },
    //     {
    //       path: '/contest',
    //       name: 'contest-list',
    //       component: ContestList
    //     },
    //     {
    //       path: '/contest/:contestId/edit',
    //       name: 'edit-contest',
    //       component: Contest
    //     },
    //     {
    //       path: '/contest/:contestId/announcement',
    //       name: 'contest-announcement',
    //       component: Announcement
    //     },
    //     {
    //       path: '/contest/:contestId/problems',
    //       name: 'contest-problem-list',
    //       component: ProblemList
    //     },
    //     {
    //       path: '/contest/:contestId/problem/create',
    //       name: 'create-contest-problem',
    //       component: Problem
    //     },
    //     {
    //       path: '/contest/:contestId/problem/:problemId/edit',
    //       name: 'edit-contest-problem',
    //       component: Problem
    //     }
      ]
    },
    {
      path: '*', redirect: '/login'
    }
  ]

  export default adminRoutes
