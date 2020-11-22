<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
    <el-card :padding="10">
      <div slot="header"><span class="panel-title">ACM Ranklist</span></div>
      <div class="echarts">
        <ECharts :options="options" ref="chart" :autoresize="true"></ECharts>
      </div>
    </el-card>
    <vxe-table :data="dataRank" :loading="loadingTable" align="center" highlight-hover-row auto-resize style="font-weight: 500;">
      <vxe-table-column type="seq" min-width="50"></vxe-table-column>
      <vxe-table-column field="username" title="User" min-width="150">
         <template v-slot="{ row }">
          <a :href="getInfoByUsername(row.username)" style="color:rgb(87, 163, 243);">{{row.username}}</a>
        </template>
      </vxe-table-column>
      <vxe-table-column field="nickname" title="Nickname" min-width="180"></vxe-table-column>
      <vxe-table-column field="signature" title="Mood" min-width="180"></vxe-table-column>
      <vxe-table-column field="ac" title="AC" min-width="80"></vxe-table-column>
      <vxe-table-column field="total" title="Total" min-width="80"></vxe-table-column>
      <vxe-table-column field="rating" title="Rating" min-width="80"></vxe-table-column>
    </vxe-table>
    
    <Pagination :total="total" :page-size.sync="limit" :current.sync="page"
                @on-change="getRankData" show-sizer
                @on-page-size-change="getRankData(1)"></Pagination>
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
        loadingTable: false,
        dataRank: [],
        options: {
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['AC', 'Total']
          },
          grid: {
            x: '3%',
            x2: '3%',
            left:'8%',
            right:'8%'
          },
          toolbox: {
            show: true,
            feature: {
              dataView: {show: true, readOnly: true},
              magicType: {show: true, type: ['line', 'bar', 'stack']},
              saveAsImage: {show: true}
            },
            right: '8%',
            top:'5%'
          },
          calculable: true,
          xAxis: [
            {
              type: 'category',
              data: ['root'],
              axisLabel: {
                interval: 0,
                showMinLabel: true,
                showMaxLabel: true,
                align: 'center',
                formatter: (value, index) => {
                  return utils.breakLongWords(value, 13)
                }
              }
            }
          ],
          yAxis: [
            {
              type: 'value',
              axisLabel: {
                rotate:50,
                textStyle:{
                  fontSize:'12em',
                },
              }
            }
          ],
          series: [
            {
              name: 'AC',
              type: 'bar',
              data: [0],
              markPoint: {
                data: [
                  {type: 'max', name: 'max'}
                ]
              }
            },
            {
              name: 'Total',
              type: 'bar',
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
      // this.getRankData(1)
      let dataRank = [
                    {username:'20180308010541111',nickname:'Himit_ZHsssssssssssssssss我说你啵啵啵啵啵啵',signature:'Show me your code',ac:9999,total:9999,rating:'99.99%'},
                    {username:'测试名字为什么要那么长的呢',nickname:'图sddddddddd',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'20180308010541111',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'测试名字为什么要那么长你有病啊',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'测试名字为什么要那么长',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'root',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'root',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'root',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'root',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
                    {username:'root',nickname:'Himit_ZH',signature:'Show me your code',ac:100,total:100,rating:'100%'},
        
                  ];
      this.dataRank = dataRank;
      this.changeCharts(dataRank);
    },
    methods: {
      getRankData (page) {
        let offset = (page - 1) * this.limit
        let bar = this.$refs.chart
        bar.showLoading({maskColor: 'rgba(250, 250, 250, 0.8)'})
        this.loadingTable = true
        api.getUserRank(offset, this.limit, RULE_TYPE.ACM).then(res => {
          this.loadingTable = false
          if (page === 1) {
            this.changeCharts(res.data.data.results.slice(0, 10))
          }
          this.total = res.data.data.total
          this.dataRank = res.data.data.results
          bar.hideLoading()
        }).catch(() => {
          this.loadingTable = false
          bar.hideLoading()
        })
      },
      changeCharts (rankData) {
        let [usernames, acData, totalData] = [[], [], []]
        rankData.forEach(ele => {
          usernames.push(ele.username)
          acData.push(ele.ac)
          totalData.push(ele.total)
        })
        this.options.xAxis[0].data = usernames
        this.options.series[0].data = acData
        this.options.series[1].data = totalData
      },
      getInfoByUsername(username){
        return '/user-home/'+username;
      }
    },
    computed:{
      getFontSize(res){
        let docEl = document.documentElement,
          clientWidth = window.innerWidth||document.documentElement.clientWidth||document.body.clientWidth;
        if (!clientWidth) return;
        let fontSize = 100 * (clientWidth / 1920);
        return res*fontSize;
      }
    }
  }
</script>

<style scoped>
  .echarts {
    margin: 0 auto;
    width: 100%;
    height: 400px;
  }
  @media screen and (max-width: 768px) {
    /deep/.el-card__body {
      padding: 0!important;
    }
  }
</style>
