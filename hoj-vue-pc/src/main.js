import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Element from 'element-ui'
import "element-ui/lib/theme-chalk/index.css"
import 'font-awesome/css/font-awesome.min.css'
import Message from 'vue-m-message'
import 'vue-m-message/dist/index.css'
import VueCropper from 'vue-cropper'
import axios from 'axios'

import 'xe-utils'
import VXETable from 'vxe-table'
import 'vxe-table/lib/style.css'

Vue.use(VXETable)

Vue.use(Element)
Vue.use(VueCropper)
Vue.use(Message, { name: 'msg' }) // `Vue.prototype.$msg`
Vue.prototype.$axios = axios
Vue.config.productionTip = false
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
