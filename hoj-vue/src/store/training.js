import api from '@/common/api'
import {  TRAINING_TYPE } from '@/common/constants'
const state = {
  intoAccess: false, // 比赛进入权限
  training: {
    auth: TRAINING_TYPE.Public.name,
    rankShowName:'username'
  },
  trainingProblemList: [],
  itemVisible: {
    table: true,
    chart: true,
  },
}

const getters = {
  isTrainingAdmin: (state, getters, _, rootGetters) => {
    return rootGetters.isAuthenticated &&
      (state.training.author === rootGetters.userInfo.username || rootGetters.isSuperAdmin)
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
    return state.training.auth !== TRAINING_TYPE.Public.name &&!state.intoAccess && !getters.isTrainingAdmin 
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
  clearTraining (state) {
    state.training = {}
    state.trainingProblemList = []
    state.intoAccess = false
    state.itemVisible = {
      table: true,
      chart: true,
      realName: false
    }
  }
}

const actions = {
  getTraining ({commit, rootState, dispatch}) {
    return new Promise((resolve, reject) => {
      api.getTraining(rootState.route.params.trainingID).then((res) => {
        resolve(res)
        let training = res.data.data
        commit('changeTraining', {training: training})
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
  }
}

export default {
  state,
  mutations,
  getters,
  actions
}
