const Home= ()=>import('@/views/oj/Home.vue')
const SetNewPassword= ()=>import("@/views/oj/user/SetNewPassword.vue")
const UserHome= ()=>import("@/views/oj/user/UserHome.vue")
const Setting= ()=>import("@/views/oj/user/Setting.vue")
const ProblemLIst= ()=>import("@/views/oj/problem/ProblemList.vue")
const Logout= ()=>import("@/views/oj/user/Logout.vue")
const SubmissionList= ()=>import("@/views/oj/status/SubmissionList.vue")
const SubmissionDetails= ()=>import("@/views/oj/status/SubmissionDetails.vue")
const ContestList= ()=>import("@/views/oj/contest/ContestList.vue")
const Problem= ()=>import("@/views/oj/problem/Problem.vue")
const ACMRank= ()=>import("@/views/oj/rank/ACMRank.vue")
const OIRank= ()=>import("@/views/oj/rank/OIRank.vue")
const ContestDetails= ()=>import("@/views/oj/contest/ContestDetails.vue")
const ContestProblemList= ()=>import("@/views/oj/contest/children/ContestProblemList.vue")
const ContestRank= ()=>import("@/views/oj/contest/children/ContestRank.vue")
const ACMInfoAdmin= ()=>import("@/views/oj/contest/children/ACMInfoAdmin.vue")
const Announcements= ()=>import("@/components/oj/common/Announcements.vue")
const ContestComment= ()=>import("@/views/oj/contest/children/ContestComment.vue")
const ContestRejudgeAdmin= ()=>import("@/views/oj/contest/children/ContestRejudgeAdmin.vue")
const DiscussionList= ()=>import("@/views/oj/discussion/discussionList.vue")
const Discussion= ()=>import("@/views/oj/discussion/discussion.vue")
const Introduction= ()=>import("@/views/oj/about/Introduction.vue")
const Developer= ()=>import("@/views/oj/about/Developer.vue")
const NotFound= ()=>import("@/views/404.vue")


const ojRoutes = [
  {
    path: '/',
    redirect: '/home',
    component: Home,
    meta: { title: 'Home' }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { title: 'Home' }
  },
  {
    path: '/problem',
    name: 'ProblemList',
    component: ProblemLIst,
    meta: { title: 'Problem' }
  },
  {
    path: '/problem/:problemID',
    name: 'ProblemDetails',
    component: Problem,
    meta: { title: 'Problem Details' }
  },
  {
    path: '/contest',
    name: 'ContestList',
    component: ContestList,
    meta: { title: 'Contest' }
  },
  {
    path: '/status',
    name: 'SubmissionList',
    component: SubmissionList,
    meta: { title: 'Status' }
  },
  {
    path: '/submission-detail/:submitID',
    name: 'SubmissionDeatil',
    component: SubmissionDetails,
    meta: {title: 'Submission Deatil' }
  },
  {
    path: '/acm-rank',
    name: 'ACM Rank',
    component: ACMRank,
    meta: { title: 'ACM Rank' }
  },
  {
    path: '/oi-rank',
    name: 'OI Rank',
    component: OIRank,
    meta: { title: 'OI Rank' }
  },
  {
    path: '/reset-password',
    name: 'SetNewPassword',
    component: SetNewPassword,
    meta: { title: 'Reset Password' }
  },
  {
    name: 'UserHome',
    path: '/user-home',
    component: UserHome,
    meta: { title: 'User Home' }
  },
  {
    name: 'Setting',
    path: '/setting',
    component: Setting,
    meta: { requireAuth: true, title: 'Setting' }
  },
  {
    name: 'Logout',
    path: '/logout',
    component: Logout,
    meta: { requireAuth: true, title: 'Logout' }
  },
  {
    name: 'ContestDetails',
    path: '/contest/:contestID/',
    component:ContestDetails,
    meta: {title: 'Contest Details',requireAuth:true},
    children: [
      {
        name: 'ContestSubmissionList',
        path: 'submissions',
        component: SubmissionList,
        meta: { title: 'Contest Submission' }
      },
      {
        name: 'ContestSubmissionDeatil',
        path: 'problem/:problemID/submission-deatil/:submitID',
        component: SubmissionDetails,
        meta: { title: 'Contest Submission Deatil' }
      },
      {
        name: 'ContestProblemList',
        path: 'problems',
        component: ContestProblemList,
        meta: { title: 'Contest Problem' }
      },
      {
        name: 'ContestProblemDetails',
        path: 'problem/:problemID/',
        component: Problem,
        meta: { title: 'Contest Problem Details' }
      },
      {
        name: 'ContestAnnouncementList',
        path: 'announcements',
        component: Announcements,
        meta: { title: 'Contest Announcement' }
      },
      {
        name: 'ContestRank',
        path: 'rank',
        component: ContestRank,
        meta: { title: 'Contest Rank' }
      },
      {
        name: 'ContestACInfo',
        path: 'ac-info',
        component: ACMInfoAdmin,
        meta: { title: 'Contest AC Info',requireAdmin: true}
      },
      {
        name:'ContestRejudgeAdmin',
        path:'rejudge',
        component:ContestRejudgeAdmin,
        meta: { title: 'Contest Rejudge',requireSuperAdmin:true }
      },
      {
        name: 'ContestComment',
        path:'comment',
        component: ContestComment,
        meta: { title: 'Contest Comment'}
      }
    ]
  },
  {
    path: '/discussion',
    name: 'AllDiscussion',
    meta: {title: 'Discussion'},
    component:DiscussionList
  },
  {
    path: '/discussion/:problemID',
    name: 'ProblemDiscussion',
    meta: {title: 'Discussion'},
    component:DiscussionList
  },
  {
    path: '/discussion-detail/:discussionID',
    name:'DiscussionDetail',
    meta: {title: 'Discussion Detail'},
    component: Discussion
  },
  {
    path: '/introduction',
    meta: {title: 'Introduction'},
    component:Introduction,
  },
  {
    path: '/developer',
    meta: {title: 'Developer'},
    component:Developer,
  },
  {
    path: '*',
    meta: {title: '404'},
    component:NotFound,
    meta: { title: '404' }
  }
]
export default ojRoutes
