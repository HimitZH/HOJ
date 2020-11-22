<template>
  <div class="problem-list">
    <vxe-table v-if="contestRuleType == 'ACM' || OIContestRealTimePermission"
            border="inner"
            stripe
            auto-resize
            :data="problems"
            @cell-click="goContestProblem">
            <vxe-table-column field="status" title="" width="50">
              <template v-slot="{ row }">
                <el-tooltip :content="JUDGE_STATUS[row.status].name" placement="top">
                  <i class="el-icon-check" :style="getIconColor(row.status)" v-if="row.status==0" ></i>
                  <i class="el-icon-minus" :style="getIconColor(row.status)" v-else-if="row.status!=-10" ></i>
                </el-tooltip>
              </template>
            </vxe-table-column>
            <vxe-table-column field="id" width="80" title="#"></vxe-table-column>
            <vxe-table-column field="title" title="Title" min-width="350"></vxe-table-column>
            <vxe-table-column field="ac" title="AC" min-width="80"></vxe-table-column>
            <vxe-table-column field="total" title="Total"  min-width="80"></vxe-table-column>
            <vxe-table-column field="ACRating" title="AC Rate"  min-width="80"></vxe-table-column>
    </vxe-table>
      <!-- <Table v-else
             :data="problems"
             :columns="OITableColumns"
             @on-row-click="goContestProblem"
             no-data-text="$t('m.No_Problems')"></Table> -->
  </div>
</template>

<script>
  import {mapState, mapGetters} from 'vuex'
  import utils from '@/common/utils'
  import {JUDGE_STATUS} from "@/common/constants"
  export default {
    name: 'ContestProblemList',
    data () {
      return {
        contestRuleType:"ACM",
        JUDGE_STATUS:JUDGE_STATUS,
        problems:[
          {status:0,id:'A',title:'测试题目AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',ac:766,total:1000,ACRating:"76.6%"},
          {status:1,id:'B',title:'测试题目AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',ac:766,total:1000,ACRating:"76.6%"}
        ]
        // OITableColumns: [
        //   {
        //     title: '#',
        //     key: '_id',
        //     width: 150
        //   },
        //   {
        //     title: this.$i18n.t('m.Title'),
        //     key: 'title'
        //   }
        // ]
      }
    },
    mounted () {
      // this.getContestProblems()
    },
    methods: {
      getContestProblems () {
        this.$store.dispatch('getContestProblems').then(res => {
          if (this.isAuthenticated) {
            if (this.contestRuleType === 'ACM') {
              this.addStatusColumn(this.ACMTableColumns, res.data.data)
            } else if (this.OIContestRealTimePermission) {
              this.addStatusColumn(this.ACMTableColumns, res.data.data)
            }
          }
        })
      },
      goContestProblem (event) {
        console.log(event.row)
        this.$router.push({
          name: 'contest-problem-details',
          params: {
            contestID: this.$route.params.contestID,
            problemID: event.row.id
          }
        })
      },
      getACRate (ACCount, TotalCount) {
        return utils.getACRate(ACCount, TotalCount)
      },
      getIconColor(status){
        return "font-weight: 600;font-size: 16px;color:"+JUDGE_STATUS[status].rgb;
      }
    },
    computed: {
      // ...mapState({
      //   problems: state => state.contest.contestProblems
      // }),
      // ...mapGetters(['isAuthenticated', 'contestRuleType', 'OIContestRealTimePermission'])
    }
  }
</script>

<style scoped>
</style>
