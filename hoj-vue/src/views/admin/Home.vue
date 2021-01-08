<template>
  <div class="admin-container">
    <div v-if="!mobileNar">
      <el-menu
        class="vertical_menu"
        :router="true"
        :default-active="currentPath"
      >
        <div class="logo">
          <img src="@/assets/backstage.png" alt="oj admin" />
        </div>
        <el-menu-item index="/admin/">
          <i class="fa fa-tachometer" aria-hidden="true"></i>Dashboard
        </el-menu-item>
        <!-- <el-submenu v-if="isSuperAdmin" index="general"> -->
        <el-submenu index="general" v-if="isSuperAdmin">
          <template slot="title"><i class="el-icon-menu"></i>General</template>
          <el-menu-item index="/admin/user">User</el-menu-item>
          <el-menu-item index="/admin/announcement">Announcement</el-menu-item>
          <el-menu-item index="/admin/conf">System Config</el-menu-item>
        </el-submenu>
        <!-- <el-submenu index="problem" v-if="hasProblemPermission"> -->
        <el-submenu index="problem">
          <template slot="title"
            ><i class="fa fa-bars" aria-hidden="true"></i>Problem</template
          >
          <el-menu-item index="/admin/problems">Problem List</el-menu-item>
          <el-menu-item index="/admin/problem/create"
            >Create Problem</el-menu-item
          >
          <el-menu-item index="/admin/problem/batch-operation"
            >Export&Import Problem</el-menu-item
          >
        </el-submenu>
        <el-submenu index="contest">
          <template slot="title"
            ><i class="fa fa-trophy" aria-hidden="true"></i>Contest</template
          >
          <el-menu-item index="/admin/contest">Contest List</el-menu-item>
          <el-menu-item index="/admin/contest/create"
            >Create Contest</el-menu-item
          >
        </el-submenu>
      </el-menu>
      <div id="header">
        <el-row>
          <el-col :span="20">
            <div class="breadcrumb-container">
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/admin/' }"
                  >Home page</el-breadcrumb-item
                >
                <el-breadcrumb-item v-for="item in routeList" :key="item.path">
                  {{ item.meta.title }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
          </el-col>
          <el-col :span="4" v-show="isAuthenticated">
            <i class="fa fa-font katex-editor" @click="katexVisible = true"></i>
            <avatar :username="userInfo.username" :inline="true" :size="30" color="#FFF" :src="userInfo.avatar" class="drop-avatar"></avatar>
            <el-dropdown @command="handleCommand" style="vertical-align: middle;">
              <span
                >{{ userInfo.username
                }}<i class="el-icon-caret-bottom el-icon--right"></i
              ></span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="logout">Logout</el-dropdown-item>
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
        HOJ Admin
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
                <mu-list-item-title>Logout</mu-list-item-title>
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
              <mu-icon value="dashboard" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>Dashboard</mu-list-item-title>
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
              <mu-icon value="view_list"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>General</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value="keyboard_arrow_down"
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
              <mu-list-item-title>User</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/announcement"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>Announcement</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/conf"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>System Config</mu-list-item-title>
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
              <mu-icon value="menu"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>Problem</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value="keyboard_arrow_down"
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
              <mu-list-item-title>Problem List</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/problem/create"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>Create Problem</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/problem/batch-operation"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>Export&Import Problem</mu-list-item-title>
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
              <mu-icon value="assessment"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>Contest</mu-list-item-title>
            <mu-list-item-action>
              <mu-icon
                class="toggle-icon"
                size="24"
                value="keyboard_arrow_down"
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
              <mu-list-item-title>Contest List</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/admin/contest/create"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>Create Contest</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>
        </mu-list>
      </mu-drawer>
    </div>
    <div class="content-app">
      <transition name="fadeInUp" mode="out-in">
        <router-view></router-view>
      </transition>
    </div>

    <el-dialog title="Latex Editor" :visible.sync="katexVisible" width="350px">
      <KatexEditor></KatexEditor>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import KatexEditor from "@/components/admin/KatexEditor.vue";
import api from "@/common/api";
import mMessage from '@/common/message'
import Avatar from 'vue-avatar'
export default {
  name: "app",
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
      openSideMenu: "",
      katexVisible: false,
      opendrawer: false,
      mobileNar: false,
      currentPath: "",
      routeList: [],
    };
  },
  components: {
    KatexEditor,
    Avatar
  },
  methods: {
    handleCommand(command) {
      if (command === "logout") {
        api.admin_logout().then((res) => {
          this.$router.push({ path: "/admin/login" });
          mMessage.success(res.data.msg)
          this.$store.commit('clearUserInfoAndToken');
        });
      }
    },
    page_width() {
      let screenWidth = window.screen.width;
      if (screenWidth < 1080) {
        this.mobileNar = true;
      } else {
        this.mobileNar = false;
      }
    },
    getBreadcrumb() {
      let matched = this.$route.matched.filter((item) => item.meta.title); //获取路由信息，并过滤保留路由标题信息存入数组
      this.routeList = matched;
    },
  },
  computed: {
    ...mapGetters(["userInfo", "isSuperAdmin","isAuthenticated"]),
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
  width: 205px;
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
}
.vertical_menu .logo img {
  background-color: #fff;
  border: 3px solid #fff;
  width: 110px;
  height: 110px;
}
.fa {
  margin-right: 5px;
  width: 24px;
  text-align: center;
  font-size: 18px;
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
  background-color: #edecec;
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
  padding-left: 210px;
  padding-right: 30px;
  line-height: 50px;
  height: 50px;
  background: #f9fafc;
}

@media screen and (min-width: 1080px) {
  .content-app {
    padding-top: 20px;
    padding-right: 10px;
    padding-left: 210px;
  }
}
@media screen and (max-width: 1080px) {
  .content-app {
    padding: 0 5px;
    margin-top: 20px;
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
  /*font-size: 18px;*/
  margin-right: 10px;
}
.drop-avatar{
  vertical-align: middle;
  margin-right: 10px;
}
</style>
