const UglifyJsPlugin = require('uglifyjs-webpack-plugin') // 清除注释
const CompressionWebpackPlugin = require('compression-webpack-plugin'); // 开启压缩

// 是否为生产环境
const isProduction = process.env.NODE_ENV === 'production';

// 本地环境是否需要使用cdn
const devNeedCdn = true

// cdn链接
const cdn = {
  // cdn：模块名称和模块作用域命名（对应window里面挂载的变量名称）
  externals: {
    vue: 'Vue',
    'vue-router':'VueRouter',
    axios:'axios',
    vuex:'Vuex',
    'element-ui':'ELEMENT',
    'highlight.js': 'hljs',
    'vxe-table':'VXETable',
    "moment": "moment",
    'vue-echarts': 'VueECharts',
    "echarts":"echarts",
    // "mavon-editor": "mavonEditor",
  },
  // cdn的css链接
  css: [
      'https://cdn.bootcdn.net/ajax/libs/element-ui/2.14.0/theme-chalk/index.min.css',
      "https://cdn.bootcdn.net/ajax/libs/github-markdown-css/4.0.0/github-markdown.min.css",
      "https://cdn.jsdelivr.net/npm/vxe-table@2.9.26/lib/style.css"
  ],
  // cdn的js链接
  js: [
    "https://cdn.bootcdn.net/ajax/libs/vue/2.6.11/vue.min.js",
    "https://cdn.bootcdn.net/ajax/libs/vue-router/3.2.0/vue-router.min.js",
    "https://cdn.bootcdn.net/ajax/libs/axios/0.21.0/axios.min.js",
    "https://cdn.bootcdn.net/ajax/libs/vuex/3.5.1/vuex.min.js",
    "https://cdn.bootcdn.net/ajax/libs/element-ui/2.14.0/index.min.js",
    "https://cdn.bootcdn.net/ajax/libs/highlight.js/10.3.2/highlight.min.js",
    "https://cdn.jsdelivr.net/npm/xe-utils",
    "https://cdn.jsdelivr.net/npm/vxe-table@2.9.26",
    "https://cdn.bootcss.com/moment.js/2.29.1/moment.min.js",
    "https://cdn.bootcss.com/moment.js/2.29.1/locale/zh-cn.min.js",
    "https://cdn.bootcdn.net/ajax/libs/moment.js/2.29.1/locale/es-us.min.js",
    "https://cdn.bootcdn.net/ajax/libs/echarts/4.9.0-rc.1/echarts.min.js",
    "https://cdn.bootcdn.net/ajax/libs/vue-echarts/5.0.0-beta.0/vue-echarts.min.js",
    // "https://unpkg.com/mavon-editor@2.9.1/dist/mavon-editor.js"
  ]
}

module.exports={
  publicPath: '/',
  assetsDir: "assets",
  devServer: {
    open: true,  // npm run serve后自动打开页面
    host: '0.0.0.0',  // 匹配本机IP地址(默认是0.0.0.0)
    port: 8088, // 开发服务器运行端口号
    proxy: {
      '/api': {                                //   以'/api'开头的请求会被代理进行转发
        target: 'http://localhost:6688',       //   要发向的后台服务器地址  如果后台服务跑在后台开发人员的机器上，就写成 `http://ip:port` 如 `http:192.168.12.213:8081`   ip为后台服务器的ip
        changeOrigin: true 
      }
    },
    disableHostCheck: true,
  },
  //去除生产环境的productionSourceMap
  productionSourceMap: false,

  chainWebpack: config => {
    // ============注入cdn start============
    config.plugin('html').tap(args => {
        // 生产环境或本地需要cdn时，才注入cdn
        if (isProduction || devNeedCdn) args[0].cdn = cdn
        return args
    })
    config.plugin('webpack-bundle-analyzer') // 查看打包文件体积大小
      .use(require('webpack-bundle-analyzer').BundleAnalyzerPlugin)
    // ============注入cdn end============
  },
  configureWebpack: (config) => {
    // 用cdn方式引入，则构建时要忽略相关资源
    const plugins = [];
    if (isProduction || devNeedCdn){
      config.externals = cdn.externals
      config.mode = 'production';
      config["performance"] = {//打包文件大小配置
        "maxEntrypointSize": 10000000,
        "maxAssetSize": 30000000
      }
      config.plugins.push(
        new UglifyJsPlugin({
          uglifyOptions: {
            output: {
              comments: false, // 去掉注释
            },
            warnings: false,
            compress: {
              drop_console: true,
              drop_debugger: false,
              pure_funcs: ['console.log']//移除console
            }
          }
        })
      )
       // 服务器也要相应开启gzip
       config.plugins.push(
        new CompressionWebpackPlugin({
            filename: '[path].gz[query]',
            algorithm: 'gzip',
            test: /\.(js|css)$/,// 匹配文件名
            threshold: 10000, // 对超过10k的数据压缩
            deleteOriginalAssets: false, // 不删除源文件
            minRatio: 0.8 // 压缩比
        })
      )
    }
  }

}