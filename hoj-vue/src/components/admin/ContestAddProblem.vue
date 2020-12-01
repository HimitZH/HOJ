<template>
  <div>
     <vxe-input v-model="keyword" placeholder="Enter keyword" 
     type="search" size="medium" @search-click="filterByKeyword" style="margin-bottom:10px"></vxe-input>
    <vxe-table 
    :data="problems" :loading="loading"
    auto-resize
    stripe
    align="center"
    >
      <vxe-table-column
        title="ID"
        min-width="100"
        field="id">
      </vxe-table-column>
      <vxe-table-column
        min-width="150"
        title="Title"
        field="title">
      </vxe-table-column>
      <vxe-table-column
        title="option"
        align="center"
        min-width="100">
        <template v-slot="{row}">
          <el-tooltip effect="dark" content="添加该题目到比赛中" placement="top">
                <el-button icon="el-icon-plus" size="mini" @click.native="handleAddProblem(row.id)" type="primary">
                </el-button>
          </el-tooltip>
        </template>
      </vxe-table-column>
    </vxe-table>

    <el-pagination
      class="page"
      layout="prev, pager, next"
      @current-change="getPublicProblem"
      :page-size="limit"
      :total="total">
    </el-pagination>
  </div>
</template>
<script>
  import api from '@/common/api'

  export default {
    name: 'add-problem-from-public',
    fields: ['contestID'],
    data () {
      return {
        page: 1,
        limit: 10,
        total: 0,
        loading: false,
        problems: [
          
            {id:1000,title:'测试'}
          
        ],
        contest: {},
        keyword: ''
      }
    },
    mounted () {
      // api.getContest(this.contestID).then(res => {
      //   this.contest = res.data.data
      //   this.getPublicProblem()
      // }).catch(() => {
      // })
    },
    methods: {
      getPublicProblem (page) {
        this.loading = true
        let params = {
          keyword: this.keyword,
          offset: (page - 1) * this.limit,
          limit: this.limit,
          rule_type: this.contest.rule_type
        }
        api.getProblemList(params).then(res => {
          this.loading = false
          this.total = res.data.data.total
          this.problems = res.data.data.results
        }).catch(() => {
        })
      },
      handleAddProblem (problemID) {
        this.$prompt('Please input display id for the contest problem', 'confirm').then(({value}) => {
          let data = {
            problem_id: problemID,
            contest_id: this.contestID,
            display_id: value
          }
          api.addProblemFromPublic(data).then(() => {
            this.$emit('on-change')
          }, () => {
          })
        }, () => {
        })
      },
      filterByKeyword(){
         this.getPublicProblem(this.page)
      }
    },
  }
</script>
<style scoped>
  .page {
    margin-top: 20px;
    text-align: right
  }

</style>
