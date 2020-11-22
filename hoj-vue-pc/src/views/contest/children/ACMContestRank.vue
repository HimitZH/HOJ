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
      <ECharts :options="options" ref="chart" :autoresize="true" ></ECharts>
    </div>
    <div v-show="showTable">
     <vxe-table
            round
            border
            auto-resize
            size="mini"
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
            <vxe-table-column field="rating" title="AC / Total" min-width="80">
              <template v-slot="{ row }">
                  <span>{{row.ac}} / <a @click="getUserTotalSubmit(row.user.username)" style="color:rgb(87, 163, 243);">{{row.total}}</a>
                  </span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="total_time" title="TotalTime" min-width="80">
              <template v-slot="{ row }">
              <span>{{parseTotalTime(row.total_time)}}</span>
            </template>
            </vxe-table-column>
            <vxe-table-column min-width="80" v-for="problem in problems" :key="problem.id">
              <template v-slot:header>
                <span><a @click="getContestProblemById(problem.id)">{{problem.id}}</a></span>
              </template>
              <template v-slot="{ row }">
                <span v-if="row.submission_info[problem.id].is_ac">{{ row.submission_info[problem.id].ac_time }}<br></span>
                <span v-if="row.submission_info[problem.id].error_number!=0">(-{{row.submission_info[problem.id].error_number}})</span>
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
  import moment from 'moment'
  import { mapActions } from 'vuex'

  import Pagination from '@/components/common/Pagination'
  import time from '@/common/time'
  import utils from '@/common/utils'
  import ContestRankMixin from './contestRankMixin'
  
  export default {
    name: 'acm-contest-rank',
    mixins: [ContestRankMixin],
    components: {
      Pagination
    },
    data () {
      return {
        refreshDisabled:true,
        isContestAdmin:true,
        showChart:true,
        showTable:true,
        contest:{
          start_time: "2018-04-17T11:16:52Z",
          end_time: "2018-04-22T11:16:36Z",
          now: "2020-11-17T15:16:22.013824Z",
        },
        total: 0,
        page: 1,
        limit:10,
        contestID: '',
        dataRank: [],
        problems:[],
        options: {
          title: {
            text: 'Top 10 Teams',
            left: 'center',
            top:0
          },
          dataZoom: [
            {
              type: 'inside',
              filterMode: 'none',
              xAxisIndex: [0],
              start: 0,
              end: 100
            }
          ],
          toolbox: {
            show: true,
            feature: {
              saveAsImage: {show: true, title: 'save as image'}
            },
            right: '0'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              axis: 'x'
            }
          },
          legend: {
            orient: 'horizontal',
            x:'center',
            top:'8%',
            right: 0,
            data: [],
            formatter: (value) => {
              return utils.breakLongWords(value, 16)
            },
            textStyle: {
              fontSize: 12
            }
          },
          grid: {
            x: 80,
            x2: 100,
            left: '5%',     //设置canvas图距左的距离
            top: '25%',
            right: '5%',
            bottom: '10%'
          },
          xAxis: [{
            type: 'time',
            splitLine: false,
            axisPointer: {
              show: true,
              snap: true
            }
          }],
          yAxis: [
            {
              type: 'category',
              boundaryGap: false,
              data: [0]
            }],
          series: []
        }
      }
    },
    mounted () {
      this.contestID = this.$route.params.contestID
      let dataRank=[
          { 
            user:{
              username:'Himit_ZH',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1000,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3600,error_number:0,is_first_ac:true,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:4500,error_number:0,is_first_ac:true,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
           { 
            user:{
              username:'user1',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:5555,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:6666,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'E':{ac_time:7000,error_number:0,is_first_ac:true,is_ac:true},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
           { 
            user:{
              username:'user2',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user2',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user3',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user4',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user5',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user6',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user7',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user6',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user7',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'我是你爸爸',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
            }
          },
          { 
            user:{
              username:'user9',
              id:'id',
            },
            ac:9,
            total:9,
            total_time:10000,
            submission_info:{
              'A':{ac_time:1500,error_number:3,is_first_ac:false,is_ac:true},
              'B':{ac_time:3800,error_number:0,is_first_ac:false,is_ac:true},
              'C':{ac_time:0,error_number:1,is_first_ac:false,is_ac:false},
              'D':{ac_time:8000,error_number:0,is_first_ac:false,is_ac:true},
              'E':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'F':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'G':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'H':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'I':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
              'J':{ac_time:0,error_number:0,is_first_ac:false,is_ac:false},
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
           {id:'J'}
      ];
      this.addChartCategory(this.problems)
      // this.getContestRankData(1)
      // if (this.contestProblems.length === 0) {
      //   this.getContestProblems().then((res) => {
      //     this.addChartCategory(res.data.data)
      //   })
      // } else {
      //   this.addChartCategory(this.contestProblems)
      // }
    },
    methods: {
      ...mapActions(['getContestProblems']),
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
      cellClassName ({ row, rowIndex, column, columnIndex }) {
        if (column.property !== 'id'&&column.property !== 'rating'&&column.property !== 'total_time'&&column.property !=='username') {
            return row.cellClassName[[this.problems[columnIndex-4].id]]
        }
      },
      applyToTable (dataRank) {
        // // deepcopy
        // let dataRank = JSON.parse(JSON.stringify(data))
        dataRank.forEach((rank, i) => {
          let info = rank.submission_info
          let cellClass = {}
          Object.keys(info).forEach(problemID => {
            dataRank[i][problemID] = info[problemID]
            dataRank[i][problemID].ac_time = time.secondFormat(dataRank[i][problemID].ac_time)
            let status = info[problemID]
            if (status.is_first_ac) {
              cellClass[problemID] = 'first-ac'
            } else if (status.is_ac) {
              cellClass[problemID] = 'ac'
            } else if(status.error_number!=0){
              cellClass[problemID] = 'wa'
            }
          })
          dataRank[i].cellClassName = cellClass
        })
        this.dataRank = dataRank
      },
      addChartCategory (contestProblems) {
        let category = []
        for (let i = 0; i <= contestProblems.length; ++i) {
          category.push(i)
        }
        this.options.yAxis[0].data = category
      },
      applyToChart (rankData) {
        let [users, seriesData] = [[], []]
        rankData.forEach(rank => {
          users.push(rank.user.username)
          let info = rank.submission_info
          // 提取出已AC题目的时间
          let timeData = []
          Object.keys(info).forEach(problemID => {
            if (info[problemID].is_ac) {
              timeData.push(info[problemID].ac_time)
            }
          })
          timeData.sort((a, b) => {
            return a - b
          })

          let data = []
          data.push([this.contest.start_time, 0])
          // index here can be regarded as stacked accepted number count.
          for (let [index, value] of timeData.entries()) {
            let realTime = moment(this.contest.start_time).add(value, 'seconds').format()
            data.push([realTime, index + 1])
          }
          seriesData.push({
            name: rank.user.username,
            type: 'line',
            data
          })
        })
        this.options.legend.data = users
        this.options.series = seriesData
      },
      parseTotalTime (totalTime) {
        return time.secondFormat(totalTime)
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
    padding-top:0px!important;
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
    padding: 0!important;
  } 
  @media screen and (max-width: 768px) {
    /deep/.el-card__body {
      padding: 0!important;
    }
  }
</style>
