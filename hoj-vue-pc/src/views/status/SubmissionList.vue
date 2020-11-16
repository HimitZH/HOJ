<template>
  <div class="flex-container">
    <div id="main">
      <el-card shadow>
      <div slot="header">
        <el-row :gutter="18">
         <el-col  :md="2" :lg="2">
        <span class="panel-title hidden-xs-only" >Status</span>
         </el-col>

         <el-col :xs="16" :md="10" :lg="10">
            <el-switch
              style="display: block"
              v-model="formFilter.myself"
              active-color="#ff4949"
              active-text="Mine"
              :width="40"
              inactive-text="All">
            </el-switch>
          </el-col>

          <el-col :xs="8" :md="4" :lg="4">
            <el-dropdown
            class="drop-menu"
            @command="handleStatusChange"
            placement="bottom"
            trigger="hover"
          >
          <span class="el-dropdown-link">
              {{formFilter.status === '' ? 'Status' : formFilter.status}}
            <i class="el-icon-caret-bottom"></i>
          </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="All">All</el-dropdown-item>
              <el-dropdown-item v-for="result in Object.keys(JUDGE_STATUS)" :key="result" :command="result">
                {{JUDGE_STATUS[result].name}}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          </el-col>

           <el-col :xs="24" :md="4" :lg="4" class="search">
            <vxe-input v-model="formFilter.pid" placeholder="Enter Problem ID" type="search" size="medium" @search-click="filterByKeyword"></vxe-input>
          </el-col>
          <el-col :xs="24" :md="4" :lg="4" class="search">
            <vxe-input v-model="formFilter.pid" placeholder="Enter Author" type="search" size="medium" @search-click="filterByKeyword"></vxe-input>
          </el-col>
        </el-row>
      </div>
      <vxe-table
            border="inner"
            stripe
            highlight-current-row
            highlight-hover-row
            align="center"
            :row-class-name="tableRowClassName"
            :data="submissions">
            <vxe-table-column field="sid" title="ID" width="150"></vxe-table-column>
            <vxe-table-column field="stime" title="Submit time" width="150"></vxe-table-column>
            <vxe-table-column field="pid" title="Problem ID" width="150">
              <template v-slot="{ row }">
                <a :href="getProblemUri(row.pid)" style="color:rgb(87, 163, 243);">{{row.pid}}</a>
              </template>
            </vxe-table-column>
            <vxe-table-column field="status" title="Status" width="150">
              <template v-slot="{ row }">
                <span :class="getStatusColor(row.status)">{{JUDGE_STATUS[row.status].name}}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="time" title="Time" width="150"></vxe-table-column>
            <vxe-table-column field="memory" title="Memory" width="150"></vxe-table-column>
            <vxe-table-column field="language" title="Language" width="150">
              <template v-slot="{ row }">
                <el-tooltip class="item" effect="dark" content="查看提交详情" placement="top">
                  <el-button type="text"  @click="showSubmitDetail(row)">{{ row.language }}</el-button>  
                </el-tooltip>
              </template>
            </vxe-table-column>
            <vxe-table-column field="judger" title="Judger" width="150"></vxe-table-column>
            <vxe-table-column field="author" title="Author" width="150"></vxe-table-column>
      </vxe-table>
    </el-card>
    <Pagination :total="total" :page-size="limit" @on-change="changeRoute" :current.sync="page"></Pagination>
    </div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import api from '@/common/api'
  import { JUDGE_STATUS, USER_TYPE } from '@/common/constants'
  import utils from '@/common/utils'
  import time from '@/common/time'
  import Pagination from '@/components/common/Pagination'
  export default {
    name: 'submissionList',
    components: {
      Pagination,
    },
    data () {
      return {
        formFilter: {
          myself: false,
          status: '',
          username: '',
          pid:''
        },
        loadingTable: false,
        submissions: [
          {
            sid:1000,
            stime:'2020-08-08 16:00:00',
            pid:'1001',
            status:0,
            time:'4ms',
            memory:'3MB',
            language:'C++',
            judger:'192.168.1.1',
            author:'Himit_ZH'
          },
          {
            sid:1001,
            stime:'2020-08-08 16:00:00',
            pid:'1001',
            status:0,
            time:'4ms',
            memory:'3MB',
            language:'C++',
            judger:'192.168.1.1',
            author:'Himit_Z'
          },
          {
            sid:1000,
            stime:'2020-08-08 16:00:00',
            pid:'1001',
            status:0,
            time:'4ms',
            memory:'3MB',
            language:'C++',
            judger:'192.168.1.1',
            author:'Himit_H'
          }

        ],
        total: 30,
        limit: 15,
        page: 1,
        contestID: '',
        problemID: '',
        routeName: '',
        JUDGE_STATUS: '',
        rejudge_column: false
      }
    },
    mounted () {
      this.init()
      this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS)
      // 去除submitting的状态 和 两个
      delete this.JUDGE_STATUS['9']
      delete this.JUDGE_STATUS['2']
    },
    methods: {
      init () {
        this.contestID = this.$route.params.contestID
        let query = this.$route.query
        this.problemID = query.problemID
        this.formFilter.myself = query.myself === '1'
        this.formFilter.status = query.status || ''
        this.formFilter.username = query.username || ''
        this.page = parseInt(query.page) || 1
        if (this.page < 1) {
          this.page = 1
        }
        this.routeName = this.$route.name
        this.getSubmissions()
      },
      buildQuery () {
        return {
          myself: this.formFilter.myself === true ? '1' : '0',
          status: this.formFilter.status,
          username: this.formFilter.username,
          page: this.page
        }
      },
      getSubmissions () {
        let params = this.buildQuery()
        params.contest_id = this.contestID
        params.problem_id = this.problemID
        let offset = (this.page - 1) * this.limit
        let func = this.contestID ? 'getContestSubmissionList' : 'getSubmissionList'
        this.loadingTable = true
        api[func](offset, this.limit, params).then(res => {
          let data = res.data.data
          for (let v of data.results) {
            v.loading = false
          }
          this.adjustRejudgeColumn()
          this.loadingTable = false
          this.submissions = data.results
          this.total = data.total
        }).catch(() => {
          this.loadingTable = false
        })
      },
      // 改变route， 通过监听route变化请求数据，这样可以产生route history， 用户返回时就会保存之前的状态
      changeRoute () {
        let query = utils.filterEmptyValue(this.buildQuery())
        query.contestID = this.contestID
        query.problemID = this.problemID
        let routeName = query.contestID ? 'contest-submission-list' : 'submission-list'
        this.$router.push({
          name: routeName,
          query: utils.filterEmptyValue(query)
        })
      },
      goRoute (route) {
        this.$router.push(route)
      },
      adjustRejudgeColumn () {
        if (!this.rejudgeColumnVisible || this.rejudge_column) {
          return
        }
        const judgeColumn = {
          title: this.$i18n.t('m.Option'),
          fixed: 'right',
          align: 'center',
          width: 90,
          render: (h, params) => {
            return h('Button', {
              props: {
                type: 'primary',
                size: 'small',
                loading: params.row.loading
              },
              on: {
                click: () => {
                  this.handleRejudge(params.row.id, params.index)
                }
              }
            }, this.$i18n.t('m.Rejudge'))
          }
        }
        this.columns.push(judgeColumn)
        this.rejudge_column = true
      },
      handleStatusChange (status) {
        this.page = 1
        this.formFilter.status = status
        this.changeRoute()
      },
      handleQueryChange () {
        this.page = 1
        this.changeRoute()
      },
      handleRejudge (id, index) {
        this.submissions[index].loading = true
        api.submissionRejudge(id).then(res => {
          this.submissions[index].loading = false
          this.$success('Succeeded')
          this.getSubmissions()
        }, () => {
          this.submissions[index].loading = false
        })
      },
      showSubmitDetail (row) {
        this.selectSubmitRow = row;
        this.showDetails = true;
      },
      getProblemUri(pid){
        return '/problem/'+pid;
      },
      getStatusColor(status){
        return 'el-tag el-tag--medium status-'+JUDGE_STATUS[status].color;
      },
      tableRowClassName({row, rowIndex}){
        if(row.author =='Himit_ZH'){
          return 'own-submit-row';
        }
      }
    },
    computed: {
      ...mapGetters(['isAuthenticated', 'userInfo']),
      title () {
        if (!this.contestID) {
          return 'Status'
        } else if (this.problemID) {
          return 'Problem Submissions'
        } else {
          return 'Submissions'
        }
      },
      status () {
        return this.formFilter.status === '' ? 'Status' : JUDGE_STATUS[this.formFilter.status].name.replace(/ /g, '_')
      },
      rejudgeColumnVisible () {
        return !this.contestID && this.userInfo.role === USER_TYPE.SUPER_ADMIN
      }
    },
    watch: {
      '$route' (newVal, oldVal) {
        if (newVal !== oldVal) {
          this.init()
        }
      },
      'rejudgeColumnVisible' () {
        this.adjustRejudgeColumn()
      },
      'isAuthenticated' () {
        this.init()
      }
    }
  }
</script>

<style scoped >

  @media only screen and (max-width: 767px){
    .search{
      margin-top: 20px;
    }
  }

  .flex-container #main {
      flex: auto;
      margin-right: 18px;
    }
 .flex-container  .filter {
        margin-right: -10px;
    }
 .flex-container  #contest-menu {
      flex: none;
      width: 210px;
    }
  /deep/ .el-card__header {
   border-bottom: 0px;
   padding-bottom: 0px;
   text-align: center;
  }
  /deep/ .el-button{
    padding: 0;
  }
  /deep/ .el-dialog {
    border-radius: 6px !important;
    text-align: center;
  }
  /deep/ .el-switch{
    padding-top: 6px;
  }
</style>
