<template>
  <el-card shadow>
    <div slot="header"><span class="panel-title">Contest Rank</span>
    <span style="float:right;font-size: 20px;">
      <el-popover trigger="hover" placement="left-start">
        <i class="el-icon-s-tools" slot="reference"></i>
        <div id="switches">
          <p>
            <span>Chart</span>
            <el-switch v-model="showChart"></el-switch>
          </p>
          <p>
            <span>Table</span>
            <el-switch v-model="showTable"></el-switch>
          </p>
          <p>
            <span>Auto Refresh(10s)</span>
            <el-switch :disabled="refreshDisabled" @on-change="handleAutoRefresh"></el-switch>
          </p>
          <template v-if="isContestAdmin">
            <p>
              <span>Force Update</span>
              <el-switch :disabled="refreshDisabled" v-model="forceUpdate"></el-switch>
            </p>
          </template>
          <template>
            <el-button type="primary" size="small" @click="downloadRankCSV">Download as CSV</el-button>
          </template>
        </div>
      </el-popover>
      </span>
    </div>
    <div v-show="showChart" class="echarts">
      <ECharts :options="options" ref="chart" :autoresize="true"></ECharts>
    </div>
    <div v-show="showTable">
     <vxe-table
            round
            border
            auto-resize
            size="small"
            align="center"
            :data="dataRank"
            :cell-class-name="cellClassName"
            :seq-config="{startIndex: (this.page - 1) * this.limit}"
            >
            <vxe-table-column field="id" type="seq" min-width="50" fixed="left"></vxe-table-column>
            <vxe-table-column field="username" min-width="150" title="User" >
              <template v-slot="{ row }">
                  <span><a @click="getUserHomeByUsername(row.user.username)" style="color:rgb(87, 163, 243);">{{row.user.username}}</a>
                  </span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="total_score" title="TotalScore" min-width="80">
              <template v-slot="{ row }">
                  <span><a @click="getUserTotalSubmit(row.user.username)" style="color:rgb(87, 163, 243);">{{row.total_score}}</a>
                  </span>
              </template>
            </vxe-table-column>
            <vxe-table-column min-width="80" v-for="problem in problems" :key="problem.id">
              <template v-slot:header>
                <span><a @click="getContestProblemById(problem.id)" class="emphasis">{{problem.id}}</a></span>
              </template>
              <template v-slot="{ row }">
                <span v-if="row.submission_info[problem.id]!=-1">{{row.submission_info[problem.id]}}</span>
              </template>
            </vxe-table-column>
     </vxe-table>
     </div>
    <Pagination :total="total"
                :page-size.sync="limit"
                :current.sync="page"
                @on-change="getContestRankData"
                @on-page-size-change="getContestRankData(1)"
                show-sizer></Pagination>
  </el-card>
</template>
<script>
  import { mapActions } from 'vuex'

  import Pagination from '@/components/common/Pagination'
  import ContestRankMixin from './contestRankMixin'
  import utils from '@/common/utils'

  export default {
    name: 'acm-contest-rank',
    components: {
      Pagination
    },
    mixins: [ContestRankMixin],
    data () {
      return {
        refreshDisabled:true,
        isContestAdmin:true,
        showChart:true,
        showTable:true,
        total: 0,
        page: 1,
        limit:10,
        contestID: '',
        dataRank: [],
        problems:[],        
        options: {
          
          title: {
            text: 'Top 10 Teams',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          toolbox: {
            show: true,
            feature: {
              dataView: {show: true, readOnly: true},
              magicType: {show: true, type: ['line', 'bar']},
              saveAsImage: {show: true}
            },
            right: '10%',
            top:'5%'
          },
          calculable: true,
          xAxis: [
            {
              type: 'category',
              data: ['root'],
              boundaryGap: true,
              axisLabel: {
                interval: 0,
                showMinLabel: true,
                showMaxLabel: true,
                align: 'center',
                formatter: (value, index) => {
                  return utils.breakLongWords(value, 14)
                }
              },
              axisTick: {
                alignWithLabel: true
              }
            }
          ],
          yAxis: [
            {
              type: 'value',
            }
          ],
          grid: {
            left:'11%'
          },
          series: [
            {
              name: 'Score',
              type: 'bar',
              barMaxWidth: '80',
              data: [0],
              markPoint: {
                data: [
                  {type: 'max', name: 'max'}
                ]
              }
            }
          ]
        }
      }
    },
    mounted () {
      this.contestID = this.$route.params.contestID
      // this.getContestRankData(1)
      // if (this.contestProblems.length === 0) {
      //   this.getContestProblems().then((res) => {
      //     this.addTableColumns(res.data.data)
      //   })
      // } else {
      //   this.addTableColumns(this.contestProblems)
      // }
      let dataRank=[{
            user:{
              username:'user1',
              id:'id',
            },
            total_score:9999,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user2',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user3',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user4',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user5',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user6',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user7',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user8',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user9',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
          {
            user:{
              username:'user10',
              id:'id',
            },
            total_score:430,
            submission_info:{
              'A':100,
              'B':-1,
              'C':100,
              'D':60,
              'E':70,
              'F':100,
              'G':0,
              'H':-1,
              'I':-1
            }
          },
        ];

      this.applyToTable(dataRank);
      this.applyToChart(dataRank);
      this.problems =[
          {id:'A'},
          {id:'B'},
          {id:'C'},
          {id:'D'},
          {id:'E'},
          {id:'F'},
          {id:'G'},
          {id:'H'},
          {id:'I'},
      ];
    },
    methods: {
      ...mapActions(['getContestProblems']),

      cellClassName ({ row, rowIndex, column, columnIndex }) {
        if (column.property !== 'id'&&column.property !== 'total_score'&&column.property !=='username') {
            return row.cellClassName[[this.problems[columnIndex-3].id]]
        }
      },
      getUserTotalSubmit(username){
        this.$router.push({
                          name: 'contest-submission-list',
                          query: {username: username}
                        })
      },
      getUserHomeByUsername(username){
        
        this.$router.push(
                      {
                        name: 'user-home',
                        query: {username:username}
        })
      },
      getContestProblemById(pid){
        this.$router.push({
                      name: 'contest-problem-details',
                      params: {
                        contestID: this.contestID,
                        problemID: pid
                      }
                  })
      },
      applyToChart (rankData) {
        let [usernames, scores] = [[], []]
        rankData.forEach(ele => {
          usernames.push(ele.user.username)
          scores.push(ele.total_score)
        })
        this.options.xAxis[0].data = usernames
        this.options.series[0].data = scores
      },
      applyToTable (dataRank) {
        // let dataRank = JSON.parse(JSON.stringify(data))

        dataRank.forEach((rank, i) => {
          let info = rank.submission_info
          let cellClass = {}
          Object.keys(info).forEach(problemID => {
            dataRank[i][problemID] = info[problemID]
            let score = info[problemID]
            if(score==0){
              cellClass[problemID] = 'oi-0';
            }else if(score>0&&score<100){
              cellClass[problemID] = 'oi-between';
            }else if(score==100){
              cellClass[problemID] = 'oi-100';
            }
          })
          dataRank[i].cellClassName = cellClass
        })
        this.dataRank = dataRank
      },
     
      downloadRankCSV () {
        utils.downloadFile(`contest_rank?download_csv=1&contest_id=${this.$route.params.contestID}&force_refrash=${this.forceUpdate ? '1' : '0'}`)
      }
    }
  }
</script>
<style scoped>
  .echarts {
    margin: 20px auto;
    height: 400px;
    width: 100%;
  }
  /deep/.el-card__body {
      padding: 20px!important;
      padding-top:0!important;
  }
  @media screen and (max-width: 768px) {
    /deep/.el-card__body {
      padding: 0!important;
    }
  }

  .screen-full {
    margin-right: 8px;
  }

  #switches p{
     margin-top: 5px;
  }
  #switches p:first-child{
    margin-top: 0;
  }
  #switches p span {
    margin-left: 8px;
    margin-right: 4px;
    
  }
  .vxe-cell p,.vxe-cell span{
    margin: 0;
    padding: 0;
  }
  /deep/.vxe-table .vxe-body--column{
    line-height: 20px!important;
    padding: 8px!important;
  } 
  a.emphasis{
    color:#495060
  }
  a.emphasis:hover{
    color:#2d8cf0
  }
</style>
