<template>
  <el-card shadow="always">
    <div slot="header"><span class="panel-title">AC Info</span>
    <div class="filter-row">
        <span>
          Auto Refresh(10s)
          <el-switch @change="handleAutoRefresh" v-model="autoRefresh"></el-switch>
        </span>
        <span>
          <el-button type="primary" @click="getACInfo" size="small" icon="el-icon-refresh" :loading="btnLoading">Refresh</el-button>
        </span>
    </div>
    </div>
    <vxe-table
            border="inner"
            stripe
            auto-resize
            align="center"
            :data="pagedAcInfo">
            <vxe-table-column field="ac_time" min-width="150" title="AC Time"></vxe-table-column>
            <vxe-table-column field="cpid" title="Problem ID" min-width="100"></vxe-table-column>
            <vxe-table-column field="first_blood" title="AC Status" min-width="80">
              <template v-slot="{ row }">
                <el-tag effect="dark" color="#ed3f14" v-if="row.first_blood">First Blood</el-tag>
                <el-tag effect="dark" color="#19be6b" v-else>Accepet</el-tag>
              </template>
            </vxe-table-column>
            <vxe-table-column field="username" title="Username"  min-width="150">
               <template v-slot="{ row }">
                  <span><a @click="getUserTotalSubmit(row.user.username)" style="color:rgb(87, 163, 243);">{{row.user.username}}</a>
                  </span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="checked" title="Status"  min-width="150">
              <template v-slot="{ row }">
                <el-tag effect="dark" color="#19be6b" v-if="row.checked">Checked</el-tag>
                <el-tag effect="dark" color="#f90" v-else>Not Checked</el-tag>
              </template>
            </vxe-table-column>
            <vxe-table-column field="option" title="Option"  min-width="150">
              <el-button type="primary" size="small" icon="el-icon-circle-check" @click="updateCheckedStatus(row)" round>Check It</el-button>
            </vxe-table-column>
    </vxe-table>
    <Pagination :total="total"
                :page-size.sync="limit"
                :current.sync="page"
                @on-change="handlePage"
                @on-page-size-change="handlePage(1)"
                show-sizer></Pagination>
  </el-card>
</template>
<script>
  import { mapState, mapActions } from 'vuex'
  import moment from 'moment'
  import Pagination from '@/components/oj/common/Pagination.vue'
  import api from '@/common/api'

  export default {
    name: 'acm-helper',
    components: {
      Pagination
    },
    data () {
      return {
        page: 1,
        total: 0,
        btnLoading: false,
        autoRefresh:false,
        acInfo: [],
        pagedAcInfo: [{
          ac_time:"2020-11-11 11:11:11",
          cpid:"A",
          first_blood:true,
          user:{
            id:"",
            username:"Himit_ZH",
          },
          checked:false,

        },
        {
          ac_time:"2020-11-11 11:11:11",
          cpid:"B",
          first_blood:false,
          user:{
            id:"",
            username:"Himit_ZH",
          },
          checked:true,
        }
        ],
        problemsMap: [
          {

          }
        ]
      }
    },
    mounted () {
      this.contestID = this.$route.params.contestID
      // if (this.contestProblems.length === 0) {
      //   this.getContestProblems().then((res) => {
      //     this.mapProblemDisplayID()
      //     this.getACInfo()
      //   })
      // } else {
      //   this.mapProblemDisplayID()
      //   this.getACInfo()
      // }
    },
    methods: {
      ...mapActions(['getContestProblems']),
      mapProblemDisplayID () {
        let problemsMap = {}
        this.contestProblems.forEach(ele => {
          problemsMap[ele.id] = ele._id
        })
        this.problemsMap = problemsMap
      },
      getUserTotalSubmit(username){
        this.$router.push({
                          name: 'contest-submission-list',
                          query: {username: username}
              })
      },
      getACInfo (page = 1) {
        this.btnLoading = true
        let params = {
          contest_id: this.$route.params.contestID
        }
        api.getACMACInfo(params).then(res => {
          this.btnLoading = false
          let data = res.data.data
          this.total = data.length
          this.acInfo = data
          this.handlePage()
        }).catch(() => {
          this.btnLoading = false
        })
      },
      updateCheckedStatus (row) {
        let data = {
          contest_id: this.contestID,
          problem_id: row.cpid,
          checked: true
        }
        api.updateACInfoCheckedStatus(data).then(res => {
          this.$success('Succeeded')
          this.getACInfo()
        }).catch(() => {
        })
      },
      handleAutoRefresh () {
        if (this.autoRefresh) {
          this.refreshFunc = setInterval(() => {
            this.page = 1
            this.getACInfo()
          }, 10000)
        } else {
          clearInterval(this.refreshFunc)
        }
      },
      handlePage (page = 1) {
        if (page !== 1) {
          this.loadingTable = true
        }
        let pageInfo = this.acInfo.slice((this.page - 1) * this.limit, this.page * this.limit)
        for (let v of pageInfo) {
          if (v.init) {
            continue
          } else {
            v.init = true
            v.problem_display_id = this.problemsMap[v.problem_id]
            v.ac_time = moment(this.contest.start_time).add(v.ac_info.ac_time, 'seconds').local().format('YYYY-MM-DD  HH:mm:ss')
          }
        }
        this.pagedAcInfo = pageInfo
        this.loadingTable = false
      }
    },
    computed: {
      ...mapState({
        'contest': state => state.contest.contest,
        'contestProblems': state => state.contest.contestProblems
      }),
      limit: {
        get () {
          return this.$store.state.contest.rankLimit
        },
        set (value) {
          this.$store.commit("ChangeContestRankLimit", {rankLimit: value})
        }
      }
    },
    beforeDestroy () {
      clearInterval(this.refreshFunc)
    }
  }
</script>
<style scoped>
.filter-row {
  float: right;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 2px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
/deep/ .el-tag--dark {
   border-color: #fff; 
}
</style>
