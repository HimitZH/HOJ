import api from '@/common/api'
import {  TRAINING_TYPE } from '@/common/constants'
const state = {
  intoAccess: true, // 训练进入权限
  training: {
    auth: TRAINING_TYPE.Public.name,
    rankShowName:'username',
    gid:null
  },
  trainingProblemList: [],
  itemVisible: {
    table: true,
    chart: true,
  },
  groupTrainingAuth: 0,
}

const getters = {
  isTrainingAdmin: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated &&
      (state.training.author === rootGetters.userInfo.username || rootGetters.isSuperAdmin || state.groupTrainingAuth == 5)
  },
  trainingMenuDisabled: (state, getters) => {
    // 训练创建者和超级管理员可以直接查看
    if (getters.isTrainingAdmin) return false

    if (state.training.auth === TRAINING_TYPE.Private.name) {
     // 私有训练需要通过验证密码方可查看比赛
      return !state.intoAccess
    }
  },
  isPrivateTraining: (state, getters) => {
    return state.training.auth === TRAINING_TYPE.Private.name
  },
  // 是否需要显示密码验证框
  trainingPasswordFormVisible: (state, getters) => {
    // 如果是公开训练，或已注册过，管理员都不用再显示
    return !state.intoAccess && state.training.auth != TRAINING_TYPE.Public.name && !getters.isTrainingAdmin 
  }
}

const mutations = {
  changeTraining (state, payload) {
    state.training = payload.training
  },
  changeTrainingItemVisible(state, payload) {
    state.itemVisible = {...state.itemVisible, ...payload}
  },

  changeTrainingProblemList(state, payload) {
    state.trainingProblemList = payload.trainingProblemList;
  },
  trainingIntoAccess(state, payload) {
    state.intoAccess = payload.intoAccess
  },
  changeGroupTrainingAuth(state, payload) {
    state.groupTrainingAuth = payload.groupTrainingAuth
  },
  clearTraining (state) {
    state.training = {}
    state.trainingProblemList = []
    state.intoAccess = false
    state.itemVisible = {
      table: true,
      chart: true,
      realName: false
    }
    state.groupTrainingAuth = 0
  }
}

const actions = {
  getTraining ({commit, rootState, dispatch}) {
    return new Promise((resolve, reject) => {
      api.getTraining(rootState.route.params.trainingID).then((res) => {
        resolve(res)
        let training = res.data.data
        commit('changeTraining', {training: training})
        if (training.gid) {
          dispatch('getGroupTrainingAuth', {gid: training.gid})
        }
        if (training.auth ==  TRAINING_TYPE.Private.name) {
          dispatch('getTrainingAccess',{auth:TRAINING_TYPE.Private.name})
        }
      }, err => {
        reject(err)
      })
    })
  },
  getTrainingProblemList ({commit, rootState}) {
    return new Promise((resolve, reject) => {
      api.getTrainingProblemList(rootState.route.params.trainingID).then(res => {
        resolve(res)
        commit('changeTrainingProblemList', {trainingProblemList: res.data.data})
      }, (err) => {
        commit('changeTrainingProblemList', {trainingProblemList: []})
        reject(err)
      })
    })
  },
  getTrainingAccess ({commit, rootState},trainingType) {
    return new Promise((resolve, reject) => {
      api.getTrainingAccess(rootState.route.params.trainingID).then(res => {
        if(trainingType.auth == TRAINING_TYPE.Private.name){
          commit('trainingIntoAccess', {intoAccess: res.data.data.access})
        }
        resolve(res)
      }).catch()
    })
  },
  getGroupTrainingAuth ({commit, rootState}, gid) {
    return new Promise((resolve, reject) => {
      api.getGroupAuth(gid.gid).then(res => {
        commit('changeGroupTrainingAuth', {groupTrainingAuth: res.data.data})
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
