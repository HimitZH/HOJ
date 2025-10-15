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
            <el-form-item
              :label="$t('m.Contest_Title')"
              required
            >
              <el-input
                v-model="contest.title"
                :placeholder="$t('m.Contest_Title')"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item
              :label="$t('m.Contest_Description')"
              required
            >
              <Editor :value.sync="contest.description"></Editor>
            </el-form-item>
          </el-col>
          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_Start_Time')"
              required
            >
              <el-date-picker
                v-model="contest.startTime"
                @change="changeDuration"
                type="datetime"
                :placeholder="$t('m.Contest_Start_Time')"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_End_Time')"
              required
            >
              <el-date-picker
                v-model="contest.endTime"
                @change="changeDuration"
                type="datetime"
                :placeholder="$t('m.Contest_End_Time')"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_Duration')"
              required
            >
              <el-input
                v-model="durationText"
                disabled
              > </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_Rule_Type')"
              required
            >
              <el-radio
                class="radio"
                v-model="contest.type"
                :label="0"
                @change="setSealRankTimeDefaultValue"
                :disabled="disableRuleType"
              >ACM</el-radio>
              <el-radio
                class="radio"
                v-model="contest.type"
                :label="1"
                :disabled="disableRuleType"
                @change="setSealRankTimeDefaultValue"
              >OI</el-radio>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.OI_Rank_Score_Type')"
              v-show="contest.type == 1"
            >
              <el-radio
                class="radio"
                v-model="contest.oiRankScoreType"
                label="Recent"
              >{{ $t('m.OI_Rank_Score_Type_Recent') }}</el-radio>
              <el-radio
                class="radio"
                v-model="contest.oiRankScoreType"
                label="Highest"
              >{{ $t('m.OI_Rank_Score_Type_Highest') }}</el-radio>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col
            :md="8"
            :xs="24"
            v-if="contest.sealRank"
          >
            <el-form-item
              :label="$t('m.Timeliness_Of_Rank')"
              required
            >
              <el-switch
                v-model="contest.sealRank"
                active-color="#13ce66"
                :active-text="$t('m.Seal_Time_Rank')"
                :inactive-text="$t('m.Real_Time_Rank')"
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col
            :md="24"
            :xs="24"
            v-else
          >
            <el-form-item
              :label="$t('m.Timeliness_Of_Rank')"
              required
            >
              <el-switch
                v-model="contest.sealRank"
                active-color="#13ce66"
                :active-text="$t('m.Seal_Time_Rank')"
                :inactive-text="$t('m.Real_Time_Rank')"
                @change="setSealRankTimeDefaultValue"
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Seal_Rank_Time')"
              :required="contest.sealRank"
              v-show="contest.sealRank"
            >
              <el-date-picker
                v-model="contest.sealRankTime"
                type="datetime"
                :placeholder="$t('m.Seal_Rank_Time')"
                :picker-options="sealPickerOptions"
              >
              </el-date-picker>
              <div v-if="contest.sealRank && contest.sealRankTime && contest.endTime && contest.startTime" class="seal-tip">
                {{$t('m.Contest_End_Time')}} - {{$t('m.Seal_Rank_Time')}} = {{ sealDiffSeconds }}s
              </div>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Auto_Real_Rank')"
              required
              v-show="contest.sealRank"
            >
              <el-switch
                v-model="contest.autoRealRank"
                :active-text="$t('m.Real_Rank_After_Contest')"
                :inactive-text="$t('m.Seal_Rank_After_Contest')"
              >
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_Outside_ScoreBoard')"
              required
            >
              <el-switch
                v-model="contest.openRank"
                :active-text="$t('m.Open')"
                :inactive-text="$t('m.Close')"
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Allow_Submission_After_The_Contest_Ends')"
              required
            >
              <el-switch
                v-model="contest.allowEndSubmit"
                :active-text="$t('m.Open')"
                :inactive-text="$t('m.Close')"
              >
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Print_Func')"
              required
            >
              <el-switch
                v-model="contest.openPrint"
                :active-text="$t('m.Support_Offline_Print')"
                :inactive-text="$t('m.Not_Support_Print')"
              >
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item
              :label="$t('m.Rank_Show_Name')"
              required
            >
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

          <el-col>
            <el-form-item
              :label="$t('m.Star_User_UserName')"
              required
            >
              <el-tag
                v-for="username in contest.starAccount"
                closable
                :close-transition="false"
                :key="username"
                type="warning"
                size="medium"
                @close="removeStarUser(username)"
                style="margin-right: 7px;margin-top:4px"
              >{{ username }}</el-tag>
              <el-input
                v-if="inputVisible"
                size="medium"
                class="input-new-star-user"
                v-model="starUserInput"
                :trigger-on-focus="true"
                @keyup.enter.native="addStarUser"
                @blur="addStarUser"
              >
              </el-input>
              <el-tooltip
                effect="dark"
                :content="$t('m.Add')"
                placement="top"
                v-else
              >
                <el-button
                  class="button-new-tag"
                  size="small"
                  @click="inputVisible = true"
                  icon="el-icon-plus"
                ></el-button>
              </el-tooltip>
            </el-form-item>
          </el-col>

          <el-col
            :md="8"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_Auth')"
              required
            >
              <el-select v-model="contest.auth">
                <el-option
                  :label="$t('m.Public')"
                  :value="0"
                ></el-option>
                <el-option
                  :label="$t('m.Private')"
                  :value="1"
                ></el-option>
                <el-option
                  :label="$t('m.Protected')"
                  :value="2"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :md="8"
            :xs="24"
          >
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
          <el-col
            :md="8"
            :xs="24"
          >
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
              <el-col
                :md="6"
                :xs="24"
              >
                <el-form-item
                  :label="$t('m.Prefix')"
                  prop="prefix"
                >
                  <el-input
                    v-model="formRule.prefix"
                    placeholder="Prefix"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col
                :md="6"
                :xs="24"
              >
                <el-form-item
                  :label="$t('m.Suffix')"
                  prop="suffix"
                >
                  <el-input
                    v-model="formRule.suffix"
                    placeholder="Suffix"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col
                :md="6"
                :xs="24"
              >
                <el-form-item
                  :label="$t('m.Start_Number')"
                  prop="number_from"
                >
                  <el-input-number
                    v-model="formRule.number_from"
                    style="width: 100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
              <el-col
                :md="6"
                :xs="24"
              >
                <el-form-item
                  :label="$t('m.End_Number')"
                  prop="number_to"
                >
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

              <el-col
                :md="24"
                :xs="24"
              >
                <el-form-item
                  :label="$t('m.Extra_Account')"
                  prop="prefix"
                >
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

          <el-col
            :md="24"
            :xs="24"
          >
            <el-form-item
              :label="$t('m.Contest_Award')"
              required
            >
              <el-select
                v-model="contest.awardType"
                @change="contestAwardTypeChange"
              >
                <el-option
                  :label="$t('m.Contest_Award_Null')"
                  :value="0"
                ></el-option>
                <el-option
                  :label="$t('m.Contest_Award_Set_Proportion')"
                  :value="1"
                ></el-option>
                <el-option
                  :label="$t('m.Contest_Award_Set_Number')"
                  :value="2"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :span="24"
            v-if="contest.awardType != 0"
          >
            <div style="margin-bottom:10px">
              <el-button
                type="primary"
                icon="el-icon-plus"
                circle
                @click="insertEvent(-1)"
                size="small"
              ></el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                @click="removeEvent()"
                size="small"
              ></el-button>
            </div>
            <vxe-table
              border
              ref="xAwardTable"
              :data="contest.awardConfigList"
              :edit-config="{trigger: 'click', mode: 'cell'}"
              :sort-config="{trigger: 'cell', defaultSort: {field: 'priority', order: 'asc'}, orders: ['desc', 'asc', null]}"
              align="center"
              @edit-closed="editClosedEvent"
              style="margin-bottom:15px"
            >
              <vxe-table-column
                type="checkbox"
                width="60"
              ></vxe-table-column>
              <vxe-table-column
                field="priority"
                width="100"
                :title="$t('m.Contest_Award_Priority')"
                :edit-render="{name: 'input', attrs: {type: 'number'}}"
                sortable
              >
              </vxe-table-column>
              <vxe-table-column
                field="name"
                min-width="150"
                :title="$t('m.Contest_Award_Name')"
                :edit-render="{name: 'input', attrs: {type: 'text'}}"
              >
              </vxe-table-column>
              <vxe-table-column
                field="background"
                min-width="150"
                :title="$t('m.Contest_Award_Background')"
              >
                <template v-slot="{ row }">
                  <el-color-picker
                    v-model="row.background"
                    size="small"
                  ></el-color-picker>
                </template>
              </vxe-table-column>
              <vxe-table-column
                field="color"
                min-width="150"
                :title="$t('m.Contest_Award_Color')"
              >
                <template v-slot="{ row }">
                  <el-color-picker
                    v-model="row.color"
                    size="small"
                  ></el-color-picker>
                </template>
              </vxe-table-column>

              <vxe-table-column
                field="show"
                min-width="150"
                :title="$t('m.Contest_Award_Show')"
              >
                <template v-slot="{ row }">
                  <RankBox
                    :name="row.name"
                    :background="row.background"
                    :color="row.color"
                    :num="1"
                  ></RankBox>
                </template>
              </vxe-table-column>

              <vxe-table-column
                field="num"
                min-width="150"
                :title="contest.awardType == 1?$t('m.Contest_Award_Proportion'):$t('m.Contest_Award_Number')"
              >
                <template v-slot="{ row }">

                  <el-input
                    :placeholder="$t('m.Contest_Award_Proportion')"
                    v-model="row.num"
                    size="small"
                    v-if="contest.awardType == 1"
                    type="number"
                  >
                    <template slot="append">%</template>
                  </el-input>
                  <el-input
                    :placeholder="$t('m.Contest_Award_Number')"
                    v-model="row.num"
                    size="small"
                    v-else
                  >
                  </el-input>
                </template>
              </vxe-table-column>
            </vxe-table>
          </el-col>
        </el-row>
      </el-form>
      <el-button
        type="primary"
        @click.native="saveContest"
      >{{
        $t('m.Save')
      }}</el-button>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import time from "@/common/time";
import moment from "moment";
import { mapGetters } from "vuex";
import myMessage from "@/common/message";
const Editor = () => import("@/components/admin/Editor.vue");
const RankBox = () => import("@/components/oj/common/RankBox");
export default {
  name: "CreateContest",
  components: {
    Editor,
    RankBox,
  },
  data() {
    return {
      title: "Create Contest",
      disableRuleType: false,
      durationText: "", // 比赛时长文本表示
      seal_rank_time: 2, // 当开启封榜模式，即实时榜单关闭时，可选择前半小时，前一小时，全程封榜,默认全程封榜
      contest: {
        title: "",
        description: "",
        startTime: "",
        endTime: "",
        duration: 0,
        type: 0,
        pwd: "",
        sealRank: false,
        sealRankTime: "", //封榜时间
        autoRealRank: true,
        auth: 0,
        openPrint: false,
        rankShowName: "username",
        openAccountLimit: false,
        allowEndSubmit: false,
        accountLimitRule: "",
        starAccount: [],
        oiRankScoreType: "Recent",
        awardType: 0,
        awardConfigList: [
          {
            priority: 1,
            name: "金牌",
            background: "#e6bf25",
            color: "#fff",
            num: 10,
          },
          {
            priority: 2,
            name: "银牌",
            background: "#b4c0c7",
            color: "#fff",
            num: 20,
          },
          {
            priority: 3,
            name: "铜牌",
            background: "#CD7F32",
            color: "#fff",
            num: 30,
          },
        ],
      },
      formRule: {
        prefix: "",
        suffix: "",
        number_from: 0,
        number_to: 10,
        extra_account: "",
      },
      starUserInput: "",
      inputVisible: false,
      sealPickerOptions: {
        disabledDate: (time) => {
          const start = this.contest.startTime ? moment(this.contest.startTime) : null;
          const end = this.contest.endTime ? moment(this.contest.endTime) : null;
          if (!start || !end) return false;
          // 仅按“日期”维度禁用：允许选择与开始/结束为同一天的日期，具体时间由保存校验控制
          // Element UI 的 disabledDate 在 datetime 下传入的是一天的 00:00 时间戳
          const dayTs = time.getTime();
          const startDayTs = start.clone().startOf('day').toDate().getTime();
          const endDayTs = end.clone().startOf('day').toDate().getTime();
          // 禁用开始日之前的所有日期，以及结束日之后的所有日期
          return dayTs < startDayTs || dayTs > endDayTs;
        },
      },
    };
  },
  mounted() {
    if (this.$route.name === "admin-edit-contest") {
      this.title = this.$i18n.t("m.Edit_Contest");
      this.disableRuleType = true;
      this.getContestByCid();
    } else {
      this.title = this.$i18n.t("m.Create_Contest");
      this.disableRuleType = false;
    }
  },
  watch: {
    $route() {
      if (this.$route.name === "admin-edit-contest") {
        this.title = this.$i18n.t("m.Edit_Contest");
        this.disableRuleType = true;
        this.getContestByCid();
      } else {
        this.title = this.$i18n.t("m.Create_Contest");
        this.disableRuleType = false;
        this.contest = {};
      }
    },
  },
  computed: {
    ...mapGetters(["userInfo"]),
    // 结束时间与封榜时间的差值（秒）
    sealDiffSeconds() {
      if (!this.contest || !this.contest.sealRankTime || !this.contest.endTime) return 0;
      const end = moment(this.contest.endTime);
      const seal = moment(this.contest.sealRankTime);
      const diff = end.diff(seal, 'seconds');
      return diff > 0 ? diff : 0;
    },
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
          // 确保编辑时sealRankTime在[startTime, endTime)范围，否则设为默认值
          if (this.contest.sealRank) {
            const start = moment(this.contest.startTime);
            const end = moment(this.contest.endTime);
            let seal = this.contest.sealRankTime ? moment(this.contest.sealRankTime) : null;
            if (!seal || !seal.isValid() || !seal.isBetween(start, end, undefined, '[)')) {
              // 默认：若为ACM且时长>=1h则默认结束前1小时，否则默认全程；OI默认全程
              if (this.contest.type == 0 && end.diff(start, 'seconds') >= 3600) {
                this.contest.sealRankTime = end.clone().subtract(3600, 'seconds');
              } else {
                this.contest.sealRankTime = start.clone();
              }
            }
          }
          if (this.contest.accountLimitRule) {
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
          this.$i18n.t("m.Contest_Title") + " " + this.$i18n.t("m.is_required")
        );
        return;
      }
      if (!this.contest.description) {
        myMessage.error(
          this.$i18n.t("m.Contest_Description") +
            " " +
            this.$i18n.t("m.is_required")
        );
        return;
      }
      if (!this.contest.startTime) {
        myMessage.error(
          this.$i18n.t("m.Contest_Start_Time") +
            " " +
            this.$i18n.t("m.is_required")
        );
        return;
      }
      if (!this.contest.endTime) {
        myMessage.error(
          this.$i18n.t("m.Contest_End_Time") +
            " " +
            this.$i18n.t("m.is_required")
        );
        return;
      }
      if (!this.contest.duration || this.contest.duration <= 0) {
        myMessage.error(this.$i18n.t("m.Contest_Duration_Check"));
        return;
      }
      if (this.contest.auth != 0 && !this.contest.pwd) {
        myMessage.error(
          this.$i18n.t("m.Contest_Password") +
            " " +
            this.$i18n.t("m.is_required")
        );
        return;
      }

      if (this.contest.openAccountLimit) {
        this.contest.accountLimitRule = this.changeAccountRuleToStr(
          this.formRule
        );
      }

      let funcName =
        this.$route.name === "admin-edit-contest"
          ? "admin_editContest"
          : "admin_createContest";

      // 保存前校验封榜时间范围，并确保为moment对象
      if (this.contest.sealRank) {
        if (!this.contest.sealRankTime) {
          myMessage.error(this.$i18n.t('m.Seal_Rank_Time') + ' ' + this.$i18n.t('m.is_required'));
          return;
        }
        const start = moment(this.contest.startTime);
        const end = moment(this.contest.endTime);
        const seal = moment(this.contest.sealRankTime);
        if (!seal.isBetween(start, end, undefined, '[)')) {
          myMessage.error(this.$i18n.t('m.Seal_Rank_Time') + ' ' + this.$i18n.t('m.Contets_Time_Check'));
          return;
        }
        // 统一存为moment对象（后端已有Date接收，将由axios序列化）
        this.contest.sealRankTime = seal;
      } else {
        this.contest.sealRankTime = null;
      }
      let data = Object.assign({}, this.contest);
      if (funcName === "admin_createContest") {
        data["uid"] = this.userInfo.uid;
        data["author"] = this.userInfo.username;
      }

      api[funcName](data)
        .then((res) => {
          myMessage.success("success");
          this.$router.push({
            name: "admin-contest-list",
            query: { refresh: "true" },
          });
        })
        .catch(() => {});
    },
    changeDuration() {
      let start = this.contest.startTime;
      let end = this.contest.endTime;
      let durationMS = time.durationMs(start, end);
      if (durationMS < 0) {
        this.durationText = this.$i18n.t("m.Contets_Time_Check");
        this.contest.duration = 0;
        return;
      }
      if (start != "" && end != "") {
        this.durationText = time.formatSpecificDuration(start, end);
        this.contest.duration = durationMS;
      }
    },
    changeAccountRuleToStr(formRule) {
      let result =
        "<prefix>" +
        formRule.prefix +
        "</prefix><suffix>" +
        formRule.suffix +
        "</suffix><start>" +
        formRule.number_from +
        "</start><end>" +
        formRule.number_to +
        "</end><extra>" +
        formRule.extra_account +
        "</extra>";
      return result;
    },
    changeStrToAccountRule(value) {
      let reg =
        "<prefix>([\\s\\S]*?)</prefix><suffix>([\\s\\S]*?)</suffix><start>([\\s\\S]*?)</start><end>([\\s\\S]*?)</end><extra>([\\s\\S]*?)</extra>";
      let re = RegExp(reg, "g");
      let tmp = re.exec(value);
      return {
        prefix: tmp[1],
        suffix: tmp[2],
        number_from: tmp[3],
        number_to: tmp[4],
        extra_account: tmp[5],
      };
    },

    addStarUser() {
      this.starUserInput = this.starUserInput.replace(/(^\s*)|(\s*$)/g, "");
      if (this.starUserInput) {
        for (var i = 0; i < this.contest.starAccount.length; i++) {
          if (this.contest.starAccount[i] == this.starUserInput) {
            myMessage.warning(this.$i18n.t("m.Add_Star_User_Error"));
            this.starUserInput = "";
            return;
          }
        }
        this.contest.starAccount.push(this.starUserInput);
        this.inputVisible = false;
        this.starUserInput = "";
      }
    },

    // 根据UserName 从打星用户列表中移除
    removeStarUser(username) {
      this.contest.starAccount.splice(
        this.contest.starAccount.map((item) => item.name).indexOf(username),
        1
      );
    },

    setSealRankTimeDefaultValue() {
      if (this.contest.sealRank == true) {
        if (this.contest.type == 0) {
          // ACM比赛开启封榜 默认为一小时,如果比赛时长小于一小时，则默认为全程
          if (this.contest.duration < 3600) {
            this.seal_rank_time = 2;
          } else {
            this.seal_rank_time = 1;
          }
        } else {
          // OI比赛开启封榜 默认全程
          this.seal_rank_time = 2;
        }
      }
    },

    contestAwardTypeChange() {
      if (this.contest.awardType != 0 && !this.contest.awardConfigList.length) {
        this.contest.awardConfigList = [
          {
            priority: 1,
            name: "金牌",
            background: "#e6bf25",
            color: "#fff",
            num: 10,
          },
          {
            priority: 2,
            name: "银牌",
            background: "#b4c0c7",
            color: "#fff",
            num: 20,
          },
          {
            priority: 3,
            name: "铜牌",
            background: "#CD7F32",
            color: "#fff",
            num: 30,
          },
        ];
      }
    },

    async insertEvent(row) {
      let record = {
        priority: this.contest.awardConfigList.length + 1,
        name: "name",
        background: "#ededed",
        color: "#666",
        num: 0,
      };
      let { row: newRow } = await this.$refs.xAwardTable.insertAt(record, row);
      const { insertRecords } = this.$refs.xAwardTable.getRecordset();
      this.contest.awardConfigList =
        this.contest.awardConfigList.concat(insertRecords);
      await this.$refs.xAwardTable.setActiveCell(newRow, "name");
    },

    async removeEvent() {
      this.$refs.xAwardTable.removeCheckboxRow();
      let removeRecords = this.$refs.xAwardTable.getRemoveRecords();
      function getDifferenceSetB(arr1, arr2, typeName) {
        return Object.values(
          arr1.concat(arr2).reduce((acc, cur) => {
            if (
              acc[cur[typeName]] &&
              acc[cur[typeName]][typeName] === cur[typeName]
            ) {
              delete acc[cur[typeName]];
            } else {
              acc[cur[typeName]] = cur;
            }
            return acc;
          }, {})
        );
      }
      this.contest.awardConfigList = getDifferenceSetB(
        this.contest.awardConfigList,
        removeRecords,
        "_XID"
      );
    },

    editClosedEvent({ row, column }) {
      let xTable = this.$refs.xAwardTable;
      let field = column.property;
      // 判断单元格值是否被修改
      if (xTable.isUpdateByRow(row, field)) {
        setTimeout(() => {
          // 局部更新单元格为已保存状态
          this.$refs.xAwardTable.reloadRow(row, null, field);
        }, 300);
      }
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
.input-new-star-user {
  width: 200px;
}
.seal-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
