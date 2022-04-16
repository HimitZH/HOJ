<template>
  <el-card>
    <div class="filter-row">
      <el-row>
        <el-col :md="3" :xs="24">
          <span class="title">{{ $t('m.Group_Contest') }}</span>
        </el-col>
        <el-col
          :md="18"
          :xs="24"
          v-if="
            (isSuperAdmin || isGroupAdmin) &&
              !problemPage &&
              !editProblemPage &&
              !announcementPage
          "
        >
          <el-button
            v-if="!editPage"
            :type="createPage ? 'warning' : 'primary'"
            size="small"
            @click="handleCreatePage"
            :icon="createPage ? 'el-icon-back' : 'el-icon-plus'"
            >{{ createPage ? $t('m.Back_To_Admin_Contest_List') : $t('m.Create') }}</el-button
          >
          <el-button
            v-if="editPage && adminPage"
            type="warning"
            size="small"
            @click="handleEditPage"
            icon="el-icon-back"
            >{{ $t('m.Back_To_Admin_Contest_List') }}</el-button
          >
          <el-button
            v-if="!editPage&&!createPage"
            :type="adminPage ? 'danger' : 'success'"
            size="small"
            @click="handleAdminPage"
            :icon="adminPage ? 'el-icon-back' : 'el-icon-s-opportunity'"
            >{{ adminPage ? $t('m.Back_To_Contest_List') : $t('m.Contest_Admin') }}</el-button
          >
        </el-col>
        <el-col
         :md="18"
         :xs="24"
          v-else-if="
            (isSuperAdmin || isGroupAdmin) &&
              problemPage &&
              !editProblemPage &&
              !createProblemPage &&
              !announcementPage
          "
        >
          <el-button
            type="primary"
            size="small"
            @click="handleCreateProblemPage"
            icon="el-icon-plus"
            >{{ $t('m.Create') }}</el-button
          >
          <el-button
            type="primary"
            size="small"
            @click="publicPage = true"
            icon="el-icon-plus"
            >{{ $t('m.Add_From_Public_Problem') }}</el-button
          >
          <el-button
            type="success"
            size="small"
            @click="handleGroupPage"
            icon="el-icon-plus"
            >{{ $t('m.Add_From_Group_Problem') }}</el-button
          >
          <el-button
            type="warning"
            size="small"
            @click="handleProblemPage(null)"
            icon="el-icon-back"
            >{{ $t('m.Back_To_Admin_Contest_List') }}</el-button
          >
        </el-col>
        <el-col
         :md="18"
         :xs="24"
          v-else-if="
            (isSuperAdmin || isGroupAdmin) &&
              (editProblemPage || createProblemPage)
          "
        >
          <el-button
            v-if="editProblemPage"
            type="primary"
            size="small"
            @click="handleEditProblemPage"
            icon="el-icon-back"
            >{{ $t('m.Back_Admin_Contest_Problem_List') }}</el-button
          >`
          <el-button
            v-if="createProblemPage"
            type="primary"
            size="small"
            @click="handleCreateProblemPage"
            icon="el-icon-back"
            >{{ $t('m.Back_Admin_Contest_Problem_List') }}</el-button
          >`
        </el-col>
        <el-col
          :md="18"
          :xs="24"
          v-else-if="(isSuperAdmin || isGroupAdmin) && announcementPage"
        >
          <el-button
            type="primary"
            size="small"
            @click="handleCreateAnnouncementPage"
            icon="el-icon-plus"
            >{{ $t('m.Create') }}</el-button
          >
          <el-button
            type="warning"
            size="small"
            @click="handleAnnouncementPage"
            icon="el-icon-back"
            >{{ $t('m.Back_To_Admin_Contest_List') }}</el-button
          >
        </el-col>
      </el-row>
    </div>
    <div v-if="!adminPage && !createPage && !problemPage">
      <p id="no-contest" v-show="contestList.length == 0">
        <el-empty :description="$t('m.No_contest')"></el-empty>
      </p>
      <ol id="contest-list">
        <li
          v-for="contest in contestList"
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
              <p class="contest-title">
                <a class="entry" @click.stop="goGroupContest(contest.id)">
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
                  <template v-if="contest.type == 0">
                    <el-button
                      size="mini"
                      round
                      :type="'primary'"
                      @click="onRuleChange(contest.type)"
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
                        size="mini"
                        round
                        :type="'warning'"
                        @click="onRuleChange(contest.type)"
                        ><i class="fa fa-trophy"></i>
                        {{ contest.type | parseContestType }}
                      </el-button>
                    </el-tooltip>
                  </template>
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
                <li v-if="contest.auth != CONTEST_TYPE.PUBLIC">
                  <i
                    class="el-icon-user-solid"
                    style="color:rgb(48, 145, 242);"
                  ></i
                  >x{{ contest.count != null ? contest.count : 0 }}
                </li>
                <li v-if="contest.openRank">
                  <el-tooltip
                    :content="$t('m.Contest_Outside_ScoreBoard')"
                    placement="top"
                    effect="dark"
                  >
                    <el-button
                      circle
                      size="small"
                      type="primary"
                      :disabled="contest.status == CONTEST_STATUS.SCHEDULED"
                      icon="el-icon-data-analysis"
                      @click="
                        goContestOutsideScoreBoard(contest.id, contest.type)
                      "
                    ></el-button>
                  </el-tooltip>
                </li>
              </ul>
            </el-col>
            <el-col :xs="4" :sm="4" :md="2" :lg="2" style="text-align: center">
              <el-tag
                effect="dark"
                :color="CONTEST_STATUS_REVERSE[contest.status]['color']"
                size="medium"
              >
                <i class="fa fa-circle" aria-hidden="true"></i>
                {{ $t('m.' + CONTEST_STATUS_REVERSE[contest.status]['name']) }}
              </el-tag>
            </el-col>
          </el-row>
        </li>
      </ol>
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="currentChange"
        :current.sync="currentPage"
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </div>
    <ContestList
      v-if="adminPage && !createPage && !problemPage && !announcementPage"
      @handleEditPage="handleEditPage"
      @currentChange="currentChange"
      @handleProblemPage="handleProblemPage"
      @handleAnnouncementPage="handleAnnouncementPage"
      ref="contestList"
    ></ContestList>
    <ProblemList
      v-if="problemPage && !createProblemPage"
      :contestId="contestId"
      @currentChangeProblem="currentChangeProblem"
      @handleEditProblemPage="handleEditProblemPage"
      ref="contestProblemList"
    >
    </ProblemList>
    <AnnouncementList
      v-if="announcementPage"
      :contestId="contestId"
      ref="contestAnnouncementList"
    >
    </AnnouncementList>
    <Contest
      v-if="createPage && !editPage && !problemPage"
      mode="add"
      :title="$t('m.Create_Contest')"
      apiMethod="addGroupContest"
      @handleCreatePage="handleCreatePage"
      @currentChange="currentChange"
    ></Contest>
    <Problem
      v-if="createProblemPage"
      mode="add"
      :contestId="contestId"
      :title="$t('m.Create_Problem')"
      apiMethod="addGroupContestProblem"
      @handleCreateProblemPage="handleCreateProblemPage"
      @currentChange="currentChange"
    ></Problem>
    <el-dialog
      :title="$t('m.Add_Contest_Problem')"
      width="90%"
      :visible.sync="publicPage"
      :close-on-click-modal="false"
    >
      <AddPublicProblem
        v-if="publicPage"
        :contestId="contestId"
        apiMethod="getGroupContestProblemList"
        @currentChangeProblem="currentChangeProblem"
        ref="addPublicProblem"
      ></AddPublicProblem>
    </el-dialog>
    <el-dialog
      :title="$t('m.Add_Contest_Problem')"
      width="350px"
      :visible.sync="groupPage"
      :close-on-click-modal="false"
    >
      <AddGroupProblem
        :contestId="contestId"
        @currentChangeProblem="currentChangeProblem"
        @handleGroupPage="handleGroupPage"
      ></AddGroupProblem>
    </el-dialog>
  </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import ContestList from '@/components/oj/group/ContestList';
import Contest from '@/components/oj/group/Contest';
import Problem from '@/components/oj/group/Problem';
import ProblemList from '@/components/oj/group/ProblemList';
import AddPublicProblem from '@/components/oj/group/AddPublicProblem.vue';
import AddGroupProblem from '@/components/oj/group/AddGroupProblem.vue';
import AnnouncementList from '@/components/oj/group/AnnouncementList';
import api from '@/common/api';
import time from '@/common/time';
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_TYPE,
  CONTEST_TYPE_REVERSE,
  CONTEST_STATUS,
} from '@/common/constants';
export default {
  name: 'GroupContestList',
  components: {
    Pagination,
    ContestList,
    Contest,
    Problem,
    ProblemList,
    AddPublicProblem,
    AddGroupProblem,
    AnnouncementList,
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      contestList: [],
      loading: false,
      adminPage: false,
      createPage: false,
      editPage: false,
      problemPage: false,
      publicPage: false,
      groupPage: false,
      editProblemPage: false,
      createProblemPage: false,
      announcementPage: false,
      contestId: null,
      acmSrc: require('@/assets/acm.jpg'),
      oiSrc: require('@/assets/oi.jpg'),
    };
  },
  mounted() {
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.CONTEST_TYPE = Object.assign({}, CONTEST_TYPE);
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.CONTEST_STATUS = Object.assign({}, CONTEST_STATUS);
    this.init();
  },
  methods: {
    init() {
      this.getGroupContestList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    currentChangeProblem() {
      this.$refs.contestProblemList.currentChange(1);
    },
    getGroupContestList() {
      this.loading = true;
      api
        .getGroupContestList(
          this.currentPage,
          this.limit,
          this.$route.params.groupID
        )
        .then(
          (res) => {
            this.contestList = res.data.data.records;
            this.total = res.data.data.total;
            this.loading = false;
          },
          (err) => {
            this.loading = false;
          }
        );
    },
    goGroupContest(contestId) {
      this.$router.push({
        name: 'ContestDetails',
        params: {
          contestID: contestId,
        },
      });
    },
    goContestOutsideScoreBoard(cid, type) {
      if (type == 0) {
        this.$router.push({
          name: 'ACMScoreBoard',
          params: { contestID: cid },
        });
      } else if (type == 1) {
        this.$router.push({
          name: 'OIScoreBoard',
          params: { contestID: cid },
        });
      }
    },
    getDuration(startTime, endTime) {
      return time.formatSpecificDuration(startTime, endTime);
    },
    getborderColor(contest) {
      return (
        'border-left: 4px solid ' +
        CONTEST_STATUS_REVERSE[contest.status]['color']
      );
    },
    handleCreatePage() {
      this.createPage = !this.createPage;
    },
    handleEditPage() {
      this.editPage = !this.editPage;
      this.$refs.contestList.editPage = this.editPage;
    },
    handleAdminPage() {
      this.adminPage = !this.adminPage;
      this.createPage = false;
      this.editPage = false;
    },
    handleProblemPage(contestId) {
      this.contestId = contestId;
      this.problemPage = !this.problemPage;
    },
    handleAnnouncementPage(contestId) {
      this.contestId = contestId;
      this.announcementPage = !this.announcementPage;
    },
    handleGroupPage() {
      this.groupPage = !this.groupPage;
    },
    handleEditProblemPage() {
      this.editProblemPage = !this.editProblemPage;
      this.$refs.contestProblemList.editPage = this.editProblemPage;
    },
    handleCreateProblemPage() {
      this.createProblemPage = !this.createProblemPage;
      this.$refs.contestProblemList.currentChange(1);
    },
    handleCreateAnnouncementPage() {
      this.$refs.contestAnnouncementList.openAnnouncementDialog(null);
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'isSuperAdmin', 'isGroupAdmin']),
  },
};
</script>

<style scoped>
.title {
  font-size: 20px;
  vertical-align: middle;
  float: left;
}
.filter-row {
  margin-bottom: 5px;
  text-align: center;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-left: 5px;
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-left: 10px;
    margin-right: 10px;
  }
}
#no-contest {
  text-align: center;
  font-size: 16px;
  padding: 20px;
}
#contest-list > li {
  padding: 5px;
  margin-left: -20px;
  margin-top: 10px;
  width: 100%;
  border-bottom: 1px solid rgba(187, 187, 187, 0.5);
  list-style: none;
  text-align: center;
}
#contest-list .trophy {
  height: 70px;
  margin-left: 10px;
  margin-right: -20px;
}
#contest-list .contest-main .contest-title {
  font-size: 1.25rem;
  padding-left: 8px;
  margin-bottom: 0;
}
#contest-list .contest-main .contest-title a.entry {
  color: #495060;
}
#contest-list .contest-main .contest-title a:hover {
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
