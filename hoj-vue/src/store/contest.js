import moment from 'moment'
import api from '@/common/api'
import { CONTEST_STATUS, CONTEST_TYPE } from '@/common/constants'
import time from '@/common/time'
const state = {
  now: moment(),
  intoAccess: false, // 比赛进入权限
  submitAccess:false, // 保护比赛的提交权限
  forceUpdate: false, // 强制实时榜单
  removeStar: false, // 榜单去除打星队伍
  concernedList:[], // 关注队伍
  isContainsAfterContestJudge: false, // 是否包含比赛结束后的提交
  contest: {
    auth: CONTEST_TYPE.PUBLIC,
    openPrint: false,
    rankShowName:'username',
    allowEndSubmit: false,
  },
  contestProblems: [],
  itemVisible: {
    table: true,
    chart: true,
  },
  disPlayIdMapColor:{}, // 展示id对应的气球颜色
  groupContestAuth: 0,
}

const getters = {
  contestStatus: (state, getters) => {
    return state.contest.status;
  },
  contestRuleType: (state,getters) => {
    return state.contest.type;
  },
  isContestAdmin: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated &&
      (state.contest.author === rootGetters.userInfo.username || rootGetters.isSuperAdmin || state.groupContestAuth == 5)
  },
  isContainsAfterContestJudge: (state, getters) => {
    return state.isContainsAfterContestJudge;
  },
  canSubmit:(state, getters)=>{
     return state.intoAccess||state.submitAccess || state.contest.auth === CONTEST_TYPE.PUBLIC ||getters.isContestAdmin
  },
  contestMenuDisabled: (state, getters) => {
    // 比赛创建者或者超级管理员可以直接查看
    if (getters.isContestAdmin) return false
     // 未开始不可查看
    if(getters.contestStatus === CONTEST_STATUS.SCHEDULED) return true

    if (state.contest.auth === CONTEST_TYPE.PRIVATE) {
     // 私有赛需要通过验证密码方可查看比赛
      return !state.intoAccess
    }
    
  },

  // 榜单是否实时刷新
  ContestRealTimePermission: (state, getters, _, rootGetters) => {
    // 比赛若是已结束，便是最后榜单
    if (getters.contestStatus === CONTEST_STATUS.ENDED) {
      return true
    }
    // 比赛管理员直接可看到实时榜单
    if(getters.isContestAdmin){
      return true
    }
    // 比赛是否开启
    if(state.contest.sealRank === true){
      // 当前时间在封榜时间之后，即不刷新榜单
      return !state.now.isAfter(moment(state.contest.sealRankTime))
    }else{
      return true
    }
  },
  problemSubmitDisabled: (state, getters, _, rootGetters) => {
    // 比赛结束不可交题, 除非开启了允许
    if (getters.contestStatus === CONTEST_STATUS.ENDED) {
      return !state.contest.allowEndSubmit
    } else if (getters.contestStatus === CONTEST_STATUS.SCHEDULED) {
      // 比赛未开始不可交题，除非是比赛管理者
      return !getters.isContestAdmin
    }
    // 未登录不可交题
    return !rootGetters.isAuthenticated
  },
  // 是否需要显示密码验证框
  passwordFormVisible: (state, getters) => {
    // 如果是公开赛，保护赛，或已注册过，管理员都不用再显示
    return state.contest.auth !== CONTEST_TYPE.PUBLIC &&state.contest.auth !== CONTEST_TYPE.PROTECTED &&!state.intoAccess && !getters.isContestAdmin 
  },
  contestStartTime: (state) => {
    return moment(state.contest.startTime)
  },
  contestEndTime: (state) => {
    return moment(state.contest.endTime)
  },
  // 比赛计时文本显示
  countdown: (state, getters) => {
    // 还未开始的显示
    if (getters.contestStatus === CONTEST_STATUS.SCHEDULED) {

      let durationMs = getters.contestStartTime.diff(state.now, 'seconds')

      let duration = moment.duration(durationMs, 'seconds')
      // time is too long
      if (duration.weeks() > 0) {
        return 'Start At ' + duration.humanize()
      }

      if(duration.asSeconds()<=0){
        state.contest.status = CONTEST_STATUS.RUNNING
      }

      let texts = time.secondFormat(durationMs)
      return '-' + texts
      // 比赛进行中的显示
    } else if (getters.contestStatus === CONTEST_STATUS.RUNNING) {
      // 倒计时文本显示
      if(getters.contestEndTime.diff(state.now, 'seconds')>0){
        let texts = time.secondFormat(getters.contestEndTime.diff(state.now, 'seconds'))
        return '-' + texts
      }else{
        state.contest.status = CONTEST_STATUS.ENDED
        return "00:00:00"
      }
      
    } else {
      return 'Ended'
    }
  },
  // 比赛开始到现在经过的秒数
  BeginToNowDuration:(state,getters)=>{
    return moment.duration(state.now.diff(getters.contestStartTime, 'seconds'), 'seconds').asSeconds()
  },

  // 比赛进度条显示
  progressValue:(state,getters)=>{
      // 还未开始的显示
    if (getters.contestStatus === CONTEST_STATUS.SCHEDULED) {
      return 0;
      // 比赛进行中的显示
    } else if (getters.contestStatus === CONTEST_STATUS.RUNNING) {
      // 获取比赛开始到现在经过的秒数
      let duration = getters.BeginToNowDuration
      // 消耗时间除以整体时间
      return (duration / state.contest.duration)*100
    }else{
      return 100;
    }
  },

  isShowContestSetting:(state, getters)=>{
    return getters.contestStatus === CONTEST_STATUS.ENDED && state.contest.allowEndSubmit
  }
}

const mutations = {
  changeContest (state, payload) {
    state.contest = payload.contest
  },
  changeContestItemVisible(state, payload) {
    state.itemVisible = {...state.itemVisible, ...payload}
  },
  changeRankForceUpdate (state, payload) {
    state.forceUpdate = payload.value
  },
  changeRankRemoveStar(state, payload){
    state.removeStar = payload.value
  },
  changeContainsAfterContestJudge(state, payload){
    state.isContainsAfterContestJudge = payload.value
  },
  changeConcernedList(state, payload){
    state.concernedList = payload.value
  },
  changeContestProblems(state, payload) {
    state.contestProblems = payload.contestProblems;
    let tmp={};
    for(var j = 0,len = payload.contestProblems.length; j < len; j++){
      tmp[payload.contestProblems[j].displayId] = payload.contestProblems[j].color;
    }
    state.disPlayIdMapColor = tmp;
  },
  changeContestRankLimit(state, payload) {
    state.rankLimit = payload.rankLimit
  },
  contestIntoAccess(state, payload) {
    state.intoAccess = payload.intoAccess
  },
  changeGroupContestAuth(state, payload) {
    state.groupContestAuth = payload.groupContestAuth
  },
  contestSubmitAccess(state, payload) {
    state.submitAccess = payload.submitAccess
  },
  clearContest (state) {
    state.contest = {}
    state.contestProblems = []
    state.intoAccess = false
    state.submitAccess = false
    state.itemVisible = {
      table: true,
      chart: true,
      realName: false
    }
    state.forceUpdate = false
    state.removeStar = false
    state.groupContestAuth = 0
    state.isContainsAfterContestJudge = false
  },
  now(state, payload) {
    state.now = payload.now
  },
  nowAdd1s (state) {
    state.now = moment(state.now.add(1, 's'))
  },
}

const actions = {
  getContest ({commit, rootState, dispatch}) {
    return new Promise((resolve, reject) => {
      api.getContest(rootState.route.params.contestID).then((res) => {
        resolve(res)
        let contest = res.data.data
        commit('changeContest', {contest: contest})
        if (contest.gid) {
          dispatch('getGroupContestAuth', {gid: contest.gid})
        }
        commit('now', {now: moment(contest.now)})
        if (contest.auth == CONTEST_TYPE.PRIVATE) {
          dispatch('getContestAccess',{auth:CONTEST_TYPE.PRIVATE})
        }else if(contest.auth == CONTEST_TYPE.PROTECTED){
          dispatch('getContestAccess',{auth:CONTEST_TYPE.PROTECTED})
        }
      }, err => {
        reject(err)
      })
    })
  },
  getScoreBoardContestInfo ({commit, rootState, dispatch}) {
    return new Promise((resolve, reject) => {
      api.getScoreBoardContestInfo(rootState.route.params.contestID).then((res) => {
        resolve(res)
        let contest = res.data.data.contest;
        let problemList = res.data.data.problemList;
        commit('changeContest', {contest: contest})
        commit('changeContestProblems', {contestProblems: problemList})
        commit('now', {now: moment(contest.now)})
      }, err => {
        reject(err)
      })
    })
  },

  getContestProblems ({commit, rootState}) {
    return new Promise((resolve, reject) => {
      api.getContestProblemList(rootState.route.params.contestID, rootState.contest.isContainsAfterContestJudge).then(res => {
        resolve(res)
        commit('changeContestProblems', {contestProblems: res.data.data})
      }, (err) => {
        commit('changeContestProblems', {contestProblems: []})
        reject(err)
      })
    })
  },
  getContestAccess ({commit, rootState},contestType) {
    return new Promise((resolve, reject) => {
      api.getContestAccess(rootState.route.params.contestID).then(res => {
        if(contestType.auth == CONTEST_TYPE.PRIVATE){
          commit('contestIntoAccess', {intoAccess: res.data.data.access})
        }else{
          commit('contestSubmitAccess', {submitAccess: res.data.data.access})
        }
        resolve(res)
      }).catch()
    })
  },
  getGroupContestAuth ({commit, rootState}, gid) {
    return new Promise((resolve, reject) => {
      api.getGroupAuth(gid.gid).then(res => {
        commit('changeGroupContestAuth', {groupContestAuth: res.data.data})
        resolve(res)
      }).catch()
    })
  }
}

export default {
  state,
  mutations,
  getters,
  actions
}
