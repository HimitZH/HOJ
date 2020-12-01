
// 引入 view 组件
import Login from '@/views/admin/Login'
import Home from '@/views/admin/Home'
import Dashboard from '@/views/admin/Dashboard'
import User from '@/views/admin/general/User'
import Announcement from '@/views/admin/general/Announcement'
import SystemConfig from '@/views/admin/general/SystemConfig'
import DeleteTestCase from '@/views/admin/general/DeleteTestCase'
import ProblemList from '@/views/admin/problem/ProblemList'
import Problem from '@/views/admin/problem/Problem'
import ProblemImportAndExport from '@/views/admin/problem/ImportAndExport'
import Contest from '@/views/admin/contest/Contest'
import ContestList from '@/views/admin/contest/ContestList'
const adminRoutes= [
    {
      path: '/admin/login',
      name: 'admin-login',
      component: Login
    },
    {
      path: '/admin/',
      component: Home,
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: Dashboard
        },
        {
          path: 'user',
          name: 'admin-user',
          component: User
      },
        {
          path: 'announcement',
          name: 'admin-announcement',
          component: Announcement
      },
        {
          path: 'conf',
          name: 'admin-conf',
          component: SystemConfig
        },
        {
          path: 'delete-test-case',
          name: 'admin-delete-test-case',
          component: DeleteTestCase
        },
        {
          path: 'problems',
          name: 'admin-problem-list',
          component: ProblemList
        },
        {
          path: 'problem/create',
          name: 'admin-create-problem',
          component: Problem
        },
        {
          path: 'problem/edit/:problemId',
          name: 'admin-edit-problem',
          component: Problem
        },
        {
          path: 'problem/batch-operation',
          name: 'admin-problem_batch_operation',
          component: ProblemImportAndExport
        },
        {
          path: 'contest/create',
          name: 'admin-create-contest',
          component: Contest
        },
        {
          path: 'contest',
          name: 'admin-contest-list',
          component: ContestList
        },
        {
          path: 'contest/:contestId/edit',
          name: 'admin-edit-contest',
          component: Contest
        },
        {
          path: 'contest/:contestId/announcement',
          name: 'admin-contest-announcement',
          component: Announcement
        },
        {
          path: 'contest/:contestId/problems',
          name: 'admin-contest-problem-list',
          component: ProblemList
        },
        {
          path: 'contest/:contestId/problem/create',
          name: 'admin-create-contest-problem',
          component: Problem
        },
        {
          path: 'contest/:contestId/problem/:problemId/edit',
          name: 'admin-edit-contest-problem',
          component: Problem
        }
      ]
    },
    {
      path: '/admin/*', redirect: '/admin/login'
    }
  ]

  export default adminRoutes
