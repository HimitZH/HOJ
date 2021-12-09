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
              <span>{{ $t('m.Star_User') }}</span>
              <el-switch
                v-model="showStarUser"
                @change="getContestRankData(page)"
              ></el-switch>
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
                  v-model="forceUpdate"
                  @change="getContestRankData(page)"
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
        ref="OIContestRank"
        :data="dataRank"
        :cell-class-name="cellClassName"
      >
        <vxe-table-column
          field="rank"
          width="50"
          fixed="left"
          :title="$t('m.Contest_Rank_Seq')"
        >
          <template v-slot="{ row }">
            {{ row.rank == -1 ? '*' : row.rank }}
          </template>
        </vxe-table-column>

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
              :username="row[contest.rankShowName]"
              :inline="true"
              :size="37"
              color="#FFF"
              :src="row.avatar"
              :title="row[contest.rankShowName]"
            ></avatar>

            <span style="float:right;text-align:right">
              <a @click="getUserHomeByUsername(row.uid, row.username)">
                <span class="contest-username"
                  ><span class="contest-rank-flag" v-if="row.rank == -1"
                    >Star</span
                  >
                  <span class="contest-rank-flag" v-if="row.gender == 'female'"
                    >Girl</span
                  >
                  {{ row[contest.rankShowName] }}</span
                >
                <span class="contest-school" v-if="row.school">{{
                  row.school
                }}</span>
              </a>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="username"
          fixed="left"
          v-else
          min-width="300"
          :title="$t('m.User')"
          header-align="center"
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row[contest.rankShowName]"
              :inline="true"
              :size="37"
              color="#FFF"
              :src="row.avatar"
              :title="row[contest.rankShowName]"
            ></avatar>

            <span style="float:right;text-align:right">
              <a @click="getUserHomeByUsername(row.uid, row.username)">
                <span class="contest-username"
                  ><span class="contest-rank-flag" v-if="row.rank == -1"
                    >Star</span
                  >
                  <span class="contest-rank-flag" v-if="row.gender == 'female'"
                    >Girl</span
                  >
                  {{ row[contest.rankShowName] }}</span
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
          field="totalScore"
          :title="$t('m.Total_Score')"
          min-width="90"
        >
          <template v-slot="{ row }">
            <span
              ><a
                @click="getUserTotalSubmit(row.username)"
                style="color:rgb(87, 163, 243);"
                >{{ row.totalScore }}</a
              >
              <br />
              <span class="problem-time">({{ row.totalTime }}ms)</span>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="80"
          v-for="problem in contestProblems"
          :key="problem.displayId"
        >
          <template v-slot:header>
            <span style="vertical-align: top;" v-if="problem.color">
              <svg
                t="1633685184463"
                class="icon"
                viewBox="0 0 1088 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="5840"
                width="25"
                height="25"
              >
                <path
                  d="M575.872 849.408c-104.576 0-117.632-26.56-119.232-31.808-6.528-22.528 32.896-70.592 63.744-96.768l-1.728-2.624c137.6-42.688 243.648-290.112 243.648-433.472A284.544 284.544 0 0 0 478.016 0a284.544 284.544 0 0 0-284.288 284.736c0 150.4 116.352 415.104 263.744 438.336-25.152 29.568-50.368 70.784-39.104 108.928 12.608 43.136 62.72 63.232 157.632 63.232 7.872 0 11.52 9.408 4.352 19.52-21.248 29.248-77.888 63.424-167.68 63.424V1024c138.944 0 215.936-74.816 215.936-126.528a46.72 46.72 0 0 0-16.32-36.608 56.32 56.32 0 0 0-36.416-11.456zM297.152 297.472c0 44.032-38.144 25.344-38.144-38.656 0-108.032 85.248-195.712 190.592-195.712 62.592 0 81.216 39.232 38.08 39.232-105.152 0.064-190.528 87.04-190.528 195.136z"
                  :fill="problem.color"
                  p-id="5841"
                ></path>
              </svg>
            </span>
            <span
              ><a
                @click="getContestProblemById(problem.displayId)"
                class="emphasis"
                >{{ problem.displayId }}</a
              ></span
            >
          </template>
          <template v-slot="{ row }">
            <div v-if="row.submissionInfo[problem.displayId]">
              <span>{{ row.submissionInfo[problem.displayId] }}</span>
              <br />
              <span
                v-if="row.timeInfo && row.timeInfo[problem.displayId]"
                style="font-size:12px;"
                >({{ row.timeInfo[problem.displayId] }}ms)</span
              >
            </div>
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
import { mapActions } from 'vuex';
import ContestRankMixin from './contestRankMixin';
import utils from '@/common/utils';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'OIContestRank',
  components: {
    Pagination,
    Avatar,
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
    if (!this.refreshDisabled) {
      this.autoRefresh = true;
      this.handleAutoRefresh(true);
    }
  },
  computed: {
    contest() {
      return this.$store.state.contest.contest;
    },
    isMobileView() {
      return window.screen.width < 768;
    },
  },
  methods: {
    ...mapActions(['getContestProblems']),

    cellClassName({ row, rowIndex, column, columnIndex }) {
      if (column.property === 'username' && row.userCellClassName) {
        return row.userCellClassName;
      }

      if (
        column.property !== 'rank' &&
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
      let [user, scores] = [[], []];
      rankData.forEach((ele) => {
        user.push(ele[this.contest.rankShowName]);
        scores.push(ele.totalScore);
      });
      this.options.xAxis[0].data = user;
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
        if (dataRank[i].rank == -1) {
          dataRank[i].userCellClassName = 'bg-star';
        }
        if (dataRank[i].gender == 'female') {
          dataRank[i].userCellClassName = 'bg-female';
        }
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
  padding: 0px !important;
}
/deep/.vxe-body--column {
  min-width: 0;
  height: 48px;
  box-sizing: border-box;
  text-align: left;
  text-overflow: ellipsis;
  vertical-align: middle;
}
a.emphasis {
  color: #495060 !important;
}
a.emphasis:hover {
  color: #2d8cf0;
}
.problem-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

/deep/.vxe-table .vxe-cell {
  padding-left: 5px !important;
  padding-right: 5px !important;
}
</style>
