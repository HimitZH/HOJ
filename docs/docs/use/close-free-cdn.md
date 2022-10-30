# 取消前端免费CDN

由于有的机房的网络不支持一些域名的访问，有防火墙挡住，所以可能前端页面的js和css的CDN访问不了，导致页面打不开。

:::info
hoj挂载了一些前端静态资源库的免费CDN，全部都是域名`bytecdntp.com`下的免费CDN
:::

可以在对应的电脑浏览器上打开以下链接，如果能正常访问则没有问题。

```html
https://lf6-cdn-tos.bytecdntp.com/cdn/expire-1-M/vue/2.6.11/vue.min.js
```

:::warning
hoj-frontend(前端vue项目)如果不挂载任何CDN，最终打包生成的文件夹大小约5.3MB
:::

## 一、全部打包且部署

:::info
如果本身hoj部署在**学校内网机器**上或者**云服务器是无带宽上限、按流量计费的实例**，那么可以不用考虑带宽问题，可以直接取消CDN挂载，直接全部自己打包成对应的静态文件，然后挂载到docker的`hoj-frontend`镜像里面
:::
**操作如下:**
  1. 下载前端源代码：[hoj-vue](https://gitee.com/himitzh0730/hoj/tree/master/hoj-vue)

  2. 进入`hoj-vue`文件夹，编辑`vue.config.js`文件，按下面的修改

     ```js
     // 该变量改成false
     const isProduction = false;

     // 本地环境是否需要使用cdn，该变量改成false
     const devNeedCdn = false;

     // 找到下面对应的cdn的js链接和css链接，全部注释掉
       css: [
           // "https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/element-ui/2.14.0/theme-chalk/index.min.css",
           // "https://lf26-cdn-tos.bytecdntp.com/cdn/expire-1-M/github-markdown-css/4.0.0/github-markdown.min.css",
           // "https://lf6-cdn-tos.bytecdntp.com/cdn/expire-1-M/KaTeX/0.12.0/katex.min.css",
           // 'https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/muse-ui/3.0.2/muse-ui.min.css'
       ],
       js: [
           // "https://lf6-cdn-tos.bytecdntp.com/cdn/expire-1-M/vue/2.6.11/vue.min.js",
           // "https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/vue-router/3.2.0/vue-router.min.js",
           // "https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/axios/0.26.0/axios.min.js",
           // "https://lf3-cdn-tos.bytecdntp.com/cdn/expire-1-M/element-ui/2.15.3/index.min.js",
           // "https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/highlight.js/10.3.2/highlight.min.js",
           // "https://lf26-cdn-tos.bytecdntp.com/cdn/expire-1-M/moment.js/2.29.1/moment.min.js",
           // "https://lf26-cdn-tos.bytecdntp.com/cdn/expire-1-M/moment.js/2.29.1/locale/zh-cn.min.js",
           // "https://lf3-cdn-tos.bytecdntp.com/cdn/expire-1-M/moment.js/2.29.1/locale/en-gb.min.js",
           // "https://lf6-cdn-tos.bytecdntp.com/cdn/expire-1-M/echarts/4.9.0-rc.1/echarts.min.js",
           // "https://lf26-cdn-tos.bytecdntp.com/cdn/expire-1-M/vue-echarts/5.0.0-beta.0/vue-echarts.min.js",
           // "https://lf3-cdn-tos.bytecdntp.com/cdn/expire-1-M/vuex/3.5.1/vuex.min.js",
           // "https://lf6-cdn-tos.bytecdntp.com/cdn/expire-1-M/KaTeX/0.12.0/katex.min.js",
           // "https://lf6-cdn-tos.bytecdntp.com/cdn/expire-1-M/KaTeX/0.12.0/contrib/auto-render.min.js",
           // 'https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/muse-ui/3.0.2/muse-ui.min.js',
           // 'https://lf26-cdn-tos.bytecdntp.com/cdn/expire-1-M/jquery/3.5.1/jquery.min.js',
       ]
     ```

  3. 进入`hoj-vue/src`文件夹，编辑`main.js`文件，将内容替换成如下：

     ```js
     import Vue from 'vue'
     import App from './App.vue'
     import store from './store'
     import Element from 'element-ui'
     import i18n from '@/i18n'

     import "element-ui/lib/theme-chalk/index.css"
     import 'font-awesome/css/font-awesome.min.css'
     import Message from 'vue-m-message'
     import 'vue-m-message/dist/index.css'
     import axios from 'axios'

     import Md_Katex from '@iktakahiro/markdown-it-katex'

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
     Vue.component('ECharts', ECharts)

     import VueECharts from 'vue-echarts';
     Vue.component('ECharts', VueECharts)
     import VueParticles from 'vue-particles'
     import SlideVerify from 'vue-monoplasty-slide-verify'

     //  markdown编辑器
     import mavonEditor from 'mavon-editor'  //引入markdown编辑器
     import 'mavon-editor/dist/css/index.css';
     Vue.use(mavonEditor)

     import {Drawer,List,Menu,Icon,AppBar,Button,Divider} from 'muse-ui';
     import 'muse-ui/dist/muse-ui.css';

     import VueDOMPurifyHTML from 'vue-dompurify-html'
     Vue.use(VueDOMPurifyHTML)

     import router from './router'
     Vue.use(Drawer)
     Vue.use(List)
     Vue.use(Menu)
     Vue.use(Icon)
     Vue.use(AppBar)
     Vue.use(Button)
     Vue.use(Divider)

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
     ```


  4. 然后使用在`hoj-vue`目录下，使用`npm run build`，npm请自行百度下载安装，之后会生成一个dist文件夹，结构如下：

     ```
     dist
     ├── index.html
     ├── favicon.ico
     └── assets
         ├── css
         │   ├── ....
         ├── fonts
         │   ├── ....
         ├── img
         │   ├── ....
         ├── js
       │   ├── ....

     ....
     ....
     ```

     将 `dist` 文件夹复制到服务器上某个目录下，比如 `/hoj/www/html/dist`，然后修改 `docker-compose.yml`，在 `hoj-frontend` 模块中的 `volumes` 中增加一行 `- /hoj/www/html/dist:/usr/share/nginx/html` （冒号前面的请修改为实际的路径），然后 `docker-compose up -d` 即可。


## 二、全部打包但有个人CDN服务器
:::info
如果云服务器是只有固定小流量出口带宽的，例如1M,2M的，害怕访问速度太慢，但是有钱买CDN服务器，可以先按照上面的方式，生成对应的本地静态文件夹，然后把`dist/assets`文件夹放在CDN服务器上，然后修改`dist/index.html`
:::
  **(建议：有弄过CDN的可以这样搞)**

  添加css等文件的导入

  ```html
  <link href="cdn服务器的地址/assets/css/文件名称.css" rel="prefetch">
  ```

  添加js等文件的导入

  ```html
  <script src="cdn服务器的地址/assets/js/文件名称.js">
  ```


    ..............................

   将 `dist` 文件夹复制到服务器上某个目录下，比如 `/hoj/www/html/dist`，然后修改 `docker-compose.yml`，在 `hoj-frontend` 模块中的 `volumes` 中增加一行 `- /hoj/www/html/dist:/usr/share/nginx/html` （冒号前面的请修改为实际的路径），然后 `docker-compose up -d` 即可。