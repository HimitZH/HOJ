<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card :padding="10">
        <div slot="header"><span class="panel-title">OI Ranklist</span></div>
        <div class="echarts">
          <ECharts :options="options" ref="chart" auto-resize></ECharts>
        </div>
      </el-card>
      <vxe-table
        :data="dataRank"
        :loading="loadingTable"
        align="center"
        highlight-hover-row
        auto-resize
        style="font-weight: 500;"
      >
        <vxe-table-column type="seq" min-width="50"></vxe-table-column>
        <vxe-table-column field="username" title="User" min-width="150">
          <template v-slot="{ row }">
            <a
              @click="getInfoByUsername(row.uid, row.username)"
              style="color:rgb(87, 163, 243);"
              >{{ row.username }}</a
            >
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="nickname"
          title="Nickname"
          min-width="180"
        ></vxe-table-column>
        <vxe-table-column
          field="signature"
          title="Mood"
          min-width="180"
        ></vxe-table-column>
        <vxe-table-column
          field="score"
          title="Score"
          min-width="80"
        ></vxe-table-column>
        <vxe-table-column title="AC" min-width="80">
          <template v-slot="{ row }">
            <a
              @click="goUserACStatus(row.username)"
              style="color:rgb(87, 163, 243);"
              >{{ row.ac }}</a
            >
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="total"
          title="Total"
          min-width="80"
        ></vxe-table-column>
        <vxe-table-column title="Rating" min-width="80">
          <template v-slot="{ row }">
            <span>{{ getACRate(row.ac, row.total) }}</span>
          </template>
        </vxe-table-column>
      </vxe-table>
      <Pagination
        :total="total"
        :page-size.sync="limit"
        :current.sync="page"
        @on-change="getRankData"
        show-sizer
        @on-page-size-change="getRankData(1)"
      ></Pagination>
    </el-col>
  </el-row>
</template>

<script>
import api from '@/common/api';
import Pagination from '@/components/oj/common/Pagination';
import utils from '@/common/utils';
import { RULE_TYPE } from '@/common/constants';
import { mapGetters } from 'vuex';

export default {
  name: 'acm-rank',
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 30,
      total: 0,
      dataRank: [],
      loadingTable: false,
      options: {
        tooltip: {
          trigger: 'axis',
        },
        legend: {
          data: ['Score'],
        },
        grid: {
          x: '3%',
          x2: '3%',
          left: '8%',
          right: '8%',
        },
        toolbox: {
          show: true,
          feature: {
            dataView: { show: true, readOnly: true },
            magicType: { show: true, type: ['line', 'bar'] },
            saveAsImage: { show: true },
          },
          right: '8%',
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
                if (this.isAuthenticated && this.userInfo.username == value) {
                  return utils.breakLongWords(value, 14);
                } else {
                  return '';
                }
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
            axisLabel: {
              rotate: 50,
              textStyle: {
                fontSize: '12em',
              },
            },
          },
        ],
        series: [
          {
            name: 'Score',
            type: 'bar',
            data: [0],
            barMaxWidth: '80',
            markPoint: {
              data: [{ type: 'max', name: 'max' }],
            },
          },
        ],
      },
    };
  },
  mounted() {
    this.getRankData(1);
  },
  methods: {
    getRankData(page) {
      let bar = this.$refs.chart;
      bar.showLoading({ maskColor: 'rgba(250, 250, 250, 0.8)' });
      this.loadingTable = true;
      api.getUserRank(page, this.limit, RULE_TYPE.OI).then(
        (res) => {
          if (page === 1) {
            this.changeCharts(res.data.data.records.slice(0, 10));
          }
          this.total = res.data.data.total;
          this.dataRank = res.data.data.records;
          this.loadingTable = false;
          bar.hideLoading();
        },
        (err) => {
          this.loadingTable = false;
          bar.hideLoading();
        }
      );
    },
    changeCharts(rankData) {
      let [usernames, scores] = [[], []];
      rankData.forEach((ele) => {
        usernames.push(ele.username);
        scores.push(ele.score);
      });
      this.options.xAxis[0].data = usernames;
      this.options.series[0].data = scores;
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },
    goUserACStatus(username) {
      this.$router.push({
        path: '/status',
        query: { username, status: 0 },
      });
    },
    getACRate(ac, total) {
      return utils.getACRate(ac, total);
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'userInfo']),
  },
};
</script>

<style scoped>
.echarts {
  margin: 0 auto;
  width: 100%;
  height: 400px;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
</style>
