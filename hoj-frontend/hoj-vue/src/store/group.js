import api from '@/common/api'
import { GROUP_TYPE } from '@/common/constants'
const state = {
  intoAccess: false,
  group: {
    auth: GROUP_TYPE.PUBLIC,
  },
  auth: 0,
}

const getters = {
  group: state=> state.group || {},
  isGroupOwner: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated && state.group.owner == getters.userInfo.username
  },
  isGroupRoot: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated && (
      state.auth === 5 || getters.isGroupOwner || rootGetters.isSuperAdmin
    )
  },
  isGroupAdmin: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated && (
      getters.isGroupRoot || state.auth === 4
    )
  },
  isGroupMember: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated && (
      getters.isGroupAdmin || state.auth === 3
    )
  },
  groupMenuDisabled: (state, getters) => {
    if (getters.isGroupMember) return false
    return !state.intoAccess
  },
  userAuth: (state, getters) => {
    return state.auth;
  },
}

const mutations = {
  changeGroup (state, payload) {
    state.group = payload.group
  },
  changeGroupAuth(state, payload) {
    state.auth = payload.auth
  },
  changeGroupIntoAccess(state, payload) {
    state.intoAccess = payload.intoAccess
  },
  clearGroup (state) {
    state.group = {}
    state.auth = 0
    state.intoAccess = false
  }
}

const actions = {
  setGroup ({commit}, group) {
    commit('changeGroup', {
      group: group
    })
  },
  getGroup ({commit, rootState, dispatch}) {
    return new Promise((resolve, reject) => {
      api.getGroup(rootState.route.params.groupID).then((res) => {
        resolve(res)
        let group = res.data.data
        commit('changeGroup', {group: group})
        dispatch('getGroupAccess')
        dispatch('getGroupAuth')
      }, err => {
        reject(err)
      })
    })
  },
  getGroupAccess ({commit, rootState}) {
    return new Promise((resolve, reject) => {
      api.getGroupAccess(rootState.route.params.groupID).then(res => {
        commit('changeGroupIntoAccess', {intoAccess: res.data.data.access})
        resolve(res)
      }).catch()
    })
  },
  getGroupAuth ({commit, rootState}) {
    return new Promise((resolve, reject) => {
      api.getGroupAuth(rootState.route.params.groupID).then(res => {
        commit('changeGroupAuth', {auth: res.data.data})
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
