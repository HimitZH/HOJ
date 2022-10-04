<template>
  <el-card
    :shadow="isSubtask?'never':'hover'"
    :class="isSubtask?'subtask-list-card':'default-list-card'"
  >
    <template v-if="!isSubtask">
      <div slot="header">
        <span class="panel-title home-title">
          {{$t('m.Test_point_details')}}
        </span>
      </div>
    </template>
    <el-row :gutter="10">
      <el-col
        :xs="24"
        :sm="8"
        :md="6"
        :lg="3"
        v-for="(item, index) in judgeCaseList"
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
            <h2 v-if="item.status != -4">{{ JUDGE_STATUS[item.status]['short'] }}</h2>
            <h2 v-else>Skipped</h2>
            <div style="text-align:center;">
              {{ submissionTimeFormat(item.time) }}/{{ submissionMemoryFormat(item.memory) }}
            </div>
            <div class="test-run-static">
              <span v-if="item.score != null">
                {{ item.score }} pts <i class="el-icon-success"></i>
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
            <h2 v-if="item.status != -4">{{ JUDGE_STATUS[item.status]['short'] }}</h2>
            <h2 v-else>Skipped</h2>
            <div style="text-align:center;">
              {{ submissionTimeFormat(item.time) }}/{{ submissionMemoryFormat(item.memory) }}
            </div>
            <div class="test-run-static">
              <span v-if="item.score != null">
                {{ item.score }} pts <i class="el-icon-error"></i>
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
</template>

<script>
import utils from "@/common/utils";
import { JUDGE_STATUS, JUDGE_STATUS_RESERVE } from "@/common/constants";
export default {
  name: "judgeCase",
  props: {
    judgeCaseList: {
      required: true,
      type: Array,
    },
    isSubtask: {
      default: false,
      type: Boolean,
    },
  },
  data() {
    return {
      JUDGE_STATUS: {},
      JUDGE_STATUS_RESERVE: {},
    };
  },
  created() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.JUDGE_STATUS_RESERVE = Object.assign({}, JUDGE_STATUS_RESERVE);
  },
  methods: {
    submissionTimeFormat(time) {
      return utils.submissionTimeFormat(time);
    },
    submissionMemoryFormat(memory) {
      return utils.submissionMemoryFormat(memory);
    },
    getTestCaseResultColor(status) {
      return (
        "color:" +
        this.JUDGE_STATUS[status]["rgb"] +
        "!important;border-color:" +
        this.JUDGE_STATUS[status]["rgb"] +
        "!important"
      );
    },
  },
};
</script>

<style scoped>
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
.subtask-list-card {
  margin: 0em 1em 1.5em;
  border-color: rgba(34, 36, 38, 0.15);
  box-shadow: 0 1px 2px 0 rgb(34 36 38 / 15%);
}
.default-list-card {
  margin-top: 13px;
}
</style>