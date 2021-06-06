<template>
  <div class="problem-list">
    <vxe-table
      border="inner"
      stripe
      auto-resize
      highlight-hover-row
      :data="problems"
      @cell-click="goContestProblem"
    >
      <!-- OI赛制的最近提交显示 -->
      <vxe-table-column
        field="status"
        title=""
        width="50"
        v-if="
          isAuthenticated && isGetStatusOk && contestRuleType == RULE_TYPE.OI
        "
      >
        <template v-slot="{ row }">
          <span :class="getScoreColor(row.score)" v-if="row.score != null">{{
            row.score
          }}</span>
          <el-tooltip
            :content="JUDGE_STATUS[row.myStatus]['name']"
            placement="top"
            v-else
          >
            <i
              class="el-icon-minus"
              :style="getIconColor(row.myStatus)"
              v-if="row.myStatus != -10"
            ></i>
          </el-tooltip>
        </template>
      </vxe-table-column>

      <!-- ACM赛制的最近提交显示 -->
      <vxe-table-column
        field="status"
        title=""
        width="50"
        v-if="
          isAuthenticated && isGetStatusOk && contestRuleType == RULE_TYPE.ACM
        "
      >
        <template v-slot="{ row }">
          <el-tooltip
            :content="JUDGE_STATUS[row.myStatus]['name']"
            placement="top"
          >
            <i
              class="el-icon-check"
              :style="getIconColor(row.myStatus)"
              v-if="row.myStatus == 0"
            ></i>
            <i
              class="el-icon-minus"
              :style="getIconColor(row.myStatus)"
              v-else-if="row.myStatus != -10"
            ></i>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="displayId"
        width="80"
        title="#"
      ></vxe-table-column>
      <vxe-table-column
        field="displayTitle"
        :title="$t('m.Title')"
        min-width="200"
      ></vxe-table-column>

      <!-- 以下列只有在实时刷新榜单的情况下才显示 -->
      <vxe-table-column
        field="ac"
        :title="$t('m.AC')"
        min-width="80"
        v-if="ContestRealTimePermission"
      ></vxe-table-column>
      <vxe-table-column
        field="total"
        :title="$t('m.Total')"
        min-width="80"
        v-if="ContestRealTimePermission"
      ></vxe-table-column>
      <vxe-table-column
        field="ACRating"
        :title="$t('m.AC_Rate')"
        min-width="80"
        v-if="ContestRealTimePermission"
      >
        <template v-slot="{ row }">
          <span>{{ getACRate(row.ac, row.total) }}</span>
        </template>
      </vxe-table-column>
    </vxe-table>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex';
import utils from '@/common/utils';
import { JUDGE_STATUS, RULE_TYPE } from '@/common/constants';
import api from '@/common/api';
export default {
  name: 'ContestProblemList',
  data() {
    return {
      JUDGE_STATUS: {},
      RULE_TYPE: {},
      isGetStatusOk: false,
    };
  },
  mounted() {
    this.RULE_TYPE = Object.assign({}, RULE_TYPE);
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.getContestProblems();
  },
  methods: {
    getContestProblems() {
      this.$store.dispatch('getContestProblems').then((res) => {
        if (this.isAuthenticated) {
          let isContestProblemList = true;
          // 如果已登录，则需要查询对当前页面题目列表中各个题目的提交情况
          let pidList = [];
          for (let index = 0; index < this.problems.length; index++) {
            pidList.push(this.problems[index].pid);
          }
          api
            .getUserProblemStatus(
              pidList,
              isContestProblemList,
              this.$route.params.contestID
            )
            .then((res) => {
              let result = res.data.data;
              for (let index = 0; index < this.problems.length; index++) {
                this.problems[index]['myStatus'] =
                  result[this.problems[index].pid]['status'];
                this.problems[index]['score'] =
                  result[this.problems[index].pid]['score'];
              }
              this.isGetStatusOk = true;
            });
        }
      });
    },
    goContestProblem(event) {
      this.$router.push({
        name: 'ContestProblemDetails',
        params: {
          contestID: this.$route.params.contestID,
          problemID: event.row.displayId,
        },
      });
    },
    getACRate(ACCount, TotalCount) {
      return utils.getACRate(ACCount, TotalCount);
    },
    getIconColor(status) {
      return (
        'font-weight: 600;font-size: 16px;color:' + JUDGE_STATUS[status].rgb
      );
    },
    getScoreColor(score) {
      if (score == 0) {
        return 'el-tag el-tag--small oi-0';
      } else if (score > 0 && score < 100) {
        return 'el-tag el-tag--small oi-between';
      } else if (score == 100) {
        return 'el-tag el-tag--small oi-100';
      }
    },
  },
  computed: {
    ...mapState({
      problems: (state) => state.contest.contestProblems,
    }),
    ...mapGetters([
      'isAuthenticated',
      'contestRuleType',
      'ContestRealTimePermission',
    ]),
  },
};
</script>

<style scoped></style>
