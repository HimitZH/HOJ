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
          <!-- <el-col :span="24">
            <el-form-item label="Allowed IP Ranges">
              <div v-for="(range, index) in contest.allowed_ip_ranges" :key="index">
                <el-row :gutter="20" style="margin-bottom: 15px">
                  <el-col :span="8">
                    <el-input v-model="range.value" placeholder="CIDR Network"></el-input>
                  </el-col>
                  <el-col :span="10">
                    <el-button  icon="el-icon-plus" @click="addIPRange" type="primary"></el-button>
                    <el-button  icon="el-icon-delete-solid" @click.native="removeIPRange(range)" type="danger"></el-button>
                  </el-col>
                </el-row>
              </div>
            </el-form-item>
          </el-col> -->
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
        // allowed_ip_ranges: [{
        //   value: ''
        // }]
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
        this.contest = [];
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
          // let ranges = []
          // for (let v of data.allowed_ip_ranges) {
          //   ranges.push({value: v})
          // }
          // if (ranges.length === 0) {
          //   ranges.push({value: ''})
          // }
          // data.allowed_ip_ranges = ranges
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
      // let ranges = []
      // for (let v of data.allowed_ip_ranges) {
      //   if (v.value !== '') {
      //     ranges.push(v.value)
      //   }
      // }
      // data.allowed_ip_ranges = ranges
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
        this.durationText = time.duration(start, end);
        this.contest.duration = durationMS;
      }
    },
    // addIPRange () {
    //   this.contest.allowed_ip_ranges.push({value: ''})
    // },
    //   removeIPRange (range) {
    //     let index = this.contest.allowed_ip_ranges.indexOf(range)
    //     if (index !== -1) {
    //       this.contest.allowed_ip_ranges.splice(index, 1)
    //     }
    //   }
  },
};
</script>
