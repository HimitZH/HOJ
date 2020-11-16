<template>
  <div>
    <div id="header">
      <el-menu
        :default-active="activeIndex"
        mode="horizontal"
        router
        active-text-color="#2196f3"
        text-color="#495060"
        
      >
        <div class="logo">
          <el-image
            style="width: 139px; height: 50px"
            :src="imgUrl"
            fit="scale-down"
          ></el-image>
        </div>
        <el-menu-item index="/home"
          ><i class="el-icon-s-home"></i>Home</el-menu-item
        >
        <el-menu-item index="/problem"
          ><i class="el-icon-s-grid"></i>Problem</el-menu-item
        >
        <el-menu-item index="/contest"
          ><i class="el-icon-trophy"></i>Contest</el-menu-item
        >
        <el-menu-item index="/status"
          ><i class="el-icon-s-marketing"></i>Status</el-menu-item
        >
        <el-submenu index="rank" >
          <template slot="title"><i class="el-icon-s-data"></i>Rank</template>
          <el-menu-item index="/acm-rank">ACM Rank</el-menu-item>
          <el-menu-item index="/oi-rank">OI Rank</el-menu-item>
        </el-submenu>

        <el-submenu index="about">
          <template slot="title"><i class="el-icon-info"></i>About</template>
          <el-menu-item index="/introduction">Introduction</el-menu-item>
          <el-menu-item index="/developer">Developer</el-menu-item>
        </el-submenu>

        <template v-if="!isAuthenticated">
          <div class="btn-menu">
            <el-button type="primary" round @click="handleBtnClick('Login')"
              >Login
            </el-button>
            <el-button
              round
              type="danger"
              @click="handleBtnClick('Register')"
              style="margin-left: 5px"
              >Register
            </el-button>
          </div>
        </template>
        <template v-else>
          <el-dropdown
            class="drop-menu"
            @command="handleRoute"
            placement="bottom"
            trigger="click"
           
          >
          <span class="el-dropdown-link">
        {{userInfo.username}}<i class="el-icon-caret-bottom"></i>
      </span>
            
            

            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="/user-home">Home</el-dropdown-item>
              <el-dropdown-item command="/status?myself=1">Submissions</el-dropdown-item>
              <el-dropdown-item command="/setting">Setting</el-dropdown-item>
              <el-dropdown-item v-if="isAdminRole" command="/admin">Management</el-dropdown-item>
              <el-dropdown-item divided command="/logout">Logout</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-menu>
    </div>
    <el-dialog
      :visible.sync="modalVisible"
      width="400px"
      class="dialog"
      :title="title"
    >
      <component :is="modalStatus.mode" v-if="modalVisible"></component>
      <div slot="footer" style="display: none"></div>
    </el-dialog>
  </div>
</template>
<script>
import Login from "@/components/common/Login";
import Register from "@/components/common/Register";
import ResetPwd from "@/components/common/ResetPassword";
import { mapGetters, mapActions } from "vuex";
import api from "@/common/api";
export default {
  components: {
    Login,
    Register,
    ResetPwd,
  },
  mounted() {
    let activeName = this.$route.path.split("/")[1];
    if(activeName === ''){
      this.activeIndex = '/home'
    }else{
      this.activeIndex = '/'+activeName
    }
  },
  data() {
    return {
      activeIndex: "home",
      centerDialogVisible: false,
      imgUrl: require("@/assets/logo.png"),
    };
  },
  methods: {
    ...mapActions(["changeModalStatus"]),
    handleBtnClick(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    handleRoute (route) {
        if (route && route.indexOf('admin') < 0) {
          this.$router.push(route)
        } else {
          window.open('/admin/')
        }
      },
  },
  computed: {
    ...mapGetters(["modalStatus", "userInfo", "isAuthenticated", "isAdminRole","token"]),
    modalVisible: {
      get() {
        return this.modalStatus.visible;
      },
      set(value) {
        this.changeModalStatus({ visible: value });
      },
    },
    title: {
      get() {
        if (this.modalStatus.mode == "ResetPwd") {
          return "Reset Password - HOJ";
        } else {
          return this.modalStatus.mode + " - HOJ";
        }
      },
    },
  },
};
</script>
<style scoped>
#header {
  min-width: 300px;
  position: fixed;
  top: 0;
  left: 0;
  height: auto;
  width: 100%;
  z-index: 1000;
  background-color: #fff;
  box-shadow: 0 1px 5px 0 rgba(0, 0, 0, 0.1);
}
.logo {
  margin-left: 2%;
  margin-right: 2%;
  float: left;
  width: 139px;
  height: 42px;
  margin-top: 5px;
}
.el-dropdown-link {
    cursor: pointer;
    color: #409EFF;
  }
.el-icon-arrow-down {
    font-size: 18px;
}
.drop-menu {
  float: right;
  margin-right: 30px;
  position: relative;
  font-weight: 500;
  right: 10px;
  margin-top: 18px;
  font-size: 18px;
}
.btn-menu {
  font-size: 16px;
  float: right;
  margin-right: 10px;
  margin-top: 10px;
}
/deep/ .el-dialog {
  border-radius: 10px !important;
  text-align: center;
}
/deep/ .el-dialog__header .el-dialog__title {
  font-size: 22px;
  font-weight: 600;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1em;
  color: #4e4e4e;
}
  .el-submenu__title i {
    color: #495060!important;
  }
  .el-menu-item i{
    color: #495060;
  }
   .is-active .el-submenu__title i,.is-active{
    color:#2196f3!important;
  }

</style>