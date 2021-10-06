<template>
  <el-card shadow>
    <div slot="header">
      <span class="panel-title">{{ $t('m.Contest_Rank') }}</span>
      <span style="float:right;font-size: 20px;">
        <el-popover trigger="hover" placement="left-start">
          <i class="el-icon-s-tools" slot="reference"></i>
          <div id="switches">
            <p>
              <span>{{ $t('m.Chart') }}</span>
              <el-switch v-model="showChart"></el-switch>
            </p>
            <p>
              <span>{{ $t('m.Table') }}</span>
              <el-switch v-model="showTable"></el-switch>
            </p>
            <p>
              <span>{{ $t('m.Auto_Refresh') }}(10s)</span>
              <el-switch
                :disabled="refreshDisabled"
                v-model="autoRefresh"
                @change="handleAutoRefresh"
              ></el-switch>
            </p>
            <template v-if="isContestAdmin">
              <p>
                <span>{{ $t('m.Force_Update') }}</span>
                <el-switch
                  :disabled="refreshDisabled"
                  v-model="forceUpdate"
                ></el-switch>
              </p>
            </template>
            <template>
              <el-button type="primary" size="small" @click="downloadRankCSV">{{
                $t('m.Download_as_CSV')
              }}</el-button>
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
        size="medium"
        align="center"
        :data="dataRank"
        :cell-class-name="cellClassName"
        :seq-config="{ startIndex: (this.page - 1) * this.limit }"
      >
        <vxe-table-column
          field="id"
          type="seq"
          width="50"
          fixed="left"
        ></vxe-table-column>
        <vxe-table-column
          field="username"
          fixed="left"
          min-width="300"
          :title="$t('m.User')"
          header-align="center"
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row.username"
              :inline="true"
              :size="37"
              color="#FFF"
              :src="row.avatar"
              :title="row.username"
            ></avatar>

            <span style="float:right;text-align:right">
              <a @click="getUserHomeByUsername(row.uid, row.username)">
                <span class="contest-username"
                  ><span class="female-flag" v-if="row.gender == 'female'"
                    >Girl</span
                  >{{ row.username }}</span
                >
                <span class="contest-school" v-if="row.school">{{
                  row.school
                }}</span>
              </a>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="realname"
          min-width="96"
          :title="$t('m.RealName')"
          v-if="isContestAdmin"
        >
        </vxe-table-column>
        <vxe-table-column
          field="rating"
          :title="$t('m.AC') + ' / ' + $t('m.Total')"
          min-width="80"
        >
          <template v-slot="{ row }">
            <span
              >{{ row.ac }} /
              <a
                @click="getUserTotalSubmit(row.username)"
                style="color:rgb(87, 163, 243);"
                >{{ row.total }}</a
              >
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="totalTime"
          :title="$t('m.TotalTime')"
          min-width="100"
        >
          <template v-slot="{ row }">
            <span>{{ parseTotalTime(row.totalTime) }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="120"
          v-for="problem in contestProblems"
          :key="problem.displayId"
        >
          <template v-slot:header>
            <span
              ><a
                @click="getContestProblemById(problem.displayId)"
                class="emphasis"
                style="color:#495060;"
                >{{ problem.displayId }}</a
              ></span
            >
          </template>
          <template v-slot="{ row }">
            <span v-if="row.submissionInfo[problem.displayId]">
              <span v-if="row.submissionInfo[problem.displayId].isAC"
                >{{ row.submissionInfo[problem.displayId].ACTime }}<br
              /></span>
              <span
                v-if="
                  row.submissionInfo[problem.displayId].tryNum == null &&
                    row.submissionInfo[problem.displayId].errorNum != 0
                "
              >
                (-{{
                  row.submissionInfo[problem.displayId].errorNum > 0
                    ? row.submissionInfo[problem.displayId].errorNum
                    : 0
                }})
              </span>
              <span v-if="row.submissionInfo[problem.displayId].tryNum != null"
                ><template
                  v-if="row.submissionInfo[problem.displayId].errorNum > 0"
                >
                  (-{{
                    row.submissionInfo[problem.displayId].errorNum
                  }})+</template
                >({{ row.submissionInfo[problem.displayId].tryNum
                }}{{
                  row.submissionInfo[problem.displayId].tryNum > 1
                    ? ' tries'
                    : ' try'
                }})
              </span>
            </span>
          </template>
        </vxe-table-column>
      </vxe-table>
    </div>
    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getContestRankData"
      @on-page-size-change="getContestRankData(1)"
      :layout="'prev, pager, next, sizes'"
    ></Pagination>
  </el-card>
</template>
<script>
import Avatar from 'vue-avatar';
import moment from 'moment';
import { mapActions } from 'vuex';
const Pagination = () => import('@/components/oj/common/Pagination');
import time from '@/common/time';
import utils from '@/common/utils';
import ContestRankMixin from './contestRankMixin';

export default {
  name: 'ACMContestRank',
  mixins: [ContestRankMixin],
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      total: 0,
      page: 1,
      limit: 30,
      autoRefresh: false,
      contestID: '',
      dataRank: [],
      options: {
        title: {
          text: this.$i18n.t('m.Top_10_Teams'),
          left: 'center',
          top: 0,
        },
        dataZoom: [
          {
            type: 'inside',
            filterMode: 'none',
            xAxisIndex: [0],
            start: 0,
            end: 100,
          },
        ],
        toolbox: {
          show: true,
          feature: {
            saveAsImage: { show: true, title: this.$i18n.t('m.save_as_image') },
          },
          right: '0',
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            axis: 'x',
          },
        },
        legend: {
          orient: 'horizontal',
          x: 'center',
          top: '8%',
          right: 0,
          data: [],
          formatter: (value) => {
            return utils.breakLongWords(value, 16);
          },
          textStyle: {
            fontSize: 12,
          },
        },
        grid: {
          x: 80,
          x2: 100,
          left: '5%', //设置canvas图距左的距离
          top: '25%',
          right: '5%',
          bottom: '10%',
        },
        xAxis: [
          {
            type: 'time',
            splitLine: false,
            axisPointer: {
              show: true,
              snap: true,
            },
          },
        ],
        yAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: [0],
          },
        ],
        series: [],
      },
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getContestRankData(1);
    this.addChartCategory(this.contestProblems);
  },
  methods: {
    ...mapActions(['getContestProblems']),
    getUserTotalSubmit(username) {
      this.$router.push({
        name: 'ContestSubmissionList',
        query: { username: username },
      });
    },
    getUserHomeByUsername(uid, username) {
      this.$router.push({
        name: 'UserHome',
        query: { username: username, uid: uid },
      });
    },
    getContestProblemById(pid) {
      this.$router.push({
        name: 'ContestProblemDetails',
        params: {
          contestID: this.contestID,
          problemID: pid,
        },
      });
    },
    cellClassName({ row, rowIndex, column, columnIndex }) {
      if (column.property === 'username' && row.userCellClassName) {
        return row.userCellClassName;
      }

      if (
        column.property !== 'id' &&
        column.property !== 'rating' &&
        column.property !== 'totalTime' &&
        column.property !== 'username' &&
        column.property !== 'realname'
      ) {
        if (this.isContestAdmin) {
          return row.cellClassName[
            [this.contestProblems[columnIndex - 5].displayId]
          ];
        } else {
          return row.cellClassName[
            [this.contestProblems[columnIndex - 4].displayId]
          ];
        }
      }
    },
    applyToTable(data) {
      let dataRank = JSON.parse(JSON.stringify(data));
      dataRank.forEach((rank, i) => {
        let info = rank.submissionInfo;
        let cellClass = {};
        Object.keys(info).forEach((problemID) => {
          dataRank[i][problemID] = info[problemID];
          dataRank[i][problemID].ACTime = time.secondFormat(
            dataRank[i][problemID].ACTime
          );
          let status = info[problemID];
          if (status.isFirstAC) {
            cellClass[problemID] = 'first-ac';
          } else if (status.isAC) {
            cellClass[problemID] = 'ac';
          } else if (status.tryNum != null && status.tryNum > 0) {
            cellClass[problemID] = 'try';
          } else if (status.errorNum != 0) {
            cellClass[problemID] = 'wa';
          }
        });
        dataRank[i].cellClassName = cellClass;
        if (dataRank[i].gender == 'female') {
          dataRank[i].userCellClassName = 'bg-female';
        }
      });
      this.dataRank = dataRank;
    },
    addChartCategory(contestProblems) {
      let category = [];
      for (let i = 0; i <= contestProblems.length; ++i) {
        category.push(i);
      }
      this.options.yAxis[0].data = category;
    },
    applyToChart(rankData) {
      let [users, seriesData] = [[], []];
      rankData.forEach((rank) => {
        users.push(rank.username);
        let info = rank.submissionInfo;
        // 提取出已AC题目的时间
        let timeData = [];
        Object.keys(info).forEach((problemID) => {
          if (info[problemID].isAC) {
            timeData.push(info[problemID].ACTime);
          }
        });
        timeData.sort((a, b) => {
          return a - b;
        });

        let data = [];
        data.push([this.contest.startTime, 0]);

        for (let [index, value] of timeData.entries()) {
          let realTime = moment(this.contest.startTime)
            .add(value, 'seconds')
            .format();
          data.push([realTime, index + 1]);
        }
        seriesData.push({
          name: rank.username,
          type: 'line',
          data,
        });
      });
      this.options.legend.data = users;
      this.options.series = seriesData;
    },
    parseTotalTime(totalTime) {
      return time.secondFormat(totalTime);
    },
    downloadRankCSV() {
      utils.downloadFile(
        `/api/file/download-contest-rank?cid=${
          this.$route.params.contestID
        }&forceRefresh=${this.forceUpdate ? true : false}`
      );
    },
  },
  watch: {
    contestProblems(newVal, OldVal) {
      if (newVal.length != 0) {
        this.addChartCategory(this.contestProblems);
      }
    },
  },
};
</script>
<style scoped>
.echarts {
  margin: 20px auto;
  height: 400px;
  width: 100%;
}
/deep/.el-card__body {
  padding: 20px !important;
  padding-top: 0px !important;
}

.screen-full {
  margin-right: 8px;
}

#switches p {
  margin-top: 5px;
}
#switches p:first-child {
  margin-top: 0;
}
#switches p span {
  margin-left: 8px;
  margin-right: 4px;
}
.vxe-cell p,
.vxe-cell span {
  margin: 0;
  padding: 0;
}
/deep/.vxe-table .vxe-body--column {
  line-height: 20px !important;
  padding: 0 !important;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
a.emphasis {
  color: #495060 !important;
}
a.emphasis:hover {
  color: #2d8cf0 !important;
}
/deep/.vxe-body--column {
  min-width: 0;
  height: 48px;
  box-sizing: border-box;
  text-align: left;
  text-overflow: ellipsis;
  vertical-align: middle;
}
/deep/.vxe-table .vxe-cell {
  padding-left: 5px !important;
  padding-right: 5px !important;
}
</style>
<style>
.contest-username {
  display: block;
  overflow: hidden;
  color: black;
  font-size: 13.5px;
  font-weight: 550;
}
.contest-school {
  font-size: 12px;
  font-weight: normal;
  color: dimgrey;
}
.female-flag {
  margin-right: 20px !important;
  background-color: rgb(255, 193, 10);
  border-radius: 4px;
  color: rgb(73, 36, 0);
  padding: 1px 3px !important;
}
</style>
