<template>
  <div>
    <el-row :gutter="20">
      <el-col :md="14" :sm="24">
        <el-card v-if="contests.length">
          <div slot="header" class="clearfix title content-center">
            <el-link @click="goContest" :underline="false">{{
              contests[index].title
            }}</el-link>
            <div class="contest-status">
              <el-tag
                effect="dark"
                size="medium"
                :color="CONTEST_STATUS_REVERSE[contests[index].status]['color']"
              >
                <i class="fa fa-circle" aria-hidden="true"></i>
                {{ CONTEST_STATUS_REVERSE[contests[index].status]['name'] }}
              </el-tag>
            </div>
          </div>
          <el-carousel
            indicator-position="outside"
            height="430px"
            :interval="interval"
            v-model="index"
          >
            <el-carousel-item v-for="(contest, index) in contests" :key="index">
              <div class="contest-info">
                <div class="contest-tags content-center">
                  <el-button
                    type="primary"
                    round
                    size="mini"
                    style="margin-top: 4px;"
                    ><i class="fa fa-calendar"></i>
                    {{ contest.startTime | localtime }}
                  </el-button>
                  <el-button
                    type="success"
                    round
                    size="mini"
                    style="margin-top: 4px;"
                    ><i class="fa fa-clock-o"></i>
                    {{ getDuration(contest.startTime, contest.endTime) }}
                  </el-button>
                  <el-button
                    type="warning"
                    round
                    size="mini"
                    style="margin-top: 4px;"
                    ><i class="fa fa-trophy"></i>
                    {{ contest.type | parseContestType }}
                  </el-button>
                </div>
                <div class="contest-description">
                  <blockquote v-html="contest.description"></blockquote>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </el-card>
        <el-card v-else>
          <div slot="header" class="content-center">
            <span class="panel-title home-title welcome-title"
              >Welcome to HOJ</span
            >
          </div>
          <div class="content-center">
            <h2>
              欢迎大家光临和使用本OJ
            </h2>
            <h3>
              这是一个的前后端分离的分布式在线评测系统。
            </h3>
            <h3>基于Vue.js,Springboot和SpringCloud.</h3>
            <h1>待续....</h1>
          </div>
        </el-card>
      </el-col>
      <el-col :md="10" :sm="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span class="panel-title home-title"
              >Recent 7 Days AC Top 10 Rank</span
            >
          </div>
          <vxe-table
            border="inner"
            stripe
            auto-resize
            align="center"
            :data="recentUserACRecord"
            max-height="460px"
          >
            <vxe-table-column type="seq" min-width="30"></vxe-table-column>
            <vxe-table-column
              field="username"
              title="Username"
              min-width="130"
            ></vxe-table-column>
            <vxe-table-column field="ac" title="AC" min-width="30">
            </vxe-table-column>
            <vxe-table-column field="solved" title="Solved" min-width="30">
            </vxe-table-column>
          </vxe-table>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 25px;">
      <el-col :md="14" :sm="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span class="panel-title home-title"
              >Other Online Judge Contest</span
            >
          </div>
          <vxe-table border="inner" stripe auto-resize :data="otherContests">
            <vxe-table-column
              field="oj"
              title="OJ"
              min-width="100"
            ></vxe-table-column>
            <vxe-table-column
              field="title"
              title="Title"
              min-width="200"
            ></vxe-table-column>
            <vxe-table-column
              field="beginTime"
              title="Begin Time"
              min-width="150"
            >
              <template v-slot="{ row }">
                <span>{{ row.beginTime | localtime }}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="endTime" title="End Time" min-width="150">
              <template v-slot="{ row }">
                <span>{{ row.endTime | localtime }}</span>
              </template>
            </vxe-table-column>
          </vxe-table>
        </el-card>
      </el-col>
      <el-col :md="10" :sm="24">
        <Announcements></Announcements>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import time from '@/common/time';
import api from '@/common/api';
import Announcements from '@/components/oj/common/Announcements.vue';
import { CONTEST_STATUS_REVERSE } from '@/common/constants';
import { mapState } from 'vuex';
export default {
  name: 'home',
  components: {
    Announcements,
  },
  data() {
    return {
      interval: 6000,
      otherContests: [
        {
          oj: 'Codeforces',
          title:
            'Codeforces Round #680 (Div. 1, based on VK Cup 2020-2021 - Final)',
          beginTime: '2020-11-08T05:00:00Z',
          endTime: '2020-11-08T08:00:00Z',
        },
      ],
      recentUserACRecord: [],
      CONTEST_STATUS_REVERSE: {},
      contests: [],
      index: 0,
    };
  },
  mounted() {
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.getRecentContests();
    this.getRecent7ACRank();
    // this.getRecentOtherContests()
  },
  methods: {
    getRecentContests() {
      api.getRecentContests().then((res) => {
        this.contests = res.data.data;
      });
    },
    getRecentOtherContests() {
      api.getRecentOtherContests().then((res) => {
        this.otherContests = res.data.data;
      });
    },
    getRecent7ACRank() {
      api.getRecent7ACRank().then((res) => {
        this.recentUserACRecord = res.data.data;
      });
    },
    goContest() {
      this.$router.push({
        name: 'ContestDetails',
        params: { contestID: this.contests[this.index].id },
      });
    },
    getDuration(startTime, endTime) {
      return time.duration(startTime, endTime);
    },
  },
  computed: {
    ...mapState(['websiteConfig']),
  },
};
</script>
<style scoped>
.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
}
.el-carousel__item:nth-child(n) {
  background-color: #fff;
}
.el-carousel__item {
  overflow-y: auto !important;
}
.content-center {
  text-align: center;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: '';
}
.clearfix:after {
  clear: both;
}
.welcome-title {
  font-weight: 600;
  font-size: 25px;
}
.contest-status {
  float: right;
}
@media screen and (max-width: 1080px) {
  .contest-status {
    text-align: center;
    float: none;
    margin-top: 5px;
  }
}
.title .el-link {
  font-size: 21px;
  font-weight: 500;
  color: #444;
}
.clearfix h2 {
  color: #409eff;
}
.el-link.el-link--default:hover {
  color: #409eff;
  transition: all 0.28s ease;
}
.contest .content-info {
  padding: 0 70px 40px 70px;
}
.contest .contest-description {
  margin-top: 25px;
}
</style>
