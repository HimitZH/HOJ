
// 引入 view 组件
import Login from '@/views/admin/Login'
import Home from '@/views/admin/Home'
import Dashboard from '@/views/admin/Dashboard'
import User from '@/views/admin/general/User'
import Announcement from '@/views/admin/general/Announcement'
import SystemConfig from '@/views/admin/general/SystemConfig'
import ProblemList from '@/views/admin/problem/ProblemList'
import Problem from '@/views/admin/problem/Problem'
import ProblemImportAndExport from '@/views/admin/problem/ImportAndExport'
import Contest from '@/views/admin/contest/Contest'
import ContestList from '@/views/admin/contest/ContestList'
const adminRoutes= [
    {
      path: '/admin/login',
      name: 'admin-login',
      component: Login,
      meta: { title: 'Login' }
    },
    {
      path: '/admin/',
      component: Home,
      meta: {requireAuth:true, requireAdmin: true},
      children: [
        {
          path: '',
          redirect: 'dashboard',
          component: Dashboard,
          meta: { title: 'Dashboard' }
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: Dashboard,
          meta: { title: 'Dashboard' }
        },
        {
          path: 'user',
          name: 'admin-user',
          component: User,
          meta: { requireSuperAdmin: true,title:'User'},
      },
        {
          path: 'announcement',
          name: 'admin-announcement',
          component: Announcement,
          meta: { requireSuperAdmin: true,title:'Announcement'},
      },
        {
          path: 'conf',
          name: 'admin-conf',
          component: SystemConfig,
          meta: { requireSuperAdmin: true,title:'System Config'},
        },
        {
          path: 'problems',
          name: 'admin-problem-list',
          component: ProblemList,
          meta: { title:'Problem List'},
        },
        {
          path: 'problem/create',
          name: 'admin-create-problem',
          component: Problem,
          meta: { title:'Create Problem'},
        },
        {
          path: 'problem/edit/:problemId',
          name: 'admin-edit-problem',
          component: Problem,
          meta: { title:'Edit Problem'},
        },
        {
          path: 'problem/batch-operation',
          name: 'admin-problem_batch_operation',
          component: ProblemImportAndExport,
          meta: { title:'Problem Operation'},
        },
        {
          path: 'contest/create',
          name: 'admin-create-contest',
          component: Contest,
          meta: { title:'Create Contest'},
        },
        {
          path: 'contest',
          name: 'admin-contest-list',
          component: ContestList,
          meta: { title:'Contest List'}
        },
        {
          path: 'contest/:contestId/edit',
          name: 'admin-edit-contest',
          component: Contest,
          meta: { title:'Edit Contest'}
        },
        {
          path: 'contest/:contestId/announcement',
          name: 'admin-contest-announcement',
          component: Announcement,
          meta: { title:'Create Contest Announcement'}
        },
        {
          path: 'contest/:contestId/problems',
          name: 'admin-contest-problem-list',
          component: ProblemList,
          meta: { title:'Contest Problem List'}
        },
        {
          path: 'contest/:contestId/problem/create',
          name: 'admin-create-contest-problem',
          component: Problem,
          meta: { title:'Create Contest Problem'}
        },
        {
          path: 'contest/:contestId/problem/:problemId/edit',
          name: 'admin-edit-contest-problem',
          component: Problem,
          meta: { title:'Edit Contest Problem'}
        }
      ]
    },
    {
      path: '/admin/*', redirect: '/admin/login'
    }
  ]

  export default adminRoutes
