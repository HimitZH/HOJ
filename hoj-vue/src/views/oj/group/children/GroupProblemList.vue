<template>
  <el-card>
    <div class="filter-row">
      <el-row>
        <el-col :md="3" :xs="5">
          <span class="title">{{ $t('m.Group_Problem') }}</span>
        </el-col>
        <el-col :md="18" :xs="19" v-if="isSuperAdmin || isGroupAdmin">
          <el-button
            v-if="!editPage"
            :type="createPage ? 'primary' : 'primary'"
            size="small"
            @click="handleCreatePage"
            :icon="createPage ? 'el-icon-back' : 'el-icon-plus'"
            >{{ createPage ? $t('m.Back_To_Problem_List') : $t('m.Create') }}</el-button
          >
          <el-button
            v-if="editPage && adminPage"
            type="primary"
            size="small"
            @click="handleEditPage"
            icon="el-icon-back"
            >{{ $t('m.Back_To_Admin_Problem_List') }}</el-button
          >`
          <el-button
            :type="adminPage ? 'danger' : 'success'"
            size="small"
            @click="handleAdminPage"
            :icon="adminPage ? 'el-icon-circle-close' : 'el-icon-s-opportunity'"
            >{{ adminPage ? $t('m.Cancel_Admin') : $t('m.Problem_Admin') }}</el-button
          >
        </el-col>
      </el-row>
    </div>
    <div v-if="!adminPage && !createPage">
      <vxe-table
        border="inner"
        stripe
        auto-resize
        highlight-hover-row
        :data="problemList"
        :loading="loading"
        align="center"
        @cell-click="goGroupProblem"
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
                :content="JUDGE_STATUS[row['myStatus']]['name']"
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
        >
        </vxe-table-column>
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
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="currentChange"
        :current.sync="currentPage"
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </div>
    <ProblemList
      ref="problemList"
      v-if="adminPage && !createPage"
      @handleEditPage="handleEditPage"
      @currentChange="currentChange"
    ></ProblemList>
    <Problem
      v-if="createPage && !editPage"
      mode="add"
      :title="title"
      :apiMethod="apiMethod"
      :contestID="contestID"
      :pid="pid"
      @handleCreatePage="handleCreatePage"
      @currentChange="currentChange"
    ></Problem>
  </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import utils from '@/common/utils';
import { JUDGE_STATUS } from '@/common/constants';
import Pagination from '@/components/oj/common/Pagination';
import ProblemList from '@/components/oj/group/ProblemList';
import Problem from '@/components/oj/group/Problem';
import api from '@/common/api';
export default {
  name: 'GroupProblemList',
  components: {
    Pagination,
    ProblemList,
    Problem,
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      problemList: [],
      contestID: null,
      pid: null,
      title: '',
      apiMethod: '',
      JUDGE_STATUS: {},
      isGetStatusOk: false,
      testcolor: 'rgba(0, 206, 209, 1)',
      showTags: false,
      loading: false,
      routeName: '',
      adminPage: false,
      createPage: false,
      editPage: false,
    };
  },
  mounted() {
    this.routeName = this.$route.name;
    if (this.routeName === 'GroupProblemList') {
      this.title = this.$t('m.Create_Problem');
      this.apiMethod = 'addGroupProblem';
    } else if (this.routeName === 'GroupContestProblemList') {
      this.title = this.$t('m.Create_Contest_Problem');
      this.apiMethod = 'addGroupContestProblem';
    }
    this.init();
  },
  methods: {
    init() {
      this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
      this.getGroupProblemList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    getGroupProblemList() {
      this.loading = true;
      api
        .getGroupProblemList(
          this.currentPage,
          this.limit,
          this.$route.params.groupID
        )
        .then(
          (res) => {
            this.problemList = res.data.data.records;
            this.total = res.data.data.total;
            this.loading = false;
            if (this.isAuthenticated) {
              let pidList = [];
              if (this.problemList && this.problemList.length > 0) {
                for (let index = 0; index < this.problemList.length; index++) {
                  pidList.push(this.problemList[index].pid);
                }
                this.isGetStatusOk = false;
                api
                  .getUserProblemStatus(
                    pidList,
                    false,
                    null,
                    this.$route.params.groupID
                  )
                  .then((res) => {
                    let result = res.data.data;
                    for (
                      let index = 0;
                      index < this.problemList.length;
                      index++
                    ) {
                      this.problemList[index]['myStatus'] =
                        result[this.problemList[index].pid]['status'];
                    }
                    this.isGetStatusOk = true;
                  });
              }
            }
          },
          (err) => {
            this.loading = false;
          }
        );
    },
    goGroupProblem(event) {
      this.$router.push({
        name: 'GroupFullProblemDetails',
        params: {
          problemID: event.row.problemId,
        },
      });
    },
    handleCreatePage() {
      this.createPage = !this.createPage;
    },
    handleEditPage() {
      this.editPage = !this.editPage;
      this.$refs.problemList.editPage = this.editPage;
    },
    handleAdminPage() {
      this.adminPage = !this.adminPage;
      this.createPage = false;
      this.editPage = false;
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
    ...mapGetters(['isAuthenticated', 'isSuperAdmin', 'isGroupAdmin']),
    isContest() {
      return !(this.routeName == 'GroupProblemList' && !this.contestId);
    },
  },
};
</script>

<style scoped>
.title {
  font-size: 20px;
  vertical-align: middle;
  float: left;
}
.filter-row {
  margin-bottom: 5px;
  text-align: center;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-left: 5px;
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-left: 10px;
    margin-right: 10px;
  }
}
</style>
