import Vue from 'vue'
import Vuex from 'vuex'
import user from '@/store/user'
import contest from "@/store/contest"
import api from '@/common/api'
Vue.use(Vuex)
const rootState = {
  modalStatus: {
    mode: 'Login', // or 'register',
    visible: false
  },
  websiteConfig:{
    recordName:'粤ICP备×××××××号-1',
    projectName:'HOJ',
    recordUrl:'#',
    projectUrl:'#'
  },
  registerTimeOut: 60,
  resetTimeOut: 90,
}

const rootGetters = {
  'modalStatus'(state) {
    return state.modalStatus
  },
  'registerTimeOut'(state) {
    return state.registerTimeOut
  },
  'resetTimeOut'(state) {
    return state.resetTimeOut
  },
  'websiteConfig' (state) {
    return state.websiteConfig
  },
}

const rootMutations = {
  changeModalStatus(state, { mode, visible }) {
    if (mode !== undefined) {
      state.modalStatus.mode = mode
    }
    if (visible !== undefined) {
      state.modalStatus.visible = visible
    }
  },
  changeRegisterTimeOut(state, { time }) {
    if (time !== undefined) {
      state.registerTimeOut = time
    }
  },
  changeResetTimeOut(state, { time }) {
    if (time !== undefined) {
      state.resetTimeOut = time
    }
  },
  startTimeOut(state, { name }) { // 注册邮件和重置邮件倒计时
    if (state.resetTimeOut == 0 ) {
      state.resetTimeOut = 90
      return;
    }
    if (state.registerTimeOut == 0 ) {
      state.registerTimeOut = 60
      return;
    }
    if (name == 'resetTimeOut') {
      state.resetTimeOut--;
    }
    if (name == 'registerTimeOut') {
      state.registerTimeOut--;
    }
    setTimeout(() => {this.commit('startTimeOut', { name: name }) }, 1000);
  },
  changeWebsiteConfig(state, payload) {
    state.websiteConfig = payload.websiteConfig
  },
}
const rootActions = {
  changeModalStatus({ commit }, payload) {
    commit('changeModalStatus', payload)
  },
  changeResetTimeOut({ commit }, payload) {
    commit('changeResetTimeOut', payload)
  },
  changeRegisterTimeOut({ commit }, payload) {
    commit('changeRegisterTimeOut', payload)
  },
  startTimeOut({ commit }, payload) {
    commit('startTimeOut', payload)
  },
  changeDomTitle ({commit, state}, payload) {
    if (payload && payload.title) {
      window.document.title = payload.title + ' - ' + (state.websiteConfig.shortName+'').toUpperCase()
    } else {
      window.document.title = state.route.meta.title + ' - '+(state.websiteConfig.shortName+'').toUpperCase()
    }
  },
  getWebsiteConfig ({commit}) {
    api.getWebsiteConfig().then(res => {
      commit('changeWebsiteConfig', {
        websiteConfig: res.data.data
      })
    })
  },
}

export default new Vuex.Store({
  modules: {
    user,
    contest
  },
  state: rootState,
  getters: rootGetters,
  mutations: rootMutations,
  actions: rootActions,
})
