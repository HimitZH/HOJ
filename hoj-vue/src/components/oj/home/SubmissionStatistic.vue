<template>
  <el-card>
    <div
      shadow
      slot="header"
      :padding="10"
    >
      <span class="home-title panel-title"><i class="el-icon-data-line"></i> {{$t('m.Statistics_Submissions_In_The_Last_Week')}}</span>
      <span v-if="isSuperAdmin">
        <el-button
          type="primary"
          icon="el-icon-refresh"
          style="float: right;"
          size="small"
          :loading="loading"
          @click="getLastWeekSubmissionStatistics(true)"
          >{{ $t('m.Refresh') }}</el-button>
      </span>
    </div>
    <div
      class="echarts"
      v-loading="loading"
    >
      <ECharts
        :options="options"
        ref="chart"
        :autoresize="true"
      ></ECharts>
    </div>
  </el-card>
</template>
<script>
import api from "@/common/api";
import { mapGetters } from 'vuex';
export default {
  name: "SubmissionStatistics",
  props: {
    title: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      loading: false,
      options: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "cross",
            label: {
              backgroundColor: "#6a7985",
            },
          },
        },
        legend: {
          data: [this.$i18n.t("m.AC"), this.$i18n.t("m.Total")],
        },
        toolbox: {
          feature: {
            saveAsImage: { show: true, title: this.$i18n.t("m.save_as_image") },
          },
        },
        grid: {
          left: "3%",
          right: "4%",
          bottom: "3%",
          containLabel: true,
        },
        xAxis: [
          {
            type: "category",
            boundaryGap: false,
            data: [],
          },
        ],
        yAxis: [
          {
            type: "value",
          },
        ],
        series: [
          {
            name: this.$i18n.t("m.AC"),
            type: "line",
            stack: "Total",
            areaStyle: {},
            emphasis: {
              focus: "series",
            },
            color: "#91cc75",
            data: [0, 0, 0, 0, 0, 0, 0],
          },
          {
            name: this.$i18n.t("m.Total"),
            type: "line",
            stack: "Total",
            label: {
              show: true,
              position: "top",
            },
            areaStyle: {},
            emphasis: {
              focus: "series",
            },
            color: "#73c0de",
            data: [0, 0, 0, 0, 0, 0, 0],
          },
        ],
      },
    };
  },
  mounted() {
    this.getLastWeekSubmissionStatistics(false);
  },
  methods: {
    getLastWeekSubmissionStatistics(forceRefresh) {
      this.loading = true;
      api.getLastWeekSubmissionStatistics(forceRefresh).then(
        (res) => {
          this.options.xAxis[0].data = res.data.data.dateStrList;
          this.options.series[0].data = res.data.data.acCountList;
          this.options.series[1].data = res.data.data.totalCountList;
          this.loading = false;
        },
        (err) => {
          this.loading = false;
        }
      );
    },
  },
  computed: {
    ...mapGetters(['isSuperAdmin','webLanguage'])
  },
  watch:{
    webLanguage(newVal, oldVal){
        this.options.legend.data = [this.$i18n.t("m.AC"), this.$i18n.t("m.Total")];
        if(this.options.series != null && this.options.series.length == 2){
            this.options.series[0].name = this.$i18n.t("m.AC");
            this.options.series[1].name = this.$i18n.t("m.Total");
        }
    }
  }
};
</script>
<style scoped>
.echarts {
  height: 400px;
  width: 100%;
}
/deep/.el-card__body {
  padding: 20px 10px !important;
}
</style>