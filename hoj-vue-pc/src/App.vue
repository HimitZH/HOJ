<template>
<div>
  <NavBar></NavBar>
  <div id="app">
      <transition name="el-zoom-in-bottom">
        <router-view></router-view>
      </transition>
  </div>
</div>
</template>

<script>
import NavBar from '@/components/common/NavBar'
export default {
  name: 'app',
  components: {
    NavBar
  },
}
</script>

<style>
 * {
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;

  }
  body{
    background-color: #eee;
    font-family: Helvetica Neue,Helvetica,PingFang SC,Hiragino Sans GB,Microsoft YaHei,\\5FAE\8F6F\96C5\9ED1,Arial,sans-serif;
    -webkit-font-smoothing: antialiased;
    color: #495060;
    font-size: 12px;
  }
  code, kbd, pre, samp {
    font-family: Consolas,Menlo,Courier,monospace;
  }
  
  a {
    text-decoration: none;
    background-color: transparent;
    color: #495060;
    outline: 0;
    cursor: pointer;
    transition: color .2s ease;
  }
  a:hover{
     color: #2196f3;
  }
  .drop-menu{
    padding-top: 7px;
  }
  .panel-title{
    font-size: 21px;
    font-weight: 500;
    padding-top: 10px;
    padding-bottom: 20px;
    line-height: 30px;
  }
  
  .home-title{
    color: #409EFF;
  }
  .oi-100,.first-ac{
    background-color: #080;
    color: #fff;
    font-weight: 700;
  }
  .oi-between{
    background-color: #2d8cf0;
    color: #fff;
  }
  .ac{
    background-color: #a9f5af;
    color: #3c763d;
  }
  .oi-0,.wa{
    color: #a94442;
    background-color: #f2dede;
  }
  .status-green{
    background-color: #19be6b!important;
    color:  #fff!important;
  }
  .status-red{
    background-color: #ed3f14!important;
    color:  #fff!important;
  }
  .status-yellow{
    background-color: #f90!important;
    color:  #fff!important;
  }
  .status-blue{
    background-color: #2d8cf0!important;
    color:  #fff!important;
  }
  .status-gray{
    background-color:#909399!important;
    color:  #fff!important;
  }
  .own-submit-row{
    background:rgb(230, 255, 223) !important;
  }
  .vxe-table{
    color: #495060!important;
    font-size: 12px!important;
    font-weight: 500!important;
  }
  .vxe-table .vxe-body--column:not(.col--ellipsis), .vxe-table .vxe-footer--column:not(.col--ellipsis), .vxe-table .vxe-header--column:not(.col--ellipsis) {
    padding: 9px 0!important;
  }
  #nprogress .bar {
    background: #66B1FF !important;
  }
@media screen and (min-width: 1200px) {
  #app {
    margin-top: 80px;
    padding: 0 4%;
  }
}
@media screen and (max-width: 1200px) {
  #app {
    margin-top: 210px;
    padding: 0 4%;
  }
}
@media screen and (max-width: 768px) {
  #app {
    margin-top: 210px;
    padding: 0 0;
  }
  .markdown-body img{
    max-width: 100%;
  }
}
@media screen and (max-width: 400px) {
  #app {
    margin-top: 270px;
    padding: 0 0;
  }
  .markdown-body img{
    width: 100%;
  }
}
  #problem-content .sample pre {
    -ms-flex: 1 1 auto;
    flex: 1 1 auto;
    -ms-flex-item-align: stretch;
    align-self: stretch;
    border-style: solid;
    background: #fafafa;
    border-left: 2px solid #3498db;
}

.markdown-body pre {
    padding: 5px 10px;
    white-space: pre-wrap;
    margin-top: 15px;
    margin-bottom: 15px;
    background: #f8f8f9;
    border: 1px dashed #e9eaec;
}
  .footer {
    margin-top: 20px;
    margin-bottom: 10px;
    text-align: center;
    font-size: small;
  }

  .fadeInUp-enter-active {
    animation: fadeInUp .8s;
  }
  .el-menu--popup {
    min-width: 120px!important;
    text-align: center;
  }


</style>
