<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card shadow>
        <div slot="header">
          <span class="panel-title"
            >{{
              query.rule_type === "" ? "All" : query.rule_type
            }}
            Contests</span
          >
          <div class="filter-row">
            <span>
              <el-dropdown
                @command="onRuleChange"
                placement="bottom"
                trigger="hover"
                class="drop-menu"
              >
                <span class="el-dropdown-link">
                  {{ query.rule_type === "" ? "Rule" : query.rule_type }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="">All</el-dropdown-item>
                  <el-dropdown-item command="OI">OI</el-dropdown-item>
                  <el-dropdown-item command="ACM">ACM</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>

            <span>
              <el-dropdown
                @on-click="onStatusChange"
                placement="bottom"
                trigger="hover"
                class="drop-menu"
              >
                <span class="el-dropdown-link">
                  {{
                    query.status === ""
                      ? "Status"
                      : CONTEST_STATUS_REVERSE[query.status].name
                  }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="">All</el-dropdown-item>
                  <el-dropdown-item command="0">Running</el-dropdown-item>
                  <el-dropdown-item command="1">Scheduled</el-dropdown-item>
                  <el-dropdown-item command="-1">Ended</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>

            <span>
              <vxe-input
                v-model="query.keyword"
                placeholder="Enter keyword"
                type="search"
                size="medium"
                @search-click="filterByKeyword"
              ></vxe-input>
            </span>
          </div>
        </div>
        <p id="no-contest" v-show="contests.length == 0">暂无比赛</p>
        <ol id="contest-list">
          <li v-for="contest in contests" :key="contest.title" :style="getborderColor(contest)">
            <el-row type="flex" justify="space-between" align="middle">
              <el-col :xs="10" :md="2" :lg="2">
                <img v-show="contest.rule_type == 'ACM'" class="trophy" :src="acmSrc" width="95px"/>
                <img v-show="contest.rule_type == 'OI'" class="trophy" :src="oiSrc" width="95px" />
              </el-col>
              <el-col :xs="10" :md="20" :lg="20" class="contest-main">
                <p class="title">
                  <a class="entry" @click.stop="toContest(contest)">
                    {{ contest.title }}
                  </a>
                  <template v-if="contest.auth != 0">
                    <i class="el-icon-lock" size="20"></i>
                  </template>
                </p>
                <ul class="detail">
                  <li>
                    <i
                      class="fa fa-calendar"
                      aria-hidden="true"
                      style="color: #3091f2"
                    ></i>
                    {{ contest.start_time | localtime }}
                  </li>
                  <li>
                    <i
                      class="fa fa-clock-o"
                      aria-hidden="true"
                      style="color: #3091f2"
                    ></i>
                    {{ getDuration(contest.start_time, contest.end_time) }}
                  </li>
                  <li>
                    <el-button
                      size="mini"
                      round
                      @click="onRuleChange(contest.rule_type)"
                    >
                      {{ contest.rule_type }}
                    </el-button>
                  </li>
                  <li>
                    <el-tooltip :content="CONTEST_TYPE_REVERSE[contest.auth].tips" placement="top" effect="light">
                      <el-tag
                          :type="CONTEST_TYPE_REVERSE[contest.auth].color"
                          effect="plain">
                          {{CONTEST_TYPE_REVERSE[contest.auth].name}}
                        </el-tag>
                    </el-tooltip>
                  </li>
                </ul>
              </el-col>
              <el-col :xs="4" :md="2" :lg="2" style="text-align: center">
                <el-tag
                  effect="dark"
                  :color="CONTEST_STATUS_REVERSE[contest.status].color"
                  size="medium"
                >
                  <i
                    class="fa fa-circle"
                    aria-hidden="true"
                  ></i>
                  {{ CONTEST_STATUS_REVERSE[contest.status].name }}
                </el-tag>
              </el-col>
            </el-row>
          </li>
        </ol>
      </el-card>
      <Pagination
        :total="total"
        :pageSize="limit"
        @on-change="getContestList"
        :current.sync="page"
      ></Pagination>
    </el-col>
  </el-row>
</template>

<script>
import api from "@/common/api";
import { mapGetters } from "vuex";
import utils from "@/common/utils";
import Pagination from "@/components/common/Pagination";
import time from "@/common/time";
import { CONTEST_STATUS_REVERSE, CONTEST_TYPE,CONTEST_TYPE_REVERSE } from "@/common/constants";

const limit = 10;

export default {
  name: "contest-list",
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      query: {
        status: "",
        keyword: "",
        rule_type: "",
      },
      limit: limit,
      total: 0,
      rows: "",
      contests: [
        {
          title: "测试比赛",
          status: 0,
          start_time: "2020-11-08T05:00:00Z",
          end_time: "2020-11-08T08:00:00Z",
          rule_type: "ACM",
          auth: 1,
        },
        {
          title: "测试比赛",
          status: 0,
          start_time: "2020-11-08T05:00:00Z",
          end_time: "2020-11-08T08:00:00Z",
          rule_type: "ACM",
          auth: 2,
        },
        {
          title: "测试比赛",
          status: -1,
          start_time: "2020-11-08T05:00:00Z",
          end_time: "2020-11-08T08:00:00Z",
          rule_type: "ACM",
          auth: 0,
        },
        {
          title: "测试比赛",
          status: 1,
          start_time: "2020-11-08T05:00:00Z",
          end_time: "2020-11-08T08:00:00Z",
          rule_type: "ACM",
          auth: 0,
        },
        {
          title: "测试比赛",
          status: 0,
          start_time: "2020-11-08T05:00:00Z",
          end_time: "2020-11-08T08:00:00Z",
          rule_type: "OI",
          auth: 0,
        },
      ],
      CONTEST_STATUS_REVERSE: CONTEST_STATUS_REVERSE,
      //      for password modal use
      cur_contest_id: "",
      CONTEST_TYPE_REVERSE:CONTEST_TYPE_REVERSE,
      acmSrc: require("../../assets/acm.jpg"),
      oiSrc: require("../../assets/oi.jpg"),
    };
  },
  // beforeRouteEnter(to, from, next) {
  //   // api.getContestList(0, limit).then(
  //   //   (res) => {
  //   //     next((vm) => {
  //   //       vm.contests = res.data.data.results;
  //   //       vm.total = res.data.data.total;
  //   //     });
  //   //   },
  //   //   (res) => {
  //   //     next();
  //   //   }
  //   // );
  // },
  methods: {
    init() {
      let route = this.$route.query;
      this.query.status = route.status || "";
      this.query.rule_type = route.rule_type || "";
      this.query.keyword = route.keyword || "";
      this.page = parseInt(route.page) || 1;
      this.getContestList();
    },
    getContestList(page = 1) {
      let offset = (page - 1) * this.limit;
      api.getContestList(offset, this.limit, this.query).then((res) => {
        this.contests = res.data.data.results;
        this.total = res.data.data.total;
      });
    },
    filterByKeyword() {
      let query = Object.assign({}, this.query);
      query.page = this.page;
      this.$router.push({
        name: "contest-list",
        query: utils.filterEmptyValue(query),
      });
    },
    onRuleChange(rule) {
      this.query.rule_type = rule;
      this.page = 1;
      this.changeRoute();
    },
    onStatusChange(status) {
      this.query.status = status;
      this.page = 1;
      this.changeRoute();
    },
    toContest(contest) {
      this.cur_contest_id = contest.id;
      if (
        contest.contest_type !== CONTEST_TYPE.PUBLIC &&
        !this.isAuthenticated
      ) {
        this.$error("请先登录");
        this.$store.dispatch("changeModalStatus", { visible: true });
      } else {
        this.$router.push({
          name: "contest-details",
          params: { contestID: contest.id },
        });
      }
    },
    getDuration(startTime, endTime) {
      return time.duration(startTime, endTime);
    },
    getborderColor(contest){
      return "border-left: 4px solid "+CONTEST_STATUS_REVERSE[contest.status].color;
    }
  },
  computed: {
    ...mapGetters(["isAuthenticated", "user"]),
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
  },
};
</script>
<style scoped>
#no-contest {
  text-align: center;
  font-size: 16px;
  padding: 20px;
}
.filter-row {
  float: right;
}
.el-tag--dark{
  border-color: #FFF;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 2px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
#contest-list > li {
  padding: 5px;
  margin-left: -20px;
  margin-top: 10px;
  width: 100%;
  border-bottom: 1px solid rgba(187, 187, 187, 0.5);
  list-style: none;
}
#contest-list .trophy {
  height: 70px;
  margin-left: 10px;
  margin-right: -20px;
}
#contest-list .contest-main {
  text-align: left;
}
#contest-list .contest-main .title {
  font-size: 18px;
  padding-left: 8px;
  margin-bottom: 0;
}
#contest-list .contest-main .title a.entry {
  color: #495060;
}
#contest-list .contest-main .title a:hover {
  color: #2d8cf0;
  border-bottom: 1px solid #2d8cf0;
}
#contest-list .contest-main .detail {
  padding-left: 0;
  padding-bottom: 10px;
}
#contest-list .contest-main li {
  display: inline-block;
  padding: 10px 0 0 10px;
}
</style>
