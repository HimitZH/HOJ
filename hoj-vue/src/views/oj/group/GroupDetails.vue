<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="24" style="margin-top: 10px;">
        <div class="sub-menu">
          <el-tabs @tab-click="tabClick" v-model="route_name">
            <el-tab-pane name="GroupDetails" lazy>
              <span slot="label">
                <i class="el-icon-s-home"></i>&nbsp;{{ $t('m.Group_Home') }}
              </span>
            </el-tab-pane>
            <el-tab-pane
              lazy
              name="GroupProblemList"
              :disabled="groupMenuDisabled"
            >
              <span slot="label">
                <i class="fa fa-list"></i>&nbsp;{{ $t('m.Group_Problem') }}
              </span>
            </el-tab-pane>
            <el-tab-pane
              lazy
              name="GroupTrainingList"
              :disabled="groupMenuDisabled"
            >
              <span slot="label">
                <i class="el-icon-s-flag"></i>&nbsp;{{ $t('m.Group_Training') }}
              </span>
            </el-tab-pane>

            <el-tab-pane
              lazy
              name="GroupContestList"
              :disabled="groupMenuDisabled"
            >
              <span slot="label">
                <i class="el-icon-s-data"></i>&nbsp;{{ $t('m.Group_Contest') }}
              </span>
            </el-tab-pane>

            <el-tab-pane
              lazy
              name="GroupSubmissionList"
              :disabled="groupMenuDisabled"
            >
              <span slot="label">
                <i class="el-icon-s-marketing"></i>&nbsp;{{
                  $t('m.Group_Submission')
                }}
              </span>
            </el-tab-pane>

            <el-tab-pane
              lazy
              name="GroupDiscussionList"
              :disabled="groupMenuDisabled"
              v-if="websiteConfig.openGroupDiscussion"
            >
              <span slot="label">
                <i class="el-icon-share"></i>&nbsp;{{
                  $t('m.Group_Discussion')
                }}
              </span>
            </el-tab-pane>
            <el-tab-pane
              lazy
              name="GroupMemberList"
              :disabled="groupMenuDisabled"
            >
              <span slot="label">
                <i class="el-icon-user-solid"></i>&nbsp;{{
                  $t('m.Group_Member')
                }}
              </span>
            </el-tab-pane>
            <el-tab-pane lazy name="GroupAnnouncementList" v-if="isGroupAdmin">
              <span slot="label">
                <i class="fa fa-bullhorn"></i>&nbsp;{{
                  $t('m.Group_Announcement')
                }}
              </span>
            </el-tab-pane>
            <el-tab-pane lazy name="GroupSetting" v-if="isGroupRoot">
              <span slot="label">
                <i class="el-icon-s-tools"></i>&nbsp;{{ $t('m.Group_Setting') }}
              </span>
            </el-tab-pane>
            <el-tab-pane 
              lazy 
              name="GroupRank" 
              :disabled="groupMenuDisabled">
              <span slot="label">
                <i class="el-icon-medal-1"></i>&nbsp;{{ $t('m.Group_Rank') }}
              </span>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>

      <template
        v-if="
          $route.name === 'GroupSubmissionList' || 
          $route.name === 'GroupSubmissionDetails' ||
            $route.name === 'GroupProblemDetails' ||
            ($route.name != 'GroupTrainingList' 
             && $route.name.startsWith('GroupTraining'))
        "
      >
        <el-col :span="24" style=" margin-bottom: 10px;">
          <transition name="el-fade-in-linear">
            <router-view></router-view>
          </transition>
        </el-col>
      </template>

      <template v-else>
        <el-col :md="18" :xs="24" style=" margin-bottom: 10px;">
          <transition name="el-fade-in-linear">
            <router-view></router-view>
          </transition>
          <el-card v-show="$route.name === 'GroupDetails'">
            <el-row>
              <el-col
                :md="isGroupMember || isSuperAdmin ? 12 : 24"
                :sm="24"
                :xs="24"
              >
                <div class="description-body">
                  <Markdown
                    v-if="group.description"
                    :isAvoidXss="true" 
                    :content="group.description">
                  </Markdown>
                  <div class="markdown-body" v-else>
                    <p>{{ $t('m.Not_set_yet') }}</p>
                  </div>
                </div>
              </el-col>
              <el-col v-if="isGroupMember || isSuperAdmin" :md="1" :lg="1">
                <div class="separator hidden-sm-and-down"></div>
                <p></p>
              </el-col>
              <el-col
                v-if="isGroupMember || isSuperAdmin"
                :md="11"
                :sm="24"
                :xs="24"
              >
                <Announcement></Announcement>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
        <el-col :md="6" :xs="24" style="margin-bottom: 10px;">
          <el-card>
            <div slot="header" style="text-align: center">
              <avatar
                :inline="true"
                :size="130"
                color="#FFF"
                :src="group.avatar ? group.avatar : defaultAvatar"
                shape="square"
              ></avatar>
            </div>
            <div class="info-rows">
              <div>
                <span>
                  <span>{{ $t('m.Group_Name') }}</span>
                </span>
                <span>
                  <el-tooltip
                    class="item"
                    effect="dark"
                    :content="group.name"
                    placement="top"
                  >
                    <span>{{ group.name | ellipsis }}</span>
                  </el-tooltip>
                </span>
              </div>
              <div>
                <span>
                  <span>{{ $t('m.Group_Owner') }}</span>
                </span>
                <span>
                  <el-link
                    style="font-size: 16px"
                    type="primary"
                    :underline="false"
                    @click="toUserHome(group.owner)"
                    ><i class="el-icon-user-solid"></i> {{ group.owner }}
                  </el-link>
                </span>
              </div>
              <div>
                <span>
                  <span>{{ $t('m.Group_Auth') }}</span>
                </span>
                <span>
                  <el-tooltip
                    v-if="group.auth != null && group.auth != undefined"
                    :content="$t('m.' + GROUP_TYPE_REVERSE[group.auth].tips)"
                  >
                    <el-tag
                      :type="GROUP_TYPE_REVERSE[group.auth].color"
                      size="medium"
                      effect="dark"
                    >
                      {{ $t('m.Group_' + GROUP_TYPE_REVERSE[group.auth].name) }}
                    </el-tag>
                  </el-tooltip>
                  <el-tooltip :content="$t('m.Group_Hidden_Tips')">
                    <el-tag
                      v-if="!group.visible"
                      size="medium"
                      type="primary"
                      effect="dark"
                    >
                      {{ $t('m.Group_Hidden') }}
                    </el-tag>
                  </el-tooltip>
                </span>
              </div>
              <div>
                <span>
                  <span>{{ $t('m.Created_Time') }}</span>
                </span>
                <span>
                  <i class="el-icon-time">
                    {{ group.gmtCreate | localtime((format = 'YYYY-MM-DD')) }}
                  </i>
                </span>
              </div>
              <div>
                <span>
                  <span>{{ $t('m.Group_Number') }}</span>
                </span>
                <span>
                  <span>{{ group.id }}</span>
                </span>
              </div>
            </div>
            <div style="text-align: center">
              <span v-if="isGroupOwner || isSuperAdmin">
                <el-button type="danger" size="small" @click="disbandGroup">
                  {{ $t('m.Disband_Group') }}
                </el-button>
              </span>
              <span v-else-if="isGroupMember">
                <el-button type="danger" size="small" @click="exitGroup">
                  {{ $t('m.Exit_Group') }}
                </el-button>
              </span>
              <span v-else-if="isAuthenticated && userAuth == 0">
                <el-button type="primary" size="small" @click="handleApply">
                  {{ $t('m.Apply_Group') }}
                </el-button>
              </span>
              <span v-else-if="userAuth == 1">
                <el-button type="warning" size="small">
                  {{ $t('m.Applying') }}
                </el-button>
              </span>
              <span v-else-if="userAuth == 2">
                <el-button type="info" size="small">
                  {{ $t('m.Refused') }}
                </el-button>
              </span>
            </div>
          </el-card>
        </el-col>
      </template>
    </el-row>
    <el-dialog
      :title="$t('m.Apply_Group')"
      :visible.sync="showApplyDialog"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="appliaction"
        label-width="100px"
        label-position="top"
        :rules="rules"
        ref="apply"
      >
        <el-row>
          <el-col :span="24" v-if="group.auth == 3">
            <el-form-item :label="$t('m.Group_Code')" required prop="code">
              <el-input
                v-model="appliaction.code"
                :placeholder="$t('m.Group_Code')"
                class="title-input"
                minlength="6"
                maxlength="6"
                show-word-limit
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Apply_Reason')" required prop="reason">
              <el-input
                v-model="appliaction.reason"
                :placeholder="$t('m.Apply_Reason')"
                class="title-input"
                type="textarea"
                rows="5"
                minlength="5"
                maxlength="100"
                show-word-limit
              >
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger" @click.native="showApplyDialog = false">{{
          $t('m.Cancel')
        }}</el-button>
        <el-button type="primary" @click.native="submitApply">{{
          $t('m.OK')
        }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { GROUP_TYPE, GROUP_TYPE_REVERSE } from '@/common/constants';
import { mapState, mapGetters, mapActions } from 'vuex';
import Avatar from 'vue-avatar';
import Announcement from '@/components/oj/group/Announcement.vue';
import api from '@/common/api';
import mMessage from '@/common/message';
import Markdown from '@/components/oj/common/Markdown';
export default {
  name: 'GroupDetails',
  components: {
    Avatar,
    Announcement,
    Markdown
  },
  data() {
    var checkGroupReason = (rule, value, callback) => {
      if (this.group.auth !== 1) {
        if (value === null || value === '') {
          callback(new Error(this.$t('m.Apply_Reason_Check_Required')));
        } else if (value.length < 5 || value.length > 100) {
          callback(new Error(this.$t('m.Apply_Reason_Check_Min_Max')));
        }
      }
      callback();
    };
    var checkGroupCode = (rule, value, callback) => {
      if (this.group.auth === 3) {
        if (value === null || value === '') {
          callback(new Error(this.$t('m.Group_Code_Check_Required')));
        } else if (value.length != 6) {
          callback(new Error(this.$t('m.Group_Code_Check_Min_Max')));
        }
      }
      callback();
    };
    return {
      route_name: 'GroupDetails',
      defaultAvatar: require('@/assets/default.jpg'),
      showApplyDialog: false,
      appliaction: {
        code: '',
        reason: '',
      },
      rules: {
        code: [
          {
            validator: checkGroupCode,
            trigger: 'blur',
          },
        ],
        reason: [
          {
            validator: checkGroupReason,
            trigger: 'blur',
          },
        ],
      },
    };
  },
  created() {
    this.route_name = this.$route.name;
    if (this.route_name == 'GroupProblemDetails') {
      this.route_name = 'GroupProblemList';
    } else if (this.route_name == 'GroupSubmissionDetails') {
      this.route_name = 'GroupSubmissionList';
    } else if (this.route_name == 'GroupDiscussionDetails') {
      this.route_name = 'GroupDiscussionList';
    }else if(this.route_name.startsWith('GroupTraining')){
      this.route_name = 'GroupTrainingList';
    }
    this.GROUP_TYPE = Object.assign({}, GROUP_TYPE);
    this.GROUP_TYPE_REVERSE = Object.assign({}, GROUP_TYPE_REVERSE);
    this.$store.dispatch('getGroup').then((res) => {
      this.changeDomTitle({ title: res.data.data.name });
    });
  },
  methods: {
    ...mapActions(['changeDomTitle']),
    tabClick(tab) {
      let name = tab.name;
      if (name !== this.$route.name) {
        this.$router.push({ name: name });
      }
    },
    handleApply() {
      if (this.group.auth === 1) {
        this.addMember();
      } else {
        this.showApplyDialog = true;
      }
    },
    submitApply() {
      this.$refs['apply'].validate((valid) => {
        if (valid) {
          this.addMember();
        }
      });
    },
    addMember() {
      api
        .addGroupMember(
          this.$route.params.groupID,
          this.appliaction.code,
          this.appliaction.reason
        )
        .then((res) => {
          mMessage.success(this.$t('m.Apply_Successfully'));
          this.$store.dispatch('getGroupAuth');
          this.showApplyDialog = false;
        })
        .catch(() => {});
    },
    exitGroup() {
      this.$confirm(
        this.$i18n.t('m.Exit_Group_Tips'),
        this.$i18n.t('m.Warning'),
        {
          confirmButtonText: this.$i18n.t('m.OK'),
          cancelButtonText: this.$i18n.t('m.Cancel'),
          type: 'warning',
        }
      )
        .then(() => {
          this.loading = true;
          api
            .exitGroup(this.$route.params.groupID)
            .then((res) => {
              this.loading = false;
              mMessage.success(this.$i18n.t('m.Exit_Successfully'));
              this.$store.dispatch('getGroup');
            })
            .catch(() => {});
        })
        .catch(() => {
          this.loading = false;
        });
    },
    disbandGroup() {
      this.$confirm(
        this.$i18n.t('m.Disband_Group_Tips'),
        this.$i18n.t('m.Warning'),
        {
          confirmButtonText: this.$i18n.t('m.OK'),
          cancelButtonText: this.$i18n.t('m.Cancel'),
          type: 'warning',
        }
      )
        .then(() => {
          this.loading = true;
          api
            .deleteGroup(this.$route.params.groupID)
            .then((res) => {
              this.loading = false;
              mMessage.success(this.$i18n.t('m.Disband_Successfully'));
              this.$router.push({
                name: 'GroupList',
              });
            })
            .catch(() => {});
        })
        .catch(() => {
          this.loading = false;
        });
    },
    toUserHome(username) {
      this.$router.push({
        name: 'UserHome',
        query: { username: username },
      });
    },
  },
  computed: {
    ...mapState({
      group: (state) => state.group.group,
    }),
    ...mapGetters([
      'userInfo',
      'isAuthenticated',
      'isGroupAdmin',
      'isGroupRoot',
      'groupMenuDisabled',
      'isGroupMember',
      'isGroupOwner',
      'userAuth',
      'isSuperAdmin',
      'websiteConfig'
    ]),
  },
  filters: {
    //文字数超出时，超出部分使用...
    ellipsis(value) {
      if (!value) return '';
      var l = value.length;
      var ans = '';
      var blen = 0;
      for (let i = 0; i < l; i++) {
        if ((value.charCodeAt(i) & 0xff00) != 0) {
          blen++;
        }
        blen++;
        if (blen > 26) {
          return ans + '...';
        }
        ans += value.charAt(i);
      }
      return ans;
    },
  },
  watch: {
    $route(newVal) {
      if (
        newVal.name == 'GroupEditProblem' ||
        newVal.name == 'GroupCreateProblem' ||
        newVal.name == 'GroupProblemDetails'
      ) {
        this.route_name = 'GroupProblemList';
      } else if (
        newVal.name == 'GroupEditTraining' ||
        newVal.name == 'GroupCreateTraining' ||
        newVal.name == 'GroupTrainingProblemList'
      ) {
        this.route_name = 'GroupTrainingList';
      } else if (
        newVal.name == 'GroupEditContest' ||
        newVal.name == 'GroupCreateContest' ||
        newVal.name == 'GroupContestProblemList' ||
        newVal.name == 'GroupContestAnnouncementList'
      ) {
        this.route_name = 'GroupContestList';
      } else if (newVal.name == 'GroupSubmissionDetails') {
        this.route_name = 'GroupSubmissionList';
      } else if (newVal.name == 'GroupDiscussionDetails') {
        this.route_name = 'GroupDiscussionList';
      }else if(this.route_name.startsWith('GroupTraining')){
        this.route_name = 'GroupTrainingList';
      } else {
        this.route_name = newVal.name;
      }
      this.changeDomTitle({ title: this.group.name });
    },
  },
  beforeDestroy() {
    this.$store.commit('clearGroup');
  },
};
</script>

<style scoped>
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
/deep/.el-tabs__nav-wrap {
  background: #fff;
  border-radius: 3px;
}
/deep/.el-tabs--top .el-tabs__item.is-top:nth-child(2) {
  padding-left: 20px;
}
.description-body {
  background: #fff;
  overflow: hidden;
  width: 100%;
  padding: 10px 10px;
  text-align: left;
  font-size: 14px;
  line-height: 1.6;
}
.separator {
  display: block;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  border: 1px dashed #eee;
}
.info-rows > * {
  margin-bottom: var(--info-row-margin-bottom, 1em);
  display: flex;
  align-items: center;
  font-size: 16px;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.75);
}
.info-rows > * > *:first-child {
  flex: 1 0 auto;
  text-align: left;
}
.info-rows > :last-child {
  margin-bottom: 0;
}
</style>
