<template>
  <el-row :gutter="18">
    <el-col :sm="24" :md="18" :lg="18">
      <el-card shadow>
        <div slot="header">
          <el-row :gutter="18">
            <el-col :sm="5" :md="5" :lg="7">
              <span class="panel-title hidden-xs-only">{{
                $t('m.Problem_List')
              }}</span>
            </el-col>
            <el-col :xs="8" :sm="3" :md="3" :lg="3" style="padding-top: 6px;">
              <el-dropdown
                class="drop-menu"
                @command="filterByOJ"
                placement="bottom"
                trigger="hover"
              >
                <span class="el-dropdown-link">
                  {{
                    query.oj === 'Mine' || query.oj === ''
                      ? $t('m.My_OJ')
                      : query.oj === 'All'
                      ? $t('m.All')
                      : query.oj
                  }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="All">{{
                    $t('m.All')
                  }}</el-dropdown-item>
                  <el-dropdown-item command="Mine">{{
                    $t('m.My_OJ')
                  }}</el-dropdown-item>
                  <el-dropdown-item
                    :command="remoteOj.key"
                    v-for="(remoteOj, index) in REMOTE_OJ"
                    :key="index"
                    >{{ remoteOj.name }}</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown>
            </el-col>
            <el-col :xs="8" :sm="3" :md="3" :lg="3" style="padding-top: 6px;">
              <el-dropdown
                class="drop-menu"
                @command="filterByDifficulty"
                placement="bottom"
                trigger="hover"
              >
                <span class="el-dropdown-link">
                  {{
                    query.difficulty === 'All' || query.difficulty === ''
                      ? $t('m.Level')
                      : query.difficulty
                  }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="All">{{
                    $t('m.All')
                  }}</el-dropdown-item>
                  <el-dropdown-item
                    :command="key"
                    v-for="(value, key, index) in PROBLEM_LEVEL_RESERVE"
                    :key="index"
                    >{{ key }}</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown>
            </el-col>
            <el-col :xs="8" :sm="3" :md="2" :lg="3" style="padding-top: 6px;">
              <vxe-checkbox
                v-model="tagVisible"
                @change="changeTagVisible(tagVisible)"
                >{{ $t('m.Tags') }}</vxe-checkbox
              >
            </el-col>
            <el-col :xs="18" :sm="7" :md="7" :lg="5" class="top-pt">
              <vxe-input
                v-model="query.keyword"
                :placeholder="$t('m.Enter_keyword')"
                type="search"
                size="medium"
                @search-click="filterByKeyword"
                @keyup.enter.native="filterByKeyword"
              ></vxe-input>
            </el-col>
            <el-col :sm="3" :md="3" :lg="3" class="hidden-xs-only">
              <el-button
                type="primary"
                size="small"
                icon="el-icon-refresh"
                round
                @click="onReset"
                >{{ $t('m.Reset') }}</el-button
              >
            </el-col>
            <el-col :xs="6" class="hidden-sm-and-up top-pt">
              <el-button
                type="primary"
                size="small"
                icon="el-icon-refresh"
                circle
                @click="onReset"
              ></el-button>
            </el-col>
          </el-row>
        </div>
        <vxe-table
          border="inner"
          stripe
          ref="problemList"
          auto-resize
          :loading="loadings.table"
          @cell-mouseenter="cellHover"
          :data="problemList"
        >
          <vxe-table-column
            title=""
            width="30"
            v-if="isAuthenticated && isGetStatusOk"
          >
            <template v-slot="{ row }">
              <el-tooltip
                :content="JUDGE_STATUS[row.myStatus].name"
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
            field="problemId"
            :title="$t('m.Problem_ID')"
            min-width="100"
          ></vxe-table-column>

          <vxe-table-column
            field="title"
            :title="$t('m.Problem')"
            min-width="180"
          >
            <template v-slot="{ row }">
              <a @click="getProblemUri(row.problemId)" class="title-a">{{
                row.title
              }}</a>
            </template>
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
                >{{ PROBLEM_LEVEL[row.difficulty].name }}</span
              >
            </template>
          </vxe-table-column>

          <vxe-table-column
            field="tag"
            :title="$t('m.Tags')"
            min-width="250"
            visible="false"
          >
            <template v-slot="{ row }">
              <span
                class="el-tag el-tag--medium el-tag--light is-hit"
                style="margin-right: 7px;margin-top:4px"
                v-for="tag in row.tags"
                :key="tag.id"
                >{{ tag.name }}</span
              >
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="total"
            :title="$t('m.Total')"
            min-width="80"
          ></vxe-table-column>
          <vxe-table-column :title="$t('m.AC_Rate')" min-width="80">
            <template v-slot="{ row }">
              <span>{{ getPercentage(row.ac, row.total) }}%</span>
            </template>
          </vxe-table-column>
        </vxe-table>
      </el-card>
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="pushRouter"
        :current.sync="query.currentPage"
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </el-col>

    <el-col :sm="24" :md="6" :lg="6">
      <el-card style="text-align:center">
        <span class="panel-title">{{ currentProblemTitle }}</span>
        <el-row v-for="(record, index) in problemRecord" :key="index">
          <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">
            <el-tag
              effect="dark"
              size="small"
              :color="JUDGE_STATUS[record.status].rgb"
              >{{ JUDGE_STATUS[record.status].short }}</el-tag
            >
          </el-col>
          <el-col :xs="19" :sm="20" :md="18" :lg="20">
            <el-progress
              :text-inside="true"
              :stroke-width="20"
              :percentage="record.count"
              :color="JUDGE_STATUS[record.status].rgb"
            ></el-progress>
          </el-col>
        </el-row>
      </el-card>
      <el-card :padding="10" style="margin-top:20px">
        <div slot="header" style="text-align: center;">
          <span class="taglist-title">{{ OJName + ' ' + $t('m.Tags') }}</span>
        </div>
        <el-button
          v-for="tag in tagList"
          :key="tag.id"
          @click="filterByTag(tag.id)"
          type="ghost"
          :disabled="query.tagId == tag.id"
          size="mini"
          class="tag-btn"
          >{{ tag.name }}
        </el-button>

        <el-button long id="pick-one" @click="pickone">
          <i class="fa fa-random"></i>
          {{ $t('m.Pick_a_random_question') }}
        </el-button>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import { mapGetters } from 'vuex';
import api from '@/common/api';
import {
  PROBLEM_LEVEL,
  PROBLEM_LEVEL_RESERVE,
  JUDGE_STATUS,
  JUDGE_STATUS_RESERVE,
  REMOTE_OJ,
} from '@/common/constants';
import myMessage from '@/common/message';
import 'element-ui/lib/theme-chalk/display.css';
import Pagination from '@/components/oj/common/Pagination';
export default {
  name: 'ProblemList',
  components: {
    Pagination,
  },
  data() {
    return {
      PROBLEM_LEVEL: {},
      PROBLEM_LEVEL_RESERVE: {},
      JUDGE_STATUS: {},
      JUDGE_STATUS_RESERVE: {},
      REMOTE_OJ: {},
      tagList: [],
      tagVisible: false,
      currentProblemTitle: '',
      problemRecord: [],
      problemList: [],
      limit: 30,
      total: 100,
      isGetStatusOk: false,
      loadings: {
        table: true,
        tag: true,
      },
      page: {
        currentPage: 1,
        pageSize: 30,
        totalResult: 300,
      },
      filterConfig: { remote: true },
      routeName: '',
      query: {
        keyword: '',
        difficulty: 'All',
        oj: '',
        tagId: '',
        currentPage: 1,
      },
    };
  },

  mounted() {
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
    this.PROBLEM_LEVEL_RESERVE = Object.assign({}, PROBLEM_LEVEL_RESERVE);
    this.JUDGE_STATUS_RESERVE = Object.assign({}, JUDGE_STATUS_RESERVE);
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.REMOTE_OJ = Object.assign({}, REMOTE_OJ);
    this.currentProblemTitle = this.$i18n.t('m.Touch_Get_Status');
    // 初始化
    this.problemRecord = [
      { status: 0, count: 100 },
      { status: -1, count: 100 },
      { status: 1, count: 100 },
      { status: 2, count: 100 },
      { status: 3, count: 100 },
      { status: -3, count: 100 },
      { status: -2, count: 100 },
      { status: 4, count: 100 },
    ];
    this.loadings.table = true;
    setTimeout(() => {
      // 将指定列设置为隐藏状态
      this.$refs.problemList.getColumnByField('tag').visible = false;
      this.$refs.problemList.refreshColumn();
      this.loadings.table = false;
    }, 200);
    this.init();
  },
  methods: {
    init() {
      this.routeName = this.$route.name;
      let query = this.$route.query;
      this.query.difficulty = query.difficulty || '';
      this.query.oj = query.oj || 'Mine';
      this.query.keyword = query.keyword || '';
      this.query.tagId = query.tagId || '';
      this.query.currentPage = parseInt(query.currentPage) || 1;
      if (this.query.currentPage < 1) {
        this.query.currentPage = 1;
      }
      this.getTagList(this.query.oj);

      this.getProblemList();
    },
    pushRouter() {
      this.$router.push({
        path: '/problem',
        query: this.query,
      });
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.getProblemList();
    },
    getPercentage(partNumber, total) {
      return partNumber == 0
        ? 0
        : Math.round((partNumber / total) * 10000) / 100.0;
    },
    // 处理触碰或鼠标悬浮某题目，在右上方显示对应的提交数据
    cellHover(event) {
      let problem = event.data[event.rowIndex];
      let record;
      if (problem.type == 0) {
        // ACM类型题目
        record = [
          { status: 0, count: this.getPercentage(problem.ac, problem.total) },
          { status: -1, count: this.getPercentage(problem.wa, problem.total) },
          { status: 1, count: this.getPercentage(problem.tle, problem.total) },
          { status: 2, count: this.getPercentage(problem.mle, problem.total) },
          { status: 3, count: this.getPercentage(problem.re, problem.total) },
          { status: -3, count: this.getPercentage(problem.pe, problem.total) },
          { status: -2, count: this.getPercentage(problem.ce, problem.total) },
          { status: 4, count: this.getPercentage(problem.se, problem.total) },
        ];
      } else {
        // OI类型题目
        record = [
          { status: 0, count: this.getPercentage(problem.ac, problem.total) },
          { status: 8, count: this.getPercentage(problem.pa, problem.total) },
          { status: -1, count: this.getPercentage(problem.wa, problem.total) },
          { status: 1, count: this.getPercentage(problem.tle, problem.total) },
          { status: 2, count: this.getPercentage(problem.mle, problem.total) },
          { status: 3, count: this.getPercentage(problem.re, problem.total) },
          { status: -3, count: this.getPercentage(problem.pe, problem.total) },
          { status: -2, count: this.getPercentage(problem.ce, problem.total) },
          { status: 4, count: this.getPercentage(problem.se, problem.total) },
        ];
      }

      this.problemRecord = record;
      this.currentProblemTitle = problem.title;
    },
    getProblemList() {
      this.loadings.table = true;
      let queryParams = Object.assign({}, this.query);
      if (queryParams.difficulty == 'All') {
        queryParams.difficulty = '';
      } else {
        queryParams.difficulty = this.PROBLEM_LEVEL_RESERVE[
          queryParams.difficulty
        ]; // 需要对题目难度的显示进行转换 从字符串转为0，1，2
      }
      if (queryParams.oj == 'All') {
        queryParams.oj = '';
      } else if (!queryParams.oj) {
        queryParams.oj = 'Mine';
      }
      api.getProblemList(this.limit, queryParams).then(
        (res) => {
          this.loadings.table = false;
          this.total = res.data.data.total;
          this.problemList = res.data.data.records;
          if (this.isAuthenticated) {
            // 如果已登录，则需要查询对当前页面题目列表中各个题目的提交情况
            let pidList = [];
            for (let index = 0; index < this.problemList.length; index++) {
              pidList.push(this.problemList[index].pid);
            }
            if (pidList.length > 0) {
              // 必须当前页有显示题目才发送查询请求
              this.isGetStatusOk = false;
              let isContestProblemList = false; // 为了与比赛题目区分
              api
                .getUserProblemStatus(pidList, isContestProblemList)
                .then((res) => {
                  let result = res.data.data;
                  for (
                    let index = 0;
                    index < this.problemList.length;
                    index++
                  ) {
                    this.problemList[index]['myStatus'] =
                      result[this.problemList[index].pid].status;
                  }
                  this.isGetStatusOk = true;
                });
            }
          }
        },
        (res) => {
          this.loadings.table = false;
        }
      );
    },
    getTagList(oj) {
      if (oj == 'Mine') {
        oj = 'ME';
      }
      api.getProblemTagList(oj).then(
        (res) => {
          this.tagList = res.data.data;
          this.loadings.tag = false;
        },
        (res) => {
          this.loadings.tag = false;
        }
      );
    },
    changeTagVisible(visible) {
      this.$refs.problemList.getColumnByField('tag').visible = visible;
      this.$refs.problemList.refreshColumn();
    },
    onReset() {
      if (JSON.stringify(this.$route.query) != '{}') {
        this.$router.push({ name: 'ProblemList' });
      }
    },
    filterByTag(tagId) {
      this.query.tagId = tagId;
      this.query.currentPage = 1;
      this.pushRouter();
    },
    filterByDifficulty(difficulty) {
      this.query.difficulty = difficulty;
      this.query.currentPage = 1;
      this.pushRouter();
    },
    filterByOJ(oj) {
      this.query.oj = oj;
      if (oj != 'All') {
        this.query.tagId = '';
      }
      this.query.currentPage = 1;
      this.pushRouter();
    },
    filterByKeyword() {
      this.query.currentPage = 1;
      this.pushRouter();
    },
    pickone() {
      api.pickone().then((res) => {
        myMessage.success(this.$i18n.t('m.Good_luck_to_you'));
        this.$router.push({
          name: 'ProblemDetails',
          params: { problemID: res.data.data.problemId },
        });
      });
    },
    getProblemUri(problemId) {
      this.$router.push({
        name: 'ProblemDetails',
        params: {
          problemID: problemId,
        },
      });
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
    getIconColor(status) {
      return (
        'font-weight: 600;font-size: 16px;color:' +
        this.JUDGE_STATUS[status].rgb
      );
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated']),
    OJName() {
      if (this.query.oj == 'Mine' || !this.$route.query.oj) {
        return this.$i18n.t('m.My_OJ');
      } else if (this.query.oj == 'All') {
        return this.$i18n.t('m.All');
      } else {
        return this.query.oj;
      }
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    isAuthenticated(newVal) {
      if (newVal === true) {
        this.init();
      }
    },
  },
};
</script>

<style scoped>
.taglist-title {
  font-size: 21px;
  font-weight: 500;
}
/deep/.el-tag--dark {
  border-color: #d9ecff;
}
/deep/.tag-btn {
  margin-left: 4px !important;
  margin-top: 4px;
}
/deep/.vxe-checkbox .vxe-checkbox--label {
  overflow: unset !important;
}

#pick-one {
  margin-top: 10px;
}
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
@media screen and (min-width: 1200px) {
  /deep/ .el-card__body {
    padding-top: 0px;
    margin-top: 5px;
  }
}
@media only screen and (max-width: 767px) {
  .top-pt {
    padding-top: 10px;
  }
}
ul {
  float: right;
}
li {
  display: inline-block;
  padding: 0 10px;
}
.title-a {
  color: #495060;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
}
.el-progress {
  margin-top: 15px;
}
</style>
