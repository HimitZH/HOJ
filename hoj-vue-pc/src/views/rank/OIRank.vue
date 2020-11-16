<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="22">
    <el-card :padding="10">
      <div slot="header"><span class="panel-title">OI Ranklist</span></div>
      <div class="echarts">
        <ECharts :options="options" ref="chart" auto-resize></ECharts>
      </div>
    </el-card>
    <vxe-table :data="dataRank" :loading="loadingTable" align="center" highlight-hover-row style="font-weight: 500;">
      <vxe-table-column type="seq" width="100"></vxe-table-column>
      <vxe-table-column field="username" title="User" width="168">
         <template v-slot="{ row }">
          <a :href="getInfoByUsername(row.username)" style="color:rgb(87, 163, 243);">{{row.username}}</a>
        </template>
      </vxe-table-column>
      <vxe-table-column field="nickname" title="Nickname" width="168"></vxe-table-column>
      <vxe-table-column field="signature" title="Mood" width="173"></vxe-table-column>
      <vxe-table-column field="score" title="Score" width="168"></vxe-table-column>
      <vxe-table-column field="ac" title="AC" width="168"></vxe-table-column>
      <vxe-table-column field="total" title="Total" width="168"></vxe-table-column>
      <vxe-table-column field="rating" title="Rating" width="168"></vxe-table-column>
    </vxe-table>
    <Pagination :total="total" :page-size.sync="limit" :current.sync="page"
                @on-change="getRankData"
                show-sizer @on-page-size-change="getRankData(1)"></Pagination>
    </el-col>
  </el-row>
</template>

<script>
  import api from '@/common/api'
  import Pagination from '@/components/common/Pagination'
  import utils from '@/common/utils'
  import { RULE_TYPE } from '@/common/constants'

  export default {
    name: 'acm-rank',
    components: {
      Pagination
    },
    data () {
      return {
        page: 1,
        limit: 30,
        total: 0,
        dataRank: [
          {username:'root',nickname:'Himit_ZH',signature:'Show me your code',score:10000,ac:100,total:100,rating:'100%'},
          {username:'root',nickname:'Himit_ZH',signature:'Show me your code',score:10000,ac:100,total:100,rating:'100%'},
          {username:'root',nickname:'Himit_ZH',signature:'Show me your code',score:10000,ac:100,total:100,rating:'100%'},
        ],
        options: {
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['Score']
          },
          grid: {
            x: '3%',
            x2: '3%'
          },
          toolbox: {
            show: true,
            feature: {
              dataView: {show: true, readOnly: true},
              magicType: {show: true, type: ['line', 'bar']},
              saveAsImage: {show: true}
            },
            right: '5%',
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
              type: 'value'
            }
          ],
          series: [
            {
              name: 'Score',
              type: 'bar',
              data: [0],
              barMaxWidth: '80',
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
      // this.getRankData(1)
    },
    methods: {
      getRankData (page) {
        let offset = (page - 1) * this.limit
        let bar = this.$refs.chart
        bar.showLoading({maskColor: 'rgba(250, 250, 250, 0.8)'})
        api.getUserRank(offset, this.limit, RULE_TYPE.OI).then(res => {
          if (page === 1) {
            this.changeCharts(res.data.data.results.slice(0, 10))
          }
          this.total = res.data.data.total
          this.dataRank = res.data.data.results
          bar.hideLoading()
        })
      },
      changeCharts (rankData) {
        let [usernames, scores] = [[], []]
        rankData.forEach(ele => {
          usernames.push(ele.user.username)
          scores.push(ele.total_score)
        })
        this.options.xAxis[0].data = usernames
        this.options.series[0].data = scores
      },
      getInfoByUsername(username){
        return '/user-home/'+username;
      }
    }
  }
</script>

<style scoped>
  .echarts {
    margin: 0 auto;
    width: 95%;
    height: 400px;
  }
</style>
