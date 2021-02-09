<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="22" id="status">
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
          <div v-if="isCE || isSE" class="content">
            <pre>{{ submission.errorMessage }}</pre>
          </div>
          <div v-else class="content">
            <span class="span-row"
              >Time: {{ submissionTimeFormat(submission.time) }}</span
            >
            <span class="span-row"
              >Memory: {{ submissionMemoryFormat(submission.memory) }}</span
            >
            <span class="span-row"
              >Length: {{ submissionLengthFormat(submission.length) }}</span
            >
            <span class="span-row">Language: {{ submission.language }}</span>
            <span class="span-row">Author: {{ submission.username }}</span>
          </div>
        </template>
      </el-alert>
    </el-col>

    <el-col v-if="tableData && !isCE" :span="22">
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
          title="ID"
          min-width="100"
        ></vxe-table-column>
        <vxe-table-column title="Submit time" min-width="150">
          <template v-slot="{ row }">
            <span>{{ row.submitTime | localtime }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="pid" title="Problem ID" min-width="100">
          <template v-slot="{ row }">
            <a
              :href="getProblemUri(row.pid)"
              style="color: rgb(87, 163, 243)"
              >{{ row.pid }}</a
            >
          </template>
        </vxe-table-column>
        <vxe-table-column field="status" title="Status" min-width="170">
          <template v-slot="{ row }">
            <span :class="getStatusColor(row.status)">{{
              JUDGE_STATUS[row.status].name
            }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Time" min-width="64">
          <template v-slot="{ row }">
            <span>{{ submissionTimeFormat(row.time) }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Memory" min-width="96">
          <template v-slot="{ row }">
            <span>{{ submissionMemoryFormat(row.memory) }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Score" min-width="64" v-if="isIOProblem">
          <template v-slot="{ row }">
            <span>{{ row.score }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Length" min-width="60">
          <template v-slot="{ row }">
            <span>{{ submissionLengthFormat(row.length) }}</span>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-col>

    <el-col :span="22">
      <Highlight
        :code="submission.code"
        :language="submission.language"
        :border-color="status.color"
      ></Highlight>
    </el-col>
    <el-col :span="22">
      <div id="share-btn">
        <el-button
          type="primary"
          icon="el-icon-document-copy"
          size="large"
          @click="doCopy"
          v-if="submission.code"
          >Copy</el-button
        >
        <template v-if="codeShare">
          <el-button
            v-if="submission.share && isAuthenticated && isMeSubmisson"
            type="warning"
            size="large"
            icon="el-icon-circle-close"
            @click="shareSubmission(false)"
          >
            Unshared
          </el-button>
          <el-button
            v-else-if="isAuthenticated && !submission.share && isMeSubmisson"
            type="primary"
            size="large"
            icon="el-icon-share"
            @click="shareSubmission(true)"
          >
            Shared
          </el-button>
        </template>
      </div>
    </el-col>

    <el-col :span="22" v-if="testCaseResult">
      <el-card style="margin-top: 20px;">
        <div slot="header">
          <span class="panel-title home-title">测试点详情</span>
        </div>
        <el-row :gutter="10">
          <el-col
            :xs="24"
            :md="12"
            :lg="6"
            v-for="(item, index) in testCaseResult"
            :key="index"
          >
            <div
              class="test-detail-item"
              :style="getTestCaseResultColor(item.status)"
              v-if="item.status == JUDGE_STATUS_RESERVE.ac"
            >
              <span>Test #{{ index + 1 }}:</span
              ><span>{{ item.time }}ms/{{ item.memory }}KB</span>
              <span class="test-run-static" v-if="item.score != null">
                {{ item.score }}分 <i class="el-icon-success"></i>
              </span>
              <span class="test-run-static" v-else>
                <i class="el-icon-success"></i>
              </span>
            </div>

            <div
              class="test-detail-item"
              :style="getTestCaseResultColor(item.status)"
              v-else
            >
              <span>Test #{{ index + 1 }}:</span
              ><span>{{ item.time }}ms/{{ item.memory }}KB</span>
              <span class="test-run-static" v-if="item.score != null">
                {{ item.score }}分 <i class="el-icon-error"></i>
              </span>
              <span class="test-run-static" v-else>
                <i class="el-icon-error"></i>
              </span>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import api from '@/common/api';
import { JUDGE_STATUS, JUDGE_STATUS_RESERVE } from '@/common/constants';
import utils from '@/common/utils';
import Highlight from '@/components/oj/common/Highlight';
import myMessage from '@/common/message';

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
        function(e) {
          myMessage.success('Code copied successfully');
        },
        function(e) {
          myMessage.success('Code copy failed');
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

    getProblemUri(pid) {
      return '/problem/' + pid;
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
          this.submission = data.submission;
          this.tableData = [data.submission];
          if (data.submission.cid != 0) {
            // 比赛的提交不可分享
            this.codeShare = false;
          } else {
            this.codeShare = data.codeShare;
          }
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
          myMessage.success(res.data.msg);
        },
        () => {}
      );
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
    isAdminRole() {
      return this.$store.getters.isAdminRole;
    },
    isAuthenticated() {
      return this.$store.getters.isAuthenticated;
    },
    isMeSubmisson() {
      return this.$store.getters.userInfo.uid === this.submission.uid;
    },
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
pre {
  border: none;
  background: none;
  padding-top: 13px;
}
.test-detail-item {
  width: 100%;
  padding: 15px;
  font-size: 14px;
  display: inline-block;
  vertical-align: top;
  border-radius: 3px;
  border: 1px solid #ff431e;
  border-left: 3px solid #ff431e;
  color: #ff431e;
  margin: 0 0 10px 0;
}
.test-detail-item > span {
  margin-right: 10px;
}
.test-run-static {
  float: right;
}
.test-detail-item.done {
  border-color: #25bb9b;
  color: #25bb9b;
}
</style>
