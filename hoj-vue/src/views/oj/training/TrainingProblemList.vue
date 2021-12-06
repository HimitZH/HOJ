<template>
  <div class="problem-list">
    <vxe-table
      border="inner"
      stripe
      auto-resize
      highlight-hover-row
      :data="problemList"
      align="center"
      @cell-click="goTrainingProblem"
    >
      <vxe-table-column
        field="status"
        title=""
        width="50"
        v-if="isAuthenticated && isGetStatusOk"
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
      <vxe-table-column field="displayId" width="80" title="#">
        <template v-slot="{ row }">
          <span>{{ row.displayId }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="displayTitle"
        :title="$t('m.Title')"
        min-width="200"
      ></vxe-table-column>

      <vxe-table-column
        field="difficulty"
        :title="$t('m.Level')"
        min-width="100"
      >
        <template v-slot="{ row }">
          <span
            class="el-tag el-tag--small"
            :style="getLevelColor(row.difficulty)"
            >{{ PROBLEM_LEVEL[row.difficulty].name }}</span
          >
        </template>
      </vxe-table-column>

      <vxe-table-column field="ac" :title="$t('m.AC')" min-width="80">
        <template v-slot="{ row }">
          <span>
            {{ row.ac }}
          </span>
        </template>
      </vxe-table-column>
      <vxe-table-column field="total" :title="$t('m.Total')" min-width="80">
        <template v-slot="{ row }">
          <span>
            {{ row.total }}
          </span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="ACRating"
        :title="$t('m.AC_Rate')"
        min-width="80"
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
import { JUDGE_STATUS, PROBLEM_LEVEL } from '@/common/constants';
import api from '@/common/api';
export default {
  name: 'TrainingProblemList',
  data() {
    return {
      JUDGE_STATUS: {},
      PROBLEM_LEVEL: {},
      isGetStatusOk: false,
      testcolor: 'rgba(0, 206, 209, 1)',
    };
  },
  mounted() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
    this.getTrainingProblemList();
  },
  methods: {
    getTrainingProblemList() {
      this.$store.dispatch('getTrainingProblemList').then((res) => {
        if (this.isAuthenticated) {
          // 如果已登录，则需要查询对当前页面题目列表中各个题目的提交情况
          let pidList = [];
          if (this.problemList && this.problemList.length > 0) {
            for (let index = 0; index < this.problemList.length; index++) {
              pidList.push(this.problemList[index].pid);
            }
            api.getUserProblemStatus(pidList, false).then((res) => {
              let result = res.data.data;
              for (let index = 0; index < this.problemList.length; index++) {
                this.problemList[index]['myStatus'] =
                  result[this.problemList[index].pid]['status'];
              }
              this.isGetStatusOk = true;
            });
          }
        }
      });
    },
    goTrainingProblem(event) {
      this.$router.push({
        name: 'TrainingProblemDetails',
        params: {
          contestID: this.$route.params.trainingID,
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
    getLevelColor(difficulty) {
      if (difficulty != undefined && difficulty != null) {
        return (
          'color: #fff !important;background-color:' +
          this.PROBLEM_LEVEL[difficulty]['color'] +
          ' !important;'
        );
      }
    },
  },
  computed: {
    ...mapState({
      problemList: (state) => state.training.trainingProblemList,
    }),
    ...mapGetters(['isAuthenticated']),
  },
};
</script>

<style scoped>
@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
</style>
