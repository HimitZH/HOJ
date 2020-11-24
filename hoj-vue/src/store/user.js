import { USER_TYPE, PROBLEM_PERMISSION } from '@/common/constants'
import storage from '@/common/storage'
const state = {
  userInfo:  storage.get('userInfo'),
  token: localStorage.getItem('token')
}

const getters = {
  userInfo: state => state.userInfo || {},
  token: state => state.token ||'',
  isAuthenticated: (state, getters) => {
    return !!getters.token
  },
  isAdminRole: (state, getters) => {
    return getters.userInfo.role === USER_TYPE.ADMIN ||
      getters.userInfo.role === USER_TYPE.SUPER_ADMIN
  },
  isSuperAdmin: (state, getters) => {
    return getters.userInfo.role === USER_TYPE.SUPER_ADMIN
  },
  hasProblemPermission: (state, getters) => {
    return getters.user.problem_permission !== PROBLEM_PERMISSION.NONE
  }
}

const mutations = {
  changeUserInfo(state, {userInfo}) {
    state.userInfo = userInfo
    storage.set('userInfo',userInfo)
  },
  changeUserToken(state,token){
    state.token = token
    localStorage.setItem("token",token)
  },
  clearUserInfoAndToken(state){
    state.token = ''
    state.userInfo = {}
    storage.clear()
  }
}

const actions = {
  setUserInfo ({commit},userInfo) {
      commit('changeUserInfo', {
        userInfo: userInfo
      })
  },
  clearUserInfoAndToken ({commit}) {
    commit('clearUserInfoAndToken')
  },
}

export default {
  state,
  getters,
  actions,
  mutations
}
