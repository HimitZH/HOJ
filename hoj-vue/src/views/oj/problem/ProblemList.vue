<template>
  <el-row :gutter="18">
    <el-col :sm="24" :md="18" :lg="18">
      <el-card shadow>
        <div slot="header">
          <el-row :gutter="18">
            <el-col :sm="9" :md="10" :lg="13">
              <span class="panel-title hidden-xs-only">Problem List</span>
            </el-col>
            <el-col :xs="7" :sm="4" :md="3" :lg="3" style="padding-top: 6px;">
              <el-dropdown
                class="drop-menu"
                @command="filterByDifficulty"
                placement="bottom"
                trigger="hover"
              >
                <span class="el-dropdown-link">
                  {{
                    query.difficulty === 'All' || query.difficulty === ''
                      ? 'Difficulty'
                      : query.difficulty
                  }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="All">All</el-dropdown-item>
                  <el-dropdown-item command="Easy">Easy</el-dropdown-item>
                  <el-dropdown-item command="Mid">Mid</el-dropdown-item>
                  <el-dropdown-item command="Hard">Hard</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-col>
            <el-col :xs="14" :sm="7" :md="7" :lg="5">
              <vxe-input
                v-model="query.keyword"
                placeholder="Enter keyword"
                type="search"
                size="medium"
                @search-click="filterByKeyword"
                @keyup.enter.native="filterByKeyword"
              ></vxe-input>
            </el-col>
            <el-col :sm="4" :md="4" :lg="3" class="hidden-xs-only">
              <el-button
                type="primary"
                size="small"
                icon="el-icon-refresh"
                round
                @click="onReset"
                >Reset</el-button
              >
            </el-col>
            <el-col :xs="3" class="hidden-sm-and-up">
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
            title="Problem ID"
            min-width="100"
          ></vxe-table-column>

          <vxe-table-column field="title" title="Title" min-width="250">
            <template v-slot="{ row }">
              <a :href="getProblemUri(row.problemId)" class="title-a">{{
                row.title
              }}</a>
            </template>
          </vxe-table-column>

          <vxe-table-column field="difficulty" title="Level" min-width="100">
            <template v-slot="{ row }">
              <span :class="getLevelColor(row.difficulty)">{{
                PROBLEM_LEVEL[row.difficulty].name
              }}</span>
            </template>
          </vxe-table-column>

          <vxe-table-column field="tag" title="Tag" min-width="200">
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
            title="Total"
            min-width="80"
          ></vxe-table-column>
          <vxe-table-column title="AC Rate" min-width="80">
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
        <div slot="header"><span class="taglist-title">Tags</span></div>
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
          Pick a random question
        </el-button>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import { mapGetters } from 'vuex';
import api from '@/common/api';
import utils from '@/common/utils';
import {
  PROBLEM_LEVEL,
  PROBLEM_LEVEL_RESERVE,
  JUDGE_STATUS,
  JUDGE_STATUS_RESERVE,
} from '@/common/constants';
import Pagination from '@/components/oj/common/Pagination';
import myMessage from '@/common/message';

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
      tagList: [],
      currentProblemTitle: '请触碰或鼠标悬浮到指定题目行即可查看提交情况',
      problemRecord: [],
      problemList: [],
      limit: 20,
      total: 100,
      isGetStatusOk: false,
      loadings: {
        table: true,
        tag: true,
      },
      page: {
        currentPage: 1,
        pageSize: 10,
        totalResult: 300,
      },
      filterConfig: { remote: true },
      routeName: '',
      query: {
        keyword: '',
        difficulty: 'All',
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
    this.init();
  },
  methods: {
    init() {
      this.routeName = this.$route.name;
      let query = this.$route.query;
      this.query.difficulty = query.difficulty || '';
      this.query.keyword = query.keyword || '';
      this.query.tagId = query.tagId || '';
      this.query.currentPage = parseInt(query.currentPage) || 1;
      if (this.query.currentPage < 1) {
        this.query.currentPage = 1;
      }

      this.getTagList();

      this.getProblemList();
    },
    pushRouter() {
      let queryParams = utils.filterEmptyValue(this.query);
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
        this.$router.push({
          path: '/problem',
          query: queryParams,
        });
      }
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
                      result[this.problemList[index].pid];
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
    getTagList() {
      api.getProblemTagList().then(
        (res) => {
          this.tagList = res.data.data;
          this.loadings.tag = false;
        },
        (res) => {
          this.loadings.tag = false;
        }
      );
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
    filterByKeyword() {
      this.query.currentPage = 1;
      this.pushRouter();
    },
    pickone() {
      api.pickone().then((res) => {
        myMessage.success('随机题目获取成功，祝你好运');
        this.$router.push({
          name: 'ProblemDetails',
          params: { problemID: res.data.data.problemId },
        });
      });
    },
    getProblemUri(problemId) {
      return '/problem/' + problemId;
    },
    getLevelColor(difficulty) {
      return 'el-tag el-tag--small status-' + PROBLEM_LEVEL[difficulty].color;
    },
    getIconColor(status) {
      return (
        'font-weight: 600;font-size: 16px;color:' + JUDGE_STATUS[status].rgb
      );
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated']),
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
  margin: 0 43%;
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

#pick-one {
  margin-top: 10px;
}
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
/deep/ .el-card__body {
  margin-top: 20px;
}
@media screen and (min-width: 1200px) {
  /deep/ .el-card__body {
    padding-top: 0px;
    margin-top: 5px;
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
