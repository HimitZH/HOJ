import { USER_TYPE } from '@/common/constants'
import storage from '@/common/storage'
import api from '@/common/api'
const state = {
  userInfo:  storage.get('userInfo'),
  token: localStorage.getItem('token'),
  loginFailNum:0,
  unreadMessage:{
    comment:0,
    reply:0,
    like:0,
    sys:0,
    mine:0
  }
}

const getters = {
  userInfo: state => state.userInfo || {},
  token: state => state.token ||'',
  unreadMessage:state => state.unreadMessage || {},
  loginFailNum:state=>state.loginFailNum || 0,
  isAuthenticated: (state, getters) => {
    return !!getters.token
  },
  isAdminRole: (state, getters) => {
    if(getters.userInfo.roleList){
      return getters.userInfo.roleList.indexOf(USER_TYPE.ADMIN)!=-1 ||
        getters.userInfo.roleList.indexOf(USER_TYPE.PROBLEM_ADMIN)!=-1 ||
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
  isProblemAdmin:(state, getters) => {
    if(getters.userInfo.roleList){
      return getters.userInfo.roleList.indexOf(USER_TYPE.PROBLEM_ADMIN) !=-1
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
  incrLoginFailNum(state,{success}){
    if(!success){
      state.loginFailNum +=1
    }else{
      state.loginFailNum = 0
    }
  },
  
  clearUserInfoAndToken(state){
    state.token = ''
    state.userInfo = {}
    state.loginFailNum = 0
    storage.clear()
  },
  updateUnreadMessageCount(state, {unreadMessage}){
    state.unreadMessage = unreadMessage
  },
  substractUnreadMessageCount(state,{needSubstractMsg}){
    // 负数也没关系
    state.unreadMessage[needSubstractMsg.name] = state.unreadMessage[needSubstractMsg.name]-needSubstractMsg.num;
  },
  changeUserAuthInfo(state, {roles}){
    state.userInfo.roleList = roles;
    storage.set('userInfo', state.userInfo);
  }
}

const actions = {
  setUserInfo ({commit},userInfo) {
      commit('changeUserInfo', {
        userInfo: userInfo
      })
  },
  incrLoginFailNum({commit},success){
    commit('incrLoginFailNum',{success:success})
  },
  clearUserInfoAndToken ({commit}) {
    commit('clearUserInfoAndToken')
  },
  updateUnreadMessageCount({commit},unreadMessage){
    commit('updateUnreadMessageCount', {
      unreadMessage: unreadMessage
    })
  },
  substractUnreadMessageCount({commit},needSubstractMsg){
    commit('substractUnreadMessageCount', {
      needSubstractMsg: needSubstractMsg
    })
  },
  refreshUserAuthInfo({commit,dispatch}){
    return new Promise((resolve, reject) => {
      api.getUserAuthInfo().then((res) => {
        commit('changeUserAuthInfo', {roles: res.data.data.roles})
        resolve(res)
      })
    }, err => {
      reject(err)
    })
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
