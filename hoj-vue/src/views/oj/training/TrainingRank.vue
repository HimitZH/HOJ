<template>
  <el-card shadow>
    <div slot="header">
      <span class="panel-title">{{ $t('m.Training_Rank') }}</span>
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
          v-if="!isMobileView"
          min-width="300"
          :title="$t('m.User')"
          header-align="center"
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row[training.rankShowName]"
              :inline="true"
              :size="37"
              color="#FFF"
              :src="row.avatar"
              :title="row[training.rankShowName]"
            ></avatar>

            <span style="float:right;text-align:right">
              <a @click="getUserHomeByUsername(row.uid, row.username)">
                <span class="training-username"
                  ><span class="female-flag" v-if="row.gender == 'female'"
                    >Girl</span
                  >{{ row[training.rankShowName] }}</span
                >
                <span class="training-school" v-if="row.school">{{
                  row.school
                }}</span>
              </a>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="username"
          v-else
          min-width="300"
          :title="$t('m.User')"
          header-align="center"
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row[training.rankShowName]"
              :inline="true"
              :size="37"
              color="#FFF"
              :src="row.avatar"
              :title="row[training.rankShowName]"
            ></avatar>

            <span style="float:right;text-align:right">
              <a @click="getUserHomeByUsername(row.uid, row.username)">
                <span class="training-username"
                  ><span class="female-flag" v-if="row.gender == 'female'"
                    >Girl</span
                  >{{ row[training.rankShowName] }}</span
                >
                <span class="training-school" v-if="row.school">{{
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
          v-if="isTrainingAdmin"
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
          v-for="problem in trainingProblemList"
          :key="problem.displayId"
        >
          <template v-slot:header>
            <span
              ><a
                @click="getTrainingProblemById(problem.displayId)"
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
            </span>
          </template>
        </vxe-table-column>
      </vxe-table>
    </div>
    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getTrainingRankData"
      @on-page-size-change="getTrainingRankData(1)"
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

export default {
  name: 'TrainingRank',
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
      trainingID: '',
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
    this.trainingID = this.$route.params.trainingID;
    this.getTrainingRankData(1);
    this.addChartCategory(this.trainingProblemList);
  },
  methods: {
    ...mapActions(['getTrainingProblems']),
    getUserTotalSubmit(username) {
      this.$router.push({
        name: 'TrainingSubmissionList',
        query: { username: username },
      });
    },
    getUserHomeByUsername(uid, username) {
      this.$router.push({
        name: 'UserHome',
        query: { username: username, uid: uid },
      });
    },
    getTrainingProblemById(pid) {
      this.$router.push({
        name: 'TrainingProblemDetails',
        params: {
          trainingID: this.trainingID,
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
        if (this.isTrainingAdmin) {
          return row.cellClassName[
            [this.trainingProblemList[columnIndex - 5].displayId]
          ];
        } else {
          return row.cellClassName[
            [this.trainingProblemList[columnIndex - 4].displayId]
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
    addChartCategory(trainingProblemList) {
      let category = [];
      for (let i = 0; i <= trainingProblemList.length; ++i) {
        category.push(i);
      }
      this.options.yAxis[0].data = category;
    },
    applyToChart(rankData) {
      let [users, seriesData] = [[], []];
      rankData.forEach((rank) => {
        users.push(rank[this.training.rankShowName]);
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
        data.push([this.training.startTime, 0]);

        for (let [index, value] of timeData.entries()) {
          let realTime = moment(this.training.startTime)
            .add(value, 'seconds')
            .format();
          data.push([realTime, index + 1]);
        }
        seriesData.push({
          name: rank[this.training.rankShowName],
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
        `/api/file/download-training-rank?cid=${
          this.$route.params.trainingID
        }&forceRefresh=${this.forceUpdate ? true : false}`
      );
    },
  },
  watch: {
    trainingProblemList(newVal, OldVal) {
      if (newVal.length != 0) {
        this.addChartCategory(this.trainingProblemList);
      }
    },
  },
  computed: {
    training() {
      return this.$store.state.training.training;
    },
    isMobileView() {
      return window.screen.width < 768;
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
