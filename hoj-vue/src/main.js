import Vue from 'vue'
import App from './App.vue'
import store from './store'
import Element from 'element-ui'
import i18n from '@/i18n'

// import "element-ui/lib/theme-chalk/index.css"
import 'font-awesome/css/font-awesome.min.css'
import Message from 'vue-m-message'
import 'vue-m-message/dist/index.css'
import axios from 'axios'

import Md_Katex from '@iktakahiro/markdown-it-katex'

// 注释表示使用cdn引入
import 'xe-utils'
import VXETable from 'vxe-table'
import 'vxe-table/lib/style.css'

import Katex from '@/common/katex'

import VueClipboard from 'vue-clipboard2'

import highlight from '@/common/highlight'

import filters from '@/common/filters.js'
import VueCropper from 'vue-cropper'

// import ECharts from 'vue-echarts/components/ECharts.vue'
// import 'echarts/lib/chart/bar'
// import 'echarts/lib/chart/line'
// import 'echarts/lib/chart/pie'
// import 'echarts/lib/component/title'
// import 'echarts/lib/component/grid'
// import 'echarts/lib/component/dataZoom'
// import 'echarts/lib/component/legend'
// import 'echarts/lib/component/tooltip'
// import 'echarts/lib/component/toolbox'
// import 'echarts/lib/component/markPoint'
// Vue.component('ECharts', ECharts)

// 使用CDN的方式如下引入echarts
import VueECharts from 'vue-echarts';
Vue.component('ECharts', VueECharts)


import VueParticles from 'vue-particles'
import SlideVerify from 'vue-monoplasty-slide-verify'

//  markdown编辑器
import mavonEditor from 'mavon-editor'  //引入markdown编辑器
import 'mavon-editor/dist/css/index.css';
Vue.use(mavonEditor)

// import 'muse-ui/dist/muse-ui.css';
import MuseUI from 'muse-ui'
Vue.use(MuseUI)

import VueDOMPurifyHTML from 'vue-dompurify-html'
Vue.use(VueDOMPurifyHTML)

import router from './router'

Object.keys(filters).forEach(key => {   // 注册全局过滤器
  Vue.filter(key, filters[key])
})
Vue.use(VueParticles) // 粒子特效背景
Vue.use(Katex)  // 数学公式渲染
VXETable.setup({
  // 对组件内置的提示语进行国际化翻译
  i18n: (key, value) => i18n.t(key, value)
})
Vue.use(VXETable) // 表格组件
Vue.use(VueClipboard) // 剪贴板
Vue.use(highlight) // 代码高亮
Vue.use(Element,{
  i18n: (key, value) => i18n.t(key, value)
})

Vue.use(VueCropper) // 图像剪切
Vue.use(Message, { name: 'msg' }) // `Vue.prototype.$msg` 全局消息提示

Vue.use(SlideVerify) // 滑动验证码组件

Vue.prototype.$axios = axios

Vue.prototype.$markDown = mavonEditor.mavonEditor.getMarkdownIt().use(Md_Katex)  // 挂载到vue

Vue.config.productionTip = false
new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
