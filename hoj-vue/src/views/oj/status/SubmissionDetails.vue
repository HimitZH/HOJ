<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24" id="status">
      <el-alert
        :type="status.type"
        show-icon
        :closable="false"
        effect="dark"
        :class="getbackgroudColor(submission.status)"
        style="padding: 18px;"
      >
        <template slot="title">
          <span class="title">{{ status.statusName }}</span>
        </template>
        <template slot>
          <div v-if="isCE || isSE || isSF" class="content">
            <pre>{{ submission.errorMessage }}</pre>
          </div>
          <div v-else class="content">
            <span class="span-row"
              >{{ $t('m.Time') }}:
              {{ submissionTimeFormat(submission.time) }}</span
            >
            <span class="span-row"
              >{{ $t('m.Memory') }}:
              {{ submissionMemoryFormat(submission.memory) }}</span
            >
            <span class="span-row"
              >{{ $t('m.Length') }}:
              {{ submissionLengthFormat(submission.length) }}</span
            >
            <span class="span-row"
              >{{ $t('m.Language') }}: {{ submission.language }}</span
            >
            <span class="span-row"
              >{{ $t('m.Author') }}: {{ submission.username }}</span
            >
          </div>
        </template>
      </el-alert>
    </el-col>

    <el-col v-if="tableData && !isCE" :span="24">
      <vxe-table
        align="center"
        :data="tableData"
        stripe
        auto-resize
        style="padding-top: 13px;"
        :loading="loadingTable"
      >
        <vxe-table-column
          field="submitId"
          :title="$t('m.Run_ID')"
          width="100"
        ></vxe-table-column>
        <vxe-table-column :title="$t('m.Submit_Time')" min-width="160">
          <template v-slot="{ row }">
            <span>{{ row.submitTime | localtime }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="pid"
          :title="$t('m.Problem_ID')"
          min-width="100"
        >
          <template v-slot="{ row }">
            <a @click="getProblemUri(row)" style="color: rgb(87, 163, 243)">{{
              row.displayPid
            }}</a>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="status"
          :title="$t('m.Status')"
          min-width="160"
        >
          <template v-slot="{ row }">
            <span :class="getStatusColor(row.status)">{{
              JUDGE_STATUS[row.status].name
            }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Time')" min-width="96">
          <template v-slot="{ row }">
            <span>{{ submissionTimeFormat(row.time) }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Memory')" min-width="96">
          <template v-slot="{ row }">
            <span>{{ submissionMemoryFormat(row.memory) }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          :title="$t('m.Score')"
          min-width="64"
          v-if="isIOProblem"
        >
          <template v-slot="{ row }">
            <template v-if="row.score != null">
              <el-tooltip placement="top">
                <div slot="content">
                  {{ $t('m.Problem_Score') }}：{{
                    row.score != null ? row.score : $t('m.Nothing')
                  }}<br />{{ $t('m.OI_Rank_Score') }}：{{
                    row.oiRankScore != null ? row.oiRankScore : $t('m.Nothing')
                  }}<br />
                  {{
                    $t('m.OI_Rank_Calculation_Rule')
                  }}:(score*0.1+diffculty*2)*(ac_testcase/sum_testcase)
                </div>
                <span>{{ row.score }}</span>
              </el-tooltip>
            </template>
            <template v-else>
              <span>--</span>
            </template>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Length')" min-width="80">
          <template v-slot="{ row }">
            <span>{{ submissionLengthFormat(row.length) }}</span>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-col>
    <el-col
      :span="24"
      v-if="testCaseResult != null && testCaseResult.length > 0"
    >
      <el-card style="margin-top: 13px;" shadow="hover">
        <div slot="header">
          <span class="panel-title home-title">{{
            $t('m.Test_point_details')
          }}</span>
        </div>
        <el-row :gutter="10">
          <el-col
            :xs="24"
            :sm="8"
            :md="6"
            :lg="3"
            v-for="(item, index) in testCaseResult"
            :key="index"
          >
            <el-tooltip placement="top">
              <div slot="content">
                <template v-if="item.inputData">
                  {{ $t('m.Input_File') }}：{{ item.inputData }}<br />
                </template>

                <template v-if="item.outputData">
                  {{ $t('m.Output_File') }}：{{ item.outputData }}<br />
                </template>

                {{ $t('m.Case_tips') }}：{{
                  item.userOutput ? item.userOutput : $t('m.Nothing')
                }}
              </div>
              <div
                class="test-detail-item"
                :style="getTestCaseResultColor(item.status)"
                v-if="item.status == JUDGE_STATUS_RESERVE.ac"
              >
                <span>Test #{{ index + 1 }}:</span>
                <h2>{{ JUDGE_STATUS[item.status]['short'] }}</h2>
                <div style="text-align:center;">
                  {{ item.time }}ms/{{ item.memory }}KB
                </div>
                <div class="test-run-static">
                  <span v-if="item.score != null">
                    {{ item.score }} <i class="el-icon-success"></i>
                  </span>
                  <span v-else>
                    <i class="el-icon-success"></i>
                  </span>
                </div>
              </div>

              <div
                class="test-detail-item"
                :style="getTestCaseResultColor(item.status)"
                v-else
              >
                <span>Test #{{ index + 1 }}: </span>
                <h2>{{ JUDGE_STATUS[item.status]['short'] }}</h2>
                <div style="text-align:center;">
                  {{ item.time }}ms/{{ item.memory }}KB
                </div>
                <div class="test-run-static">
                  <span v-if="item.score != null">
                    {{ item.score }} <i class="el-icon-error"></i>
                  </span>
                  <span v-else>
                    <i class="el-icon-error"></i>
                  </span>
                </div>
              </div>
            </el-tooltip>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
    <template
      v-if="
        (submission.code && submission.share && codeShare) ||
          isSubmissionOwner ||
          isAdminRole ||
          (submission.code && submission.cid!=0)
      "
    >
      <el-col :span="24" style="margin-top: 13px;" v-if="submission.code">
        <Highlight
          :code="submission.code"
          :language="submission.language"
          :border-color.sync="status.color"
        ></Highlight>
      </el-col>
      <el-col :span="24">
        <div id="share-btn">
          <el-button
            type="primary"
            icon="el-icon-document-copy"
            size="large"
            @click="doCopy"
            v-if="submission.code"
            >{{ $t('m.Copy') }}</el-button
          >
          <template v-if="codeShare && isSubmissionOwner">
            <el-button
              v-if="submission.share"
              type="warning"
              size="large"
              icon="el-icon-circle-close"
              @click="shareSubmission(false)"
            >
              {{ $t('m.Unshared') }}
            </el-button>
            <el-button
              v-else-if="!submission.share"
              type="primary"
              size="large"
              icon="el-icon-share"
              @click="shareSubmission(true)"
            >
              {{ $t('m.Shared') }}
            </el-button>
          </template>
        </div>
      </el-col>
    </template>
  </el-row>
</template>

<script>
import api from '@/common/api';
import { JUDGE_STATUS, JUDGE_STATUS_RESERVE } from '@/common/constants';
import utils from '@/common/utils';
import myMessage from '@/common/message';
import { addCodeBtn } from '@/common/codeblock';
import Highlight from '@/components/oj/common/Highlight';

export default {
  name: 'submissionDetails',
  components: {
    Highlight,
  },
  data() {
    return {
      submission: {
        code: '',
        submitId: '',
        submitTime: '',
        pid: '',
        cid: '',
        displayPid: '',
        status: 0,
        time: '',
        memory: '',
        language: '',
        author: '',
        errorMessage: '',
        share: true,
      },
      tableData: [],
      testCaseResult: [],
      codeShare: true,
      isIOProblem: false,
      loadingTable: false,
      JUDGE_STATUS: '',
      JUDGE_STATUS_RESERVE: '',
    };
  },
  mounted() {
    this.getSubmission();
    this.getAllCaseResult();
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.JUDGE_STATUS_RESERVE = Object.assign({}, JUDGE_STATUS_RESERVE);
  },
  methods: {
    doCopy() {
      this.$copyText(this.submission.code).then(
        () => {
          myMessage.success(this.$i18n.t('m.Copied_successfully'));
        },
        () => {
          myMessage.success(this.$i18n.t('m.Copied_failed'));
        }
      );
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

    getProblemUri(row) {
      if (row.cid != 0) {
        // 比赛题目
        this.$router.push({
          name: 'ContestProblemDetails',
          params: {
            contestID: row.cid,
            problemID: row.displayPid,
          },
        });
      } else if (row.gid) {
        this.$router.push({
          name: 'GroupProblemDetails',
          params: {
            problemID: row.displayPid,
            groupID: row.gid,
          },
        });
      } else {
        this.$router.push({
          name: 'ProblemDetails',
          params: {
            problemID: row.displayPid,
          },
        });
      }
    },
    getStatusColor(status) {
      return 'el-tag el-tag--medium status-' + JUDGE_STATUS[status].color;
    },
    getTestCaseResultColor(status) {
      return (
        'color:' +
        JUDGE_STATUS[status].rgb +
        '!important;border-color:' +
        JUDGE_STATUS[status].rgb +
        '!important'
      );
    },
    getbackgroudColor(status) {
      return 'status-' + JUDGE_STATUS[status].color;
    },
    getSubmission() {
      this.loadingTable = true;
      api.getSubmission(this.$route.params.submitID).then(
        (res) => {
          this.loadingTable = false;
          let data = res.data.data;
          if (
            data.submission.memory &&
            data.submission.score &&
            !this.isIOProblem
          ) {
            // score exist means the submission is OI problem submission
            if (data.submission.score !== null) {
              this.isIOProblem = true;
            }
          }
          // 如果是比赛 需要显示的是比赛题号
          if (this.$route.params.problemID && data.submission.cid != 0) {
            data.submission.displayPid = this.$route.params.problemID;
          }
          this.submission = data.submission;
          this.tableData = [data.submission];
          if (data.submission.cid != 0) {
            // 比赛的提交不可分享
            this.codeShare = false;
          } else {
            this.codeShare = data.codeShare;
          }

          this.$nextTick((_) => {
            addCodeBtn();
          });
        },
        () => {
          this.loadingTable = false;
        }
      );
    },

    //首先该题必须支持开放测试点结果查看，同时若是比赛题目，只支持IO查看测试点情况，ACM强制禁止查看
    getAllCaseResult() {
      api.getAllCaseResult(this.$route.params.submitID).then((res) => {
        this.testCaseResult = res.data.data;
      });
    },

    shareSubmission(shared) {
      let data = {
        submitId: this.submission.submitId,
        share: shared,
        uid: this.submission.uid,
      };
      api.updateSubmission(data).then(
        (res) => {
          this.getSubmission();
          if (shared) {
            myMessage.success(this.$i18n.t('m.Shared_successfully'));
          } else {
            myMessage.success(this.$i18n.t('m.Cancel_Sharing_Successfully'));
          }
        },
        () => {}
      );
    },
  },
  watch: {
    submission(newVal, oldVal) {
      if (newVal.code) {
        this.$nextTick((_) => {
          addCodeBtn();
        });
      }
    },
  },
  computed: {
    status() {
      return {
        type: JUDGE_STATUS[this.submission.status].type,
        statusName: JUDGE_STATUS[this.submission.status].name,
        color: JUDGE_STATUS[this.submission.status].rgb,
      };
    },
    isCE() {
      return this.submission.status === JUDGE_STATUS_RESERVE.ce;
    },
    isSE() {
      return this.submission.status === JUDGE_STATUS_RESERVE.se;
    },
    isSF() {
      return this.submission.status === JUDGE_STATUS_RESERVE.sf;
    },
    isAdminRole() {
      return this.$store.getters.isAdminRole;
    },
    isAuthenticated() {
      return this.$store.getters.isAuthenticated;
    },
    isSubmissionOwner() {
      return this.$store.getters.userInfo.uid === this.submission.uid;
    }
  },
};
</script>

<style scoped>
#status .title {
  font-size: 20px;
}
#status .content {
  margin-top: 10px;
  font-size: 14px;
}
#status .content span {
  margin-right: 10px;
}
#status .span-row {
  display: block;
  float: left;
}
#status .content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-all;
}

#share-btn {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}
#share-btn:nth-child(1) {
  margin-right: 0px;
}
.el-row--flex {
  flex-wrap: wrap;
}
.el-col {
  padding-left: 5px !important;
  padding-right: 5px !important;
}

.test-detail-item {
  width: 100%;
  padding: 5px;
  font-size: 14px;
  display: inline-block;
  vertical-align: top;
  border-radius: 3px;
  border: 1px solid #ff431e;
  border-left: 3px solid #ff431e;
  color: #ff431e;
  margin: 0 0 10px 0;
}
.test-detail-item h2 {
  font-weight: bolder;
  text-align: center;
  margin: 2px;
  padding: 0;
}
.test-detail-item > span {
  margin-right: 10px;
}
.test-run-static {
  text-align: center;
}
.test-detail-item.done {
  border-color: #25bb9b;
  color: #25bb9b;
}
@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
</style>
