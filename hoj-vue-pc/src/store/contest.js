import moment from 'moment'
import api from '@/common/api'
import { CONTEST_STATUS, USER_TYPE, CONTEST_TYPE } from '@/common/constants'

const state = {
  now: moment(),
  access: false,
  rankLimit: 30,
  forceUpdate: false,
  contest: {
    created_by: {},
    contest_type: CONTEST_TYPE.PUBLIC
  },
  contestProblems: [],
  itemVisible: {
    menu: true,
    chart: true,
    realName: false
  }
}

const getters = {
  // contest 是否加载完成
  contestLoaded: (state) => {
    return !!state.contest.status
  },
  contestStatus: (state, getters) => {
    if (!getters.contestLoaded) return null
    let startTime = moment(state.contest.start_time)
    let endTime = moment(state.contest.end_time)
    let now = state.now

    if (startTime > now) {
      return CONTEST_STATUS.NOT_START
    } else if (endTime < now) {
      return CONTEST_STATUS.ENDED
    } else {
      return CONTEST_STATUS.UNDERWAY
    }
  },
  contestRuleType: (state) => {
    return state.contest.rule_type || null
  },
  isContestAdmin: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated &&
      (state.contest.created_by.id === rootGetters.user.id || rootGetters.user.admin_type === USER_TYPE.SUPER_ADMIN)
  },
  contestMenuDisabled: (state, getters) => {
    if (getters.isContestAdmin) return false
    if (state.contest.contest_type === CONTEST_TYPE.PUBLIC) {
      return getters.contestStatus === CONTEST_STATUS.NOT_START
    }
    return !state.access
  },
  OIContestRealTimePermission: (state, getters, _, rootGetters) => {
    if (getters.contestRuleType === 'ACM' || getters.contestStatus === CONTEST_STATUS.ENDED) {
      return true
    }
    return state.contest.real_time_rank === true || getters.isContestAdmin
  },
  problemSubmitDisabled: (state, getters, _, rootGetters) => {
    if (getters.contestStatus === CONTEST_STATUS.ENDED) {
      return true
    } else if (getters.contestStatus === CONTEST_STATUS.NOT_START) {
      return !getters.isContestAdmin
    }
    return !rootGetters.isAuthenticated
  },
  passwordFormVisible: (state, getters) => {
    return state.contest.contest_type !== CONTEST_TYPE.PUBLIC && !state.access && !getters.isContestAdmin
  },
  contestStartTime: (state) => {
    return moment(state.contest.start_time)
  },
  contestEndTime: (state) => {
    return moment(state.contest.end_time)
  },
  countdown: (state, getters) => {
    if (getters.contestStatus === CONTEST_STATUS.NOT_START) {
      let duration = moment.duration(getters.contestStartTime.diff(state.now, 'seconds'), 'seconds')
      // time is too long
      if (duration.weeks() > 0) {
        return 'Start At ' + duration.humanize()
      }
      let texts = [Math.floor(duration.asHours()), duration.minutes(), duration.seconds()]
      return '-' + texts.join(':')
    } else if (getters.contestStatus === CONTEST_STATUS.UNDERWAY) {
      let duration = moment.duration(getters.contestEndTime.diff(state.now, 'seconds'), 'seconds')
      let texts = [Math.floor(duration.asHours()), duration.minutes(), duration.seconds()]
      return '-' + texts.join(':')
    } else {
      return 'Ended'
    }
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
  changeContestProblems(state, payload) {
    state.contestProblems = payload.contestProblems
  },
  changeContestRankLimit(state, payload) {
    state.rankLimit = payload.rankLimit
  },
  contestAccess(state, payload) {
    state.access = payload.access
  },
  clearContest (state) {
    state.contest = {created_by: {}}
    state.contestProblems = []
    state.access = false
    state.itemVisible = {
      menu: true,
      chart: true,
      realName: false
    }
    state.forceUpdate = false
  },
  now(state, payload) {
    state.now = payload.now
  },
  nowAdd1s (state) {
    state.now = moment(state.now.add(1, 's'))
  }
}

const actions = {
  getContest ({commit, rootState, dispatch}) {
    return new Promise((resolve, reject) => {
      api.getContest(rootState.route.params.contestID).then((res) => {
        resolve(res)
        let contest = res.data.data
        commit('changeContest', {contest: contest})
        commit('now', {now: moment(contest.now)})
        if (contest.contest_type === CONTEST_TYPE.PRIVATE) {
          dispatch('getContestAccess')
        }
      }, err => {
        reject(err)
      })
    })
  },
  getContestProblems ({commit, rootState}) {
    return new Promise((resolve, reject) => {
      api.getContestProblemList(rootState.route.params.contestID).then(res => {
        res.data.data.sort((a, b) => {
          if (a._id === b._id) {
            return 0
          } else if (a._id > b._id) {
            return 1
          }
          return -1
        })
        commit('changeContestProblems', {contestProblems: res.data.data})
        resolve(res)
      }, () => {
        commit('changeContestProblems', {contestProblems: []})
      })
    })
  },
  getContestAccess ({commit, rootState}) {
    return new Promise((resolve, reject) => {
      api.getContestAccess(rootState.route.params.contestID).then(res => {
        commit('contestAccess', {access: res.data.data.access})
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
