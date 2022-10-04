
// 引入 view 组件
const Login= ()=>import('@/views/admin/Login')
const Home= ()=>import('@/views/admin/Home')
const Dashboard= ()=>import('@/views/admin/Dashboard')
const User= ()=>import('@/views/admin/general/User')
const Announcement= ()=>import('@/views/admin/general/Announcement')
const SysNotice= ()=>import('@/views/admin/general/SysNotice')
const SystemConfig= ()=>import('@/views/admin/general/SystemConfig')
const SysSwitch= ()=>import('@/views/admin/general/SysSwitch')
const ProblemList= ()=>import('@/views/admin/problem/ProblemList')
const AdminGroupProblemList= ()=>import('@/views/admin/problem/GroupProblemList')
const Problem= ()=>import('@/views/admin/problem/Problem')
const Tag= ()=>import('@/views/admin/problem/Tag')
const ProblemImportAndExport= ()=>import('@/views/admin/problem/ImportAndExport')
const Contest= ()=>import('@/views/admin/contest/Contest')
const ContestList= ()=>import('@/views/admin/contest/ContestList')
const Training= ()=>import('@/views/admin/training/Training')
const TrainingList= ()=>import('@/views/admin/training/TrainingList')
const TrainingProblemList= ()=>import('@/views/admin/training/TrainingProblemList')
const TrainingCategory= ()=>import('@/views/admin/training/Category')
const DiscussionList= ()=>import('@/views/admin/discussion/Discussion')
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
      meta: { requireAuth:true, requireAdmin: true },
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
          meta: { requireSuperAdmin: true,title:'User Admin'},
      },
        {
          path: 'announcement',
          name: 'admin-announcement',
          component: Announcement,
          meta: { requireSuperAdmin: true,title:'Announcement Admin'},
      },
      {
        path: 'notice',
        name: 'admin-notice',
        component: SysNotice,
        meta: { requireSuperAdmin: true,title:'Notice Admin'},
      },
        {
          path: 'conf',
          name: 'admin-conf',
          component: SystemConfig,
          meta: { requireSuperAdmin: true,title:'System Config'},
        },
        {
          path: 'switch',
          name: 'admin-switch',
          component: SysSwitch,
          meta: { requireSuperAdmin: true,title:'System Switch'},
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
          path: 'problem/tag',
          name: 'admin-problem-tag',
          component: Tag,
          meta: { title:'Admin Tag'},
        },
        {
          path: 'group-problem/apply',
          name: 'admin-group-apply-problem',
          component: AdminGroupProblemList,
          meta: { title:'Admin Group Apply Problem'},
        },
        {
          path: 'problem/batch-operation',
          name: 'admin-problem_batch_operation',
          component: ProblemImportAndExport,
          meta: { title:'Export Import Problem'},
        },
        {
          path: 'training/create',
          name: 'admin-create-training',
          component: Training,
          meta: { title:'Create Training'},
        },
        {
          path: 'training',
          name: 'admin-training-list',
          component: TrainingList,
          meta: { title:'Training List'}
        },
        {
          path: 'training/:trainingId/edit',
          name: 'admin-edit-training',
          component: Training,
          meta: { title:'Edit Training'}
        },
        {
          path: 'training/:trainingId/problems',
          name: 'admin-training-problem-list',
          component: TrainingProblemList,
          meta: { title:'Training Problem List'}
        },
        {
          path: 'training/category',
          name: 'admin-training-category',
          component: TrainingCategory,
          meta: { title:'Admin Category'}
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
          meta: { title:'Contest Announcement'}
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
          meta: { title:'Create Problem'}
        },
        {
          path: 'contest/:contestId/problem/:problemId/edit',
          name: 'admin-edit-contest-problem',
          component: Problem,
          meta: { title:'Edit Problem'}
        },
        {
          path: 'discussion',
          name: 'admin-discussion-list',
          component: DiscussionList,
          meta: { title:'Discussion Admin'}
        },
      ]
    },
    {
      path: '/admin/*', redirect: '/admin/login'
    }
  ]

  export default adminRoutes
