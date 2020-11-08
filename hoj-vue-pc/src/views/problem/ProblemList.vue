<template>
  <el-row :gutter="18">
    <el-col :sm="24" :md="18" :lg="18">
    <el-card shadow>
      <div slot="header">
        <el-row :gutter="18">
         <el-col  :md="15" :lg="15">
        <span class="panel-title hidden-xs-only" >Problem List</span>
         </el-col>
          <el-col :xs="8" :md="4" :lg="4">
            <el-dropdown
            class="drop-menu"
            @command="filterByDifficulty"
            placement="bottom"
            trigger="hover"
          >
          <span class="el-dropdown-link">
              {{query.difficulty === '' ? 'Difficulty' : query.difficulty}}
            <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="All">All</el-dropdown-item>
              <el-dropdown-item command="Easy">Easy</el-dropdown-item>
              <el-dropdown-item command="Mid">Mid</el-dropdown-item>
              <el-dropdown-item command="Hard">Hard</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          </el-col>
          <el-col :xs="16" :md="5" :lg="5">
            <vxe-input v-model="query.keyword" placeholder="Enter keyword" type="search" size="medium" @search-click="filterByKeyword"></vxe-input>
        </el-col>
        </el-row>
      </div>
      <vxe-table
            border="inner"
            stripe
            @cell-mouseenter="cellHover"
            :data="problemList">
            <vxe-table-column field="pid" title="Problem ID" width="100"></vxe-table-column>
            <vxe-table-column field="title" title="Title" width="300" type="html"></vxe-table-column>
            <vxe-table-column field="level" title="Level" width="150" type="html"></vxe-table-column>
            <vxe-table-column field="tag" title="Tag" width="250" type="html"></vxe-table-column>
            <vxe-table-column field="total" title="Total" width="100"></vxe-table-column>
            <vxe-table-column field="ACRate" title="AC Rate" width="100"></vxe-table-column>
      </vxe-table>
    </el-card>
    <Pagination :total="total" :page-size="limit" @on-change="pushRouter" :current.sync="query.page"></Pagination>

    </el-col>

    <el-col :sm="24" :md="6" :lg="6">
     <el-card style="text-align:center">
       <span class="panel-title" >题目</span>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="success" effect="dark" size="small">AC</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="success"></el-progress>
        </el-col>
      </el-row>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="danger" effect="dark" size="small">WA</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="exception"></el-progress>
        </el-col>
      </el-row>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="danger" effect="dark" size="small">MLE</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="exception"></el-progress>
        </el-col>
      </el-row>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="danger" effect="dark" size="small">TLE</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="exception"></el-progress>
        </el-col>
      </el-row>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="danger" effect="dark" size="small">RTE</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="exception"></el-progress>
        </el-col>
      </el-row>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="warning" effect="dark" size="small">PE</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="warning"></el-progress>
        </el-col>
      </el-row>
      <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="warning" effect="dark" size="small">CE</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" status="warning"></el-progress>
        </el-col>
      </el-row>
       <el-row>
        <el-col :xs="5" :sm="4" :md="6" :lg="4" style="margin-top: 10px;">  
          <el-tag type="info" effect="dark" size="small">SE</el-tag>
        </el-col>
        <el-col :xs="19" :sm="20" :md="18" :lg="20" > 
          <el-progress :text-inside="true" :stroke-width="20" :percentage="70" :color="SEcolor"></el-progress>
        </el-col>
      </el-row>
      </el-card> 
    <el-card :padding="10" style="margin-top:20px">
      <div slot="header" ><span class="taglist-title">Tags</span></div>
      <el-button v-for="tag in tagList"
              :key="tag.id"
              @click="filterByTag(tag.name)"
              type="ghost"
              :disabled="query.tag === tag.name"
              size="mini"
              class="tag-btn">{{tag.name}}
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
  import { mapGetters } from 'vuex'
  import api from '@/common/api'
  import utils from '@/common/utils'
  import Pagination from '@/components/common/Pagination'

  export default {
    name: 'ProblemList',
    components: {
      Pagination
    },
    data () {
      return {
        SEcolor:'#909399',
        tagList: [{
          id:'12',
          name:'模拟题'
        },
        {
          id:'13',
          name:'模拟题'
        },
        {
          id:'14',
          name:'模拟题'
        },
         {
          id:'15',
          name:'模拟题'
        },
         {
          id:'16',
          name:'模拟题'
        },],
        problemList: [
          {pid:'1000',title:'<a href="https://github.com/x-extends/vxe-table" class="title-link">测试标题</a>',level:'<span class="el-tag el-tag--dark el-tag--small">Easy</span>',
          tag:'<span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span><span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span>',
          total:'10000',ACRate:'59.12%'
          },
          {pid:'1000',title:'<a href="https://github.com/x-extends/vxe-table" class="title-link">测试标题</a>',level:'<span class="el-tag el-tag--dark el-tag--small">Easy</span>',
          tag:'<span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span><span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span>',
          total:'10000',ACRate:'59.12%'
          },
          {pid:'1000',title:'<a href="https://github.com/x-extends/vxe-table" class="title-link">测试标题</a>',level:'<span class="el-tag el-tag--dark el-tag--small">Easy</span>',
          tag:'<span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span><span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span>',
          total:'10000',ACRate:'59.12%'
          },
          {pid:'1000',title:'<a href="https://github.com/x-extends/vxe-table" class="title-link">测试标题</a>',level:'<span class="el-tag el-tag--dark el-tag--small">Easy</span>',
          tag:'<span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span><span class="el-tag el-tag--medium el-tag--light is-hit" style="margin-right: 7px;">简单题</span>',
          total:'10000',ACRate:'59.12%'
          },
        ],
        limit: 20,
        total: 100,
        loadings: {
          table: true,
          tag: true
        },
        page: {
                currentPage: 1,
                pageSize: 10,
                totalResult: 300
        },
        filterConfig:{remote:true},
        routeName: '',
        query: {
          keyword: '',
          difficulty: 'All',
          tag: '',
          page: 1
        }
      }
    },
    mounted () {
       this.init()
    },
    methods: {
      init (simulate = false) {
        this.routeName = this.$route.name
        let query = this.$route.query
        this.query.difficulty = query.difficulty || ''
        this.query.keyword = query.keyword || ''
        this.query.tag = query.tag || ''
        this.query.page = parseInt(query.page) || 1
        if (this.query.page < 1) {
          this.query.page = 1
        }
        if (!simulate) {
          this.getTagList()
        }
        this.getProblemList()
      },
      pushRouter () {
        this.$router.push({
          path: '/problem',
          query: utils.filterEmptyValue(this.query)
        })
      },
      cellHover(event){
        console.log(event.data[event.rowIndex])
      },
      getProblemList () {
        let offset = (this.query.page - 1) * this.limit
        this.loadings.table = true
        api.getProblemList(offset, this.limit, this.query).then(res => {
          this.loadings.table = false
          this.total = res.data.data.total
          this.problemList = res.data.data.results
        }, res => {
          this.loadings.table = false
        })
      },
      getTagList () {
        api.getProblemTagList().then(res => {
          this.tagList = res.data.data
          this.loadings.tag = false
        }, res => {
          this.loadings.tag = false
        })
      },
      filterByTag (tagName) {
        this.query.tag = tagName
        this.query.page = 1
        this.pushRouter()
      },
      filterByDifficulty (difficulty) {
        if(difficulty==='All'){
          this.query.difficulty = ''
        }else{
          this.query.difficulty = difficulty
        }
        this.query.page = 1
        this.pushRouter()
      },
      filterByKeyword () {
        this.query.page = 1
        this.pushRouter()
      },
      pickone () {
        api.pickone().then(res => {
          this.$success('Good Luck')
          this.$router.push({name: 'problem-details', params: {problemID: res.data.data}})
        })
      }
    },
    computed: {
      ...mapGetters(['isAuthenticated'])
    },
    watch: {
      '$route' (newVal, oldVal) {
        if (newVal !== oldVal) {
          this.init(true)
        }
      },
      'isAuthenticated' (newVal) {
        if (newVal === true) {
          this.init()
        }
      }
    }
  }
</script>

<style scoped >
  .taglist-title {
    margin: 0 43%;
    font-size: 21px;
    font-weight: 500;
  }

  /deep/.tag-btn {
    margin-left: 4px!important;
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
      margin-top:5px;
    }
  }
  .panel-title{
    font-size: 21px;
    font-weight: 500;
    padding-top: 10px;
    padding-bottom: 20px;
    line-height: 30px;
  }
  ul{
    float: right;
  }
  li{
    display: inline-block;
    padding: 0 10px;
  }
  .el-progress {
    margin-top: 15px;
  }
</style>
