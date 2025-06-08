import { createStore } from 'vuex'

export default createStore({
  state: {
    websiteConfig: { // Placeholder
      shortName: "HOJ"
    },
    // Ensure webLanguage is part of the state if it's not already
    webLanguage: 'en', // or 'cn', or load from localStorage
    modalStatus: { // Added for modal control
      mode: '', // 'Login', 'Register', etc.
      visible: false
    }
  },
  getters: {
    isAuthenticated: state => false, // Placeholder
    isSuperAdmin: state => false, // Placeholder for isSuperAdmin
    webLanguage: state => state.webLanguage, // Getter for webLanguage
    modalStatus: state => state.modalStatus // Getter for modal status
  },
  mutations: {
    setWebsiteConfig(state, config) {
      state.websiteConfig = config;
    },
    // Example mutation for webLanguage, if needed
    // setWebLanguage(state, language) {
    //   state.webLanguage = language;
    //   localStorage.setItem('OJLang', language); // Persist to localStorage
    // }
    SET_MODAL_STATUS(state, payload) { // Added mutation for modal
      state.modalStatus = payload;
    }
  },
  actions: {
    // _TODO: Action to fetch website config if needed
    // async fetchWebsiteConfig({ commit }) {
    //   try {
    //     const response = await api.getWebsiteConfig(); // Assuming an API endpoint
    //     commit('setWebsiteConfig', response.data.data);
    //   } catch (error) {
    //     console.error('Failed to fetch website config:', error);
    //   }
    // }
    changeModalStatus({ commit }, payload) { // Added action for modal
      commit('SET_MODAL_STATUS', payload);
    }
  },
  modules: {
  }
})
