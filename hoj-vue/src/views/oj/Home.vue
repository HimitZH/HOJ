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
            height="460px"
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
                  <blockquote
                    v-html="contest.description"
                    v-katex
                    v-highlight
                    class="markdown-body"
                  ></blockquote>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </el-card>
        <el-card v-else>
          <div slot="header" class="content-center">
            <span class="panel-title home-title welcome-title"
              >Welcome to {{ toUpper(websiteConfig.shortName) }}</span
            >
          </div>
          <el-carousel
            :interval="4000"
            :height="srcHight"
            class="img-carousel"
            arrow="always"
            indicator-position="outside"
          >
            <el-carousel-item v-for="item in srcList" :key="item">
              <el-image :src="item" fit="fill"></el-image>
            </el-carousel-item>
          </el-carousel>
        </el-card>
      </el-col>
      <el-col :md="10" :sm="24" class="phone-margin">
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
            max-height="500px"
            :loading="loading.recent7ACRankLoading"
          >
            <vxe-table-column type="seq" min-width="30">
              <template v-slot="{ rowIndex }">
                <span :class="getRankTagClass(rowIndex)"
                  >{{ rowIndex + 1 }}
                </span>
                <span :class="'cite no' + rowIndex"></span>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              title="Username"
              min-width="130"
              align="left"
            >
              <template v-slot="{ row }">
                <avatar
                  :username="row.username"
                  :inline="true"
                  :size="25"
                  color="#FFF"
                  :src="row.avatar"
                  class="user-avatar"
                ></avatar>
                <a
                  @click="goUserHome(row.username, row.uid)"
                  style="color: rgb(87, 163, 243)"
                  >{{ row.username }}</a
                >
              </template>
            </vxe-table-column>
            <vxe-table-column field="ac" title="AC" min-width="30" align="left">
            </vxe-table-column>
            <vxe-table-column
              field="solved"
              title="Solved"
              min-width="50"
              align="left"
            >
            </vxe-table-column>
          </vxe-table>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :md="14" :sm="24" style="margin-top: 20px;">
        <el-card>
          <div slot="header" class="clearfix">
            <span class="panel-title home-title"
              >Other Online Judge Contest</span
            >
          </div>
          <vxe-table
            border="inner"
            highlight-hover-row
            stripe
            :loading="loading.recentOtherContestsLoading"
            auto-resize
            :data="otherContests"
            @cell-click="goOtherOJContest"
          >
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
      <el-col :md="10" :sm="24" style="margin-top: 20px;">
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
import { addCodeBtn } from '@/common/codeblock';
import Avatar from 'vue-avatar';
export default {
  name: 'home',
  components: {
    Announcements,
    Avatar,
  },
  data() {
    return {
      interval: 6000,
      otherContests: [],
      recentUserACRecord: [],
      CONTEST_STATUS_REVERSE: {},
      contests: [],
      index: 0,
      loading: {
        recent7ACRankLoading: false,
        recentOtherContestsLoading: false,
      },
      srcList: [
        'https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/home1.jfif',
        'https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/home2.jpeg',
      ],
      srcHight: '440px',
    };
  },
  mounted() {
    let screenWidth = window.screen.width;
    if (screenWidth < 768) {
      this.srcHight = '200px';
    } else {
      this.srcHight = '440px';
    }
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.getRecentContests();
    this.getRecent7ACRank();
    this.getRecentOtherContests();
  },
  methods: {
    getRecentContests() {
      api.getRecentContests().then((res) => {
        this.contests = res.data.data;
        for (let i = 0; i < this.contests.length; i++) {
          this.contests[i].description = this.$markDown.render(
            this.contests[i].description
          );
        }
        if (this.contests.length > 0) {
          this.$nextTick((_) => {
            addCodeBtn();
          });
        }
      });
    },
    getRecentOtherContests() {
      this.loading.recentOtherContestsLoading = true;
      api.getRecentOtherContests().then(
        (res) => {
          this.otherContests = res.data.data;
          this.loading.recentOtherContestsLoading = false;
        },
        (err) => {
          this.loading.recentOtherContestsLoading = false;
        }
      );
    },
    getRecent7ACRank() {
      this.loading.recent7ACRankLoading = true;
      api.getRecent7ACRank().then(
        (res) => {
          this.recentUserACRecord = res.data.data;
          this.loading.recent7ACRankLoading = false;
        },
        (err) => {
          this.loading.recent7ACRankLoading = false;
        }
      );
    },
    goContest() {
      this.$router.push({
        name: 'ContestDetails',
        params: { contestID: this.contests[this.index].id },
      });
    },
    goOtherOJContest(event) {
      window.open(event.row.url);
    },
    goUserHome(username, uid) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },
    getDuration(startTime, endTime) {
      return time.duration(startTime, endTime);
    },
    getRankTagClass(rowIndex) {
      return 'rank-tag no' + (rowIndex + 1);
    },
    toUpper(str) {
      if (str) {
        return str.toUpperCase();
      }
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
.img-carousel {
  height: 490px;
}
@media screen and (max-width: 768px) {
  .contest-status {
    text-align: center;
    float: none;
    margin-top: 5px;
  }
  .img-carousel {
    height: 220px;
    overflow: hidden;
  }
  .phone-margin {
    margin-top: 20px;
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
span.rank-tag.no1 {
  line-height: 24px;
  background: #bf2c24;
}

span.rank-tag.no2 {
  line-height: 24px;
  background: #e67225;
}

span.rank-tag.no3 {
  line-height: 24px;
  background: #e6bf25;
}

span.rank-tag {
  font: 16px/22px FZZCYSK;
  min-width: 14px;
  height: 22px;
  padding: 0 4px;
  text-align: center;
  color: #fff;
  background: #000;
  background: rgba(0, 0, 0, 0.6);
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
.cite {
  display: block;
  width: 14px;
  height: 0;
  margin: 0 auto;
  margin-top: -3px;
  border-right: 11px solid transparent;
  border-bottom: 0 none;
  border-left: 11px solid transparent;
}
.cite.no0 {
  border-top: 5px solid #bf2c24;
}
.cite.no1 {
  border-top: 5px solid #e67225;
}
.cite.no2 {
  border-top: 5px solid #e6bf25;
}
</style>
