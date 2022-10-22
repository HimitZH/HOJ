import axios from 'axios'
import Vue from 'vue'
import mMessage from '@/common/message'
import router from '@/router'
import store from "@/store"
import utils from '@/common/utils'
import i18n from '@/i18n'
// import NProgress from 'nprogress' // nprogress插件
// import 'nprogress/nprogress.css' // nprogress样式

// // 配置NProgress进度条选项  —— 动画效果
// NProgress.configure({ ease: 'ease', speed: 1000,showSpinner: false})
Vue.prototype.$http = axios

const isMobile = /ipad|iphone|midp|rv:1.2.3.4|ucweb|android|windows ce|windows mobile/.test(navigator.userAgent.toLowerCase());


// 请求超时时间
axios.defaults.timeout = 90000;

axios.interceptors.request.use(

  config => {

    // NProgress.start();
    // 每次发送请求之前判断vuex中是否存在token
    // 如果存在，则统一在http请求的header都加上token，这样后台根据token判断你的登录情况
    // 即使本地存在token，也有可能token是过期的，所以在响应拦截器中要对返回状态进行判断
    const token = localStorage.getItem('token')
    if(config.url != '/api/login' && config.url != '/api/admin/login'){
      token && (config.headers.Authorization = token);
    }
    let type = config.url.split("/")[1];
    if (type === 'admin') { // 携带请求区别是否为admin
      config.headers['Url-Type'] = type
    } else {
      config.headers['Url-Type'] = 'general'
    }

    return config;
  },
  error => {
    // NProgress.done();
    mMessage.error(error.response.data.msg);
    if (!isMobile) {
      Vue.prototype.$notify.error({
        title: i18n.t('m.Error'),
        message: error.response.data.msg,
        duration: 5000,
        offset: 50
      });
    }
    return Promise.error(error);
  })

// 响应拦截器
axios.interceptors.response.use(
  response => {
    // NProgress.done();
    if (response.headers['refresh-token']) { // token续约！
      store.commit('changeUserToken', response.headers['authorization'])
    }
    if (response.data.status === 200 || response.data.status == undefined) {
      return Promise.resolve(response);
    } else {
      mMessage.error(response.data.msg);
      if (!isMobile) {
        Vue.prototype.$notify.error({
          title: i18n.t('m.Error'),
          message: response.data.msg,
          duration: 5000,
          offset: 50
        });
      }
      return Promise.reject(response);
    }

  },
  // 服务器状态码不是200的情况
  error => {
    // NProgress.done();
    if (error.response) {
      if (error.response.headers['refresh-token']) { // token续约！！
        store.commit('changeUserToken', error.response.headers['authorization'])
      }
      if (error.response.data instanceof Blob) { // 如果是文件操作的返回，由后续进行处理
        return Promise.resolve(error.response);
      }
      switch (error.response.status) {
        // 401: 未登录 token过期
        // 未登录则跳转登录页面，并携带当前页面的路径
        // 在登录成功后返回当前页面，这一步需要在登录页操作。
        case 401:
          if (error.response.data.msg) {
            mMessage.error(error.response.data.msg);
            if (!isMobile) {
              Vue.prototype.$notify.error({
                title: i18n.t('m.Error'),
                message: error.response.data.msg,
                duration: 5000,
                offset: 50
              });
            }
          }
          if (error.response.config.headers['Url-Type'] === 'admin') {
            router.push("/api/admin/login")
          } else {
            store.commit('changeModalStatus', { mode: 'Login', visible: true });
          }
          store.commit('clearUserInfoAndToken');
          break;
        // 403
        // 无权限访问或操作的请求
        case 403:
          if (error.response.data.msg) {
            mMessage.error(error.response.data.msg);
            if (!isMobile) {
              Vue.prototype.$notify.error({
                title: i18n.t('m.Error'),
                message: error.response.data.msg,
                duration: 5000,
                offset: 50
              });
            }
          }
          if(error.response.config.url.startsWith('/api/admin')){
            router.push("/admin")
          }
          break;
        // 404请求不存在
        case 404:
          mMessage.error(i18n.t('m.Query_error_unable_to_find_the_resource_to_request'));
          break;
        // 其他错误，直接抛出错误提示
        default:
          if (error.response.data) {
            if (error.response.data.msg) {
              mMessage.error(error.response.data.msg);
              if (!isMobile) {
                Vue.prototype.$notify.error({
                  title: i18n.t('m.Error'),
                  message: error.response.data.msg,
                  duration: 5000,
                  offset: 50
                });
              }
            } else {
              mMessage.error(i18n.t('m.Server_error_please_refresh_again'));
            }
          }
          break;
      }
      return Promise.reject(error);
    } else { //处理断网或请求超时，请求没响应
      if (error.code == 'ECONNABORTED' || error.message.includes('timeout')) {
        mMessage.error(i18n.t('m.Request_timed_out_please_try_again_later'));
      } else {
        mMessage.error(i18n.t('m.Network_error_abnormal_link_with_server_please_try_again_later'));
      }
      return Promise.reject(error);
    }
  }
);


// 处理oj前台的请求
const ojApi = {
  // Home页的请求
  getWebsiteConfig() {
    return ajax('/api/get-website-config', 'get', {
    })
  },
  getHomeCarousel() {
    return ajax('/api/home-carousel', 'get', {
    })
  },
  getRecentContests() {
    return ajax('/api/get-recent-contest', 'get', {
    })
  },
  getRecentOtherContests() {
    return ajax('/api/get-recent-other-contest', 'get', {
    })
  },
  getAnnouncementList(currentPage, limit) {
    let params = {
      currentPage: currentPage,
      limit: limit
    }
    return ajax('/api/get-common-announcement', 'get', {
      params
    })
  },
  getRecent7ACRank() {
    return ajax('/api/get-recent-seven-ac-rank', 'get', {
    })
  },
  getLastWeekSubmissionStatistics(forceRefresh){
    let params = {
      forceRefresh
    }
    return ajax('/api/get-last-week-submission-statistics', 'get', {
      params
    })
  },

  getRecentUpdatedProblemList(){
    return ajax('/api/get-recent-updated-problem', 'get', {
    })
  },

  // 用户账户的相关请求
  getRegisterEmail(email) {
    let params = {
      email: email
    }
    return ajax('/api/get-register-code', 'get', {
      params
    })
  },

  login(data) {
    return ajax('/api/login', 'post', {
      data
    })
  },
  checkUsernameOrEmail(username, email) {
    return ajax('/api/check-username-or-email', 'post', {
      data: {
        username,
        email
      }
    })
  },
  // 获取验证码
  getCaptcha() {
    return ajax('/api/captcha', 'get')
  },
  // 注册
  register(data) {
    return ajax('/api/register', 'post', {
      data
    })
  },
  logout() {
    return ajax('/api/logout', 'get')
  },

  // 账户的相关操作
  applyResetPassword(data) {
    return ajax('/api/apply-reset-password', 'post', {
      data
    })
  },
  resetPassword(data) {
    return ajax('/api/reset-password', 'post', {
      data
    })
  },
  // Problem List页的相关请求
  getProblemTagList(oj) {
    return ajax('/api/get-all-problem-tags', 'get', {
      params: {
        oj
      }
    })
  },

  getProblemTagsAndClassification(oj) {
    return ajax('/api/get-problem-tags-and-classification', 'get', {
      params: {
        oj
      }
    })
  },

  getProblemList(limit, searchParams) {
    let params = {
      limit
    }
    Object.keys(searchParams).forEach((element) => {
      if (searchParams[element] !== '' && searchParams[element] !== null && searchParams[element] !== undefined) {
        params[element] = searchParams[element]
      }
    })
    return ajax('/api/get-problem-list', 'get', {
      params: params
    })
  },

  // 查询当前登录用户对题目的提交状态
  getUserProblemStatus(pidList, isContestProblemList, cid, gid) {
    return ajax("/api/get-user-problem-status", 'post', {
      data: {
        pidList,
        isContestProblemList,
        cid,
        gid
      }
    })
  },
  // 随机来一题
  pickone() {
    return ajax('/api/get-random-problem', 'get')
  },

  // Problem详情页的相关请求
  getProblem(problemId, cid, gid) {
    return ajax('/api/get-problem-detail', 'get', {
      params: {
        problemId,
        gid
      }
    })
  },

  // 获取题目代码模板
  getProblemCodeTemplate(pid) {
    return ajax('/api/get-problem-code-template', 'get', {
      params: {
        pid
      }
    })
  },

  // 提交评测模块
  submitCode(data) {
    return ajax('/api/submit-problem-judge', 'post', {
      data
    })
  },
  // 获取单个提交的信息
  getSubmission(submitId) {
    return ajax('/api/get-submission-detail', 'get', {
      params: {
        submitId
      }
    })
  },
  // 在线调试
  submitTestJudge(data) {
    return ajax('/api/submit-problem-test-judge', 'post', {
      data
    })
  },
  // 获取调试结果
  getTestJudgeResult(testJudgeKey) {
    return ajax('/api/get-test-judge-result', 'get', {
      params: {
        testJudgeKey
      }
    })
  },
  getUserLastAccepetedCode(pid, cid){
    let params = {
      pid
    }
    if(cid){
      params.cid = cid
    }
    return ajax('/api/get-last-ac-code', 'get', {
      params: params
    })
  },
  // 获取单个提交的全部测试点详情
  getAllCaseResult(submitId) {
    return ajax('/api/get-all-case-result', 'get', {
      params: {
        submitId,
      }
    })
  },
  // 远程虚拟判题失败进行重新提交
  reSubmitRemoteJudge(submitId) {
    return ajax("/api/resubmit", 'get', {
      params: {
        submitId,
      }
    })
  },
  // 更新提交详情
  updateSubmission(data) {
    return ajax('/api/submission', 'put', {
      data
    })
  },
  getSubmissionList(limit, params) {
    params.limit = limit
    return ajax('/api/get-submission-list', 'get', {
      params
    })
  },
  checkSubmissonsStatus(submitIds, cid) {
    return ajax('/api/check-submissions-status', 'post', {
      data: { submitIds, cid }
    })
  },
  checkContestSubmissonsStatus(submitIds, cid) {
    return ajax('/api/check-contest-submissions-status', 'post', {
      data: { submitIds, cid }
    })
  },

  submissionRejudge(submitId) {
    return ajax('/api/admin/judge/rejudge', 'get', {
      params: {
        submitId
      }
    })
  },

  admin_manualJudge(submitId, status, score) {
    return ajax('/api/admin/judge/manual-judge', 'get', {
      params: {
        submitId,
        status,
        score
      }
    })
  },

  admin_cancelJudge(submitId) {
    return ajax('/api/admin/judge/cancel-judge', 'get', {
      params: {
        submitId
      }
    })
  },

  // ------------------------------------训练模块的请求---------------------------------------------

  // 获取训练分类列表
  getTrainingCategoryList() {
    return ajax('/api/get-training-category', 'get')
  },


  // 获取训练列表
  getTrainingList(currentPage, limit, query) {
    let params = {
      currentPage,
      limit
    }
    if (query !== undefined) {
      Object.keys(query).forEach((element) => {
        if (query[element]) {
          params[element] = query[element]
        }
      })
    }
    return ajax('/api/get-training-list', 'get', {
      params: params
    })
  },

  // 获取训练详情
  getTraining(tid) {
    return ajax('/api/get-training-detail', 'get', {
      params: { tid }
    })
  },
  // 注册私有训练
  registerTraining(tid, password) {
    return ajax('/api/register-training', 'post', {
      data: {
        tid,
        password
      }
    })
  },
  // 获取注册训练权限
  getTrainingAccess(tid) {
    return ajax('/api/get-training-access', 'get', {
      params: { tid }
    })
  },
  // 获取训练题目列表
  getTrainingProblemList(tid) {
    return ajax('/api/get-training-problem-list', 'get', {
      params: { tid }
    })
  },
  // 获取训练题目详情
  getTrainingProblem(displayId, cid) {
    return ajax('/api/get-training-problem-details', 'get', {
      params: { displayId, cid }
    })
  },
  // 获取训练记录榜单
  getTrainingRank(params) {
    return ajax('/api/get-training-rank', 'get', {
      params
    })
  },



  // ------------------------------------------------------------------------------------------------


  // 比赛列表页的请求
  getContestList(currentPage, limit, query) {
    let params = {
      currentPage,
      limit
    }
    if (query !== undefined) {
      Object.keys(query).forEach((element) => {
        if (query[element]) {
          params[element] = query[element]
        }
      })
    }
    return ajax('/api/get-contest-list', 'get', {
      params: params
    })
  },

  // 比赛详情的请求
  getContest(cid) {
    return ajax('/api/get-contest-info', 'get', {
      params: { cid }
    })
  },
  // 获取赛外榜单比赛的信息
  getScoreBoardContestInfo(cid) {
    return ajax('/api/get-contest-outsize-info', 'get', {
      params: { cid }
    })
  },
  // 提供比赛外榜排名数据
  getContestOutsideScoreboard(data) {
    return ajax('/api/get-contest-outside-scoreboard', 'post', {
      data
    })
  },
  // 注册私有比赛权限
  registerContest(cid, password) {
    return ajax('/api/register-contest', 'post', {
      data: {
        cid,
        password
      }
    })
  },
  // 获取注册比赛权限
  getContestAccess(cid) {
    return ajax('/api/get-contest-access', 'get', {
      params: { cid }
    })
  },
  // 获取比赛题目列表
  getContestProblemList(cid) {
    return ajax('/api/get-contest-problem', 'get', {
      params: { cid }
    })
  },
  // 获取比赛题目详情
  getContestProblem(displayId, cid) {
    return ajax('/api/get-contest-problem-details', 'get', {
      params: { displayId, cid }
    })
  },
  // 获取比赛提交列表
  getContestSubmissionList(limit, params) {
    params.limit = limit
    return ajax('/api/contest-submissions', 'get', {
      params
    })
  },

  getContestRank(data) {
    return ajax('/api/get-contest-rank', 'post', {
      data
    })
  },

  // 获取比赛公告列表
  getContestAnnouncementList(currentPage, limit, cid) {
    let params = {
      currentPage,
      limit,
      cid
    }
    return ajax('/api/get-contest-announcement', 'get', {
      params
    })
  },

  // 获取比赛未阅读公告列表
  getContestUserNotReadAnnouncement(data) {
    return ajax('/api/get-contest-not-read-announcement', 'post', {
      data
    })
  },

  // 获取acm比赛ac信息
  getACMACInfo(params) {
    return ajax('/api/get-contest-ac-info', 'get', {
      params
    })
  },
  // 确认ac信息
  updateACInfoCheckedStatus(data) {
    return ajax('/api/check-contest-ac-info', 'put', {
      data
    })
  },

  // 提交打印文本
  submitPrintText(data) {
    return ajax('/api/submit-print-text', 'post', {
      data
    })
  },

  // 获取比赛打印文本列表
  getContestPrintList(params) {
    return ajax('/api/get-contest-print', 'get', {
      params
    })
  },

  // 更新比赛打印的状态
  updateContestPrintStatus(params) {
    return ajax('/api/check-contest-print-status', 'put', {
      params
    })
  },


  // 比赛题目对应的提交重判
  ContestRejudgeProblem(params) {
    return ajax('/api/admin/judge/rejudge-contest-problem', 'get', {
      params
    })
  },

  // ACM赛制或OI赛制的排行榜
  getUserRank(currentPage, limit, type, searchUser) {
    return ajax('/api/get-rank-list', 'get', {
      params: {
        currentPage,
        limit,
        type,
        searchUser
      }
    })
  },

  // about页部分请求
  getAllLanguages(all) {
    return ajax("/api/languages", 'get', {
      params: {
        all
      }
    })
  },
  // userhome页的请求
  getUserInfo(uid, username) {
    return ajax("/api/get-user-home-info", 'get', {
      params: { uid, username }
    })
  },

  getUserCalendarHeatmap(uid, username) {
    return ajax("/api/get-user-calendar-heatmap", 'get', {
      params: { uid, username }
    })
  },

  // setting页的请求
  changePassword(data) {
    return ajax("/api/change-password", 'post', {
      data
    })
  },
  changeEmail(data) {
    return ajax("/api/change-email", 'post', {
      data
    })
  },
  changeUserInfo(data) {
    return ajax("/api/change-userInfo", 'post', {
      data
    })
  },

  // 讨论页相关请求
  getCategoryList() {
    return ajax("/api/discussion-category", 'get')
  },

  upsertCategoryList(data) {
    return ajax("/api/discussion-category", 'post', {
      data
    })
  },

  getDiscussionList(limit, searchParams) {
    let params = {
      limit
    }
    Object.keys(searchParams).forEach((element) => {
      if (searchParams[element] !== '' && searchParams[element] !== null && searchParams[element] !== undefined) {
        params[element] = searchParams[element]
      }
    })
    return ajax("/api/get-discussion-list", 'get', {
      params
    })
  },

  getDiscussion(did) {
    return ajax("/api/get-discussion-detail", 'get', {
      params: {
        did
      }
    })
  },

  addDiscussion(data) {
    return ajax("/api/discussion", 'post', {
      data
    })
  },

  updateDiscussion(data) {
    return ajax("/api/discussion", 'put', {
      data
    })
  },

  deleteDiscussion(did) {
    return ajax("/api/discussion", 'delete', {
      params: {
        did
      }
    })
  },

  toLikeDiscussion(did, toLike) {
    return ajax("/api/discussion-like", 'get', {
      params: {
        did,
        toLike
      }
    })
  },
  toReportDiscussion(data) {
    return ajax("/api/discussion-report", 'post', {
      data
    })
  },

  getCommentList(params) {
    return ajax("/api/comments", 'get', {
      params
    })
  },

  addComment(data) {
    return ajax("/api/comment", 'post', {
      data
    })
  },

  deleteComment(data) {
    return ajax("/api/comment", 'delete', {
      data
    })
  },

  toLikeComment(cid, toLike, sourceId, sourceType) {
    return ajax("/api/comment-like", 'get', {
      params: {
        cid,
        toLike,
        sourceId,
        sourceType
      }
    })
  },

  addReply(data) {
    return ajax("/api/reply", 'post', {
      data
    })
  },

  deleteReply(data) {
    return ajax("/api/reply", 'delete', {
      data
    })
  },

  getAllReply(commentId, cid) {
    return ajax("/api/reply", 'get', {
      params: {
        commentId,
        cid
      }
    })
  },

  // Group
  getGroupList(currentPage, limit, query) {
    let params = { currentPage, limit }
    Object.keys(query).forEach((element) => {
      if (query[element] !== '' && query[element] !== null && query[element] !== undefined) {
        params[element] = query[element]
      }
    })
    return ajax('/api/get-group-list', 'get', {
      params: params
    })
  },

  getGroup(gid) {
    return ajax('/api/get-group-detail', 'get', {
      params: { gid }
    })
  },

  addGroup(data) {
    return ajax("/api/group", 'post', {
      data
    })
  },

  updateGroup(data) {
    return ajax("/api/group", 'put', {
      data
    })
  },

  deleteGroup(gid) {
    return ajax("/api/group", 'delete', {
      params: { gid }
    })
  },

  getGroupAccess(gid) {
    return ajax('/api/get-group-access', 'get', {
      params: { gid }
    })
  },

  getGroupAuth(gid) {
    return ajax('/api/get-group-auth', 'get', {
      params: { gid }
    })
  },

  // Group Member
  getGroupMemberList(currentPage, limit, gid) {
    return ajax('/api/group/get-member-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupApplyList(currentPage, limit, gid) {
    return ajax('/api/group/get-apply-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  addGroupMember(gid, code, reason) {
    return ajax("/api/group/member", 'post', {
      params: { gid, code, reason }
    })
  },

  updateGroupMember(data) {
    return ajax("/api/group/member", 'put', {
      data
    })
  },


  deleteGroupMember(uid, gid) {
    return ajax("/api/group/member", 'delete', {
      params: { uid, gid }
    })
  },

  exitGroup(gid) {
    return ajax("/api/group/member/exit", 'delete', {
      params: { gid }
    })
  },

  // Group Announcement
  getGroupAnnouncementList(currentPage, limit, gid) {
    return ajax('/api/group/get-announcement-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupAdminAnnouncementList(currentPage, limit, gid) {
    return ajax('/api/group/get-admin-announcement-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  addGroupAnnouncement(data) {
    return ajax("/api/group/announcement", 'post', {
      data
    })
  },

  updateGroupAnnouncement(data) {
    return ajax("/api/group/announcement", 'put', {
      data
    })
  },

  deleteGroupAnnouncement(aid) {
    return ajax("/api/group/announcement", 'delete', {
      params: { aid }
    })
  },

  // Group Problem
  getGroupProblemList(currentPage, limit, gid) {
    return ajax('/api/group/get-problem-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupAdminProblemList(currentPage, limit, gid) {
    return ajax('/api/group/get-admin-problem-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupProblem(pid) {
    return ajax("/api/group/problem", 'get', {
      params: { pid }
    })
  },

  addGroupProblem(data) {
    return ajax("/api/group/problem", 'post', {
      data: data
    })
  },

  updateGroupProblem(data) {
    return ajax("/api/group/problem", 'put', {
      data
    })
  },

  deleteGroupProblem(pid) {
    return ajax("/api/group/problem", 'delete', {
      params: { pid }
    })
  },

  getGroupProblemCases(pid, isUpload) {
    return ajax("/api/group/get-problem-cases", 'get', {
      params: { pid, isUpload }
    })
  },
  getGroupProblemTags(pid) {
    return ajax('/api/get-problem-tags', 'get', {
      params: {
        pid
      }
    })
  },

  getGroupProblemTagList(gid) {
    return ajax('/api/group/get-all-problem-tags', 'get', {
      params: {
        gid
      }
    })
  },

  groupCompileSpj(data, gid) {
    return ajax("/api/group/compile-spj", 'post', {
      data: data,
      params: { gid }
    })
  },

  groupCompileInteractive(data, gid) {
    return ajax("/api/group/compile-interactive", 'post', {
      data: data,
      params: { gid }
    })
  },

  changeGroupProblemAuth(pid, auth) {
    return ajax("/api/group/change-problem-auth", 'put', {
      params: { pid, auth }
    })
  },

  // Group Training
  getGroupTrainingList(currentPage, limit, gid) {
    return ajax('/api/group/get-training-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupAdminTrainingList(currentPage, limit, gid) {
    return ajax('/api/group/get-admin-training-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupTraining(tid) {
    return ajax("/api/group/training", 'get', {
      params: { tid }
    })
  },

  addGroupTraining(data) {
    return ajax("/api/group/training", 'post', {
      data
    })
  },

  updateGroupTraining(data) {
    return ajax("/api/group/training", 'put', {
      data
    })
  },

  deleteGroupTraining(tid) {
    return ajax("/api/group/training", 'delete', {
      params: { tid }
    })
  },

  changeGroupTrainingStatus(tid, status) {
    return ajax("/api/group/change-training-status", 'put', {
      params: { tid, status }
    })
  },

  getGroupTrainingProblemList(currentPage, limit, query) {
    let params = { currentPage, limit }
    Object.keys(query).forEach((element) => {
      if (query[element] !== '' && query[element] !== null && query[element] !== undefined) {
        params[element] = query[element]
      }
    })
    return ajax("/api/group/get-training-problem-list", 'get', {
      params: params
    })
  },

  updateGroupTrainingProblem(data) {
    return ajax("/api/group/training-problem", 'put', {
      data
    })
  },

  deleteGroupTrainingProblem(pid, tid) {
    return ajax("/api/group/training-problem", 'delete', {
      params: { pid, tid }
    })
  },

  addGroupTrainingProblemFromPublic(data) {
    return ajax("/api/group/add-training-problem-from-public", 'post', {
      data
    })
  },

  addGroupTrainingProblemFromGroup(problemId, tid) {
    return ajax("/api/group/add-training-problem-from-group", 'post', {
      params: { problemId, tid }
    })
  },

  //Group Contest
  getGroupContestList(currentPage, limit, gid) {
    return ajax('/api/group/get-contest-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupAdminContestList(currentPage, limit, gid) {
    return ajax('/api/group/get-admin-contest-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  getGroupContest(cid) {
    return ajax("/api/group/contest", 'get', {
      params: { cid }
    })
  },

  addGroupContest(data) {
    return ajax("/api/group/contest", 'post', {
      data
    })
  },

  updateGroupContest(data) {
    return ajax("/api/group/contest", 'put', {
      data
    })
  },

  deleteGroupContest(cid) {
    return ajax("/api/group/contest", 'delete', {
      params: { cid }
    })
  },

  changeGroupContestVisible(cid, visible) {
    return ajax("/api/group/change-contest-visible", 'put', {
      params: { cid, visible }
    })
  },

  getGroupContestProblemList(currentPage, limit, query) {
    let params = { currentPage, limit }
    Object.keys(query).forEach((element) => {
      if (query[element] !== '' && query[element] !== null && query[element] !== undefined) {
        params[element] = query[element]
      }
    })
    return ajax("/api/group/get-contest-problem-list", 'get', {
      params: params
    })
  },

  addGroupContestProblem(data) {
    return ajax("/api/group/contest-problem", 'post', {
      data
    })
  },

  getGroupContestProblem(pid, cid) {
    return ajax("/api/group/contest-problem", 'get', {
      params: { pid, cid }
    })
  },

  updateGroupContestProblem(data) {
    return ajax("/api/group/contest-problem", 'put', {
      data
    })
  },

  applyGroupProblemPublic(pid, isApplied) {
    return ajax("/api/group/apply-public", 'put', {
      params: { pid, isApplied }
    })
  },

  deleteGroupContestProblem(pid, cid) {
    return ajax("/api/group/contest-problem", 'delete', {
      params: { pid, cid }
    })
  },

  addGroupContestProblemFromPublic(data) {
    return ajax("/api/group/add-contest-problem-from-public", 'post', {
      data
    })
  },

  addGroupContestProblemFromGroup(problemId, cid, displayId) {
    return ajax("/api/group/add-contest-problem-from-group", 'post', {
      params: { problemId, cid, displayId }
    })
  },

  getGroupContestAnnouncementList(currentPage, limit, cid) {
    return ajax('/api/group/get-contest-announcement-list', 'get', {
      params: { currentPage, limit, cid }
    })
  },

  addGroupContestAnnouncement(data) {
    return ajax("/api/group/contest-announcement", 'post', {
      data
    })
  },

  updateGroupContestAnnouncement(data) {
    return ajax("/api/group/contest-announcement", 'put', {
      data
    })
  },

  deleteGroupContestAnnouncement(aid, cid) {
    return ajax("/api/group/contest-announcement", 'delete', {
      params: { aid, cid }
    })
  },

  // Group Discussion
  getGroupDiscussionList(currentPage, limit, gid, pid) {
    return ajax('/api/group/get-discussion-list', 'get', {
      params: { currentPage, limit, gid, pid }
    })
  },

  getGroupAdminDiscussionList(currentPage, limit, gid) {
    return ajax('/api/group/get-admin-discussion-list', 'get', {
      params: { currentPage, limit, gid }
    })
  },

  addGroupDiscussion(data) {
    return ajax("/api/group/discussion", 'post', {
      data
    })
  },

  updateGroupDiscussion(data) {
    return ajax("/api/group/discussion", 'put', {
      data
    })
  },

  deleteGroupDiscussion(did) {
    return ajax("/api/group/discussion", 'delete', {
      params: { did }
    })
  },

  getGroupRank(currentPage, limit, gid, type, searchUser) {
    return ajax('/api/get-group-rank-list', 'get', {
      params: {
        currentPage,
        limit,
        gid,
        type,
        searchUser
      }
    })
  },

  // 站内消息

  getUnreadMsgCount() {
    return ajax("/api/msg/unread", 'get')
  },

  getMsgList(routerName, searchParams) {
    let params = {};
    Object.keys(searchParams).forEach((element) => {
      if (searchParams[element] !== '' && searchParams[element] !== null && searchParams[element] !== undefined) {
        params[element] = searchParams[element]
      }
    })
    switch (routerName) {
      case "DiscussMsg":
        return ajax("/api/msg/comment", 'get', {
          params
        });
      case "ReplyMsg":
        return ajax("/api/msg/reply", 'get', {
          params
        });
      case "LikeMsg":
        return ajax("/api/msg/like", 'get', {
          params
        });
      case "SysMsg":
        return ajax("/api/msg/sys", 'get', {
          params
        });
      case "MineMsg":
        return ajax("/api/msg/mine", 'get', {
          params
        });
    }
  },

  cleanMsg(routerName, id) {
    let params = {};
    if (id) {
      params.id = id;
    }
    switch (routerName) {
      case "DiscussMsg":
        params.type = 'Discuss';
        break;
      case "ReplyMsg":
        params.type = 'Reply';
        break;
      case "LikeMsg":
        params.type = 'Like';
        break;
      case "SysMsg":
        params.type = 'Sys';
        break;
      case "MineMsg":
        params.type = 'Mine';
        break;
    }
    return ajax("/api/msg/clean", 'delete', {
      params
    });
  }

}

// 处理admin后台管理的请求
const adminApi = {
  // 登录
  admin_login(username, password) {
    return ajax('/api/admin/login', 'post', {
      data: {
        username,
        password
      }
    })
  },
  admin_logout() {
    return ajax('/api/admin/logout', 'get')
  },
  admin_getDashboardInfo() {
    return ajax('/api/admin/dashboard/get-dashboard-info', 'get')
  },
  getSessions(data) {
    return ajax('/api/admin/dashboard/get-sessions', 'post', {
      data
    })
  },
  //获取数据后台服务和nacos相关详情
  admin_getGeneralSystemInfo() {
    return ajax('/api/admin/config/get-service-info', 'get')
  },

  getJudgeServer() {
    return ajax('/api/admin/config/get-judge-service-info', 'get')
  },

  // 获取用户列表
  admin_getUserList(currentPage, limit, keyword, onlyAdmin) {
    let params = { currentPage, limit }
    if (keyword) {
      params.keyword = keyword
    }
    params.onlyAdmin = onlyAdmin
    return ajax('/api/admin/user/get-user-list', 'get', {
      params: params
    })
  },
  // 编辑用户
  admin_editUser(data) {
    return ajax('/api/admin/user/edit-user', 'put', {
      data
    })
  },
  admin_deleteUsers(ids) {
    return ajax('/api/admin/user/delete-user', 'delete', {
      data: { ids }
    })
  },
  admin_importUsers(users) {
    return ajax('/api/admin/user/insert-batch-user', 'post', {
      data: {
        users
      }
    })
  },
  admin_generateUser(data) {
    return ajax('/api/admin/user/generate-user', 'post', {
      data
    })
  },
  // 获取公告列表
  admin_getAnnouncementList(currentPage, limit) {
    return ajax('/api/admin/announcement', 'get', {
      params: {
        currentPage,
        limit
      }
    })
  },
  // 删除公告
  admin_deleteAnnouncement(aid) {
    return ajax('/api/admin/announcement', 'delete', {
      params: {
        aid
      }
    })
  },
  // 修改公告
  admin_updateAnnouncement(data) {
    return ajax('/api/admin/announcement', 'put', {
      data
    })
  },
  // 添加公告
  admin_createAnnouncement(data) {
    return ajax('/api/admin/announcement', 'post', {
      data
    })
  },


  // 获取公告列表
  admin_getNoticeList(currentPage, limit, type) {
    return ajax('/api/admin/msg/notice', 'get', {
      params: {
        currentPage,
        limit,
        type
      }
    })
  },
  // 删除公告
  admin_deleteNotice(id) {
    return ajax('/api/admin/msg/notice', 'delete', {
      params: {
        id
      }
    })
  },
  // 修改公告
  admin_updateNotice(data) {
    return ajax('/api/admin/msg/notice', 'put', {
      data
    })
  },
  // 添加公告
  admin_createNotice(data) {
    return ajax('/api/admin/msg/notice', 'post', {
      data
    })
  },

  // 系统配置
  admin_getSMTPConfig() {
    return ajax('/api/admin/config/get-email-config', 'get')
  },
  admin_editSMTPConfig(data) {
    return ajax('/api/admin/config/set-email-config', 'put', {
      data
    })
  },

  admin_deleteHomeCarousel(id) {
    return ajax('/api/admin/config/home-carousel', 'delete', {
      params: {
        id
      }
    })
  },

  admin_testSMTPConfig(email) {
    return ajax('/api/admin/config/test-email', 'post', {
      data: {
        email
      }
    })
  },
  admin_getWebsiteConfig() {
    return ajax('/api/admin/config/get-web-config', 'get')
  },
  admin_editWebsiteConfig(data) {
    return ajax('/api/admin/config/set-web-config', 'put', {
      data
    })
  },
  admin_getDataBaseConfig() {
    return ajax('/api/admin/config/get-db-and-redis-config', 'get')
  },
  admin_editDataBaseConfig(data) {
    return ajax('/api/admin/config/set-db-and-redis-config', 'put', {
      data
    })
  },

  // 系统开关
  admin_getSwitchConfig() {
    return ajax('/api/admin/switch/info', 'get')
  },

  admin_saveSwitchConfig(data) {
    return ajax('/api/admin/switch/update', 'put', {
      data
    })
  },

  getLanguages(pid, all) {
    return ajax('/api/languages', 'get', {
      params: {
        pid,
        all
      }
    })
  },
  getProblemLanguages(pid) {
    return ajax('/api/get-problem-languages', 'get', {
      params: {
        pid: pid
      }
    })
  },

  admin_getProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/problem/get-problem-list', 'get', {
      params
    })
  },

  admin_addRemoteOJProblem(name, problemId) {
    return ajax("/api/admin/problem/import-remote-oj-problem", "get", {
      params: {
        name,
        problemId
      }
    })
  },

  admin_addContestRemoteOJProblem(name, problemId, cid, displayId) {
    return ajax("/api/admin/contest/import-remote-oj-problem", "get", {
      params: {
        name,
        problemId,
        cid,
        displayId
      }
    })
  },

  admin_createProblem(data) {
    return ajax('/api/admin/problem', 'post', {
      data
    })
  },
  admin_editProblem(data) {
    return ajax('/api/admin/problem', 'put', {
      data
    })
  },
  admin_deleteProblem(pid) {
    return ajax('/api/admin/problem', 'delete', {
      params: {
        pid
      }
    })
  },
  admin_changeProblemAuth(data) {
    return ajax('/api/admin/problem/change-problem-auth', 'put', {
      data
    })
  },
  admin_getProblem(pid) {
    return ajax('/api/admin/problem', 'get', {
      params: {
        pid
      }
    })
  },
  admin_getAllProblemTagList(oj) {
    return ajax('/api/get-all-problem-tags', 'get', {
      params: {
        oj
      }
    })
  },

  admin_getProblemTags(pid) {
    return ajax('/api/get-problem-tags', 'get', {
      params: {
        pid
      }
    })
  },
  admin_getProblemCases(pid, isUpload) {
    return ajax('/api/admin/problem/get-problem-cases', 'get', {
      params: {
        pid,
        isUpload
      }
    })
  },
  compileSPJ(data) {
    return ajax('/api/admin/problem/compile-spj', 'post', {
      data
    })
  },
  compileInteractive(data) {
    return ajax('/api/admin/problem/compile-interactive', 'post', {
      data
    })
  },

  admin_addTag(data) {
    return ajax('/api/admin/tag', 'post', {
      data
    })
  },

  admin_updateTag(data) {
    return ajax('/api/admin/tag', 'put', {
      data
    })
  },

  admin_deleteTag(tid) {
    return ajax('/api/admin/tag', 'delete', {
      params: {
        tid
      }
    })
  },

  admin_getTagClassification(oj) {
    return ajax('/api/admin/tag/classification', 'get', {
      params: {
        oj
      }
    })
  },

  admin_addTagClassification(data) {
    return ajax('/api/admin/tag/classification', 'post', {
      data
    })
  },

  admin_updateTagClassification(data) {
    return ajax('/api/admin/tag/classification', 'put', {
      data
    })
  },

  admin_deleteTagClassification(tcid) {
    return ajax('/api/admin/tag/classification', 'delete', {
      params: {
        tcid
      }
    })
  },

  admin_getGroupApplyProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/group-problem/list', 'get', {
      params
    })
  },

  admin_changeGroupProblemApplyProgress(data) {
    return ajax('/api/admin/group-problem/change-progress', 'put', {
      data
    })
  },

  admin_getTrainingList(currentPage, limit, keyword) {
    let params = { currentPage, limit }
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('/api/admin/training/get-training-list', 'get', {
      params: params
    })
  },
  admin_changeTrainingStatus(tid, status, author) {
    return ajax('/api/admin/training/change-training-status', 'put', {
      params: {
        tid,
        status,
        author
      }
    })
  },

  admin_getTrainingProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/training/get-problem-list', 'get', {
      params
    })
  },

  admin_deleteTrainingProblem(pid, tid) {
    return ajax('/api/admin/training/problem', 'delete', {
      params: {
        pid,
        tid
      }
    })
  },

  admin_addTrainingProblemFromPublic(data) {
    return ajax('/api/admin/training/add-problem-from-public', 'post', {
      data
    })
  },

  admin_addTrainingRemoteOJProblem(name, problemId, tid) {
    return ajax("/api/admin/training/import-remote-oj-problem", "get", {
      params: {
        name,
        problemId,
        tid,
      }
    })
  },

  admin_updateTrainingProblem(data) {
    return ajax('/api/admin/training/problem', 'put', {
      data
    })
  },

  admin_createTraining(data) {
    return ajax('/api/admin/training', 'post', {
      data
    })
  },
  admin_getTraining(tid) {
    return ajax('/api/admin/training', 'get', {
      params: {
        tid
      }
    })
  },
  admin_editTraining(data) {
    return ajax('/api/admin/training', 'put', {
      data
    })
  },
  admin_deleteTraining(tid) {
    return ajax('/api/admin/training', 'delete', {
      params: {
        tid
      }
    })
  },

  admin_addCategory(data) {
    return ajax('/api/admin/training/category', 'post', {
      data
    })
  },

  admin_updateCategory(data) {
    return ajax('/api/admin/training/category', 'put', {
      data
    })
  },

  admin_deleteCategory(cid) {
    return ajax('/api/admin/training/category', 'delete', {
      params: {
        cid
      }
    })
  },


  admin_getContestProblemInfo(pid, cid) {
    return ajax('/api/admin/contest/contest-problem', 'get', {
      params: {
        cid,
        pid
      }
    })
  },
  admin_setContestProblemInfo(data) {
    return ajax('/api/admin/contest/contest-problem', 'put', {
      data
    })
  },

  admin_getContestProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/contest/get-problem-list', 'get', {
      params
    })
  },

  admin_getContestProblem(pid) {
    return ajax('/api/admin/contest/problem', 'get', {
      params: {
        pid,
      }
    })
  },
  admin_createContestProblem(data) {
    return ajax('/api/admin/contest/problem', 'post', {
      data
    })
  },
  admin_editContestProblem(data) {
    return ajax('/api/admin/contest/problem', 'put', {
      data
    })
  },
  admin_deleteContestProblem(pid, cid) {
    return ajax('/api/admin/contest/problem', 'delete', {
      params: {
        pid,
        cid
      }
    })
  },
  admin_addContestProblemFromPublic(data) {
    return ajax('/api/admin/contest/add-problem-from-public', 'post', {
      data
    })
  },

  exportProblems(data) {
    return ajax('export_problem', 'post', {
      data
    })
  },

  admin_createContest(data) {
    return ajax('/api/admin/contest', 'post', {
      data
    })
  },
  admin_getContest(cid) {
    return ajax('/api/admin/contest', 'get', {
      params: {
        cid
      }
    })
  },
  admin_editContest(data) {
    return ajax('/api/admin/contest', 'put', {
      data
    })
  },
  admin_deleteContest(cid) {
    return ajax('/api/admin/contest', 'delete', {
      params: {
        cid
      }
    })
  },
  admin_changeContestVisible(cid, visible, uid) {
    return ajax('/api/admin/contest/change-contest-visible', 'put', {
      params: {
        cid,
        visible,
        uid
      }
    })
  },
  admin_getContestList(currentPage, limit, keyword) {
    let params = { currentPage, limit }
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('/api/admin/contest/get-contest-list', 'get', {
      params: params
    })
  },
  admin_getContestAnnouncementList(cid, currentPage, limit) {
    return ajax('/api/admin/contest/announcement', 'get', {
      params: {
        cid,
        currentPage,
        limit
      }
    })
  },
  admin_createContestAnnouncement(data) {
    return ajax('/api/admin/contest/announcement', 'post', {
      data
    })
  },
  admin_deleteContestAnnouncement(aid) {
    return ajax('/api/admin/contest/announcement', 'delete', {
      params: {
        aid
      }
    })
  },
  admin_updateContestAnnouncement(data) {
    return ajax('/api/admin/contest/announcement', 'put', {
      data
    })
  },

  admin_updateDiscussion(data) {
    return ajax("/api/admin/discussion", 'put', {
      data
    })
  },

  admin_deleteDiscussion(data) {
    return ajax("/api/admin/discussion", 'delete', {
      data
    })
  },
  admin_getDiscussionReport(currentPage, limit) {
    return ajax("/api/admin/discussion-report", 'get', {
      params: {
        currentPage,
        limit
      }
    })
  },
  admin_updateDiscussionReport(data) {
    return ajax("/api/admin/discussion-report", 'put', {
      data
    })
  }
}

// 集中导出oj前台的api和admin管理端的api
let api = Object.assign(ojApi, adminApi)
export default api
/**
 * @param url
 * @param method get|post|put|delete...
 * @param params like queryString. if a url is index?a=1&b=2, params = {a: '1', b: '2'}
 * @param data post data, use for method put|post
 * @returns {axios}
 */
function ajax(url, method, options) {
  if (options !== undefined) {
    var { params = {}, data = {} } = options
  } else {
    params = data = {}
  }
  return new Promise((resolve, reject) => {
    axios({
      url,
      method,
      params,
      data
    }).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}

