<template>
  <div>
    <el-row :gutter="30">
      <el-col
        :md="15"
        :sm="24"
      >
        <el-card>
          <div
            slot="header"
            class="content-center"
          >
            <span class="panel-title home-title welcome-title">{{ $t('m.Welcome_to')
              }}{{ websiteConfig.shortName }}</span>
          </div>
          <el-carousel
            :interval="interval"
            :height="srcHight"
            class="img-carousel"
            arrow="always"
            indicator-position="outside"
          >
            <el-carousel-item
              v-for="(item, index) in carouselImgList"
              :key="index"
            >
              <el-image
                :src="item.url"
                fit="fill"
              >
                <div
                  slot="error"
                  class="image-slot"
                >
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </el-carousel-item>
          </el-carousel>
        </el-card>
        <Announcements class="card-top"></Announcements>
        <SubmissionStatistic class="card-top"></SubmissionStatistic>
        <el-card class="card-top">
          <div
            slot="header"
            class="clearfix"
          >
            <span class="panel-title home-title">
              <i class="el-icon-magic-stick"></i> {{
              $t('m.Latest_Problem')
            }}</span>
          </div>
          <vxe-table
            border="inner"
            highlight-hover-row
            stripe
            :loading="loading.recentUpdatedProblemsLoading"
            auto-resize
            :data="recentUpdatedProblems"
            @cell-click="goProblem"
          >
            <vxe-table-column
              field="problemId"
              :title="$t('m.Problem_ID')"
              min-width="100"
              show-overflow
              align="center"
            >
            </vxe-table-column>
            <vxe-table-column
              field="title"
              :title="$t('m.Title')"
              show-overflow
              min-width="130"
              align="center"
            >
            </vxe-table-column>
            <vxe-table-column
              field="gmtModified"
              :title="$t('m.Recent_Update')"
              show-overflow
              min-width="96"
              align="center"
            >
              <template v-slot="{ row }">
                <el-tooltip
                  :content="row.gmtModified | localtime"
                  placement="top"
                >
                  <span>{{ row.gmtModified | fromNow }}</span>
                </el-tooltip>
              </template>
            </vxe-table-column>

          </vxe-table>
        </el-card>
      </el-col>
      <el-col
        :md="9"
        :sm="24"
        class="phone-margin"
      >
        <template v-if="contests.length">
          <el-card>
            <div
              slot="header"
              class="clearfix title content-center"
            >
              <div class="home-title home-contest">
                <i class="el-icon-trophy"></i> {{ $t('m.Recent_Contest') }}
              </div>
            </div>
            <el-card
              shadow="hover"
              v-for="(contest, index) in contests"
              :key="index"
              class="contest-card"
              :class="
                contest.status == 0
                  ? 'contest-card-running'
                  : 'contest-card-schedule'
              "
            >
              <div
                slot="header"
                class="clearfix contest-header"
              >
                <a
                  class="contest-title"
                  @click="goContest(contest.id)"
                >{{
                  contest.title
                }}</a>
                <div class="contest-status">
                  <el-tag
                    effect="dark"
                    size="medium"
                    :color="CONTEST_STATUS_REVERSE[contest.status]['color']"
                  >
                    <i
                      class="fa fa-circle"
                      aria-hidden="true"
                    ></i>
                    {{
                      $t('m.' + CONTEST_STATUS_REVERSE[contest.status]['name'])
                    }}
                  </el-tag>
                </div>
              </div>
              <div class="contest-type-auth">
                <template v-if="contest.type == 0">
                  <el-button
                    :type="'primary'"
                    round
                    @click="goContestList(contest.type)"
                    size="mini"
                    style="margin-right: 10px;"
                  ><i class="fa fa-trophy"></i>
                    {{ contest.type | parseContestType }}
                  </el-button>
                </template>
                <template v-else>
                  <el-tooltip
                    :content="
                      $t('m.Contest_Rank') +
                        '：' +
                        (contest.oiRankScoreType == 'Recent'
                          ? $t(
                              'm.Based_on_The_Recent_Score_Submitted_Of_Each_Problem'
                            )
                          : $t(
                              'm.Based_on_The_Highest_Score_Submitted_For_Each_Problem'
                            ))
                    "
                    placement="top"
                  >
                    <el-button
                      :type="'warning'"
                      round
                      @click="goContestList(contest.type)"
                      size="mini"
                      style="margin-right: 10px;"
                    ><i class="fa fa-trophy"></i>
                      {{ contest.type | parseContestType }}
                    </el-button>
                  </el-tooltip>
                </template>
                <el-tooltip
                  :content="$t('m.' + CONTEST_TYPE_REVERSE[contest.auth].tips)"
                  placement="top"
                  effect="light"
                >
                  <el-tag
                    :type="CONTEST_TYPE_REVERSE[contest.auth]['color']"
                    size="medium"
                    effect="plain"
                  >
                    {{ $t('m.' + CONTEST_TYPE_REVERSE[contest.auth]['name']) }}
                  </el-tag>
                </el-tooltip>
              </div>
              <ul class="contest-info">
                <li>
                  <el-button
                    type="primary"
                    round
                    size="mini"
                    style="margin-top: 4px;"
                  ><i class="fa fa-calendar"></i>
                    {{
                      contest.startTime | localtime((format = 'MM-DD HH:mm'))
                    }}
                  </el-button>
                </li>
                <li>
                  <el-button
                    type="success"
                    round
                    size="mini"
                    style="margin-top: 4px;"
                  ><i class="fa fa-clock-o"></i>
                    {{ getDuration(contest.startTime, contest.endTime) }}
                  </el-button>
                </li>
                <li>
                  <el-button
                    size="mini"
                    round
                    plain
                    v-if="contest.count != null"
                  >
                    <i
                      class="el-icon-user-solid"
                      style="color:rgb(48, 145, 242);"
                    ></i>x{{ contest.count }}
                  </el-button>
                </li>
              </ul>
            </el-card>
          </el-card>
        </template>
        <el-card :class="contests.length ? 'card-top' : ''">
          <div
            slot="header"
            class="clearfix"
          >
            <span class="panel-title home-title">
              <i class="el-icon-s-data"></i> {{ $t('m.Recent_7_Days_AC_Rank')}}
            </span>
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
            <vxe-table-column
              type="seq"
              min-width="50"
            >
              <template v-slot="{ rowIndex }">
                <span :class="getRankTagClass(rowIndex)">{{ rowIndex + 1 }}
                </span>
                <span :class="'cite no' + rowIndex"></span>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              :title="$t('m.Username')"
              min-width="200"
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
                  style="color:#2d8cf0;"
                >{{ row.username }}</a>
                <span
                  style="margin-left:2px"
                  v-if="row.titleName"
                >
                  <el-tag
                    effect="dark"
                    size="small"
                    :color="row.titleColor"
                  >
                    {{ row.titleName }}
                  </el-tag>
                </span>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="ac"
              :title="$t('m.AC')"
              min-width="50"
              align="left"
            >
            </vxe-table-column>
          </vxe-table>
        </el-card>
        <el-card class="card-top">
          <div
            slot="header"
            class="clearfix title"
          >
            <span class="home-title panel-title">
              <i class="el-icon-monitor"></i> {{ $t('m.Supported_Remote_Online_Judge') }}
            </span>
          </div>
          <el-row :gutter="20">
            <el-col
              :md="8"
              :sm="24"
              v-for="(oj, index) in remoteJudgeList"
              :key="index"
            >
              <a
                :href="oj.url"
                target="_blank"
              >
                <el-tooltip
                  :content="oj.name"
                  placement="top"
                >
                  <el-image
                    :src="oj.logo"
                    fit="fill"
                    class="oj-logo"
                    :class="
                      oj.status ? 'oj-normal ' + oj.name : 'oj-error ' + oj.name
                    "
                  >
                    <div
                      slot="error"
                      class="image-slot"
                    >
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </el-image>
                </el-tooltip>
              </a>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import time from "@/common/time";
import api from "@/common/api";
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_TYPE_REVERSE,
} from "@/common/constants";
import { mapState, mapGetters } from "vuex";
import Avatar from "vue-avatar";
import myMessage from "@/common/message";
const Announcements = () => import("@/components/oj/common/Announcements.vue");
const SubmissionStatistic = () =>
  import("@/components/oj/home/SubmissionStatistic.vue");
export default {
  name: "home",
  components: {
    Announcements,
    SubmissionStatistic,
    Avatar,
  },
  data() {
    return {
      interval: 5000,
      recentUpdatedProblems: [],
      recentUserACRecord: [],
      CONTEST_STATUS_REVERSE: {},
      CONTEST_TYPE_REVERSE: {},
      contests: [],
      loading: {
        recent7ACRankLoading: false,
        recentUpdatedProblemsLoading: false,
        recentContests: false,
      },
      carouselImgList: [
        {
          url: "https://s1.ax1x.com/2022/05/15/ORSjyT.jpg",
        },
        {
          url: "https://s1.ax1x.com/2022/05/15/ORp86f.jpg",
        },
      ],
      srcHight: "440px",
      remoteJudgeList: [
        {
          url: "http://acm.hdu.edu.cn",
          name: "HDU",
          logo: require("@/assets/hdu-logo.png"),
          status: true,
        },
        {
          url: "http://poj.org",
          name: "POJ",
          logo: require("@/assets/poj-logo.png"),
          status: true,
        },
        {
          url: "https://codeforces.com",
          name: "Codeforces",
          logo: require("@/assets/codeforces-logo.png"),
          status: true,
        },
        {
          url: "https://codeforces.com/gyms",
          name: "GYM",
          logo: require("@/assets/gym-logo.png"),
          status: true,
        },
        {
          url: "https://atcoder.jp",
          name: "AtCoder",
          logo: require("@/assets/atcoder-logo.png"),
          status: true,
        },
        {
          url: "https://www.spoj.com",
          name: "SPOJ",
          logo: require("@/assets/spoj-logo.png"),
          status: true,
        },
      ],
    };
  },
  mounted() {
    let screenWidth = window.screen.width;
    if (screenWidth < 768) {
      this.srcHight = "200px";
    } else {
      this.srcHight = "440px";
    }
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.getHomeCarousel();
    this.getRecentContests();
    this.getRecent7ACRank();
    this.getRecentUpdatedProblemList();
  },
  methods: {
    getHomeCarousel() {
      api.getHomeCarousel().then((res) => {
        if (res.data.data != null && res.data.data.length > 0) {
          this.carouselImgList = res.data.data;
        }
      });
    },

    getRecentContests() {
      this.loading.recentContests = true;
      api.getRecentContests().then(
        (res) => {
          this.contests = res.data.data;
          this.loading.recentContests = false;
        },
        (err) => {
          this.loading.recentContests = false;
        }
      );
    },
    getRecentUpdatedProblemList() {
      this.loading.recentUpdatedProblemsLoading = true;
      api.getRecentUpdatedProblemList().then(
        (res) => {
          this.recentUpdatedProblems = res.data.data;
          this.loading.recentUpdatedProblemsLoading = false;
        },
        (err) => {
          this.loading.recentUpdatedProblemsLoading = false;
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
    goContest(cid) {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t("m.Please_login_first"));
        this.$store.dispatch("changeModalStatus", { visible: true });
      } else {
        this.$router.push({
          name: "ContestDetails",
          params: { contestID: cid },
        });
      }
    },
    goContestList(type) {
      this.$router.push({
        name: "ContestList",
        query: {
          type,
        },
      });
    },
    goProblem(event) {
      this.$router.push({
        name: "ProblemDetails",
        params: {
          problemID: event.row.problemId,
        },
      });
    },
    goUserHome(username, uid) {
      this.$router.push({
        path: "/user-home",
        query: { uid, username },
      });
    },
    getDuration(startTime, endTime) {
      return time.formatSpecificDuration(startTime, endTime);
    },
    getRankTagClass(rowIndex) {
      return "rank-tag no" + (rowIndex + 1);
    },
  },
  computed: {
    ...mapState(["websiteConfig"]),
    ...mapGetters(["isAuthenticated"]),
  },
};
</script>
<style>
.contest-card-running {
  border-color: rgb(25, 190, 107);
}
.contest-card-schedule {
  border-color: #f90;
}
</style>
<style scoped>
/deep/.el-card__header {
  padding: 0.6rem 1.25rem !important;
}
.card-top {
  margin-top: 20px;
}
.home-contest {
  text-align: left;
  font-size: 21px;
  font-weight: 500;
  line-height: 30px;
}
.oj-logo {
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  margin-bottom: 1rem;
  padding: 0.5rem 1rem;
  background: rgb(255, 255, 255);
  min-height: 47px;
}
.oj-normal {
  border-color: #409eff;
}
.oj-error {
  border-color: #e65c47;
}

.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
}

.contest-card {
  margin-bottom: 20px;
}
.contest-title {
  font-size: 1.15rem;
  font-weight: 600;
}
.contest-type-auth {
  text-align: center;
  margin-top: -10px;
  margin-bottom: 5px;
}
ul,
li {
  padding: 0;
  margin: 0;
  list-style: none;
}
.contest-info {
  text-align: center;
}
.contest-info li {
  display: inline-block;
  padding-right: 10px;
}

/deep/.contest-card-running .el-card__header {
  border-color: rgb(25, 190, 107);
  background-color: rgba(94, 185, 94, 0.15);
}
.contest-card-running .contest-title {
  color: #5eb95e;
}

/deep/.contest-card-schedule .el-card__header {
  border-color: #f90;
  background-color: rgba(243, 123, 29, 0.15);
}

.contest-card-schedule .contest-title {
  color: #f37b1d;
}

.content-center {
  text-align: center;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
.welcome-title {
  font-weight: 600;
  font-size: 25px;
  font-family: "Raleway";
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
  .contest-header {
    text-align: center;
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

@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
</style>
