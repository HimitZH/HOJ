<template>
  <el-row class="flex-container">
    <div id="main">
      <el-card shadow>
        <div slot="header">
          <el-row :gutter="18">
            <el-col
              :md="4"
              :lg="2"
            >
              <span class="panel-title hidden-md-and-down">{{
                $t('m.Status')
              }}</span>
            </el-col>
            <el-col
              :xs="10"
              :sm="8"
              :md="4"
              :lg="4"
            >
              <el-switch
                style="display: block"
                v-model="formFilter.onlyMine"
                :active-text="$t('m.Mine')"
                :width="40"
                @change="handleOnlyMine"
                :inactive-text="$t('m.All')"
              >
              </el-switch>
            </el-col>

            <el-col
              :xs="10"
              :sm="8"
              :md="5"
              :lg="4"
              style="padding-top: 5px;"
            >
              <el-dropdown
                class="drop-menu"
                @command="handleStatusChange"
                placement="bottom"
                trigger="hover"
              >
                <span class="el-dropdown-link">
                  {{ status }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="All">{{
                    $t('m.All')
                  }}</el-dropdown-item>
                  <el-dropdown-item
                    v-for="result in Object.keys(JUDGE_STATUS_LIST)"
                    :key="result"
                    :command="result"
                  >
                    {{ JUDGE_STATUS_LIST[result].name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-col>

            <el-col
              :sm="8"
              :md="5"
              :lg="4"
              class="hidden-xs-only"
            >
              <el-button
                type="primary"
                size="small"
                icon="el-icon-refresh"
                round
                @click="getSubmissions"
              >{{ $t('m.Refresh') }}</el-button>
            </el-col>
            <el-col
              :xs="4"
              class="hidden-sm-and-up"
            >
              <el-button
                type="primary"
                size="small"
                icon="el-icon-refresh"
                circle
                @click="getSubmissions"
              ></el-button>
            </el-col>

            <el-col
              :xs="24"
              :sm="12"
              :md="5"
              :lg="5"
              class="search"
            >
              <vxe-input
                v-model="formFilter.problemID"
                :placeholder="$t('m.Enter_Problem_ID')"
                type="search"
                size="medium"
                @keyup.enter.native="handleQueryChange('probemID')"
                @search-click="handleQueryChange('probemID')"
              ></vxe-input>
            </el-col>
            <el-col
              :xs="24"
              :sm="12"
              :md="5"
              :lg="5"
              class="search"
            >
              <vxe-input
                v-model="formFilter.username"
                :disabled="formFilter.onlyMine"
                :placeholder="$t('m.Enter_Author')"
                type="search"
                size="medium"
                @keyup.enter.native="handleQueryChange('username')"
                @search-click="handleQueryChange('username')"
              ></vxe-input>
            </el-col>
          </el-row>
        </div>
        <vxe-table
          border="inner"
          stripe
          keep-source
          ref="xTable"
          highlight-current-row
          highlight-hover-row
          align="center"
          auto-resize
          :row-class-name="tableRowClassName"
          :data="submissions"
          :loading="loadingTable"
        >
          <vxe-table-column
            field="submitId"
            :title="$t('m.Run_ID')"
            width="80"
          ></vxe-table-column>
          <vxe-table-column
            field="pid"
            :title="$t('m.Problem')"
            min-width="150"
            show-overflow
          >
            <template v-slot="{ row }">
              <span
                v-if="contestID"
                @click="getProblemUri(row.displayId)"
                style="color: rgb(87, 163, 243)"
              >{{ row.displayId + ' ' + row.title }}
              </span>
              <span
                v-else
                @click="getProblemUri(row.displayPid)"
                style="color: rgb(87, 163, 243)"
              >{{ row.displayPid + ' ' + row.title }}
              </span>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="status"
            :title="$t('m.Status')"
            min-width="200"
          >
            <template v-slot="{ row }">
              <el-tooltip
                effect="dark"
                :content="$t('m.Click_to_Manually_Judge')"
                :disabled="hideManuallyJugdeTooltip || disabledManualJudge(row.status)"
                placement="top"
              >
                <el-popover
                  placement="top"
                  width="230"
                  trigger="click"
                  @show="hideManuallyJugdeTooltip = true"
                  @hide="hideManuallyJugdeTooltip = false"
                  :disabled="disabledManualJudge(row.status)"
                >
                  <div class="manual-judge-title">
                    <span>{{ $t('m.Manually_Jugde') }}</span>
                  </div>
                  <div>
                    <span>
                      <el-button
                        size="mini"
                        type="warning"
                        icon="el-icon-edit"
                        @click="openChangeJudgeStatusDialog(row)"
                        :disabled="row.status == JUDGE_STATUS_RESERVE['Pending']"
                      >{{ $t('m.Modify_Evaluation') }}</el-button>
                      <el-button
                        size="mini"
                        type="danger"
                        icon="el-icon-close"
                        :disabled="row.status == JUDGE_STATUS_RESERVE['ca']"
                        @click="cancelJudge(row)"
                      >{{ $t('m.Cancel_Evaluation') }}</el-button>
                    </span>
                  </div>
                  <span
                    :class="getStatusColor(row.status)"
                    slot="reference"
                  >
                    <i
                      class="el-icon-loading"
                      v-if="
                    row.status == JUDGE_STATUS_RESERVE['Pending'] ||
                      row.status == JUDGE_STATUS_RESERVE['Compiling'] ||
                      row.status == JUDGE_STATUS_RESERVE['Judging']
                  "
                    ></i>
                    <i
                      class="el-icon-refresh"
                      v-if="row.status == JUDGE_STATUS_RESERVE['sf']"
                      @click="reSubmit(row)"
                    ></i>
                    {{ JUDGE_STATUS[row.status].name }}
                  </span>
                </el-popover>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('m.Has_Been_Manually_Judged')"
                placement="top"
                v-if="row.isManual"
              >
                <svg
                  style="vertical-align: middle;margin-left: 4px;"
                  t="1660976601239"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="9872"
                  width="16"
                  height="16"
                >
                  <path
                    d="M706.1 489.4c24.3-36.7 38.6-80.6 38.6-128 0-128.1-103.9-232-232-232s-232 103.9-232 232c0 47.3 14.2 91.3 38.6 128C168.9 561.5 65 715.2 
                  65 893.2h895.3c0.1-178-103.8-331.7-254.2-403.8z"
                    fill="#D6F3FA"
                    p-id="9873"
                  >
                  </path>
                  <path
                    d="M587 800.7l86.3 61.1-72.6 33.4c-10.5 4.8-22.2-3.5-21.1-15l7.4-79.5z m274.9-205L685.4 844.8l-86.3-61.1 176.5-249.1 86.3 61.1zM825 612.3c3.1-4.4 
                  2.1-10.5-2.3-13.6-4.4-3.1-10.5-2.1-13.6 2.3l-132 186.2c-3.1 4.4-2.1 10.5 2.3 13.6 4.4 3.1 10.5 2.1 13.6-2.3l132-186.2z m41.5-23.1c6.1-8.6 4-20.6-4.6-26.7l-55-39c-8.6-6.1-20.6-4-26.7 
                  4.6l-1.6 2.2 86.3 61.1 1.6-2.2z m-75.4 200.1c0 10.4 8.5 19 19 19h78c10.4 0 19-8.5 19-19 0-10.4-8.5-19-19-19h-78c-10.5 0-19 8.5-19 19z m97 63.5H766.6c-10.4 0-19 8.5-19 19 0 10.4 8.5 
                  19 19 19h121.5c10.4 0 19-8.5 19-19 0-10.4-8.6-19-19-19z m-231-275.2c-44.9-21-94.5-32.1-144.5-32.1-113.5 0-220.2 57.4-283.1 151.1-6.3 9.4-19 12-28.8 6.3-10.8-6.4-14-20.6-7-31 53.6-79.5 
                  135.2-135.7 227-158.5-51.8-31.3-86.6-87.9-86.6-152.7 0-98.4 80.1-178.5 178.5-178.5s178.5 80.1 178.5 178.5c0 64.7-34.8 121.4-86.5 152.7 24.2 5.9 47.8 14.1 70.3 24.6 11.5 5.3 16.2 19.2 
                  10.2 30.3-5.4 9.9-17.7 14.1-28 9.3z m-144.4-76.4c77.5 0 140.5-63 140.5-140.5s-63-140.5-140.5-140.5-140.5 63-140.5 140.5 63 140.5 140.5 140.5zM196.2 711c-11-6.1-24.9-1.7-30.3 9.7-3 6.2-5.8 
                  12.5-8.4 18.8-4.8 11.6 1.2 24.9 13 29 10.8 3.7 22.7-1.6 27-12.2 2.4-5.8 4.9-11.5 7.6-17.1 5.1-10.3 1.1-22.7-8.9-28.2z m-28 64.5c-12.1-3.9-24.8 3.4-27.9 15.7-6 23.5-9.7 47.4-11.2 71.7-0.8 12.6 
                  9.1 23.4 21.7 23.4 11.4 0 21-8.7 21.6-20.1 1.3-22 4.7-43.8 10.1-65.1 3-10.9-3.5-22.1-14.3-25.6z"
                    fill="#18BAE5"
                    p-id="9874"
                  >
                  </path>
                </svg>
              </el-tooltip>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="score"
            :title="$t('m.Score')"
            width="64"
            v-if="scoreColumnVisible"
          >
            <template v-slot="{ row }">
              <template v-if="contestID && row.score != null">
                <el-tag
                  effect="plain"
                  size="medium"
                  :type="JUDGE_STATUS[row.status]['type']"
                >{{ row.score }}</el-tag>
              </template>
              <template v-else-if="row.score != null">
                <el-tooltip placement="top">
                  <div slot="content">
                    {{ $t('m.Problem_Score') }}：{{
                      row.score != null ? row.score : $t('m.Unknown')
                    }}<br />{{ $t('m.OI_Rank_Score') }}：{{
                      row.oiRankScore != null
                        ? row.oiRankScore
                        : $t('m.Unknown')
                    }}<br />
                    {{
                      $t('m.OI_Rank_Calculation_Rule')
                    }}：(score*0.1+difficulty*2)
                  </div>
                  <el-tag
                    effect="plain"
                    size="medium"
                    :type="JUDGE_STATUS[row.status]['type']"
                  >{{ row.score }}</el-tag>
                </el-tooltip>
              </template>
              <template v-else-if="
                  row.status == JUDGE_STATUS_RESERVE['Pending'] ||
                    row.status == JUDGE_STATUS_RESERVE['Compiling'] ||
                    row.status == JUDGE_STATUS_RESERVE['Judging']
                ">
                <el-tag
                  effect="plain"
                  size="medium"
                  :type="JUDGE_STATUS[row.status]['type']"
                >
                  <i class="el-icon-loading"></i>
                </el-tag>
              </template>
              <template v-else>
                <el-tag
                  effect="plain"
                  size="medium"
                  :type="JUDGE_STATUS[row.status]['type']"
                >--</el-tag>
              </template>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="time"
            :title="$t('m.Time')"
            min-width="96"
          >
            <template v-slot="{ row }">
              <span>{{ submissionTimeFormat(row.time) }}</span>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="memory"
            :title="$t('m.Memory')"
            min-width="96"
          >
            <template v-slot="{ row }">
              <span>{{ submissionMemoryFormat(row.memory) }}</span>
            </template>
          </vxe-table-column>

          <vxe-table-column
            field="length"
            :title="$t('m.Length')"
            min-width="80"
          >
            <template v-slot="{ row }">
              <span>{{ submissionLengthFormat(row.length) }}</span>
            </template>
          </vxe-table-column>

          <vxe-table-column
            field="language"
            :title="$t('m.Language')"
            show-overflow
            min-width="96"
          >
            <template v-slot="{ row }">
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('m.View_submission_details')"
                placement="top"
              >
                <span
                  @click="showSubmitDetail(row)"
                  style="color: rgb(87, 163, 243); font-size: .8125rem;"
                >{{ row.language }}
                </span>
              </el-tooltip>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="judger"
            :title="$t('m.Judger')"
            min-width="96"
            show-overflow
          >
            <template v-slot="{ row }">
              <span v-if="row.judger">{{ row.judger }}</span>
              <span v-else>--</span>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="username"
            :title="$t('m.Author')"
            min-width="96"
            show-overflow
          >
            <template v-slot="{ row }">
              <a
                @click="goUserHome(row.username, row.uid)"
                style="color: rgb(87, 163, 243)"
              >{{ row.username }}</a>
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="submitTime"
            :title="$t('m.Submit_Time')"
            min-width="96"
          >
            <template v-slot="{ row }">
              <span>
                <el-tooltip
                  :content="row.submitTime | localtime"
                  placement="top"
                >
                  <span>{{ row.submitTime | fromNow }}</span>
                </el-tooltip>
              </span>
            </template>
          </vxe-table-column>
          <!-- 非比赛提交记录，超级管理员可以对提交进行重判 -->
          <vxe-table-column
            v-if="rejudgeColumnVisible"
            :title="$t('m.Option')"
            min-width="90"
          >
            <template v-slot="{ row }">
              <vxe-button
                status="primary"
                @click="handleRejudge(row)"
                size="mini"
                :loading="row.loading"
              >{{ $t('m.Rejudge') }}</vxe-button>
            </template>
          </vxe-table-column>
        </vxe-table>
      </el-card>
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="changeRoute"
        :current.sync="currentPage"
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </div>
    <el-dialog
      :title="$t('m.Manually_Jugde')+'(Run ID：'+changeJudgeStatus.submitId+')'"
      width="350px"
      :visible.sync="changeJudgeStatusDialogVisible"
      :close-on-click-modal="false"
      center
    >
      <el-form>
        <el-form-item
          :label="$t('m.Status')"
          required
        >
          <el-select
            v-model="changeJudgeStatus.status"
            size="small"
          >
            <el-option
              :label="JUDGE_STATUS[result].name"
              :key="result"
              :value="parseInt(result)"
              v-for="result in Object.keys(CHANGE_JUDGE_STATUS_LIST)"
            ></el-option>
            <el-option
              label="Cancelled"
              :value="-4"
              disabled
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('m.Score')"
          required
          v-if="changeJudgeStatus.score != null || changeJudgeStatus.status == JUDGE_STATUS_RESERVE['ca']"
        >
          <el-input-number
            size="small"
            v-model="changeJudgeStatus.score"
          ></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button
          type="primary"
          @click="manualJudge"
          :disabled="changeJudgeStatus.status == JUDGE_STATUS_RESERVE['ca']"
          :loading="changeJudgeStatusLoading"
        >{{ $t('m.To_Update') }}
        </el-button>
      </span>
    </el-dialog>
  </el-row>
</template>

<script>
import { mapActions, mapGetters } from "vuex";
import api from "@/common/api";
import {
  JUDGE_STATUS,
  CONTEST_STATUS,
  JUDGE_STATUS_RESERVE,
  RULE_TYPE,
} from "@/common/constants";
import utils from "@/common/utils";
import Pagination from "@/components/oj/common/Pagination";
import myMessage from "@/common/message";
import "element-ui/lib/theme-chalk/display.css";
export default {
  name: "submissionList",
  components: {
    Pagination,
  },
  data() {
    return {
      formFilter: {
        onlyMine: false,
        status: "",
        username: "",
        problemID: "",
      },
      loadingTable: false,
      submissions: [],
      needCheckSubmitIds: {}, // 当前状态为6和7的提交记录Id 需要重新检查更新
      total: 30,
      limit: 15,
      currentPage: 1,
      contestID: null,
      groupID: null,
      routeName: "",
      checkStatusNum: 0,
      JUDGE_STATUS: "",
      JUDGE_STATUS_LIST: "",
      CHANGE_JUDGE_STATUS_LIST: "",
      autoCheckOpen: false,
      JUDGE_STATUS_RESERVE: {},
      CONTEST_STATUS: {},
      RULE_TYPE: {},
      hideManuallyJugdeTooltip:false,
      changeJudgeStatusDialogVisible: false,
      changeJudgeStatusLoading: false,
      changeJudgeStatus: {
        submitId: null,
        status: null,
        score: null,
      },
    };
  },
  created() {
    this.init();
  },
  mounted() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.JUDGE_STATUS_LIST = Object.assign({}, JUDGE_STATUS);
    this.JUDGE_STATUS_RESERVE = Object.assign({}, JUDGE_STATUS_RESERVE);
    this.CONTEST_STATUS = Object.assign({}, CONTEST_STATUS);
    this.RULE_TYPE = Object.assign({}, RULE_TYPE);
    // 去除下拉框选择中的Not Submitted,Submitted Unknown Result 三种状态
    delete this.JUDGE_STATUS_LIST["9"];
    delete this.JUDGE_STATUS_LIST["-5"];
    delete this.JUDGE_STATUS_LIST["-10"];

    // 再次去除Cancelled,Compile Error,Compiling,Judging,Submitting
    this.CHANGE_JUDGE_STATUS_LIST = Object.assign({}, this.JUDGE_STATUS_LIST);
    delete this.CHANGE_JUDGE_STATUS_LIST["-4"];
    delete this.CHANGE_JUDGE_STATUS_LIST["-2"];
    delete this.CHANGE_JUDGE_STATUS_LIST["5"];
    delete this.CHANGE_JUDGE_STATUS_LIST["6"];
    delete this.CHANGE_JUDGE_STATUS_LIST["7"];
    this.getData();
  },
  methods: {
    init() {
      this.checkStatusNum = 0;
      this.contestID = this.$route.params.contestID;
      this.groupID = this.$route.params.groupID;
      let query = this.$route.query;
      this.formFilter.problemID = query.problemID;
      this.formFilter.username = query.username || "";
      this.formFilter.onlyMine = query.onlyMine + "" == "true" ? true : false; // 统一换成字符串判断
      this.formFilter.status = query.status;
      this.formFilter.completeProblemID = query.completeProblemID || false;
      if (this.formFilter.onlyMine) {
        // 当前为搜索自己的提交 那么不可搜索别人的提交
        this.formFilter.username = "";
      }
      this.currentPage = parseInt(query.currentPage) || 1;
      if (this.currentPage < 1) {
        this.currentPage = 1;
      }
      this.limit = parseInt(query.limit) || 15;
      this.routeName = this.$route.name;
    },

    getData() {
      this.getSubmissions();
    },

    buildQuery() {
      return {
        onlyMine: this.formFilter.onlyMine,
        status: this.formFilter.status,
        username: this.formFilter.username,
        problemID: this.formFilter.problemID,
        currentPage: this.currentPage,
        limit: this.limit,
        completeProblemID: this.formFilter.completeProblemID,
      };
    },

    submissionTimeFormat(time) {
      return utils.submissionTimeFormat(time);
    },

    submissionMemoryFormat(memory) {
      return utils.submissionMemoryFormat(memory);
    },

    submissionLengthFormat(length) {
      return utils.submissionLengthFormat(length);
    },
    reSubmit(row) {
      api.reSubmitRemoteJudge(row.submitId).then((res) => {
        let xTable = this.$refs.xTable;
        // 重新提交开始，需要将该提交的部分参数初始化
        row.status = res.data.data.status;
        row.time = res.data.data.time;
        row.memory = res.data.data.memory;
        row.errorMessage = res.data.data.errorMessage;
        row.judger = res.data.data.judger;
        row.isManual = false;
        // 重新加载该行数据到view
        xTable.reloadRow(row, null, null);

        this.submissions[row.index] = res.data.data;
        myMessage.success(this.$i18n.t("m.Resubmitted_Successfully"));

        // 加入待重判列表
        this.needCheckSubmitIds[row.submitId] = row.index;

        this.checkStatusNum = 0;
        if (!this.autoCheckOpen) {
          // 如果当前未开启自动检查提交状态的定时任务，则开启
          this.checkSubmissionsStatus();
        }
      });
    },
    getSubmissions() {
      let params = this.buildQuery();
      params.contestID = this.contestID;
      params.gid = this.groupID;
      if (this.contestID) {
        if (this.contestStatus == CONTEST_STATUS.SCHEDULED) {
          params.beforeContestSubmit = true;
        } else {
          params.beforeContestSubmit = false;
        }
        params.containsEnd = true;
      }
      if (this.formFilter.onlyMine) {
        // 需要判断是否为登陆状态
        if (this.isAuthenticated) {
          params.username = ""; // 如果是搜索当前用户的提交记录，那么用户名搜索应该无效
          this.formFilter.username = "";
        } else {
          this.formFilter.onlyMine = false;
          myMessage.error(this.$i18n.t("m.Please_login_first"));
          return;
        }
      }

      this.loadingTable = true;
      this.submissions = [];
      this.needCheckSubmitIds = {};
      let func = this.contestID
        ? "getContestSubmissionList"
        : "getSubmissionList";
      api[func](this.limit, utils.filterEmptyValue(params))
        .then((res) => {
          let data = res.data.data;
          let index = 0;
          for (let v of data.records) {
            if (
              v.status == JUDGE_STATUS_RESERVE["Pending"] ||
              v.status == JUDGE_STATUS_RESERVE["Compiling"] ||
              v.status == JUDGE_STATUS_RESERVE["Judging"]
            ) {
              this.needCheckSubmitIds[v.submitId] = index;
            }
            v.loading = false;
            v.index = index;
            index += 1;
          }
          this.loadingTable = false;
          this.submissions = data.records;
          this.total = data.total;
          if (Object.keys(this.needCheckSubmitIds).length > 0) {
            this.checkSubmissionsStatus();
          }
        })
        .catch(() => {
          this.loadingTable = false;
        });
    },
    // 对当前提交列表 状态为Pending（6）和Judging（7）的提交记录每2秒查询一下最新结果
    checkSubmissionsStatus() {
      // 使用setTimeout避免一些问题
      if (this.refreshStatus) {
        // 如果之前的提交状态检查还没有停止,则停止,否则将会失去timeout的引用造成无限请求
        clearTimeout(this.refreshStatus);
        this.autoCheckOpen = false;
      }
      const checkStatus = () => {
        let submitIds = this.needCheckSubmitIds;
        let func = this.contestID
          ? "checkContestSubmissonsStatus"
          : "checkSubmissonsStatus";
        api[func](Object.keys(submitIds), this.contestID).then(
          (res) => {
            let result = res.data.data;
            if (!this.$refs.xTable) {
              // 避免请求一半退出view保错
              return;
            }
            let viewData = this.$refs.xTable.getTableData().tableData;
            for (let key in submitIds) {
              let submitId = parseInt(key);
              if (!result[submitId]) {
                continue;
              }
              // 更新数据列表
              this.submissions[submitIds[key]] = result[submitId];
              // 更新view中的结果，f分数，耗时，空间消耗，判题机ip
              viewData[submitIds[key]].status = result[submitId].status;
              viewData[submitIds[key]].score = result[submitId].score;
              viewData[submitIds[key]].time = result[submitId].time;
              viewData[submitIds[key]].memory = result[submitId].memory;
              viewData[submitIds[key]].judger = result[submitId].judger;
              viewData[submitIds[key]].isManual = result[submitId].isManual;
              // 重新加载这行数据到view中
              this.$refs.xTable.reloadRow(viewData[submitIds[key]], null, null);

              if (
                result[submitId].status != JUDGE_STATUS_RESERVE["Pending"] &&
                result[submitId].status != JUDGE_STATUS_RESERVE["Compiling"] &&
                result[submitId].status != JUDGE_STATUS_RESERVE["Judging"]
              ) {
                delete this.needCheckSubmitIds[key];
              }
            }
            // 当前提交列表的提交都判题结束或者检查结果600s（2s*300）还没判题结束，为了避免无用请求加重服务器负担，直接停止检查的请求。
            if (
              Object.keys(this.needCheckSubmitIds).length == 0 ||
              this.checkStatusNum == 300
            ) {
              clearTimeout(this.refreshStatus);
              this.autoCheckOpen = false;
            } else {
              this.checkStatusNum += 1;
              this.refreshStatus = setTimeout(checkStatus, 2000);
            }
          },
          (res) => {
            clearTimeout(this.refreshStatus);
            this.autoCheckOpen = false;
          }
        );
      };
      // 设置每2秒检查一下提交结果
      this.checkStatusNum += 1;
      this.refreshStatus = setTimeout(checkStatus, 2000);
      this.autoCheckOpen = true;
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.changeRoute();
    },
    // 改变route， 通过监听route变化请求数据，这样可以产生route history， 用户返回时就会保存之前的状态
    changeRoute() {
      let query = this.buildQuery();
      query.contestID = this.contestID;
      let queryParams = utils.filterEmptyValue(query);
      // 判断新路径请求参数与当前路径请求的参数是否一致，避免重复访问路由报错
      let equal = true;
      for (let key in queryParams) {
        if (queryParams[key] != this.$route.query[key]) {
          equal = false;
          break;
        }
      }
      if (equal) {
        // 判断请求参数的长短
        if (
          Object.keys(queryParams).length !=
          Object.keys(this.$route.query).length
        ) {
          equal = false;
        }
      }

      if (!equal) {
        // 避免重复同个路径请求导致报错
        let routeName = queryParams.contestID
          ? "ContestSubmissionList"
          : this.groupID
          ? "GroupSubmissionList"
          : "SubmissionList";
        this.$router.push({
          name: routeName,
          query: queryParams,
        });
      }
    },
    goRoute(route) {
      this.$router.push(route);
    },
    goUserHome(username, uid) {
      this.$router.push({
        path: "/user-home",
        query: { uid, username },
      });
    },
    handleStatusChange(status) {
      if (status == "All") {
        this.formFilter.status = "";
      } else {
        this.formFilter.status = status;
      }
      this.currentPage = 1;
      this.changeRoute();
    },
    handleQueryChange(searchParam) {
      if (searchParam == "probemID") {
        this.formFilter.completeProblemID = false; // 并非走完全检索displayID了
      }
      this.currentPage = 1;
      this.changeRoute();
    },
    handleRejudge(row) {
      this.submissions[row.index].loading = true;
      api.submissionRejudge(row.submitId).then(
        (res) => {
          let xTable = this.$refs.xTable;
          // 重判开始，需要将该提交的部分参数初始化
          row.status = res.data.data.status;
          row.score = null;
          row.time = res.data.data.time;
          row.memory = res.data.data.memory;
          row.errorMessage = res.data.data.errorMessage;
          row.judger = res.data.data.judger;
          row.loading = false;
          row.isManual = false;
          // 重新加载该行数据到view
          xTable.reloadRow(row, null, null);

          this.submissions[row.index] = res.data.data;
          this.submissions[row.index].loading = false;
          myMessage.success(this.$i18n.t("m.Rejudge_successfully"));

          // 加入待重判列表
          this.needCheckSubmitIds[row.submitId] = row.index;
          this.checkStatusNum = 0;
          if (!this.autoCheckOpen) {
            // 如果当前未开启自动检查提交状态的定时任务，则开启
            this.checkSubmissionsStatus();
          }
        },
        () => {
          this.submissions[row.index].loading = false;
        }
      );
    },
    handleOnlyMine() {
      if (this.formFilter.onlyMine) {
        // 需要判断是否为登陆状态
        if (this.isAuthenticated) {
          this.formFilter.username = "";
        } else {
          this.formFilter.onlyMine = false;
          myMessage.error(this.$i18n.t("m.Please_login_first"));
          return;
        }
      }
      this.currentPage = 1;
      this.changeRoute();
    },
    ...mapActions(["changeModalStatus"]),

    showSubmitDetail(row) {
      if (this.contestID != null) {
        // 比赛提交详情
        this.$router.push({
          name: "ContestSubmissionDetails",
          params: {
            contestID: this.contestID,
            problemID: row.displayId,
            submitID: row.submitId,
          },
        });
      } else if (this.groupID != null) {
        this.$router.push({
          name: "GroupSubmissionDetails",
          params: { submitID: row.submitId },
        });
      } else {
        this.$router.push({
          name: "SubmissionDetails",
          params: { submitID: row.submitId },
        });
      }
    },
    getProblemUri(pid) {
      if (this.contestID) {
        this.$router.push({
          name: "ContestProblemDetails",
          params: {
            contestID: this.$route.params.contestID,
            problemID: pid,
          },
        });
      } else if (this.groupID) {
        this.$router.push({
          name: "GroupProblemDetails",
          params: {
            problemID: pid,
            groupID: this.groupID,
          },
        });
      } else {
        this.$router.push({
          name: "ProblemDetails",
          params: {
            problemID: pid,
          },
        });
      }
    },
    getStatusColor(status) {
      return "el-tag el-tag--medium status-" + JUDGE_STATUS[status]["color"];
    },
    tableRowClassName({ row, rowIndex }) {
      if (row.username == this.userInfo.username && this.isAuthenticated) {
        return "own-submit-row";
      }
    },
    disabledManualJudge(status){
      return !this.isSuperAdmin || status == JUDGE_STATUS_RESERVE['Judging'] 
                || status == JUDGE_STATUS_RESERVE['Compiling']
                || status == JUDGE_STATUS_RESERVE['ce']
    },
    openChangeJudgeStatusDialog(judge) {
      this.changeJudgeStatus = {
        submitId: judge.submitId,
        score: judge.score,
        status: judge.status,
        index: judge.index,
      };
      this.changeJudgeStatusDialogVisible = true;
    },
    cancelJudge(row) {
      this.$confirm(this.$i18n.t("m.Cancel_Judge_Tips"), "Run ID："+row.submitId, {
        type: "warning",
      }).then(
        () => {
          api
            .admin_cancelJudge(row.submitId)
            .then((res) => {
              myMessage.success(this.$i18n.t("m.Cancel_Successfully"));
              let data = res.data.data;
              row.status = data.status;
              row.score = data.score;
              row.oiRankScore = data.oiRankScore;
              row.judger = data.judger;
              row.loading = false;
              row.isManual = true;
              // 重新加载这行数据到view中
              this.$refs.xTable.reloadRow(row, null, null);
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    manualJudge() {
      this.changeJudgeStatusLoading = true;
      api
        .admin_manualJudge(
          this.changeJudgeStatus.submitId,
          this.changeJudgeStatus.status,
          this.changeJudgeStatus.score
        )
        .then(
          (res) => {
            myMessage.success(this.$i18n.t("m.Update_Successfully"));
            let data = res.data.data;
            // 更新数据列表
            this.submissions[this.changeJudgeStatus.index].status = data.status;
            this.submissions[this.changeJudgeStatus.index].score = data.score;
            this.submissions[this.changeJudgeStatus.index].oiRankScore =
              data.oiRankScore;
            this.submissions[this.changeJudgeStatus.index].judger = data.judger;
            this.submissions[this.changeJudgeStatus.index].isManual = true;
            // 更新view中的结果，f分数，耗时，空间消耗，判题机ip
            let viewData = this.$refs.xTable.getTableData().tableData;
            let row = viewData[this.changeJudgeStatus.index];
            row.status = data.status;
            row.score = data.score;
            row.oiRankScore = data.oiRankScore;
            row.judger = data.judger;
            row.isManual = true;
            // 重新加载这行数据到view中
            this.$refs.xTable.reloadRow(row, null, null);
            this.changeJudgeStatusLoading = false;
            this.changeJudgeStatusDialogVisible = false;
          },
          (err) => {
            this.changeJudgeStatusLoading = false;
          }
        )
        .catch(() => {});
    },
  },
  computed: {
    ...mapGetters([
      "isAuthenticated",
      "userInfo",
      "isSuperAdmin",
      "isAdminRole",
      "contestRuleType",
      "contestStatus",
      "ContestRealTimePermission",
    ]),
    title() {
      if (!this.contestID) {
        return "Status";
      } else if (this.problemID) {
        return "Problem Submissions";
      } else {
        return "Submissions";
      }
    },
    status() {
      return this.formFilter.status === ""
        ? this.$i18n.t("m.Status")
        : JUDGE_STATUS[this.formFilter.status]
        ? JUDGE_STATUS[this.formFilter.status].name
        : this.$i18n.t("m.Status");
    },
    rejudgeColumnVisible() {
      return this.isSuperAdmin;
    },
    scoreColumnVisible() {
      return (
        (this.contestID && this.contestRuleType == this.RULE_TYPE.OI) ||
        !this.contestID
      );
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        if (this.autoCheckOpen) {
          clearInterval(this.refreshStatus);
        }
        this.init();
        this.getData();
      }
    },
    isAuthenticated() {
      this.init();
      this.getData();
    },
  },
  beforeRouteLeave(to, from, next) {
    // 防止切换组件后仍然不断请求
    clearInterval(this.refreshStatus);
    next();
  },
};
</script>

<style scoped>
@media only screen and (max-width: 767px) {
  .search {
    margin-top: 20px;
  }
}

.flex-container #main {
  flex: auto;
}
.flex-container .filter {
  margin-right: -10px;
}
.flex-container #contest-menu {
  flex: none;
  width: 210px;
}
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
  text-align: center;
}

/deep/ .el-dialog {
  border-radius: 6px !important;
  text-align: center;
}
/deep/ .el-switch {
  padding-top: 6px;
}
@media only screen and (min-width: 768px) and (max-width: 992px) {
  .el-col-sm-12 {
    padding-top: 10px;
  }
}
@media screen and (min-width: 1350px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
/deep/.vxe-table .vxe-cell{
  padding: 0 !important;
}
/deep/.el-dialog--center .el-dialog__body {
  padding-bottom: 0px !important;
}
.manual-judge-title {
  text-align: center;
  font-weight: bolder;
  font-size: 15px;
  margin-bottom: 5px;
}
</style>
