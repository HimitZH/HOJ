import axios from 'axios'
import mMessage from '@/common/message'
// import NProgress from 'nprogress' // nprogress插件
// import 'nprogress/nprogress.css' // nprogress样式

// // 配置NProgress进度条选项  —— 动画效果
// NProgress.configure({ ease: 'ease', speed: 1000,showSpinner: false})

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
    if (response.data.status === 200) {
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
      switch (error.response.status) {
        // 401: 未登录                
        // 未登录则跳转登录页面，并携带当前页面的路径                
        // 在登录成功后返回当前页面，这一步需要在登录页操作。                
        case 401:
          mMessage.error('请您先登录！');
          store.commit('changeModalStatus', { mode: 'Login', visible: true });
          break;
        // 403 token过期                
        // 登录过期对用户进行提示                
        // 清除本地token和清空vuex中token对象                
        // 跳转登录页面                
        case 403:
          mMessage.error('登录过期，请重新登录！');
          // 清除token  userInfo                                                      
          store.dispatch('clearUserInfoAndToken');
          store.commit('changeModalStatus', { mode: 'Login', visible: true });
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
      mMessage.error( '网络出现异常，请稍后再尝试！');
      return Promise.reject(error);
    }
  }
);

// 集中导出oj前台的api和admin管理端的api 
let api = {...ojApi,...adminApi}
export default api

// 处理oj前台的请求
const ojApi = {
  getRegisterEmail(email) {
    let params = {
      email: email
    }
    return ajax('/api/get-register-code', 'get', {
      params
    })
  },
  getAnnouncementList(offset, limit) {
    let params = {
      offset: offset,
      limit: limit
    }
    return ajax('/api/announcement', 'get', {
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
  // 注册
  register(data) {
    return ajax('/api/register', 'post', {
      data
    })
  },
  logout() {
    return ajax('/api/logout', 'get')
  },
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
  changePassword (data) {
    return ajax('change_password', 'post', {
      data
    })
  },
  changeEmail (data) {
    return ajax('change_email', 'post', {
      data
    })
  },
  getProblemTagList () {
    return ajax('/api/get-problem-tags', 'get')
  },
  getProblemList (offset, limit, searchParams) {
    let params = {
      paging: true,
      offset,
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
  pickone () {
    return ajax('pickone', 'get')
  },
  // 提交评测模块
  submitCode (data) {
    return ajax('submission', 'post', {
      data
    })
  },
  getSubmissionList (offset, limit, params) {
    params.limit = limit
    params.offset = offset
    return ajax('submissions', 'get', {
      params
    })
  },
  getContestSubmissionList (offset, limit, params) {
    params.limit = limit
    params.offset = offset
    return ajax('contest_submissions', 'get', {
      params
    })
  },
  getSubmission (id) {
    return ajax('submission', 'get', {
      params: {
        id
      }
    })
  },
  submissionExists (problemID) {
    return ajax('submission_exists', 'get', {
      params: {
        problem_id: problemID
      }
    })
  },
  submissionRejudge (id) {
    return ajax('admin/submission/rejudge', 'get', {
      params: {
        id
      }
    })
  },
}

// 处理admin后台管理的请求
const adminApi = {
  // 登录
  login (username, password) {
    return ajax('login', 'post', {
      data: {
        username,
        password
      }
    })
  },
  logout () {
    return ajax('logout', 'get')
  },
  getProfile () {
    return ajax('profile', 'get')
  },
  // 获取公告列表
  getAnnouncementList (offset, limit) {
    return ajax('admin/announcement', 'get', {
      params: {
        paging: true,
        offset,
        limit
      }
    })
  },
  // 删除公告
  deleteAnnouncement (id) {
    return ajax('admin/announcement', 'delete', {
      params: {
        id
      }
    })
  },
  // 修改公告
  updateAnnouncement (data) {
    return ajax('admin/announcement', 'put', {
      data
    })
  },
  // 添加公告
  createAnnouncement (data) {
    return ajax('admin/announcement', 'post', {
      data
    })
  },
  // 获取用户列表
  getUserList (offset, limit, keyword) {
    let params = {paging: true, offset, limit}
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('admin/user', 'get', {
      params: params
    })
  },
  // 获取单个用户信息
  getUser (id) {
    return ajax('admin/user', 'get', {
      params: {
        id
      }
    })
  },
  // 编辑用户
  editUser (data) {
    return ajax('admin/user', 'put', {
      data
    })
  },
  deleteUsers (id) {
    return ajax('admin/user', 'delete', {
      params: {
        id
      }
    })
  },
  importUsers (users) {
    return ajax('admin/user', 'post', {
      data: {
        users
      }
    })
  },
  generateUser (data) {
    return ajax('admin/generate_user', 'post', {
      data
    })
  },
  getLanguages () {
    return ajax('languages', 'get')
  },
  getSMTPConfig () {
    return ajax('admin/smtp', 'get')
  },
  createSMTPConfig (data) {
    return ajax('admin/smtp', 'post', {
      data
    })
  },
  editSMTPConfig (data) {
    return ajax('admin/smtp', 'put', {
      data
    })
  },
  testSMTPConfig (email) {
    return ajax('admin/smtp_test', 'post', {
      data: {
        email
      }
    })
  },
  getWebsiteConfig () {
    return ajax('admin/website', 'get')
  },
  editWebsiteConfig (data) {
    return ajax('admin/website', 'post', {
      data
    })
  },
  getJudgeServer () {
    return ajax('admin/judge_server', 'get')
  },
  deleteJudgeServer (hostname) {
    return ajax('admin/judge_server', 'delete', {
      params: {
        hostname: hostname
      }
    })
  },
  updateJudgeServer (data) {
    return ajax('admin/judge_server', 'put', {
      data
    })
  },
  getInvalidTestCaseList () {
    return ajax('admin/prune_test_case', 'get')
  },
  pruneTestCase (id) {
    return ajax('admin/prune_test_case', 'delete', {
      params: {
        id
      }
    })
  },
  createContest (data) {
    return ajax('admin/contest', 'post', {
      data
    })
  },
  getContest (id) {
    return ajax('admin/contest', 'get', {
      params: {
        id
      }
    })
  },
  editContest (data) {
    return ajax('admin/contest', 'put', {
      data
    })
  },
  getContestList (offset, limit, keyword) {
    let params = {paging: true, offset, limit}
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('admin/contest', 'get', {
      params: params
    })
  },
  getContestAnnouncementList (contestID) {
    return ajax('admin/contest/announcement', 'get', {
      params: {
        contest_id: contestID
      }
    })
  },
  createContestAnnouncement (data) {
    return ajax('admin/contest/announcement', 'post', {
      data
    })
  },
  deleteContestAnnouncement (id) {
    return ajax('admin/contest/announcement', 'delete', {
      params: {
        id
      }
    })
  },
  updateContestAnnouncement (data) {
    return ajax('admin/contest/announcement', 'put', {
      data
    })
  },
  getProblemTagList () {
    return ajax('problem/tags', 'get')
  },
  compileSPJ (data) {
    return ajax('admin/compile_spj', 'post', {
      data
    })
  },
  createProblem (data) {
    return ajax('admin/problem', 'post', {
      data
    })
  },
  editProblem (data) {
    return ajax('admin/problem', 'put', {
      data
    })
  },
  deleteProblem (id) {
    return ajax('admin/problem', 'delete', {
      params: {
        id
      }
    })
  },
  getProblem (id) {
    return ajax('admin/problem', 'get', {
      params: {
        id
      }
    })
  },
  getProblemList (params) {
    params = utils.filterEmptyValue(params)
    return ajax('admin/problem', 'get', {
      params
    })
  },
  getContestProblemList (params) {
    params = utils.filterEmptyValue(params)
    return ajax('admin/contest/problem', 'get', {
      params
    })
  },
  getContestProblem (id) {
    return ajax('admin/contest/problem', 'get', {
      params: {
        id
      }
    })
  },
  createContestProblem (data) {
    return ajax('admin/contest/problem', 'post', {
      data
    })
  },
  editContestProblem (data) {
    return ajax('admin/contest/problem', 'put', {
      data
    })
  },
  deleteContestProblem (id) {
    return ajax('admin/contest/problem', 'delete', {
      params: {
        id
      }
    })
  },
  makeContestProblemPublic (data) {
    return ajax('admin/contest_problem/make_public', 'post', {
      data
    })
  },
  addProblemFromPublic (data) {
    return ajax('admin/contest/add_problem_from_public', 'post', {
      data
    })
  },
  getReleaseNotes () {
    return ajax('admin/versions', 'get')
  },
  getDashboardInfo () {
    return ajax('admin/dashboard_info', 'get')
  },
  getSessions () {
    return ajax('sessions', 'get')
  },
  exportProblems (data) {
    return ajax('export_problem', 'post', {
      data
    })
  }
}


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

