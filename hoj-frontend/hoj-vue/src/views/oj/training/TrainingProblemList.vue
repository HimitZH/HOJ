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
        v-if="isAuthenticated"
      >
        <template v-slot="{ row }">
          <template v-if="isGetStatusOk">
            <el-tooltip
              :content="JUDGE_STATUS[row.myStatus]['name']"
              placement="top"
            >
              <template v-if="row.myStatus == 0">
                <i
                  class="el-icon-check"
                  :style="getIconColor(row.myStatus)"
                ></i>
              </template>

              <template v-else-if="row.myStatus != -10">
                <i
                  class="el-icon-minus"
                  :style="getIconColor(row.myStatus)"
                ></i>
              </template>
            </el-tooltip>
          </template>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="problemId"
        :title="$t('m.Problem_ID')"
        width="150"
        show-overflow
      >
      </vxe-table-column>
      <vxe-table-column
        field="title"
        :title="$t('m.Title')"
        min-width="150"
        show-overflow
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
            >{{ getLevelName(row.difficulty) }}</span
          >
        </template>
      </vxe-table-column>

      <vxe-table-column field="tag" min-width="100">
        <template v-slot:header
          ><el-link
            type="primary"
            v-if="!showTags"
            :underline="false"
            @click="showTags = !showTags"
            >{{ $t('m.Show_Tags') }}</el-link
          >
          <el-link
            type="danger"
            v-else
            @click="showTags = !showTags"
            :underline="false"
            >{{ $t('m.Hide_Tags') }}</el-link
          >
        </template>
        <template v-slot="{ row }">
          <div v-if="showTags">
            <span
              class="el-tag el-tag--small"
              :style="
                'margin-right:7px;color:#FFF;background-color:' +
                  (tag.color ? tag.color : '#409eff')
              "
              v-for="tag in row.tags"
              :key="tag.id"
              >{{ tag.name }}</span
            >
          </div>
        </template>
      </vxe-table-column>

      <vxe-table-column field="ac" :title="$t('m.AC_Rate')" min-width="120">
        <template v-slot="{ row }">
          <span>
            <el-tooltip
              effect="dark"
              :content="row.ac + '/' + row.total"
              placement="top"
            >
              <el-progress
                :text-inside="true"
                :stroke-width="20"
                :percentage="getPassingRate(row.ac, row.total)"
              ></el-progress>
            </el-tooltip>
          </span>
        </template>
      </vxe-table-column>
    </vxe-table>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex';
import utils from '@/common/utils';
import { JUDGE_STATUS } from '@/common/constants';
import api from '@/common/api';
export default {
  name: 'TrainingProblemList',
  data() {
    return {
      JUDGE_STATUS: {},
      isGetStatusOk: false,
      testcolor: 'rgba(0, 206, 209, 1)',
      showTags: false,
      groupID:null,
    };
  },
  created(){
    let gid = this.$route.params.groupID;
    if(gid){
      this.groupID = gid;
    }
  },
  mounted() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
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
            this.isGetStatusOk = false;
            api.getUserProblemStatus(pidList, false,null,this.groupID).then((res) => {
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
      if(this.groupID){
        this.$router.push({
          name: 'GroupTrainingProblemDetails',
          params: {
            trainingID: this.$route.params.trainingID,
            problemID: event.row.problemId,
            groupID: this.groupID
          },
        });
      }else{
        this.$router.push({
          name: 'TrainingProblemDetails',
          params: {
            trainingID: this.$route.params.trainingID,
            problemID: event.row.problemId,
          },
        });
      }
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
      return utils.getLevelColor(difficulty);
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
    getPassingRate(ac, total) {
      if (!total) {
        return 0;
      }
      return ((ac / total) * 100).toFixed(2);
    },
  },
  computed: {
    ...mapState({
      problemList: (state) => state.training.trainingProblemList
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
