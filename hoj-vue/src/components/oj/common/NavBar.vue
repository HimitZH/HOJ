<template>
  <div>
    <template v-if="!mobileNar">
      <div id="header">
        <el-menu
          :default-active="activeMenuName"
          mode="horizontal"
          router
          active-text-color="#2196f3"
          text-color="#495060"
        >
          <div class="logo">
            <el-tooltip
              :content="$t('m.Click_To_Change_Web_Language')"
              placement="bottom"
              effect="dark"
            >
              <el-image
                style="width: 139px; height: 50px"
                :src="imgUrl"
                fit="scale-down"
                @click="changeWebLanguage"
              ></el-image>
            </el-tooltip>
          </div>
          <el-menu-item index="/home"
            ><i class="el-icon-s-home"></i>{{ $t('m.NavBar_Home') }}</el-menu-item
          >
          <el-menu-item index="/problem"
            ><i class="el-icon-s-grid"></i
            >{{ $t('m.NavBar_Problem') }}</el-menu-item
          >
          <el-menu-item index="/training"
            ><i class="el-icon-s-claim"></i
            >{{ $t('m.NavBar_Training') }}</el-menu-item
          >
          <el-menu-item index="/contest"
            ><i class="el-icon-trophy"></i
            >{{ $t('m.NavBar_Contest') }}</el-menu-item
          >
          <el-menu-item index="/status"
            ><i class="el-icon-s-marketing"></i
            >{{ $t('m.NavBar_Status') }}</el-menu-item
          >
          <el-submenu index="rank">
            <template slot="title"
              ><i class="el-icon-s-data"></i>{{ $t('m.NavBar_Rank') }}</template
            >
            <el-menu-item index="/acm-rank">{{
              $t('m.NavBar_ACM_Rank')
            }}</el-menu-item>
            <el-menu-item index="/oi-rank">{{
              $t('m.NavBar_OI_Rank')
            }}</el-menu-item>
          </el-submenu>

          <el-menu-item index="/discussion"
            v-if="websiteConfig.openPublicDiscussion"
            ><i class="el-icon-s-comment"></i
            >{{ $t('m.NavBar_Discussion') }}</el-menu-item
          >

          <el-menu-item index="/group"
            ><i
              class="fa fa-users"
              style="margin-right: 5px;width: 24px;text-align: center;"
            ></i
            >{{ $t('m.NavBar_Group') }}</el-menu-item
          >

          <el-submenu index="about">
            <template slot="title"
              ><i class="el-icon-info"></i>{{ $t('m.NavBar_About') }}</template
            >
            <el-menu-item index="/introduction">{{
              $t('m.NavBar_Introduction')
            }}</el-menu-item>
            <el-menu-item index="/developer">{{
              $t('m.NavBar_Developer')
            }}</el-menu-item>
          </el-submenu>

          <template v-if="!isAuthenticated">
            <div class="btn-menu">
              <el-button type="primary" round @click="handleBtnClick('Login')"
                >{{ $t('m.NavBar_Login') }}
              </el-button>
              <el-button
                v-if="websiteConfig.register"
                round
                type="danger"
                @click="handleBtnClick('Register')"
                style="margin-left: 5px"
                >{{ $t('m.NavBar_Register') }}
              </el-button>
            </div>
          </template>
          <template v-else>
            <el-dropdown
              class="drop-menu"
              @command="handleRoute"
              placement="bottom"
              trigger="hover"
            >
              <span class="el-dropdown-link">
                {{ userInfo.username }}<i class="el-icon-caret-bottom"></i>
              </span>

              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="/user-home">{{
                  $t('m.NavBar_UserHome')
                }}</el-dropdown-item>
                <el-dropdown-item command="/status?onlyMine=true">{{
                  $t('m.NavBar_Submissions')
                }}</el-dropdown-item>
                <el-dropdown-item command="/setting">{{
                  $t('m.NavBar_Setting')
                }}</el-dropdown-item>
                <el-dropdown-item v-if="isAdminRole" command="/admin">{{
                  $t('m.NavBar_Management')
                }}</el-dropdown-item>
                <el-dropdown-item divided command="/logout">{{
                  $t('m.NavBar_Logout')
                }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="avatar"
              class="drop-avatar"
            ></avatar>
            <el-dropdown
              class="drop-msg"
              @command="handleRoute"
              placement="bottom"
            >
              <span class="el-dropdown-link">
                <i class="el-icon-message-solid"></i>
                <svg
                  v-if="
                    unreadMessage.comment > 0 ||
                      unreadMessage.reply > 0 ||
                      unreadMessage.like > 0 ||
                      unreadMessage.sys > 0 ||
                      unreadMessage.mine > 0
                  "
                  width="10"
                  height="10"
                  style="vertical-align: top;margin-left: -11px;margin-top: 3px;"
                >
                  <circle cx="5" cy="5" r="5" style="fill: red;"></circle>
                </svg>
              </span>

              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="/message/discuss">
                  <span>{{ $t('m.DiscussMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.comment > 0">
                    <MsgSvg :total="unreadMessage.comment"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/reply">
                  <span>{{ $t('m.ReplyMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.reply > 0">
                    <MsgSvg :total="unreadMessage.reply"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/like">
                  <span>{{ $t('m.LikeMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.like > 0">
                    <MsgSvg :total="unreadMessage.like"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/sys">
                  <span>{{ $t('m.SysMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.sys > 0">
                    <MsgSvg :total="unreadMessage.sys"></MsgSvg>
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="/message/mine">
                  <span>{{ $t('m.MineMsg') }}</span>
                  <span class="drop-msg-count" v-if="unreadMessage.mine > 0">
                    <MsgSvg :total="unreadMessage.mine"></MsgSvg>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-menu>
      </div>
      <div id="header-hidden" v-show="isScrolled">
      </div>
    </template>
    <template v-else>
      <div style="top:0px;left:0px;">
      <mu-appbar class="mobile-nav" color="primary">
        <mu-button icon slot="left" @click="opendrawer = !opendrawer">
          <i class="el-icon-s-unfold"></i>
        </mu-button>
        <el-tooltip
            :content="$t('m.Click_To_Change_Web_Language')"
            placement="bottom"
            effect="dark"
          >
          <span @click="changeWebLanguage">
          {{
            websiteConfig.shortName ? websiteConfig.shortName : 'OJ'
          }}
          </span>
        </el-tooltip>
        <mu-button
          flat
          slot="right"
          @click="handleBtnClick('Login')"
          v-show="!isAuthenticated"
          >{{ $t('m.NavBar_Login') }}</mu-button
        >
        <mu-button
          flat
          slot="right"
          @click="handleBtnClick('Register')"
          v-show="!isAuthenticated && websiteConfig.register"
          >{{ $t('m.NavBar_Register') }}</mu-button
        >

        <mu-menu slot="right" v-show="isAuthenticated" :open.sync="openmsgmenu">
          <mu-button flat>
            <mu-icon value=":el-icon-message-solid" size="24"></mu-icon>
            <svg
              v-if="
                unreadMessage.comment > 0 ||
                  unreadMessage.reply > 0 ||
                  unreadMessage.like > 0 ||
                  unreadMessage.sys > 0 ||
                  unreadMessage.mine > 0
              "
              width="10"
              height="10"
              style="margin-left: -11px;margin-top: -13px;"
            >
              <circle cx="5" cy="5" r="5" style="fill: red;"></circle>
            </svg>
          </mu-button>
          <mu-list slot="content" @change="handleCommand">
            <mu-list-item button value="/message/discuss">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.DiscussMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.comment > 0">
                    <MsgSvg :total="unreadMessage.comment"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/message/reply">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.ReplyMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.reply > 0">
                    <MsgSvg :total="unreadMessage.reply"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/message/like">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.LikeMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.like > 0">
                    <MsgSvg :total="unreadMessage.like"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/message/sys">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.SysMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.sys > 0">
                    <MsgSvg :total="unreadMessage.sys"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>

            <mu-list-item button value="/message/mine">
              <mu-list-item-content>
                <mu-list-item-title>
                  {{ $t('m.MineMsg') }}
                  <span class="drop-msg-count" v-if="unreadMessage.mine > 0">
                    <MsgSvg :total="unreadMessage.mine"></MsgSvg>
                  </span>
                </mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
          </mu-list>
        </mu-menu>

        <mu-menu
          slot="right"
          v-if="isAuthenticated"
          :open.sync="openusermenu"
        >
          <mu-button flat>
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="avatar"
              :title="userInfo.username"
            ></avatar>
            <i class="el-icon-caret-bottom"></i>
          </mu-button>
          <mu-list slot="content" @change="handleCommand">
            <mu-list-item button value="/user-home">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_UserHome')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/status?onlyMine=true">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Submissions')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/setting">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Setting')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>
            <mu-list-item button value="/admin" v-show="isAdminRole">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Management')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
            <mu-divider></mu-divider>

            <mu-list-item button value="/logout">
              <mu-list-item-content>
                <mu-list-item-title>{{
                  $t('m.NavBar_Logout')
                }}</mu-list-item-title>
              </mu-list-item-content>
            </mu-list-item>
          </mu-list>
        </mu-menu>
      </mu-appbar>

      <mu-appbar style="width: 100%;">
        <!--占位，刚好占领导航栏的高度-->
      </mu-appbar>

      <mu-drawer :open.sync="opendrawer" :docked="false" :right="false">
        <mu-list toggle-nested>
          <mu-list-item
            button
            to="/home"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-home" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Home') }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            to="/problem"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-grid" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.NavBar_Problem')
            }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            to="/training"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-claim" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.NavBar_Training')
            }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            to="/contest"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-trophy" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.NavBar_Contest')
            }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            to="/status"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-marketing" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Status') }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'rank'"
            @toggle-nested="openSideMenu = arguments[0] ? 'rank' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-s-data" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Rank') }}</mu-list-item-title>
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
              to="/acm-rank"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_ACM_Rank')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/oi-rank"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_OI_Rank')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>

          <mu-list-item
            v-if="websiteConfig.openPublicDiscussion"
            button
            to="/discussion"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-comments" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{
              $t('m.NavBar_Discussion')
            }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            to="/group"
            @click="opendrawer = !opendrawer"
            active-class="mobile-menu-active"
          >
            <mu-list-item-action>
              <mu-icon value=":fa fa-users" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_Group') }}</mu-list-item-title>
          </mu-list-item>

          <mu-list-item
            button
            :ripple="false"
            nested
            :open="openSideMenu === 'about'"
            @toggle-nested="openSideMenu = arguments[0] ? 'about' : ''"
          >
            <mu-list-item-action>
              <mu-icon value=":el-icon-info" size="24"></mu-icon>
            </mu-list-item-action>
            <mu-list-item-title>{{ $t('m.NavBar_About') }}</mu-list-item-title>
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
              to="/introduction"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_Introduction')
              }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item
              button
              :ripple="false"
              slot="nested"
              to="/developer"
              @click="opendrawer = !opendrawer"
              active-class="mobile-menu-active"
            >
              <mu-list-item-title>{{
                $t('m.NavBar_Developer')
              }}</mu-list-item-title>
            </mu-list-item>
          </mu-list-item>
        </mu-list>
      </mu-drawer>
    </div>
    </template>
    
    <el-dialog
      :visible.sync="modalVisible"
      width="370px"
      class="dialog"
      :title="title"
      :close-on-click-modal="false"
    >
      <component :is="modalStatus.mode" v-if="modalVisible"></component>
      <div slot="footer" style="display: none"></div>
    </el-dialog>
  </div>
</template>
<script>
import Login from '@/components/oj/common/Login';
import Register from '@/components/oj/common/Register';
import ResetPwd from '@/components/oj/common/ResetPassword';
import MsgSvg from '@/components/oj/msg/msgSvg';
import { mapGetters, mapActions } from 'vuex';
import Avatar from 'vue-avatar';
import api from '@/common/api';
export default {
  components: {
    Login,
    Register,
    ResetPwd,
    Avatar,
    MsgSvg,
  },
  created(){
    this.page_width();
    window.onresize = () => {
      this.page_width();
      this.setHiddenHeaderHeight();
    };
  },
  mounted() {
    this.setHiddenHeaderHeight();
    if (this.isAuthenticated) {
      this.getUnreadMsgCount();
      this.msgTimer = setInterval(() => {
        this.getUnreadMsgCount();
      }, 120 * 1000);
    }
  },
  beforeDestroy() {
    clearInterval(this.msgTimer);
  },
  data() {
    return {
      centerDialogVisible: false,
      mobileNar: false,
      opendrawer: false,
      openusermenu: false,
      openmsgmenu: false,
      openSideMenu: '',
      imgUrl: require('@/assets/logo.png'),
      avatarStyle:
        'display: inline-flex;width: 30px;height: 30px;border-radius: 50%;align-items: center;justify-content: center;text-align: center;user-select: none;',
    };
  },
  methods: {
    ...mapActions(['changeModalStatus']),
    page_width() {
      let screenWidth = window.screen.width;
      if (screenWidth < 1050) {
        this.mobileNar = true;
      } else {
        this.mobileNar = false;
      }
    },
    handleBtnClick(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    handleRoute(route) {
      //电脑端导航栏路由跳转事件
      if (route && route.split('/')[1] != 'admin') {
        this.$router.push(route);
      } else {
        window.open('/admin/');
      }
    },
    handleCommand(route) {
      // 移动端导航栏路由跳转事件
      this.openusermenu = false;
      this.openmsgmenu = false;
      if (route && route.split('/')[1] != 'admin') {
        this.$router.push(route);
      } else {
        window.open('/admin/');
      }
    },
    getUnreadMsgCount() {
      api.getUnreadMsgCount().then((res) => {
        let data = res.data.data;
        this.$store.dispatch('updateUnreadMessageCount', data);
        let sumMsg =
          data.comment + data.reply + data.like + data.mine + data.sys;
        if (sumMsg > 0) {
          if (this.webLanguage == 'zh-CN') {
            this.$notify.info({
              title: '未读消息',
              message:
                '亲爱的【' +
                this.userInfo.username +
                '】，您有最新的' +
                sumMsg +
                '条未读消息，请注意查看！',
              position: 'bottom-right',
              duration: 5000,
            });
          } else {
            this.$notify.info({
              title: 'Unread Message',
              message:
                'Dear【' +
                this.userInfo.username +
                '】, you have the latest ' +
                sumMsg +
                ' unread messages. Please check them!',
              position: 'bottom-right',
              duration: 5000,
            });
          }
        }
      });
    },
    changeWebLanguage() {
      this.$store.commit('changeWebLanguage', { language: this.webLanguage == 'zh-CN' ? 'en-US' : 'zh-CN' });
    },
    setHiddenHeaderHeight(){
      if(!this.mobileNar){
        try {
          let headerHeight = document.getElementById('header').offsetHeight;
          document.getElementById('header-hidden').setAttribute('style','height:'+ headerHeight + 'px')
        } catch (e) {}
      }
    }
  },
  computed: {
    ...mapGetters([
      'modalStatus',
      'userInfo',
      'isAuthenticated',
      'isAdminRole',
      'token',
      'websiteConfig',
      'unreadMessage',
      'webLanguage',
    ]),
    avatar() {
      return this.$store.getters.userInfo.avatar;
    },
    activeMenuName() {
      if (this.$route.path.split('/')[1] == 'submission-detail') {
        return '/status';
      } else if (this.$route.path.split('/')[1] == 'discussion-detail') {
        return '/discussion';
      }
      return '/' + this.$route.path.split('/')[1];
    },
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
        let ojName = this.websiteConfig.shortName
          ? this.websiteConfig.shortName
          : 'OJ';
        if (this.modalStatus.mode == 'ResetPwd') {
          return this.$i18n.t('m.Dialog_Reset_Password') + ' - ' + ojName;
        } else {
          return (
            this.$i18n.t('m.Dialog_' + this.modalStatus.mode) + ' - ' + ojName
          );
        }
      },
    },
  },
  watch: {
    isAuthenticated() {
      if (this.isAuthenticated) {
        if (this.msgTimer) {
          clearInterval(this.msgTimer);
        }
        this.getUnreadMsgCount();
        this.msgTimer = setInterval(() => {
          this.getUnreadMsgCount();
        }, 120 * 1000);
      } else {
        clearInterval(this.msgTimer);
      }
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
  z-index: 2000;
  background-color: #fff;
  box-shadow: 0 1px 5px 0 rgba(0, 0, 0, 0.1);
}
.mobile-nav {
  position: fixed;
  left: 0px;
  top: 0px;
  z-index: 2500;
  height: auto;
  width: 100%;
}

#drawer {
  position: fixed;
  left: 0px;
  bottom: 0px;
  z-index: 1000;
  width: 100%;
  box-shadow: 00px 0px 00px rgb(255, 255, 255), 0px 0px 10px rgb(255, 255, 255),
    0px 0px 0px rgb(255, 255, 255), 1px 1px 0px rgb(218, 218, 218);
}

.logo {
  cursor: pointer;
  margin-left: 2%;
  margin-right: 2%;
  float: left;
  width: 139px;
  height: 42px;
  margin-top: 5px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409eff !important;
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
.drop-avatar {
  float: right;
  margin-right: 15px;
  position: relative;
  margin-top: 16px;
}
.drop-msg {
  float: right;
  font-size: 25px;
  margin-right: 10px;
  position: relative;
  margin-top: 13px;
}
.drop-msg-count {
  margin-left: 2px;
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
  color: #495060 !important;
}
.el-menu-item i {
  color: #495060;
}
.is-active .el-submenu__title i,
.is-active {
  color: #2196f3 !important;
}
.el-menu-item.is-active i {
  color: #2196f3 !important;
}
</style>
