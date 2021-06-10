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
                @change="handleAutoRefresh"
                v-model="autoRefresh"
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
        size="small"
        align="center"
        :data="dataRank"
        :cell-class-name="cellClassName"
        :seq-config="{ startIndex: (this.page - 1) * this.limit }"
      >
        <vxe-table-column
          field="id"
          type="seq"
          min-width="50"
          fixed="left"
        ></vxe-table-column>
        <vxe-table-column
          field="username"
          min-width="150"
          :title="$t('m.User')"
        >
          <template v-slot="{ row }">
            <span
              ><a
                @click="getUserHomeByUsername(row.uid, row.username)"
                style="color:rgb(87, 163, 243);"
                >{{ row.username }}</a
              >
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="realname"
          min-width="100"
          :title="$t('m.RealName')"
          v-if="isContestAdmin"
        >
        </vxe-table-column>
        <vxe-table-column
          field="totalScore"
          :title="$t('m.Total_Score')"
          min-width="80"
        >
          <template v-slot="{ row }">
            <span
              ><a
                @click="getUserTotalSubmit(row.username)"
                style="color:rgb(87, 163, 243);"
                >{{ row.totalScore }}</a
              >
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="80"
          v-for="problem in contestProblems"
          :key="problem.displayId"
        >
          <template v-slot:header>
            <span
              ><a
                @click="getContestProblemById(problem.displayId)"
                class="emphasis"
                >{{ problem.displayId }}</a
              ></span
            >
          </template>
          <template v-slot="{ row }">
            <span v-if="row.submissionInfo[problem.displayId]">{{
              row.submissionInfo[problem.displayId]
            }}</span>
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
import { mapActions } from 'vuex';
import ContestRankMixin from './contestRankMixin';
import utils from '@/common/utils';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'OIContestRank',
  components: {
    Pagination,
  },
  mixins: [ContestRankMixin],
  data() {
    return {
      total: 0,
      page: 1,
      limit: 30,
      contestID: '',
      dataRank: [],
      autoRefresh: false,
      options: {
        title: {
          text: this.$i18n.t('m.Top_10_Teams'),
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
        },
        toolbox: {
          show: true,
          feature: {
            dataView: { show: true, readOnly: true },
            magicType: { show: true, type: ['line', 'bar'] },
            saveAsImage: { show: true, title: this.$i18n.t('m.save_as_image') },
          },
          right: '10%',
          top: '5%',
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
                return utils.breakLongWords(value, 14);
              },
            },
            axisTick: {
              alignWithLabel: true,
            },
          },
        ],
        yAxis: [
          {
            type: 'value',
          },
        ],
        grid: {
          left: '11%',
        },
        series: [
          {
            name: this.$i18n.t('m.Score'),
            type: 'bar',
            barMaxWidth: '80',
            data: [0],
            markPoint: {
              data: [{ type: 'max', name: 'max' }],
            },
          },
        ],
      },
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getContestRankData(1);
  },
  methods: {
    ...mapActions(['getContestProblems']),

    cellClassName({ row, rowIndex, column, columnIndex }) {
      if (
        column.property !== 'id' &&
        column.property !== 'totalScore' &&
        column.property !== 'username' &&
        column.property !== 'realname'
      ) {
        if (this.isContestAdmin) {
          return row.cellClassName[
            [this.contestProblems[columnIndex - 4].displayId]
          ];
        } else {
          return row.cellClassName[
            [this.contestProblems[columnIndex - 3].displayId]
          ];
        }
      }
    },
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
    applyToChart(rankData) {
      let [usernames, scores] = [[], []];
      rankData.forEach((ele) => {
        usernames.push(ele.username);
        scores.push(ele.totalScore);
      });
      this.options.xAxis[0].data = usernames;
      this.options.series[0].data = scores;
    },
    applyToTable(data) {
      let dataRank = JSON.parse(JSON.stringify(data));
      dataRank.forEach((rank, i) => {
        let info = rank.submissionInfo;
        let cellClass = {};
        Object.keys(info).forEach((problemID) => {
          dataRank[i][problemID] = info[problemID];
          let score = info[problemID];
          if (score == 0) {
            cellClass[problemID] = 'oi-0';
          } else if (score > 0 && score < 100) {
            cellClass[problemID] = 'oi-between';
          } else if (score == 100) {
            cellClass[problemID] = 'oi-100';
          }
        });
        dataRank[i].cellClassName = cellClass;
      });
      this.dataRank = dataRank;
    },

    downloadRankCSV() {
      utils.downloadFile(
        `/api/file/download-contest-rank?cid=${
          this.$route.params.contestID
        }&forceRefresh=${this.forceUpdate ? true : false}`
      );
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
  padding-top: 0 !important;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
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
  padding: 8px !important;
}
a.emphasis {
  color: #495060 !important;
}
a.emphasis:hover {
  color: #2d8cf0;
}
</style>
