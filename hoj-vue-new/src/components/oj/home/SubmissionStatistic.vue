<template>
  <div class="bg-white shadow-md rounded-lg p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-xl font-semibold">
        <i class="fas fa-chart-line mr-2"></i>{{ $t('m.Statistics_Submissions_In_The_Last_Week') }}
      </h3>
      <button v-if="isSuperAdmin" @click="refreshData(true)" class="text-blue-500 hover:text-blue-700" :disabled="loading">
        <i :class="['fas fa-sync-alt', loading ? 'animate-spin' : '']"></i>
        {{ $t('m.Refresh') }}
      </button>
    </div>
    <div v-loading="loading" class="h-96"> {/* h-96 is 384px, adjust as needed */}
      <v-chart v-if="!loading && chartOptions.series[0].data.length > 0" :option="chartOptions" autoresize />
      <div v-if="!loading && chartOptions.series[0].data.length === 0" class="flex items-center justify-center h-full">
        <p>{{ $t('m.No_data_available') }}</p>
      </div>
      <div v-if="loading" class="flex items-center justify-center h-full">
        <p>Loading statistics...</p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent,
} from 'echarts/components';
import VChart, { THEME_KEY } from 'vue-echarts';
import { mapGetters } from 'vuex'; // Assuming Vuex is set up for isSuperAdmin

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent,
]);

export default {
  name: 'SubmissionStatistic',
  components: {
    VChart,
  },
  // provide: { // If you want to use a global theme for ECharts
  //   [THEME_KEY]: 'light', // or 'dark'
  // },
  data() {
    return {
      loading: false,
      chartOptions: {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985',
            },
          },
        },
        legend: {
          data: [this.$i18n.t('m.AC'), this.$i18n.t('m.Total')],
        },
        toolbox: {
          feature: {
            saveAsImage: { show: true, title: this.$i18n.t('m.save_as_image') },
          },
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: [], // Dates will go here
          },
        ],
        yAxis: [
          {
            type: 'value',
          },
        ],
        series: [
          {
            name: this.$i18n.t('m.AC'),
            type: 'line',
            stack: 'Total', // Original used 'Total', Tailwind might not need this for styling a stack
            areaStyle: {},
            emphasis: {
              focus: 'series',
            },
            color: '#91cc75',
            data: [], // AC counts
          },
          {
            name: this.$i18n.t('m.Total'),
            type: 'line',
            stack: 'Total',
            label: {
              show: true,
              position: 'top',
            },
            areaStyle: {},
            emphasis: {
              focus: 'series',
            },
            color: '#73c0de',
            data: [], // Total counts
          },
        ],
      },
    };
  },
  computed: {
     ...mapGetters(['isSuperAdmin', 'webLanguage']) // Assuming these getters exist
  },
  mounted() {
    this.getLastWeekSubmissionStatistics(false);
  },
  methods: {
    getLastWeekSubmissionStatistics(forceRefresh = false) {
      this.loading = true;
      api.getLastWeekSubmissionStatistics(forceRefresh).then(
        (res) => {
          if (res.data.data) {
            this.chartOptions.xAxis[0].data = res.data.data.dateStrList || [];
            this.chartOptions.series[0].data = res.data.data.acCountList || [];
            this.chartOptions.series[1].data = res.data.data.totalCountList || [];
          } else {
             this.chartOptions.xAxis[0].data = [];
             this.chartOptions.series[0].data = [];
             this.chartOptions.series[1].data = [];
          }
          this.loading = false;
        },
        (err) => {
          console.error("Failed to load submission statistics:", err);
          this.loading = false;
          // Clear data on error
          this.chartOptions.xAxis[0].data = [];
          this.chartOptions.series[0].data = [];
          this.chartOptions.series[1].data = [];
        }
      );
    },
    refreshData(forceRefresh = true){
      this.getLastWeekSubmissionStatistics(forceRefresh);
    },
    updateChartLanguage(){
     this.chartOptions.legend.data = [this.$i18n.t('m.AC'), this.$i18n.t('m.Total')];
     if(this.chartOptions.series != null && this.chartOptions.series.length == 2){
         this.chartOptions.series[0].name = this.$i18n.t('m.AC');
         this.chartOptions.series[1].name = this.$i18n.t('m.Total');
     }
     if(this.chartOptions.toolbox.feature.saveAsImage){
         this.chartOptions.toolbox.feature.saveAsImage.title = this.$i18n.t('m.save_as_image');
     }
    }
  },
  watch:{
     webLanguage(newVal, oldVal){
         this.updateChartLanguage();
     }
  }
};
</script>

<style scoped>
/* Add FontAwesome if not already globally available */
/* @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'); */
/* v-loading directive styling might need to be handled if not using Element Plus or similar that provides it.
   For a simple Tailwind approach, you might conditionally render a loading spinner.
   The template above uses a simple text "Loading statistics..."
*/
</style>
