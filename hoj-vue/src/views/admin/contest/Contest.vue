<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">
          {{ title }}
        </span>
      </div>
      <el-form label-position="top">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item :label="$t('m.Contest_Title')" required>
              <el-input
                v-model="contest.title"
                :placeholder="$t('m.Contest_Title')"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Contest_Description')" required>
              <Editor :value.sync="contest.description"></Editor>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Contest_Start_Time')" required>
              <el-date-picker
                v-model="contest.startTime"
                @change="changeDuration"
                type="datetime"
                :placeholder="$t('m.Contest_Start_Time')"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Contest_End_Time')" required>
              <el-date-picker
                v-model="contest.endTime"
                @change="changeDuration"
                type="datetime"
                :placeholder="$t('m.Contest_End_Time')"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Contest_Duration')" required>
              <el-input v-model="durationText" disabled> </el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Contest_Rule_Type')">
              <el-radio
                class="radio"
                v-model="contest.type"
                :label="0"
                :disabled="disableRuleType"
                >ACM</el-radio
              >
              <el-radio
                class="radio"
                v-model="contest.type"
                :label="1"
                :disabled="disableRuleType"
                >OI</el-radio
              >
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24" v-if="contest.sealRank">
            <el-form-item :label="$t('m.Seal_Time_Rank')">
              <el-switch
                v-model="contest.sealRank"
                active-color="#13ce66"
                inactive-color="#ff4949"
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col :md="16" :xs="24" v-else>
            <el-form-item :label="$t('m.Real_Time_Rank')">
              <el-switch
                v-model="contest.sealRank"
                active-color="#13ce66"
                inactive-color=""
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24">
            <el-form-item
              :label="$t('m.Seal_Rank_Time')"
              :required="contest.sealRank"
              v-show="contest.sealRank"
            >
              <el-select v-model="seal_rank_time">
                <el-option
                  :label="$t('m.Contest_Seal_Half_Hour')"
                  :value="0"
                  :disabled="contest.duration < 1800"
                ></el-option>
                <el-option
                  :label="$t('m.Contest_Seal_An_Hour')"
                  :value="1"
                  :disabled="contest.duration < 3600"
                ></el-option>
                <el-option
                  :label="$t('m.Contest_Seal_All_Hour')"
                  :value="2"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item :label="$t('m.Print_Func')" required>
              <el-switch
                v-model="contest.openPrint"
                :active-text="$t('m.Support_Offline_Print')"
                :inactive-text="$t('m.Not_Support_Print')"
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item :label="$t('m.Rank_Show_Name')" required>
              <el-radio-group v-model="contest.rankShowName">
                <el-radio label="username">{{
                  $t('m.Show_Username')
                }}</el-radio>
                <el-radio label="nickname">{{
                  $t('m.Show_Nickname')
                }}</el-radio>
                <el-radio label="realname">{{
                  $t('m.Show_Realname')
                }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24">
            <el-form-item :label="$t('m.Contest_Auth')" required>
              <el-select v-model="contest.auth">
                <el-option :label="$t('m.Public')" :value="0"></el-option>
                <el-option :label="$t('m.Private')" :value="1"></el-option>
                <el-option :label="$t('m.Protected')" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item
              :label="$t('m.Contest_Password')"
              v-show="contest.auth != 0"
              :required="contest.auth != 0"
            >
              <el-input
                v-model="contest.pwd"
                :placeholder="$t('m.Contest_Password')"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item
              :label="$t('m.Account_Limit')"
              v-show="contest.auth != 0"
              :required="contest.auth != 0"
            >
              <el-switch v-model="contest.openAccountLimit"> </el-switch>
            </el-form-item>
          </el-col>

          <template v-if="contest.openAccountLimit">
            <el-form :model="formRule">
              <el-col :md="6" :xs="24">
                <el-form-item :label="$t('m.Prefix')" prop="prefix">
                  <el-input
                    v-model="formRule.prefix"
                    placeholder="Prefix"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :md="6" :xs="24">
                <el-form-item :label="$t('m.Suffix')" prop="suffix">
                  <el-input
                    v-model="formRule.suffix"
                    placeholder="Suffix"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :md="6" :xs="24">
                <el-form-item :label="$t('m.Start_Number')" prop="number_from">
                  <el-input-number
                    v-model="formRule.number_from"
                    style="width: 100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :md="6" :xs="24">
                <el-form-item :label="$t('m.End_Number')" prop="number_to">
                  <el-input-number
                    v-model="formRule.number_to"
                    style="width: 100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>

              <div
                class="userPreview"
                v-if="formRule.number_from <= formRule.number_to"
              >
                {{ $t('m.The_allowed_account_will_be') }}
                {{ formRule.prefix + formRule.number_from + formRule.suffix }},
                <span v-if="formRule.number_from + 1 < formRule.number_to">
                  {{
                    formRule.prefix +
                      (formRule.number_from + 1) +
                      formRule.suffix +
                      '...'
                  }}
                </span>
                <span v-if="formRule.number_from + 1 <= formRule.number_to">
                  {{ formRule.prefix + formRule.number_to + formRule.suffix }}
                </span>
              </div>

              <el-col :md="24" :xs="24">
                <el-form-item :label="$t('m.Extra_Account')" prop="prefix">
                  <el-input
                    type="textarea"
                    :placeholder="$t('m.Extra_Account_Tips')"
                    :rows="8"
                    v-model="formRule.extra_account"
                  >
                  </el-input>
                </el-form-item>
              </el-col>
            </el-form>
          </template>
        </el-row>
      </el-form>
      <el-button type="primary" @click.native="saveContest">{{
        $t('m.Save')
      }}</el-button>
    </el-card>
  </div>
</template>

<script>
import api from '@/common/api';
import time from '@/common/time';
import moment from 'moment';
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
const Editor = () => import('@/components/admin/Editor.vue');
export default {
  name: 'CreateContest',
  components: {
    Editor,
  },
  data() {
    return {
      title: 'Create Contest',
      disableRuleType: false,
      durationText: '', // 比赛时长文本表示
      seal_rank_time: 0, // 当开启封榜模式，即实时榜单关闭时，可选择前半小时，前一小时，全程封榜,默认半小时1800s
      contest: {
        title: '',
        description: '',
        startTime: '',
        endTime: '',
        duration: 0,
        type: 0,
        pwd: '',
        sealRank: true,
        sealRankTime: '', //封榜时间
        auth: 0,
        openPrint: false,
        rankShowName: 'username',
        openAccountLimit: false,
        accountLimitRule: '',
      },
      formRule: {
        prefix: '',
        suffix: '',
        number_from: 0,
        number_to: 10,
        extra_account: '',
      },
    };
  },
  mounted() {
    if (this.$route.name === 'admin-edit-contest') {
      this.title = this.$i18n.t('m.Edit_Contest');
      this.disableRuleType = true;
      this.getContestByCid();
    }
  },
  watch: {
    $route() {
      if (this.$route.name === 'admin-edit-contest') {
        this.title = this.$i18n.t('m.Edit_Contest');
        this.disableRuleType = true;
        this.getContestByCid();
      } else {
        this.title = this.$i18n.t('m.Create_Contest');
        this.disableRuleType = false;
        this.contest = {};
      }
    },
  },
  computed: {
    ...mapGetters(['userInfo']),
  },
  methods: {
    getContestByCid() {
      api
        .admin_getContest(this.$route.params.contestId)
        .then((res) => {
          let data = res.data.data;
          this.contest = data;
          this.changeDuration();
          // 封榜时间转换
          let halfHour = moment(this.contest.endTime)
            .subtract(1800, 'seconds')
            .toString();
          let oneHour = moment(this.contest.endTime)
            .subtract(3600, 'seconds')
            .toString();
          let allHour = moment(this.contest.startTime).toString();
          let sealRankTime = moment(this.contest.sealRankTime).toString();
          switch (sealRankTime) {
            case halfHour:
              this.seal_rank_time = 0;
              break;
            case oneHour:
              this.seal_rank_time = 1;
              break;
            case allHour:
              this.seal_rank_time = 2;
              break;
          }
          if (!this.contest.accountLimitRule) {
            this.formRule = this.changeStrToAccountRule(
              this.contest.accountLimitRule
            );
          }
        })
        .catch(() => {});
    },

    saveContest() {
      if (!this.contest.title) {
        myMessage.error(
          this.$i18n.t('m.Contest_Title') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.contest.description) {
        myMessage.error(
          this.$i18n.t('m.Contest_Description') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.contest.startTime) {
        myMessage.error(
          this.$i18n.t('m.Contest_Start_Time') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.contest.endTime) {
        myMessage.error(
          this.$i18n.t('m.Contest_End_Time') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.contest.duration || this.contest.duration <= 0) {
        myMessage.error(this.$i18n.t('m.Contest_Duration_Check'));
        return;
      }
      if (this.contest.auth != 0 && !this.contest.pwd) {
        myMessage.error(
          this.$i18n.t('m.Contest_Password') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }

      if (this.contest.openAccountLimit) {
        this.contest.accountLimitRule = this.changeAccountRuleToStr(
          this.formRule
        );
      }

      let funcName =
        this.$route.name === 'admin-edit-contest'
          ? 'admin_editContest'
          : 'admin_createContest';

      switch (this.seal_rank_time) {
        case 0: // 结束前半小时
          this.contest.sealRankTime = moment(this.contest.endTime).subtract(
            1800,
            'seconds'
          );
          break;
        case 1: // 结束前一小时
          this.contest.sealRankTime = moment(this.contest.endTime).subtract(
            3600,
            'seconds'
          );
          break;
        case 2: // 全程
          this.contest.sealRankTime = moment(this.contest.startTime);
      }
      let data = Object.assign({}, this.contest);
      if (funcName === 'admin_createContest') {
        data['uid'] = this.userInfo.uid;
        data['author'] = this.userInfo.username;
      }

      api[funcName](data)
        .then((res) => {
          myMessage.success('success');
          this.$router.push({
            name: 'admin-contest-list',
            query: { refresh: 'true' },
          });
        })
        .catch(() => {});
    },
    changeDuration() {
      let start = this.contest.startTime;
      let end = this.contest.endTime;
      let durationMS = time.durationMs(start, end);
      if (durationMS < 0) {
        this.durationText = this.$i18n.t('m.Contets_Time_Check');
        this.contest.duration = 0;
        return;
      }
      if (start != '' && end != '') {
        this.durationText = time.formatSpecificDuration(start, end);
        this.contest.duration = durationMS;
      }
    },
    changeAccountRuleToStr(formRule) {
      let result =
        '<prefix>' +
        formRule.prefix +
        '</prefix><suffix>' +
        formRule.suffix +
        '</suffix><start>' +
        formRule.number_from +
        '</start><end>' +
        formRule.number_to +
        '</end><extra>' +
        formRule.extra_account +
        '</extra>';
      return result;
    },
    changeStrToAccountRule(value) {
      let reg =
        '<prefix>([\\s\\S]*?)</prefix><suffix>([\\s\\S]*?)</suffix><start>([\\s\\S]*?)</start><end>([\\s\\S]*?)</end><extra>([\\s\\S]*?)</extra>';
      let re = RegExp(reg, 'g');
      let tmp = re.exec(value);
      return {
        prefix: tmp[1],
        suffix: tmp[2],
        number_from: tmp[3],
        number_to: tmp[4],
        extra_account: tmp[5],
      };
    },
  },
};
</script>
<style scoped>
.userPreview {
  padding-left: 10px;
  padding-top: 20px;
  padding-bottom: 20px;
  color: red;
  font-size: 16px;
  margin-bottom: 10px;
}
</style>
