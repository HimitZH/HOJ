<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{contestId ?'Contest Problem List': 'Problem List'}}</span>
        <div class="filter-row">
            <span>
              <el-button type="primary" size="small"
                   @click="goCreateProblem" icon="el-icon-plus">Create
              </el-button>
              <el-button v-if="contestId" type="primary"
                        size="small" icon="el-icon-plus"
                        @click="addProblemDialogVisible = true">Add From Public Problem
              </el-button>
            </span>
            <span>
            <vxe-input v-model="keyword" placeholder="Enter keyword" type="search" size="medium" @search-click="filterByKeyword"></vxe-input>
            </span>
        </div>
      </div>
      <vxe-table stripe auto-resize :data="problemList"  
      ref="xTable"
      :loading="loading"
      @row-dblclick="handleDblclick"
      align="center"
      >
        <vxe-table-column
          min-width="100"
          field="id"
          title="ID">
        </vxe-table-column>
        <vxe-table-column
          field="title"
          min-width="150"
          title="Title">
        </vxe-table-column>
        <vxe-table-column
          field="author"
          min-width="150"
          title="Author">
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="gmtCreate"
          title="Create Time">
          <template v-slot="{row}">
            {{row.create_time | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="auth"
          title="Auth">
          <template v-slot="{row}">
            <el-select v-model="row.auth" @change="updateProblem(row)" size="small">
                <el-option label="公开" :value="1"></el-option>
                <el-option label="私有" :value="2"></el-option>
                <el-option label="比赛中" :value="3"></el-option>
            </el-select>
          </template>
        </vxe-table-column>
        <vxe-table-column
          title="Option"
          min-width="200">
          <template v-slot="{row}">
            <el-tooltip effect="dark" content="编辑题目" placement="top">
                <el-button icon="el-icon-edit-outline" size="mini" @click.native="goEdit(row)" type="primary">
                </el-button>
            </el-tooltip>

            <el-tooltip  effect="dark" content="下载测试样例" placement="top">
                <el-button  icon="el-icon-download" size="mini" @click.native="downloadTestCase(row.id)" type="success">
                </el-button>
            </el-tooltip>

            <el-tooltip  effect="dark" content="删除题目" placement="top">
                <el-button  icon="el-icon-delete-solid" size="mini" @click.native="deleteProblem(row.id)" type="danger">
                </el-button>
            </el-tooltip>

          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="Add Contest Problem"
               v-if="contestId"
               width="90%"
               :visible.sync="addProblemDialogVisible"
               @close-on-click-modal="false">
      <ContestAddProblem :contestID="contestId" @on-change="getProblemList"></ContestAddProblem>
    </el-dialog>


  </div>
</template>

<script>
  import api from '@/common/api'
  import utils from '@/common/utils'
  import ContestAddProblem from '@/components/admin/ContestAddProblem.vue'

  export default {
    name: 'ProblemList',
    components: {
      ContestAddProblem
    },
    data () {
      return {
        pageSize: 10,
        total: 0,
        problemList: [
          {id:1001,title:'测试标题',author:'Himit_ZH',gmtCreate:'2020-11-11 22:22:22',auth:1}
        ],
        keyword: '',
        loading: false,
        currentPage: 1,
        routeName: '',
        contestId: '',
        // for make public use
        currentProblemID: '',
        currentRow: {},
        addProblemDialogVisible: false
      }
    },
    mounted () {
      this.routeName = this.$route.name
      this.contestId = this.$route.params.contestId
      // this.getProblemList(this.currentPage)
    },
    methods: {
      handleDblclick (row) {
        row.isEditing = true
      },
      goEdit (problemId) {
        if (this.routeName === 'admin-problem-list') {
          this.$router.push({name: 'admin-edit-problem', params: {problemId}})
        } else if (this.routeName === 'admin-contest-problem-list') {
          this.$router.push({name: 'admin-edit-contest-problem', params: {problemId: problemId, contestId: this.contestId}})
        }
      },
      goCreateProblem () {
        if (this.routeName === 'admin-problem-list') {
          this.$router.push({name: 'admin-create-problem'})
        } else if (this.routeName === 'admin-contest-problem-list') {
          this.$router.push({name: 'admin-create-contest-problem', params: {contestId: this.contestId}})
        }
      },
      // 切换页码回调
      currentChange (page) {
        this.currentPage = page
        this.getProblemList(page)
      },
      getProblemList (page = 1) {
        this.loading = true
        let funcName = this.routeName === 'admin-problem-list' ? 'getProblemList' : 'getContestProblemList'
        let params = {
          limit: this.pageSize,
          offset: (page - 1) * this.pageSize,
          keyword: this.keyword,
          contest_id: this.contestId
        }
        api[funcName](params).then(res => {
          this.loading = false
          this.total = res.data.data.total
          for (let problem of res.data.data.results) {
            problem.isEditing = false
          }
          this.problemList = res.data.data.results
        }, err => {
          this.loading = false
        })
      },
      deleteProblem (id) {
        this.$confirm('确定要删除此问题吗？注意：该问题的相关提交数据也将被删除。', '删除题目', {
          type: 'warning'
        }).then(() => {
          let funcName = this.routeName === 'admin-problem-list' ? 'deleteProblem' : 'deleteContestProblem'
          api[funcName](id).then(() => [
            this.getProblemList(this.currentPage - 1)
          ]).catch(() => {
          })
        }, () => {
        })
      },
      updateProblem (row) {
        let data = Object.assign({}, row)
        let funcName = ''
        if (this.contestId) {
          data.contest_id = this.contestId
          funcName = 'editContestProblem'
        } else {
          funcName = 'editProblem'
        }
        api[funcName](data).then(res => {
          this.getProblemList(this.currentPage)
        }).catch(() => {
        })
      },
      downloadTestCase (problemID) {
        let url = '/admin/test_case?problem_id=' + problemID
        utils.downloadFile(url)
      },
      getPublicProblem () {
        api.getProblemList()
      },
      filterByKeyword(){
         this.currentChange()
      }
    },
    watch: {
      '$route' (newVal, oldVal) {
        this.contestId = newVal.params.contestId
        this.routeName = newVal.name
        this.getProblemList(this.currentPage)
      }
    }
  }
</script>

<style scoped>
  .filter-row{
    margin-top: 10px;
  }
  @media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
  @media screen and (min-width: 768px) {
    .filter-row span {
      margin-right: 20px;
    }
  }
</style>
