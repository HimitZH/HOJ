import axios from 'axios'
import Vue from 'vue'
import mMessage from '@/common/message'
import router from '@/router'
import store from "@/store"
import utils from '@/common/utils'
// import NProgress from 'nprogress' // nprogress插件
// import 'nprogress/nprogress.css' // nprogress样式

// // 配置NProgress进度条选项  —— 动画效果
// NProgress.configure({ ease: 'ease', speed: 1000,showSpinner: false})
Vue.prototype.$http = axios

// 环境的切换
if (process.env.NODE_ENV == 'development') {
  axios.defaults.baseURL = "http://localhost:9010";
} else if (process.env.NODE_ENV == 'debug') {
  axios.defaults.baseURL = "http://localhost:9010";
} else if (process.env.NODE_ENV == 'production') {
  axios.defaults.baseURL = 'https://hoj.hcode.top';
}

// 请求超时时间
axios.defaults.timeout = 15000;

axios.interceptors.request.use(

  config => {

    // NProgress.start();
    // 每次发送请求之前判断vuex中是否存在token
    // 如果存在，则统一在http请求的header都加上token，这样后台根据token判断你的登录情况
    // 即使本地存在token，也有可能token是过期的，所以在响应拦截器中要对返回状态进行判断
    const token = localStorage.getItem('token')
    token && (config.headers.Authorization = token);
    let type = config.url.split("/")[1];
    if (type === 'admin'){ // 携带请求区别是否为admin
      config.headers['Url-Type'] = type
    }else{
      config.headers['Url-Type'] = 'general'
    }

    return config;
  },
  error => {
    // NProgress.done();
    mmMessage.error(error.response.data.mMessage);
    return Promise.error(error);
  })

// 响应拦截器
axios.interceptors.response.use(
  response => {
    // NProgress.done();
    if(response.headers['refresh-token']){ // token续约！
      store.commit('changeUserToken',response.headers['authorization'])
    }
    if (response.data.status === 200 || response.data.status==undefined) {
      return Promise.resolve(response);
    } else {
      mMessage.error(response.data.msg);
      return Promise.reject(response);
    }

  },
  // 服务器状态码不是200的情况
  error => {
    // NProgress.done();
    if (error.response) {
      if(error.response.headers['refresh-token']){ // token续约！！
        store.commit('changeUserToken',error.response.headers['authorization'])
      }
      switch (error.response.status) {
        // 401: 未登录 token过期
        // 未登录则跳转登录页面，并携带当前页面的路径
        // 在登录成功后返回当前页面，这一步需要在登录页操作。
        case 401:
          mMessage.error(error.response.data.msg);
          if(error.response.config.headers['Url-Type'] === 'admin'){
            router.push("/admin/login")
          }else{
            store.commit('changeModalStatus', { mode: 'Login', visible: true });
          }
          store.commit('clearUserInfoAndToken');
          break;
        // 403
        // 无权限访问或操作的请求
        case 403:
          mMessage.error(error.response.data.msg);
          break;
        // 404请求不存在
        case 404:
          mMessage.error('查询错误，找不到要请求的资源！');
          break;
        // 其他错误，直接抛出错误提示
        default:
          mMessage.error( error.response.data.msg);
          break;
      }
      return Promise.reject(error);
    } else { //处理断网，请求没响应
      mMessage.error( '与服务器链接出现异常，请稍后再尝试！');
      return Promise.reject(error);
    }
  }
);


// 处理oj前台的请求
const ojApi = {
  // Home页的请求
  getWebsiteConfig(){
    return ajax('/api/get-website-config', 'get', {
    })
  },
  getRecentContests(){
    return ajax('/api/get-recent-contest', 'get', {
    })
  },
  getRecentOtherContests(){
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
  getRecent7ACRank(){
    return ajax('/api/get-recent-seven-ac-rank', 'get', {
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
  getCaptcha(){
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
  updateProfile(profile) {
    return ajax('profile', 'put', {
      data: profile
    })
  },
  applyResetPassword (data) {
    return ajax('/api/apply-reset-password', 'post', {
      data
    })
  },
  resetPassword (data) {
    return ajax('/api/reset-password', 'post', {
      data
    })
  },
  // Problem List页的相关请求
  getProblemTagList () {
    return ajax('/api/get-all-problem-tags', 'get')
  },
  getProblemList (limit, searchParams) {
    let params = {
      limit
    }
    Object.keys(searchParams).forEach((element) => {
      if (searchParams[element]) {
        params[element] = searchParams[element]
      }
    })
    return ajax('/api/get-problem-list', 'get', {
      params: params
    })
  },

  // 查询当前登录用户对题目的提交状态
  getUserProblemStatus(pidList,isContestProblemList){
    return ajax("/api/get-user-problem-status",'post',{
      data:{
        pidList,
        isContestProblemList
      }
    })
  },
  // 随机来一题
  pickone () {
    return ajax('/api/get-random-problem', 'get')
  },

  // Problem详情页的相关请求
  getProblem(pid){
    return ajax('/api/get-problem','get',{
      params:{
        pid
      }
    })
  },

  // 提交评测模块
  submitCode (data) {
    return ajax('/api/submit-problem-judge', 'post', {
      data
    })
  },
  // 获取单个提交的信息
  getSubmission (submitId) {
    return ajax('/api/submission', 'get', {
      params: {
        submitId
      }
    })
  },

  // 获取单个提交的全部测试点详情
  getAllCaseResult (submitId) {
    return ajax('/api/get-all-case-result', 'get', {
      params: {
        submitId,
      }
    })
  },

  // 更新提交详情
  updateSubmission(data){
    return ajax('/api/submission', 'put', {
     data
    })
  },
  getSubmissionList (limit, params) {
    params.limit = limit
    return ajax('/api/submissions', 'get', {
      params
    })
  },
  checkSubmissonsStatus(submitIds){
    return ajax('/api/check-submissions-status', 'post', {
      data:{submitIds}
    })
  },

  submissionRejudge (submitId) {
    return ajax('/admin/judge/rejudge', 'get', {
      params: {
        submitId
      }
    })
  },

  // 比赛列表页的请求
  getContestList(currentPage,limit,query){
    let params = {
      currentPage,
      limit
    }
    if(query!==undefined){
      Object.keys(query).forEach((element) => {
        if (query[element]) {
          params[element] = query[element]
        }
      })
    }
    return ajax('/api/get-contest-list','get',{
      params: params
    })
  },

  // 比赛详情的请求
  getContest(cid){
    return ajax('/api/get-contest-info','get',{
      params: {cid}
    })
  },
  // 注册私有比赛权限
  registerContest(cid,password){
    return ajax('/api/register-contest','post',{
      data:{
        cid,
        password
      }
    })
  },
  // 获取注册比赛权限
  getContestAccess(cid){
    return ajax('/api/get-contest-access','get',{
      params: {cid}
    })
  },
  // 获取比赛题目列表
  getContestProblemList(cid){
    return ajax('/api/get-contest-problem','get',{
      params: {cid}
    })
  },
  // 获取比赛题目详情
  getContestProblem(displayId,cid){
    return ajax('/api/get-contest-problem-details','get',{
      params: {displayId,cid}
    })
  },
  // 获取比赛提交列表
  getContestSubmissionList (limit, params) {
    params.limit = limit
    return ajax('/api/contest-submissions', 'get', {
      params
    })
  },

  getContestRank(params){
    return ajax('/api/get-contest-rank', 'get', {
      params
    })
  },

  // 获取比赛公告列表
  getContestAnnouncementList(currentPage,limit,cid){
    let params = {
      currentPage,
      limit,
      cid
    }
    return ajax('/api/get-contest-announcement', 'get', {
      params
    })
  },
  // 获取acm比赛ac信息
  getACMACInfo(params){
    return ajax('/api/get-contest-ac-info', 'get', {
      params
    })
  },
  // 确认ac信息
  updateACInfoCheckedStatus(data){
    return ajax('/api/check-contest-ac-info', 'put', {
      data
    })
  },
  // 比赛题目对应的提交重判
  ContestRejudgeProblem(params){
    return ajax('/admin/judge/rejudge-contest-problem', 'get', {
      params
    })
  },

  // ACM赛制或OI赛制的排行榜
  getUserRank(currentPage,limit,type){
    return ajax('/api/get-rank-list','get',{
      params: {
       currentPage,
        limit,
        type
      }
    })
  },

  // about页部分请求
  getAllLanguages(){
    return ajax("/api/languages",'get')
  },
  // userhome页的请求
  getUserInfo(uid){
    return ajax("/api/get-user-home-info",'get',{
      params:{uid}
    })
  },
  // setting页的请求
  changePassword(data){
    return ajax("/api/change-password",'post',{
      data
    })
  },
  changeEmail(data){
    return ajax("/api/change-email",'post',{
      data
    })
  },
  changeUserInfo(data){
    return ajax("/api/change-userInfo",'post',{
      data
    })
  }
}

// 处理admin后台管理的请求
const adminApi = {
  // 登录
  admin_login (username, password) {
    return ajax('/admin/login', 'post', {
      data: {
        username,
        password
      }
    })
  },
  admin_logout () {
    return ajax('/admin/logout', 'get')
  },
  admin_getDashboardInfo () {
    return ajax('/admin/dashboard/get-dashboard-info', 'get')
  },
  getSessions (data) {
    return ajax('/admin/dashboard/get-sessions', 'post',{
      data
    })
  },
  //获取数据后台服务和nacos相关详情
  admin_getGeneralSystemInfo(){
    return ajax('/admin/config/get-service-info','get')
  },

  getJudgeServer () {
    return ajax('/admin/config/get-judge-service-info', 'get')
  },

  deleteJudgeServer (hostname) {
    return ajax('admin/judge_server', 'delete', {
      params: {
        hostname: hostname
      }
    })
  },

  // 获取用户列表
  admin_getUserList (currentPage, limit, keyword) {
    let params = {currentPage, limit}
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('/admin/user/get-user-list', 'get', {
      params: params
    })
  },
  // 编辑用户
  admin_editUser (data) {
    return ajax('/admin/user/edit-user', 'put', {
      data
    })
  },
  admin_deleteUsers (ids) {
    return ajax('/admin/user/delete-user', 'delete', {
      data:{ids}
    })
  },
  admin_importUsers (users) {
    return ajax('/admin/user/insert-batch-user', 'post', {
      data: {
        users
      }
    })
  },
  admin_generateUser (data) {
    return ajax('/admin/user/generate-user', 'post', {
      data
    })
  },
  // 获取公告列表
  admin_getAnnouncementList (currentPage, limit) {
    return ajax('/admin/announcement', 'get', {
      params: {
        currentPage,
        limit
      }
    })
  },
  // 删除公告
  admin_deleteAnnouncement (aid) {
    return ajax('/admin/announcement', 'delete', {
      params: {
        aid
      }
    })
  },
  // 修改公告
  admin_updateAnnouncement (data) {
    return ajax('/admin/announcement', 'put', {
      data
    })
  },
  // 添加公告
  admin_createAnnouncement (data) {
    return ajax('/admin/announcement', 'post', {
      data
    })
  },
  // 系统配置
  admin_getSMTPConfig () {
    return ajax('/admin/config/get-email-config', 'get')
  },
  admin_editSMTPConfig (data) {
    return ajax('/admin/config/set-email-config', 'put', {
      data
    })
  },
  admin_testSMTPConfig (email) {
    return ajax('/admin/config/test-email', 'post', {
      data: {
        email
      }
    })
  },
  admin_getWebsiteConfig () {
    return ajax('/admin/config/get-web-config', 'get')
  },
  admin_editWebsiteConfig (data) {
    return ajax('/admin/config/set-web-config', 'put', {
      data
    })
  },
  admin_getDataBaseConfig(){
    return ajax('/admin/config/get-db-and-redis-config', 'get')
  },
  admin_editDataBaseConfig(data){
    return ajax('/admin/config/set-db-and-redis-config', 'put', {
      data
    })
  },

  getLanguages () {
    return ajax('/api/languages', 'get')
  },
  getProblemLanguages (pid) {
    return ajax('/api/get-Problem-languages', 'get',{
      params: {
        pid: pid
      }
    })
  },

  admin_getProblemList (params) {
    params = utils.filterEmptyValue(params)
    return ajax('/admin/problem/get-problem-list', 'get', {
      params
    })
  },
  admin_createProblem (data) {
    return ajax('/admin/problem', 'post', {
      data
    })
  },
  admin_editProblem (data) {
    return ajax('/admin/problem', 'put', {
      data
    })
  },
  admin_deleteProblem (id) {
    return ajax('/admin/problem', 'delete', {
      params: {
        id
      }
    })
  },

  admin_getProblem (id) {
    return ajax('/admin/problem', 'get', {
      params: {
        id
      }
    })
  },
  admin_getAllProblemTagList () {
    return ajax('/api/get-all-problem-tags', 'get')
  },

  admin_getProblemTags(pid){
    return ajax('/api/get-problem-tags', 'get',{
      params: {
        pid
      }
    })
  },
  admin_getProblemCases(pid){
    return ajax('/admin/problem/get-problem-cases', 'get',{
      params: {
        pid
      }
    })
  },
  compileSPJ (data) {
    return ajax('/admin/compile_spj', 'post', {
      data
    })
  },

  admin_getContestProblemInfo(pid,cid) {
    return ajax('/admin/contest/contest-problem', 'get', {
      params: {
        cid,
        pid
      }
    })
  },
  admin_setContestProblemInfo(data) {
    return ajax('/admin/contest/contest-problem', 'put', {
      data
    })
  },

  admin_getContestProblemList (params) {
    params = utils.filterEmptyValue(params)
    return ajax('/admin/contest/get-problem-list', 'get', {
      params
    })
  },

  admin_getContestProblem (pid) {
    return ajax('/admin/contest/problem', 'get', {
      params: {
        pid,
      }
    })
  },
  admin_createContestProblem (data) {
    return ajax('/admin/contest/problem', 'post', {
      data
    })
  },
  admin_editContestProblem (data) {
    return ajax('/admin/contest/problem', 'put', {
      data
    })
  },
  admin_deleteContestProblem (pid) {
    return ajax('/admin/contest/problem', 'delete', {
      params: {
        id
      }
    })
  },
  admin_changeProblemPublic (data) {
    return ajax('/admin/contest/change-problem-auth', 'put', {
      data
    })
  },
  admin_addProblemFromPublic (data) {
    return ajax('/admin/contest/add-problem-from-public', 'post', {
      data
    })
  },

  exportProblems (data) {
    return ajax('export_problem', 'post', {
      data
    })
  },

  admin_createContest (data) {
    return ajax('/admin/contest', 'post', {
      data
    })
  },
  admin_getContest (cid) {
    return ajax('/admin/contest', 'get', {
      params: {
        cid
      }
    })
  },
  admin_editContest (data) {
    return ajax('/admin/contest', 'put', {
      data
    })
  },
  admin_getContestList (currentPage, limit, keyword) {
    let params = {currentPage, limit}
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('/admin/contest/get-contest-list', 'get', {
      params: params
    })
  },
  admin_getContestAnnouncementList (cid,currentPage,limit) {
    return ajax('/admin/contest/announcement', 'get', {
      params: {
        cid,
        currentPage,
        limit
      }
    })
  },
  admin_createContestAnnouncement (data) {
    return ajax('/admin/contest/announcement', 'post', {
      data
    })
  },
  admin_deleteContestAnnouncement (aid) {
    return ajax('/admin/contest/announcement', 'delete', {
      params: {
        aid
      }
    })
  },
  admin_updateContestAnnouncement (data) {
    return ajax('admin/contest/announcement', 'put', {
      data
    })
  },
}

// 集中导出oj前台的api和admin管理端的api
let api = Object.assign(ojApi,adminApi)
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

