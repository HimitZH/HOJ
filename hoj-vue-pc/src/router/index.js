import Vue from 'vue'
import VueRouter from 'vue-router'
import { sync } from 'vuex-router-sync'
import routes from '@/router/routes'
import mMessage from '@/common/message'
import store from '@/store'
import NProgress from 'nprogress' // nprogress插件
import 'nprogress/nprogress.css' // nprogress样式

// 配置NProgress进度条选项  —— 动画效果
NProgress.configure({ ease: 'ease', speed: 1000,showSpinner: false })

Vue.use(VueRouter)

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { x: 0, y: 0 }
    }
  },
  routes
})


// 路由判断登录 根据路由配置文件的参数(全局身份验证token)
router.beforeEach((to, from, next) => {

  
  NProgress.start()
  if (to.matched.some(record => record.meta.requireAuth)) { // 判断该路由是否需要登录权限
    const token = localStorage.getItem('token')
    if (token) { // 判断当前的token是否存在 ； 登录存入的token
      next()
    } else {
      next({
        path: '/'  // 无token认证的一致返回到主页
      })
      store.commit('changeModalStatus',{mode: 'Login', visible: true})
      mMessage.error('请您先登录！')
    }
  } else {
    next()
  }
})

router.afterEach((to, from, next) => {
  NProgress.done()
})

sync(store, router)

export default router
