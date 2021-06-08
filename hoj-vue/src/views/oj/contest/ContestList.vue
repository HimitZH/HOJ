<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card shadow>
        <div slot="header">
          <span class="panel-title"
            >{{
              query.type === '' ? $t('m.All') : parseContestType(query.type)
            }}
            {{ $t('m.Contests') }}</span
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
                  {{
                    query.type == ''
                      ? $t('m.Rule')
                      : parseContestType(query.type)
                  }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="">{{
                    $t('m.All')
                  }}</el-dropdown-item>
                  <el-dropdown-item command="0">ACM</el-dropdown-item>
                  <el-dropdown-item command="1">OI</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>

            <span>
              <el-dropdown
                @command="onStatusChange"
                placement="bottom"
                trigger="hover"
                class="drop-menu"
              >
                <span class="el-dropdown-link">
                  {{
                    query.status === ''
                      ? $t('m.Status')
                      : CONTEST_STATUS_REVERSE[query.status]['name']
                  }}
                  <i class="el-icon-caret-bottom"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="">{{
                    $t('m.All')
                  }}</el-dropdown-item>
                  <el-dropdown-item command="-1">{{
                    $t('m.Scheduled')
                  }}</el-dropdown-item>
                  <el-dropdown-item command="0">{{
                    $t('m.Running')
                  }}</el-dropdown-item>
                  <el-dropdown-item command="1">{{
                    $t('m.Ended')
                  }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>

            <span>
              <vxe-input
                v-model="query.keyword"
                :placeholder="$t('m.Enter_keyword')"
                type="search"
                size="medium"
                @keyup.enter.native="filterByChange"
                @search-click="filterByChange"
              ></vxe-input>
            </span>
          </div>
        </div>
        <p id="no-contest" v-show="contests.length == 0">
          {{ $t('m.No_contest') }}
        </p>
        <ol id="contest-list">
          <li
            v-for="contest in contests"
            :key="contest.title"
            :style="getborderColor(contest)"
          >
            <el-row type="flex" justify="space-between" align="middle">
              <el-col :xs="10" :sm="4" :md="3" :lg="2">
                <img
                  v-show="contest.type == 0"
                  class="trophy"
                  :src="acmSrc"
                  width="95px"
                />
                <img
                  v-show="contest.type == 1"
                  class="trophy"
                  :src="oiSrc"
                  width="95px"
                />
              </el-col>
              <el-col :xs="10" :sm="16" :md="19" :lg="20" class="contest-main">
                <p class="title">
                  <a class="entry" @click.stop="toContest(contest)">
                    {{ contest.title }}
                  </a>
                  <template v-if="contest.auth == 1">
                    <i class="el-icon-lock" size="20" style="color:#d9534f"></i>
                  </template>
                  <template v-if="contest.auth == 2">
                    <i class="el-icon-lock" size="20" style="color:#f0ad4e"></i>
                  </template>
                </p>
                <ul class="detail">
                  <li>
                    <i
                      class="fa fa-calendar"
                      aria-hidden="true"
                      style="color: #3091f2"
                    ></i>
                    {{ contest.startTime | localtime }}
                  </li>
                  <li>
                    <i
                      class="fa fa-clock-o"
                      aria-hidden="true"
                      style="color: #3091f2"
                    ></i>
                    {{ getDuration(contest.startTime, contest.endTime) }}
                  </li>
                  <li>
                    <el-button
                      size="mini"
                      round
                      @click="onRuleChange(contest.type)"
                    >
                      {{ contest.type | parseContestType }}
                    </el-button>
                  </li>
                  <li>
                    <el-tooltip
                      :content="
                        $t('m.' + CONTEST_TYPE_REVERSE[contest.auth].tips)
                      "
                      placement="top"
                      effect="light"
                    >
                      <el-tag
                        :type="CONTEST_TYPE_REVERSE[contest.auth]['color']"
                        effect="plain"
                      >
                        {{
                          $t('m.' + CONTEST_TYPE_REVERSE[contest.auth]['name'])
                        }}
                      </el-tag>
                    </el-tooltip>
                  </li>
                </ul>
              </el-col>
              <el-col
                :xs="4"
                :sm="4"
                :md="2"
                :lg="2"
                style="text-align: center"
              >
                <el-tag
                  effect="dark"
                  :color="CONTEST_STATUS_REVERSE[contest.status]['color']"
                  size="medium"
                >
                  <i class="fa fa-circle" aria-hidden="true"></i>
                  {{
                    $t('m.' + CONTEST_STATUS_REVERSE[contest.status]['name'])
                  }}
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
        :current.sync="currentPage"
      ></Pagination>
    </el-col>
  </el-row>
</template>

<script>
import api from '@/common/api';
import { mapGetters } from 'vuex';
import utils from '@/common/utils';
import time from '@/common/time';
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_TYPE_REVERSE,
} from '@/common/constants';
import myMessage from '@/common/message';
const Pagination = () => import('@/components/oj/common/Pagination');
const limit = 10;

export default {
  name: 'contest-list',
  components: {
    Pagination,
  },
  data() {
    return {
      currentPage: 1,
      query: {
        status: '',
        keyword: '',
        type: '',
      },
      limit: limit,
      total: 0,
      rows: '',
      contests: [],
      CONTEST_STATUS_REVERSE: {},
      CONTEST_TYPE_REVERSE: CONTEST_TYPE_REVERSE,
      acmSrc: require('@/assets/acm.jpg'),
      oiSrc: require('@/assets/oi.jpg'),
    };
  },
  mounted() {
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.init();
  },
  methods: {
    init() {
      let route = this.$route.query;
      this.query.status = route.status || '';
      this.query.type = route.type || '';
      this.query.keyword = route.keyword || '';
      this.currentPage = parseInt(route.currentPage) || 1;
      this.getContestList();
    },
    getContestList(page = 1) {
      api.getContestList(page, this.limit, this.query).then((res) => {
        this.contests = res.data.data.records;
        this.total = res.data.data.total;
      });
    },
    filterByChange() {
      let query = Object.assign({}, this.query);
      query.currentPage = this.currentPage;
      this.$router.push({
        name: 'ContestList',
        query: utils.filterEmptyValue(query),
      });
    },

    parseContestType(type) {
      if (type == 0) {
        return 'ACM';
      } else if (type == 1) {
        return 'OI';
      }
    },
    onRuleChange(rule) {
      this.query.type = rule;
      this.currentPage = 1;
      this.filterByChange();
    },
    onStatusChange(status) {
      this.query.status = status;
      this.currentPage = 1;
      this.filterByChange();
    },
    toContest(contest) {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t('m.Please_login_first'));
        this.$store.dispatch('changeModalStatus', { visible: true });
      } else {
        this.$router.push({
          name: 'ContestDetails',
          params: { contestID: contest.id },
        });
      }
    },
    getDuration(startTime, endTime) {
      return time.duration(startTime, endTime);
    },
    getborderColor(contest) {
      return (
        'border-left: 4px solid ' +
        CONTEST_STATUS_REVERSE[contest.status]['color']
      );
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'userInfo']),
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
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 2px;
  }
  ol {
    padding-inline-start: 5px;
  }
  /deep/ .el-card__header {
    margin-bottom: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
/deep/ .el-card__header {
  border-bottom: 0px;
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
