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
import Introduction from "@/views/oj/about/Introduction.vue"
import NotFound from "@/views/404.vue"
const ojRoutes = [
  {
    path: '/',
    redirect: {
      name: 'Home',
    }
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
    path: '/problem/1',
    name: 'problem-details',
    component: Problem
  },
  {
    path: '/contest',
    name: 'ContestList',
    component: ContestList
  },
  {
    path: '/status',
    name: 'SubmissionList',
    component: SubmissionList
  },
  {
    path: '/submission-detail',
    name: 'SubmissionDeatil',
    component: SubmissionDetails
  },
  {
    path: '/acm-rank',
    name: 'ACM Rank',
    component: ACMRank
  },
  {
    path: '/oi-rank',
    name: 'OI Rank',
    component: OIRank
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
    meta: { requireAuth: true, title: 'User Home' }
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
    name: 'contest-details',
    path: '/contest/:contestID/',
    component:ContestDetails,
    meta: {title: 'Contest Details'},
    children: [
      {
        name: 'contest-submission-list',
        path: 'submissions',
        component: SubmissionList
      },
      {
        name: 'contest-problem-list',
        path: 'problems',
        component: ContestProblemList
      },
      {
        name: 'contest-problem-details',
        path: 'problem/:problemID/',
        component: Problem
      },
      {
        name: 'contest-announcement-list',
        path: 'announcements',
        component: Announcements
      },
      {
        name: 'contest-rank',
        path: 'rank',
        component: ContestRank
      },
      {
        name: 'contest-ac-info',
        path: 'ac-info',
        component: ACMInfoAdmin
      }
    ]
  },
  {
    path: '/introduction',
    meta: {title: 'Introduction'},
    component:Introduction
  },
  {
    path: '*',
    meta: {title: '404'},
    component:NotFound
  }
]
export default ojRoutes
