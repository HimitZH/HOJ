<template>
  <el-card class="box-card">
    <div
      slot="header"
      class="clearfix"
    >
      <span class="panel-title">{{$t('m.ScrollBoard_Parameter_Config')}}</span>
    </div>
    <el-alert
      :title="$t('m.Formula_for_calculating_the_number_of_medals')"
      type="success"
      show-icon
    >
      <template slot>
        <p>
          {{ $i18n.t('m.Number_of_gold_medals') }} : {{this.total}} × 10% = {{this.goldMedal}}
        </p>
        <p>
          {{ $i18n.t('m.Number_of_silver_medals') }} : {{this.total}} × 20% = {{this.silverMedal}}
        </p>
        <p>
          {{ $i18n.t('m.Number_of_bronze_medals') }} : {{this.total}} × 30% = {{this.bronzeMedal}}
        </p>
      </template>
    </el-alert>
    <el-form>
      <el-form-item :label="$t('m.Contest_ID')">
        <el-input
          v-model="cid"
          size="small"
          disabled
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Number_of_gold_medals')">
        <el-input
          v-model="goldMedal"
          size="small"
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Number_of_silver_medals')">
        <el-input
          v-model="silverMedal"
          size="small"
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Number_of_bronze_medals')">
        <el-input
          v-model="bronzeMedal"
          size="small"
        ></el-input>
      </el-form-item>
    </el-form>
    <div style="text-align:center">
      <template v-if="!contestEnded">
        <el-popconfirm
          :title="$t('m.Contest_Non_Ended_But_Want_to_Scroll_Board')"
          @confirm="goScrollBoard"
        >
          <el-button
            style="padding: 3px 0; font-size: 16px;"
            type="text"
            slot="reference"
          >{{$t('m.Start_Rolling')}}</el-button>
        </el-popconfirm>
      </template>
      <template v-else>
        <el-button
          style="padding: 3px 0; font-size: 16px;"
          type="text"
          @click="goScrollBoard"
        >{{$t('m.Start_Rolling')}}</el-button>
      </template>
    </div>
  </el-card>
</template>

<script>
import api from "@/common/api";
import { mapGetters } from "vuex";
import { CONTEST_STATUS } from "@/common/constants";
export default {
  name: "ScrollBoard",
  data() {
    return {
      cid: null,
      total: 0,
      goldMedal: 0,
      silverMedal: 0,
      bronzeMedal: 0,
    };
  },
  created() {
    this.cid = this.$route.params.contestID;
    this.getContestRankListCount();
  },
  methods: {
    getContestRankListCount() {
      let data = {
        currentPage: 1,
        limit: 10,
        cid: this.cid,
        forceRefresh: true,
        removeStar: true,
      };
      api.getContestRank(data).then((res) => {
        this.total = res.data.data.total;
        this.goldMedal = Math.floor(this.total * 0.1);
        this.silverMedal = Math.floor(this.total * 0.2);
        this.bronzeMedal = Math.floor(this.total * 0.3);
      });
    },
    goScrollBoard() {
      let url = `/scrollBoard?cid=${this.cid}&medals[]=${this.goldMedal}&medals[]=${this.silverMedal}&medals[]=${this.bronzeMedal}`;
      window.open(url);
    },
  },
  computed: {
    ...mapGetters(["contestStatus"]),
    contestEnded() {
      return this.contestStatus === CONTEST_STATUS.ENDED;
    },
  },
};
</script>

<style>
</style>