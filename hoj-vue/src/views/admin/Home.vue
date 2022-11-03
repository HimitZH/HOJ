<template>
  <div class="admin-container">
    <div v-if="!mobileNar">
      <el-menu
        class="vertical_menu"
        :router="true"
        :default-active="currentPath"
      >
        <el-tooltip
            :content="$t('m.Click_To_Change_Web_Language')"
            placement="bottom"
            effect="dark"
          >
          <div class="logo" @click="changeWebLanguage(webLanguage == 'zh-CN' ? 'en-US' : 'zh-CN')">
            <img :src="imgUrl" alt="Online Judge Admin" />
          </div>
        </el-tooltip>
        <el-menu-item index="/admin/">
          <i class="fa fa-tachometer fa-size" aria-hidden="true"></i
          >{{ $t('m.Dashboard') }}
        </el-menu-item>
        <!-- <el-submenu v-if="isSuperAdmin" index="general"> -->
        <el-submenu index="general" v-if="isSuperAdmin">
          <template slot="title"
            ><i class="el-icon-menu"></i>{{ $t('m.General') }}</template
          >
          <el-menu-item index="/admin/user">{{
            $t('m.User_Admin')
          }}</el-menu-item>
          <el-menu-item index="/admin/announcement">{{
            $t('m.Announcement_Admin')
          }}</el-menu-item>
          <el-menu-item index="/admin/notice">{{
            $t('m.SysNotice')
          }}</el-menu-item>
          <el-menu-item index="/admin/conf">{{
            $t('m.System_Config')
          }}</el-menu-item>
          <el-menu-item index="/admin/switch">{{
            $t('m.System_Switch')
          }}</el-menu-item>
        </el-submenu>
        <!-- <el-submenu index="problem" v-if="hasProblemPermission"> -->
        <el-submenu index="problem">
          <template slot="title"
            ><i class="fa fa-bars fa-size" aria-hidden="true"></i
            >{{ $t('m.Problem_Admin') }}</template
          >
          <el-menu-item index="/admin/problems">{{
            $t('m.Problem_List')
          }}</el-menu-item>
          <el-menu-item index="/admin/problem/create">{{
            $t('m.Create_Problem')
          }}</el-menu-item>
          <el-menu-item index="/admin/problem/tag">{{
            $t('m.Admin_Tag')
          }}</el-menu-item>
           <el-menu-item index="/admin/group-problem/apply"
           v-if="isSuperAdmin || isProblemAdmin"
           >{{$t('m.Admin_Group_Apply_Problem')}}
           </el-menu-item>
          <el-menu-item
            index="/admin/problem/batch-operation"
            v-if="isSuperAdmin || isProblemAdmin"
            >{{ $t('m.Export_Import_Problem') }}</el-menu-item
          >
        </el-submenu>

        <el-submenu index="training">
          <template slot="title"
            ><i
              class="el-icon-s-claim"
              aria-hidden="true"
              style="font-size: 20px;"
            ></i
            >{{ $t('m.Training_Admin') }}</template
          >
          <el-menu-item index="/admin/training">{{
            $t('m.Training_List')
          }}</el-menu-item>
          <el-menu-item index="/admin/training/create">{{
            $t('m.Create_Training')
          }}</el-menu-item>
          <el-menu-item index="/admin/training/category">{{
            $t('m.Admin_Category')
          }}</el-menu-item>
        </el-submenu>

        <el-submenu index="contest">
          <template slot="title"
            ><i class="fa fa-trophy fa-size" aria-hidden="true"></i
            >{{ $t('m.Contest_Admin') }}</template
          >
          <el-menu-item index="/admin/contest">{{
            $t('m.Contest_List')
          }}</el-menu-item>
          <el-menu-item index="/admin/contest/create">{{
            $t('m.Create_Contest')
          }}</el-menu-item>
        </el-submenu>

        <el-submenu index="discussion">
          <template slot="title"
            ><i class="fa fa-comments fa-size" aria-hidden="true"></i
            >{{ $t('m.Discussion') }}</template
          >
          <el-menu-item index="/admin/discussion">{{
            $t('m.Discussion_Admin')
          }}</el-menu-item>
        </el-submenu>
      </el-menu>
      <div id="header">
        <el-row>
          <el-col :span="18">
            <div class="breadcrumb-container">
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/admin/' }">{{
                  $t('m.Home_Page')
                }}</el-breadcrumb-item>
                <el-breadcrumb-item v-for="item in routeList" :key="item.path">
                  {{ $t('m.' + item.meta.title.replaceAll(' ', '_')) }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
          </el-col>
          <el-col :span="6" v-show="isAuthenticated">
            <i
              class="fa fa-font katex-editor fa-size"
              @click="katexVisible = true"
            ></i>
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="userInfo.avatar"
              class="drop-avatar"
            ></avatar>
            <el-dropdown
              @command="handleCommand"
              style="vertical-align: middle;"
            >
              <span
                >{{ userInfo.username
                }}<i class="el-icon-caret-bottom el-icon--right"></i
              ></span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="logout">{{
                  $t('m.Logout')
                }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-col>
        </el-row>
      </div>
    </div>

    <div v-else>
      <mu-appbar class="mobile-nav" color="primary">
        <mu-button icon slot="left" @click="opendrawer = !opendrawer">
          <i class="el-icon-s-unfold"></i>
        </mu-button>
        {{
          websiteConfig.shortName
            ? websiteConfig.shortName + ' ADMIN'
            : 'ADMIN'
        }}
        <mu-menu slot="right" v-show="isAuthenticated">
          <mu-button flat @click="katexVisible = true">
            <i class="fa fa-font katex-editor"></i>
          </mu-button>
        </mu-menu>
        <mu-menu
          slot="right"
          v-show="isAuthenticated"
          :open.sync="openusermenu"
        >
          <mu-button flat>
            {{ userInfo.username }}<i class="el-icon-caret-bottom"></i>
          </mu-button>
          <mu-list slot="content" @change="handleCommand">
            <mu-list-item button value="logout">
              <mu-list-item-content>
                <mu-list-item-title>{{ $t('m.Logout') }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
          </mu-list>
        </mu-menu>
      </mu-appbar>

      <mu-drawer :open.sync="opendrawer" :docked="false" :right="false">
        <mu-list toggle-nested>
          <mu-list-item
            button
            :ripple="true"
            nested
            to="/admin/dashboard"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-tachometer" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Dashboard') }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            v-if="isSuperAdmin"
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'general'"
            @toggle-nested="openSideMenu = arguments[0] ? 'general' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-menu" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.General') }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/user"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{ $t('m.User_Admin') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/announcement"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Announcement_Admin')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/notice"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{ $t('m.SysNotice') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/conf"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.System_Config')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/switch"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.System_Switch')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'problem'"
            @toggle-nested="openSideMenu = arguments[0] ? 'problem' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-bars" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Problem_Admin') }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/problems"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Problem_List')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/problem/create"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Create_Problem')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              v-if="isSuperAdmin || isProblemAdmin"
              button
              :ripple="false"
              slot="nested"
              to="/admin/problem/tag"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{ $t('m.Admin_Tag') }}</mu-list-item-title>
            </mu-list-item>

            <mu-list-item
              v-if="isSuperAdmin || isProblemAdmin"
              button
              :ripple="false"
              slot="nested"
              to="/admin/group-problem/apply"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{ $t('m.Admin_Group_Apply_Problem') }}</mu-list-item-title>
            </mu-list-item>

            <mu-list-item
              v-if="isSuperAdmin"
              button
              :ripple="false"
              slot="nested"
              to="/admin/problem/batch-operation"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Export_Import_Problem')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'training'"
            @toggle-nested="openSideMenu = arguments[0] ? 'training' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-claim fa-size" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.Training_Admin')
            }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/training"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Training_List')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/training/create"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Create_Training')
              }}</mu-list-item-title>
            </mu-list-item>

            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/training/category"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Admin_Category')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'contest'"
            @toggle-nested="openSideMenu = arguments[0] ? 'contest' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-trophy fa-size" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Contest_Admin') }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/contest"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Contest_List')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/contest/create"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Create_Contest')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'discussion'"
            @toggle-nested="openSideMenu = arguments[0] ? 'discussion' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-comments fa-size" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Discussion') }}</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value=":el-icon-arrow-down"
              ></mu-icon>
            </mu-list-item-action>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/discussion"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.Discussion_Admin')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>
        </mu-list>
      </mu-drawer>
    </div>
    <div class="content-app">
      <transition name="fadeInUp" mode="out-in">
        <router-view></router-view>
      </transition>
      <div class="footer">
        Powered by
        <a
          :href="websiteConfig.projectUrl"
          style="color:#1E9FFF"
          target="_blank"
          >{{ websiteConfig.projectName }}</a
        >
        <span style="margin-left:10px">
          <el-dropdown @command="changeWebLanguage" placement="top">
            <span class="el-dropdown-link" style="font-size:14px">
              <i class="fa fa-globe" aria-hidden="true">
                {{ this.webLanguage == 'zh-CN' ? '简体中文' : 'English' }}</i
              ><i class="el-icon-arrow-up el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
              <el-dropdown-item command="en-US">English</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
      </div>
    </div>

    <el-dialog title="Latex Editor" :visible.sync="katexVisible" width="350px">
      <KatexEditor></KatexEditor>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
const KatexEditor = () => import('@/components/admin/KatexEditor.vue');
import api from '@/common/api';
import mMessage from '@/common/message';
import Avatar from 'vue-avatar';
export default {
  name: 'app',
  mounted() {
    this.currentPath = this.$route.path;
    this.getBreadcrumb();
    window.onresize = () => {
      this.page_width();
    };
    this.page_width();
  },
  data() {
    return {
      openusermenu: false,
      openSideMenu: '',
      katexVisible: false,
      opendrawer: false,
      mobileNar: false,
      currentPath: '',
      routeList: [],
      imgUrl: require('@/assets/backstage.png'),
    };
  },
  components: {
    KatexEditor,
    Avatar,
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        api.admin_logout().then((res) => {
          this.$router.push({ path: '/admin/login' });
          mMessage.success(this.$i18n.t('m.Log_Out_Successfully'));
          this.$store.commit('clearUserInfoAndToken');
        });
      }
    },
    page_width() {
      let screenWidth = window.screen.width;
      if (screenWidth < 992) {
        this.mobileNar = true;
      } else {
        this.mobileNar = false;
      }
    },
    getBreadcrumb() {
      let matched = this.$route.matched.filter((item) => item.meta.title); //获取路由信息，并过滤保留路由标题信息存入数组
      this.routeList = matched;
    },
    changeWebLanguage(language) {
      this.$store.commit('changeWebLanguage', { language: language });
    },
  },
  computed: {
    ...mapGetters([
      'userInfo',
      'isSuperAdmin',
      'isProblemAdmin',
      'isAuthenticated',
      'websiteConfig',
      'webLanguage',
    ]),
    'window.screen.width'(newVal, oldVal) {
      if (newVal < 992) {
        this.mobileNar = true;
      } else {
        this.mobileNar = false;
      }
    },
  },
  watch: {
    $route() {
      this.getBreadcrumb(); //监听路由变化
    },
  },
};
</script>

<style scoped>
.vertical_menu {
  overflow: auto;
  width: 15%;
  height: 100%;
  position: fixed !important;
  z-index: 100;
  top: 0;
  bottom: 0;
  left: 0;
}
.vertical_menu .logo {
  margin: 20px 0;
  text-align: center;
  cursor: pointer;
}
.vertical_menu .logo img {
  background-color: #fff;
  border: 3px solid #fff;
  width: 110px;
  height: 110px;
}
.fa-size {
  text-align: center;
  font-size: 18px;
  vertical-align: middle;
  margin-right: 5px;
  width: 24px;
}
a {
  background-color: transparent;
}

a:active,
a:hover {
  outline-width: 0;
}

img {
  border-style: none;
}

.admin-container {
  overflow: auto;
  font-weight: 400;
  height: 100%;
  -webkit-font-smoothing: antialiased;
  background-color: #eff3f5;
  overflow-y: auto;
}
.breadcrumb-container {
  padding: 17px;
  background-color: #fff;
}
* {
  box-sizing: border-box;
}

#header {
  text-align: right;
  margin-left: 15%;
  padding-right: 30px;
  line-height: 50px;
  height: 50px;
  background: #f9fafc;
}
.footer {
  margin: 15px;
  text-align: center;
  font-size: small;
}
@media screen and (max-width: 992px) {
  .content-app {
    padding: 0 5px;
    margin-top: 20px;
  }
}
@media screen and (min-width: 992px) {
  .content-app {
    margin-top: 10px;
    margin-right: 10px;
    margin-left: calc(20% + 10px);;
  }
  .vertical_menu {
    width: 20%;
  }
  #header {
    margin-left: 20%;
  }
}
@media screen and (min-width: 1150px) {
  .content-app {
    margin-top: 10px;
    margin-right: 10px;
    margin-left: 220px;
  }
  .vertical_menu {
    width: 210px;
  }
  #header {
    margin-left: 210px;
  }
}


@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translate(0, 30px);
  }

  to {
    opacity: 1;
    transform: none;
  }
}

.fadeInUp-enter-active {
  animation: fadeInUp 0.8s;
}

.katex-editor {
  margin-right: 5px;
  cursor: pointer;
  vertical-align: middle;
  margin-right: 10px;
}
.drop-avatar {
  vertical-align: middle;
  margin-right: 10px;
}
</style>
