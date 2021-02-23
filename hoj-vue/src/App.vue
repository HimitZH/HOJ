<template>
  <div>
    <el-backtop :right="10"></el-backtop>
    <div v-if="!isAdminView">
      <NavBar></NavBar>
      <div id="oj-content">
        <transition name="el-zoom-in-bottom">
          <router-view></router-view>
        </transition>
      </div>
      <div class="footer">
        <a style="color:#1E9FFF" :href="websiteConfig.recordUrl">{{
          websiteConfig.recordName
        }}</a>
        <p>
          Powered by
          <a :href="websiteConfig.projectUrl" style="color:#1E9FFF">{{
            websiteConfig.projectName
          }}</a>
        </p>
      </div>
    </div>
    <div v-else>
      <div id="admin-content">
        <transition name="el-zoom-in-bottom">
          <router-view></router-view>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from '@/components/oj/common/NavBar';
import { mapActions, mapState } from 'vuex';
export default {
  name: 'app-content',
  components: {
    NavBar,
  },
  data() {
    return {
      isAdminView: false,
    };
  },
  methods: {
    ...mapActions(['changeDomTitle', 'getWebsiteConfig']),
  },
  watch: {
    $route() {
      this.changeDomTitle();
    },
    websiteConfig() {
      this.changeDomTitle();
    },
  },
  computed: {
    ...mapState(['websiteConfig']),
  },
  created: function() {
    try {
      document.body.removeChild(document.getElementById('app-loader'));
    } catch (e) {
      console.log(e);
    }
    if (this.$route.path.split('/')[1] != 'admin') {
      this.isAdminView = false;
    } else {
      this.isAdminView = true;
    }
  },
  mounted() {
    this.getWebsiteConfig();
  },
};
</script>

<style>
* {
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  touch-action: none !important;
  -ms-touch-action: none;
}
body {
  background-color: #eee !important;
  font-family: Helvetica Neue, Helvetica, PingFang SC, Hiragino Sans GB,
    Microsoft YaHei, Arial, sans-serif !important;
  -webkit-font-smoothing: antialiased !important;
  color: #495060 !important;
  font-size: 12px !important;
}
code,
kbd,
pre,
samp {
  font-family: Consolas, Menlo, Courier, monospace;
}
#admin-content {
  background-color: #1e9fff;
  position: absolute;
  top: 0;
  bottom: 0;
  width: 100%;
}
.mobile-menu-active {
  background-color: rgba(0, 0, 0, 0.1);
}
.mobile-menu-active .mu-item-title {
  color: #2d8cf0 !important;
}
.mobile-menu-active .mu-icon {
  color: #2d8cf0 !important;
}
#particles-js {
  position: fixed;
  z-index: 0;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}
a {
  text-decoration: none;
  background-color: transparent;
  color: #495060;
  outline: 0;
  cursor: pointer;
  transition: color 0.2s ease;
}
a:hover {
  color: #2196f3 !important;
}
.markdown-body a {
  color: #2196f3;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.28s ease;
  -moz-transition: all 0.28s ease;
  -webkit-transition: all 0.28s ease;
  -o-transition: all 0.28s ease;
}
.markdown-body a:hover {
  color: #ff5722 !important;
  text-decoration: underline;
}
.panel-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
}

.home-title {
  color: #409eff;
}
.oi-100,
.first-ac {
  background-color: #080;
  color: #fff;
  font-weight: 700;
}
.oi-between {
  background-color: #2d8cf0;
  color: #fff;
}
.ac {
  background-color: #a9f5af;
  color: #3c763d;
}
.oi-0,
.wa {
  color: #a94442;
  background-color: #f2dede;
}
.status-green {
  background-color: #19be6b !important;
  color: #fff !important;
}
.status-red {
  background-color: #ed3f14 !important;
  color: #fff !important;
}
.status-yellow {
  background-color: #f90 !important;
  color: #fff !important;
}
.status-blue {
  background-color: #2d8cf0 !important;
  color: #fff !important;
}
.status-gray {
  background-color: #909399 !important;
  color: #fff !important;
}
.own-submit-row {
  background: rgb(230, 255, 223) !important;
}
.vxe-table {
  color: #495060 !important;
  font-size: 12px !important;
  font-weight: 500 !important;
}
.row--hover {
  cursor: pointer;
  background-color: #ebf7ff !important;
}
.vxe-table .vxe-body--column:not(.col--ellipsis),
.vxe-table .vxe-footer--column:not(.col--ellipsis),
.vxe-table .vxe-header--column:not(.col--ellipsis) {
  padding: 9px 0 !important;
}
#nprogress .bar {
  background: #66b1ff !important;
}
@media screen and (min-width: 1080px) {
  #oj-content {
    margin-top: 80px;
    padding: 0 4%;
  }
  .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
.markdown-body img {
  max-width: 100%;
}
.contest-description img {
  max-width: 100%;
}
@media screen and (max-width: 1080px) {
  #oj-content {
    margin-top: 20px;
    padding: 0 5px;
  }
  .el-row {
    margin-left: 0px !important;
    margin-right: 0px !important;
  }
  .el-col {
    padding-left: 0px !important;
    padding-right: 0px !important;
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
.el-menu--popup {
  min-width: 120px !important;
  text-align: center;
}
.panel-options {
  margin-top: 10px;
  text-align: center;
}
.el-tag--dark {
  border-color: #fff !important;
}
.v-note-wrapper .v-note-panel {
  height: 460px !important;
}
</style>
