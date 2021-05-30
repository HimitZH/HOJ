<template>
  <div id="app">
    <el-backtop :right="10"></el-backtop>
    <div v-if="!isAdminView">
      <NavBar></NavBar>
      <div id="oj-content">
        <transition name="el-zoom-in-bottom">
          <router-view></router-view>
        </transition>
      </div>
      <footer>
        <div class="mundb-footer">
          <el-row>
            <el-col :md="6" :xs="24">
              <h1>{{ toUpper(websiteConfig.shortName) }}</h1>
              <p style="line-height:25px">
                {{ websiteConfig.description }}
              </p>
            </el-col>
            <el-col class="hr-none">
              <el-divider></el-divider>
            </el-col>
            <el-col :md="6" :xs="24">
              <h1>Service</h1>
              <p><a @click="goRoute('/status')">Judging Queue</a></p>
              <p><a @click="goRoute('/developer')">System Info</a></p>
            </el-col>
            <el-col class="hr-none">
              <el-divider></el-divider>
            </el-col>
            <el-col :md="6" :xs="24">
              <h1>Development</h1>
              <p class="mb-1">
                <a :href="websiteConfig.projectUrl" target="_blank"
                  >Open Source</a
                >
              </p>
              <p class="mb-1"><a @click="goRoute('/#')">API</a></p>
            </el-col>
            <el-col class="hr-none">
              <el-divider></el-divider>
            </el-col>
            <el-col :md="6" :xs="24">
              <h1>Support</h1>
              <p>
                <i class="fa fa-info-circle" aria-hidden="true"></i
                ><a @click="goRoute('/introduction')"> Help</a>
              </p>
              <p>
                <i class="fa fa-envelope" aria-hidden="true"></i>
                {{ websiteConfig.email }}
              </p>
            </el-col>
          </el-row>
        </div>
        <div class="mundb-footer">
          <a style="color:#1E9FFF" :href="websiteConfig.recordUrl"
            >© 2020-2021</a
          >
          Powered by
          <a
            :href="websiteConfig.projectUrl"
            style="color:#1E9FFF"
            target="_blank"
            >{{ websiteConfig.projectName }}</a
          >
        </div>
      </footer>
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
    goRoute(path) {
      this.$router.push({
        path: path,
      });
    },
    toUpper(str) {
      if (str) {
        return str.toUpperCase();
      }
    },
  },
  watch: {
    $route(newVal, oldVal) {
      this.changeDomTitle();
      if (newVal !== oldVal && newVal.path.split('/')[1] == 'admin') {
        this.isAdminView = true;
      } else {
        this.isAdminView = false;
      }
    },
    websiteConfig() {
      this.changeDomTitle();
    },
  },
  computed: {
    ...mapState(['websiteConfig']),
  },
  created: function() {
    this.$nextTick(function() {
      try {
        document.body.removeChild(document.getElementById('app-loader'));
      } catch (e) {}
    });

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
}
body {
  background-color: #eff3f5 !important;
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
  .el-message-box {
    width: 70% !important;
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

.tex-formula {
  font-family: times new roman, sans-serif;
  vertical-align: middle;
  margin: 0;
  border: medium none;
  position: relative;
  bottom: 2px;
}

.tex-span {
  font-size: 125%;
  font-family: times new roman, sans-serif;
  white-space: nowrap;
}

.tex-font-size-tiny {
  font-size: 70%;
}

.tex-font-size-script {
  font-size: 75%;
}

.tex-font-size-footnotes {
  font-size: 85%;
}

.tex-font-size-small {
  font-size: 85%;
}

.tex-font-size-normal {
  font-size: 100%;
}

.tex-font-size-large-1 {
  font-size: 115%;
}

.tex-font-size-large-2 {
  font-size: 130%;
}

.tex-font-size-large-3 {
  font-size: 145%;
}

.tex-font-size-huge-1 {
  font-size: 175%;
}

.tex-font-size-huge-2 {
  font-size: 200%;
}

.tex-font-style-sf {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.tex-font-style-tt {
  font-size: 110%;
  font-family: courier new, monospace;
}

.tex-font-style-bf {
  font-weight: bold;
}

.tex-font-style-it {
  font-style: italic;
}

.tex-font-style-sl {
  font-style: italic;
}

.tex-font-style-sc {
  text-transform: uppercase;
}

.tex-font-style-striked {
  text-decoration: line-through;
}

.tex-font-style-underline {
  text-decoration: underline;
}

.tex-graphics {
  display: block;
}

footer {
  margin-top: 2rem;
  color: #555 !important;
  background-color: #fff;
  text-align: center;
}
footer a {
  color: #555;
}
footer a:hover {
  color: #409eff;
  text-decoration: none;
}
footer h1 {
  font-family: -apple-system, BlinkMacSystemFont, Segoe UI, PingFang SC,
    Hiragino Sans GB, Microsoft YaHei, Helvetica Neue, Helvetica, Arial,
    sans-serif, Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol;
  font-weight: 300;
  color: #3d3d3d;
  line-height: 1.1;
  font-size: 1.5rem;
}

.mundb-footer {
  padding: 1rem 2.5rem;
  width: 100%;
  font-weight: 400;
  font-size: 1rem;
  line-height: 1;
}
@media (min-width: 768px) {
  .hr-none {
    display: none !important;
  }
}
</style>
