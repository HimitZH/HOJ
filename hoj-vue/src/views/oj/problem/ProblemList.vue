<template>
  <el-row :gutter="18">
    <el-col :sm="24" :md="18" :lg="18">
      <el-card shadow>
        <div slot="header">
          <el-row :gutter="20" style="margin-bottom: 0.5em;">
            <el-col :xs="24" :sm="6">
              <span class="problem-list-title">{{ $t('m.Problem_List') }}</span>
            </el-col>
            <el-col :xs="24" :sm="6">
              <vxe-input
                v-model="query.keyword"
                :placeholder="$t('m.Enter_keyword')"
                type="search"
                size="medium"
                @search-click="filterByKeyword"
                @keyup.enter.native="filterByKeyword"
                class="filter-mt"
              ></vxe-input>
            </el-col>
            <el-col
              :xs="12"
              :sm="6"
              style="text-align: center;padding-top: 6px;"
              class="filter-mt"
            >
              <vxe-checkbox
                v-model="tagVisible"
                @change="changeTagVisible(tagVisible)"
                >{{ $t('m.Show_Tags') }}</vxe-checkbox
              >
            </el-col>
            <el-col
              :xs="12"
              :sm="6"
              style="text-align: center;"
              class="filter-mt"
            >
              <el-button
                type="primary"
                size="small"
                icon="el-icon-refresh"
                round
                @click="onReset"
                >{{ $t('m.Reset') }}</el-button
              >
            </el-col>
          </el-row>

          <section>
            <b class="problem-filter">{{ $t('m.Problem_Bank') }}</b>
            <div>
              <el-tag
                size="medium"
                class="filter-item"
                :effect="query.oj === 'All' ? 'dark' : 'plain'"
                @click="filterByOJ('All')"
                >{{ $t('m.All') }}</el-tag
              >
              <el-tag
                size="medium"
                class="filter-item"
                :effect="
                  query.oj === 'Mine' || query.oj === '' ? 'dark' : 'plain'
                "
                @click="filterByOJ('Mine')"
                >{{ $t('m.My_OJ') }}</el-tag
              >
              <el-tag
                size="medium"
                class="filter-item"
                v-for="(remoteOj, index) in REMOTE_OJ"
                :effect="query.oj == remoteOj.key ? 'dark' : 'plain'"
                :key="index"
                @click="filterByOJ(remoteOj.key)"
                >{{ remoteOj.name }}</el-tag
              >
            </div>
          </section>

          <section>
            <b class="problem-filter">{{ $t('m.Level') }}</b>
            <div>
              <el-tag
                size="medium"
                class="filter-item"
                :effect="
                  query.difficulty === 'All' || query.difficulty === ''
                    ? 'dark'
                    : 'plain'
                "
                @click="filterByDifficulty('All')"
                >{{ $t('m.All') }}</el-tag
              >
              <el-tag
                size="medium"
                class="filter-item"
                v-for="(value, key, index) in PROBLEM_LEVEL"
                :effect="query.difficulty == key ? 'dark' : 'plain'"
                :style="getLevelBlockColor(key)"
                :key="index"
                @click="filterByDifficulty(key)"
                >{{ getLevelName(key) }}</el-tag
              >
            </div>
          </section>
          <template v-if="filterTagList.length > 0 && buildFilterTagList">
            <el-row>
              <b class="problem-filter">{{ $t('m.Tags') }}</b>
              <el-tag
                :key="index"
                v-for="(tag, index) in filterTagList"
                closable
                :color="tag.color ? tag.color : '#409eff'"
                effect="dark"
                :disable-transitions="false"
                @close="removeTag(tag)"
                size="medium"
                class="filter-item"
              >
                {{ tag.name }}
              </el-tag>
            </el-row>
          </template>
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
          <vxe-table-column title="" width="30" v-if="isAuthenticated">
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
          ></vxe-table-column>

          <vxe-table-column
            field="title"
            :title="$t('m.Problem')"
            min-width="150"
            show-overflow
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
                >{{ getLevelName(row.difficulty) }}</span
              >
            </template>
          </vxe-table-column>

          <vxe-table-column
            field="tag"
            :title="$t('m.Tags')"
            min-width="230"
            visible="false"
          >
            <template v-slot="{ row }">
              <span
                class="el-tag el-tag--small"
                :style="
                  'cursor: pointer;margin-right:7px;color:#FFF;background-color:' +
                    (tag.color ? tag.color : '#409eff')
                "
                v-for="tag in row.tags"
                :key="tag.id"
                @click="addTag(tag)"
                >{{ tag.name }}</span
              >
            </template>
          </vxe-table-column>
          <vxe-table-column
            field="total"
            :title="$t('m.Total')"
            min-width="80"
          ></vxe-table-column>
          <vxe-table-column
            field="ac"
            :title="$t('m.AC_Rate')"
            min-width="120"
            align="center"
          >
            <template v-slot="{ row }">
              <span>
                <el-tooltip
                  effect="dark"
                  :content="row.ac + '/' + row.total"
                  placement="top"
                  style="margin-top:0"
                >
                  <el-progress
                    :text-inside="true"
                    :stroke-width="20"
                    :color="customColors"
                    :percentage="getPassingRate(row.ac, row.total)"
                  ></el-progress>
                </el-tooltip>
              </span>
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
          <div style="margin: 10px 0;">
            <el-input
              size="medium"
              prefix-icon="el-icon-search"
              :placeholder="$t('m.Search_Filter_Tag')"
              v-model="searchTag"
              @keyup.enter.native="filterSearchTag"
              @input="filterSearchTag"
              clearable
            >
            </el-input>
          </div>
        </div>
        <template v-if="searchTagClassificationList.length > 0" v-loading="loadings.tag">
          <el-row :gutter="10" v-for="(item,index) in secondClassificationTemp" 
              :key="index">
            <el-col  v-for="(tagsAndClassification,i) in item" :key="i"
              :span="query.oj == 'All' || (secondClassificationTemp.length==index+1 && item.length == i+1 && i%2 ==0)
              ?24:12">
              <el-collapse v-model="activeTagClassificationIdList" style="margin-top:10px">
                  <el-collapse-item :title="getTagClassificationName(tagsAndClassification.classification)"
                    v-if="tagsAndClassification.classification != null 
                        || tagsAndClassification.tagList.length > 0 " 
                    :name="tagsAndClassification.classification == null?-1:tagsAndClassification.classification.id">
                    <el-button
                      v-for="tag in tagsAndClassification.tagList"
                      :key="tag.id"
                      @click="addTag(tag)"
                      type="ghost"
                      size="mini"
                      class="tag-btn"
                      :style="
                        'color:#FFF;background-color:' +
                          (tag.color ? tag.color : '#409eff')
                      "
                      >{{ tag.name }}
                    </el-button>
                  </el-collapse-item>
              </el-collapse>
            </el-col>
          </el-row>
          <el-button long id="pick-one" @click="pickone">
            <i class="fa fa-random"></i>
            {{ $t('m.Pick_a_random_question') }}
          </el-button>
        </template>
        <template v-else>
          <el-empty :description="$t('m.No_Data')"></el-empty>
        </template>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import { mapGetters } from 'vuex';
import api from '@/common/api';
import {
  PROBLEM_LEVEL,
  JUDGE_STATUS,
  JUDGE_STATUS_RESERVE,
  REMOTE_OJ,
} from '@/common/constants';
import utils from '@/common/utils';
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
      JUDGE_STATUS: {},
      JUDGE_STATUS_RESERVE: {},
      REMOTE_OJ: {},
      tagsAndClassificationList:[],
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
      filterConfig: { remote: true },
      filterTagList: [],
      buildFilterTagList: false,
      routeName: '',
      query: {
        keyword: '',
        difficulty: 'All',
        oj: '',
        tagId: '',
        currentPage: 1,
      },
      customColors: [
        { color: '#909399', percentage: 20 },
        { color: '#f56c6c', percentage: 40 },
        { color: '#e6a23c', percentage: 60 },
        { color: '#1989fa', percentage: 80 },
        { color: '#67c23a', percentage: 100 },
      ],
      searchTag: '',
      searchTagClassificationList: [],
      activeTagClassificationIdList:[]
    };
  },
  created() {
    this.init();
  },

  mounted() {
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
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
    this.getTagList(this.query.oj);
    this.loadings.table = true;
    setTimeout(() => {
      // 将指定列设置为隐藏状态
      this.$refs.problemList.getColumnByField('tag').visible = false;
      this.$refs.problemList.refreshColumn();
      this.loadings.table = false;
    }, 200);
    this.getData();
  },
  methods: {
    init() {
      this.routeName = this.$route.name;
      let query = this.$route.query;
      this.query.difficulty = query.difficulty || '';
      this.query.oj = query.oj || 'Mine';
      this.query.keyword = query.keyword || '';
      try {
        this.query.tagId = JSON.parse(query.tagId);
      } catch (error) {
        this.query.tagId = [];
      }
      this.query.currentPage = parseInt(query.currentPage) || 1;
      this.limit = parseInt(query.limit) || 30;
      if (this.query.currentPage < 1) {
        this.query.currentPage = 1;
      }
    },

    getData() {
      this.getProblemList();
    },

    pushRouter() {
      this.query.tagId = JSON.stringify(
        this.filterTagList.map((tagJson) => tagJson.id)
      );
      this.query.limit = this.limit;
      this.$router.push({
        path: '/problem',
        query: this.query,
      });
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.pushRouter();
    },
    getPercentage(partNumber, total) {
      return partNumber == 0
        ? 0
        : Math.round((partNumber / total) * 10000) / 100.0;
    },
    getPassingRate(ac, total) {
      if (!total) {
        return 0;
      }
      return ((ac / total) * 100).toFixed(2);
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
      let queryParams = Object.assign({}, this.query);
      if (queryParams.difficulty == 'All') {
        queryParams.difficulty = '';
      }
      if (queryParams.oj == 'All') {
        queryParams.oj = '';
      } else if (!queryParams.oj) {
        queryParams.oj = 'Mine';
      }
      queryParams.tagId = queryParams.tagId + '';
      queryParams.limit = this.limit;
      this.loadings.table = true;
      api.getProblemList(queryParams).then(
        (res) => {
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
          this.loadings.table = false;
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
      this.loadings.tag = true;
      api.getProblemTagsAndClassification(oj).then(
        (res) => {
          this.tagsAndClassificationList = res.data.data;
          this.searchTagClassificationList = res.data.data;
          this.filterTagList = [];
          let tidLen = this.query.tagId.length;
          let tagLen = this.tagsAndClassificationList.length;
          for (let x = 0; x < tidLen; x++) {
            for (let y = 0; y < tagLen; y++) {
              let tmpTagAndClassification = this.tagsAndClassificationList[y].tagList;
              let tmpLen = tmpTagAndClassification.length;
              for(let z = 0; z <tmpLen; z++){
                if (this.query.tagId[x] == tmpTagAndClassification[z].id) {
                  this.filterTagList.push(tmpTagAndClassification[z]);
                  break;
                }
              }
            }
          }
          this.buildFilterTagList = true;
          this.loadings.tag = false;
        },
        (res) => {
          this.loadings.tag = false;
        }
      );
    },
    filterSearchTag() {
      if (this.searchTag) {
        this.searchTagClassificationList = [];
        this.activeTagClassificationIdList = [];
        for(let tagsAndClassification of this.tagsAndClassificationList){
          let tmpTagList = [];
          for(let tag of tagsAndClassification.tagList){
            if(tag.name.toLowerCase().indexOf(this.searchTag.toLowerCase()) >= 0){
              tmpTagList.push(tag);
            }
          }
          if(tmpTagList.length > 0){
            this.searchTagClassificationList.push(
              {
                classification: tagsAndClassification.classification,
                tagList:tmpTagList
              }
            )
            this.activeTagClassificationIdList.push(tagsAndClassification.classification == null? -1:tagsAndClassification.classification.id);
          }
        }
      } else {
        this.searchTagClassificationList = this.tagsAndClassificationList;
        this.activeTagClassificationIdList = [];
      }
    },
    changeTagVisible(visible) {
      this.$refs.problemList.getColumnByField('tag').visible = visible;
      this.$refs.problemList.refreshColumn();
    },
    onReset() {
      this.filterTagList = [];
      if (JSON.stringify(this.$route.query) != '{}') {
        this.$router.push({ name: 'ProblemList' });
      }
    },
    removeTag(tag) {
      this.filterTagList.splice(this.filterTagList.indexOf(tag), 1);
      this.filterByTag();
    },
    addTag(tag) {
      let len = this.filterTagList.length;
      for (let i = 0; i < len; i++) {
        if (this.filterTagList[i].id == tag.id) {
          return;
        }
      }
      this.filterTagList.push(tag);
      this.filterByTag();
    },
    filterByTag() {
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
        this.filterTagList = [];
      }
      this.query.currentPage = 1;
      this.getTagList(this.query.oj);
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
      return utils.getLevelColor(difficulty);
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
    getIconColor(status) {
      return (
        'font-weight: 600;font-size: 16px;color:' +
        this.JUDGE_STATUS[status].rgb
      );
    },
    getLevelBlockColor(difficulty) {
      if (difficulty == this.query.difficulty) {
        return this.getLevelColor(difficulty);
      }
    },
    getTagClassificationName(classification){
      if(classification !=null){
        let name = '';
        let oj = this.query.oj;
        if(oj == 'All'){
          switch(classification.oj){
            case "ME":
              name = '['+this.$i18n.t('m.My_OJ')+'] '
              break;
            case "AC":
              name = '[AtCoder] ';
              break;
            case "CF":
              name = '[Codeforces] ';
              break;
            default:
              name = '['+classification.oj+'] '
          }
        }
        return name + classification.name;
      }else{
        return this.$i18n.t('m.Unclassified');
      }
    }
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
    secondClassificationTemp(){
      let index = 0;
      let count = 2;		//两个一组
      let arrTemp = [];
      let tagsClassificationList = this.searchTagClassificationList;
      let len = tagsClassificationList.length;
      for(let i=0;i<len;i++){
        index = parseInt(i/count);
        if (arrTemp.length <= index) {
          arrTemp.push([]);
        }
        arrTemp[index].push(tagsClassificationList[i])
      }
      return arrTemp
    }
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
        this.getData();
      }
    },
    isAuthenticated(newVal) {
      if (newVal === true) {
        this.init();
        this.getData();
      }
    },
  },
};
</script>

<style scoped>
.problem-list-title {
  font-size: 2em;
  font-weight: 500;
  line-height: 30px;
}

.taglist-title {
  font-size: 21px;
  font-weight: 500;
}

section {
  display: flex;
  align-items: baseline;
  margin-bottom: 0.8em;
}
.problem-filter {
  margin-right: 1em;
  font-weight: bolder;
  white-space: nowrap;
  font-size: 16px;
  margin-top: 8px;
}
.filter-item {
  margin-right: 1em;
  margin-top: 0.5em;
  font-size: 13px;
}
.filter-item:hover {
  cursor: pointer;
}

@media only screen and (max-width: 767px) {
  .filter-mt {
    margin-top: 8px;
  }
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
/deep/ .vxe-input {
  width: 100%;
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
ul {
  float: right;
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

@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}

/deep/.el-collapse-item__header{
  font-weight: bolder !important;
  height: 38px !important;
  line-height: 38px !important;
  font-size: 15px !important;
}
/deep/.el-collapse-item__content {
  padding-bottom: 10px !important;
}
</style>
