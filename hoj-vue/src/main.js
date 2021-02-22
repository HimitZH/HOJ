import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Element from 'element-ui'

import MuseUI from 'muse-ui';
import 'muse-ui/dist/muse-ui.css';


import "element-ui/lib/theme-chalk/index.css"
import 'font-awesome/css/font-awesome.min.css'
import 'default-passive-events'
import Message from 'vue-m-message'
import 'vue-m-message/dist/index.css'
import axios from 'axios'

import 'xe-utils'
import VXETable from 'vxe-table'
import 'vxe-table/lib/style.css'

import Katex from '@/common/katex'

import VueClipboard from 'vue-clipboard2'

import highlight from '@/common/highlight'

import filters from '@/common/filters.js'
import VueCropper from 'vue-cropper'

import ECharts from 'vue-echarts/components/ECharts.vue'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/pie'
import 'echarts/lib/component/title'
import 'echarts/lib/component/grid'
import 'echarts/lib/component/dataZoom'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/toolbox'
import 'echarts/lib/component/markPoint'

import VueParticles from 'vue-particles'
import SlideVerify from 'vue-monoplasty-slide-verify'

//  markdown编辑器
import mavonEditor from 'mavon-editor'  //引入markdown编辑器
import 'mavon-editor/dist/css/index.css'
// // 前端所用markdown样式
// import '@/assets/css/maize.css'
Vue.use(mavonEditor)

Object.keys(filters).forEach(key => {   // 注册全局过滤器
  Vue.filter(key, filters[key])
})
Vue.use(VueParticles) // 粒子特效背景
Vue.use(Katex)  // 数学公式渲染
Vue.use(VXETable) // 表格组件
Vue.use(VueClipboard) // 剪贴板
Vue.use(highlight) // 代码高亮
Vue.use(Element)
Vue.use(MuseUI) // 移动端导航栏需要该组件
Vue.use(VueCropper) // 图像剪切
Vue.use(Message, { name: 'msg' }) // `Vue.prototype.$msg` 全局消息提示

Vue.use(SlideVerify) // 滑动验证码组件

Vue.component('ECharts', ECharts)
Vue.prototype.$axios = axios

Vue.prototype.$markDown = mavonEditor.markdownIt

Vue.config.productionTip = false
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
