import Home from '@/views/oj/Home.vue'
import SetNewPassword from "@/views/oj/user/SetNewPassword.vue"
import UserHome from "@/views/oj/user/UserHome.vue"
import Setting from "@/views/oj/user/Setting.vue"
import ProblemLIst from "@/views/oj/problem/ProblemList.vue"
import Logout from "@/views/oj/user/Logout.vue"
import SubmissionList from "@/views/oj/status/SubmissionList.vue"
import SubmissionDetails from "@/views/oj/status/SubmissionDetails.vue"
import ContestList from "@/views/oj/contest/ContestList.vue"
import Problem from "@/views/oj/problem/Problem.vue"
import ACMRank from "@/views/oj/rank/ACMRank.vue"
import OIRank from "@/views/oj/rank/OIRank.vue"
import ContestDetails from "@/views/oj/contest/ContestDetails.vue"
import ContestProblemList from "@/views/oj/contest/children/ContestProblemList.vue"
import ContestRank from "@/views/oj/contest/children/ContestRank.vue"
import ACMInfoAdmin from "@/views/oj/contest/children/ACMInfoAdmin.vue"
import Announcements from "@/components/oj/common/Announcements.vue"
import ContestComment from "@/views/oj/contest/children/ContestComment.vue"
import ContestRejudgeAdmin from "@/views/oj/contest/children/ContestRejudgeAdmin.vue"
import DiscussionList from "@/views/oj/discussion/discussionList.vue"
import Discussion from "@/views/oj/discussion/discussion.vue"
import Introduction from "@/views/oj/about/Introduction.vue"
import Developer from "@/views/oj/about/Developer.vue"
import NotFound from "@/views/404.vue"
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
