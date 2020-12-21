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
    if(getters.userInfo.roleList){
      return getters.userInfo.roleList.indexOf(USER_TYPE.ADMIN)!=-1 ||
        getters.userInfo.roleList.indexOf(USER_TYPE.SUPER_ADMIN)!=-1
    }else{
      return false;
    }
  },
  isSuperAdmin: (state, getters) => {
    if(getters.userInfo.roleList){
      return getters.userInfo.roleList.indexOf(USER_TYPE.SUPER_ADMIN) !=-1
    }else{
      return false;
    }
  },
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
