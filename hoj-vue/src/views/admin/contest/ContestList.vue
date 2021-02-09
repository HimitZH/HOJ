<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">Contest List</span>
        <div class="filter-row">
          <span>
            <vxe-input
              v-model="keyword"
              placeholder="Enter keyword"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
        </div>
      </div>
      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="contestList"
        auto-resize
        stripe
      >
        <vxe-table-column field="id" min-width="80" title="ID">
        </vxe-table-column>
        <vxe-table-column field="title" min-width="150" title="Title">
        </vxe-table-column>
        <vxe-table-column title="Type" min-width="130">
          <template v-slot="{ row }">
            <el-tag type="gray">{{ row.type | parseContestType }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Auth" min-width="150">
          <template v-slot="{ row }">
            <el-tooltip
              :content="CONTEST_TYPE_REVERSE[row.auth].tips"
              placement="top"
              effect="light"
            >
              <el-tag
                :type="CONTEST_TYPE_REVERSE[row.auth].color"
                effect="plain"
              >
                {{ CONTEST_TYPE_REVERSE[row.auth].name }}
              </el-tag>
            </el-tooltip>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Status" min-width="130">
          <template v-slot="{ row }">
            <el-tag
              effect="dark"
              :color="CONTEST_STATUS_REVERSE[row.status].color"
              size="medium"
            >
              {{ CONTEST_STATUS_REVERSE[row.status].name }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="210" title="More">
          <template v-slot="{ row }">
            <p>Start Time: {{ row.startTime | localtime }}</p>
            <p>End Time: {{ row.endTime | localtime }}</p>
            <p>Create Time: {{ row.gmtCreate | localtime }}</p>
            <p>Creator: {{ row.author }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="250" title="Option">
          <template v-slot="{ row }">
            <el-tooltip effect="dark" content="编辑比赛" placement="top">
              <el-button
                icon="el-icon-edit"
                size="mini"
                @click.native="goEdit(row.id)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              content="查看比赛题目列表"
              placement="top"
            >
              <el-button
                icon="el-icon-tickets"
                size="mini"
                @click.native="goContestProblemList(row.id)"
                type="success"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              content="查看比赛公告列表"
              placement="top"
            >
              <el-button
                icon="el-icon-info"
                size="mini"
                @click.native="goContestAnnouncement(row.id)"
                type="info"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              content="下载通过的提交代码"
              placement="top"
            >
              <el-button
                icon="el-icon-download"
                size="mini"
                @click.native="openDownloadOptions(row.id)"
                type="danger"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>
    <el-dialog
      title="Download Contest Submissions"
      width="320px"
      :visible.sync="downloadDialogVisible"
    >
      <el-switch
        v-model="excludeAdmin"
        active-text="Exclude admin submissions"
      ></el-switch>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="downloadSubmissions">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_TYPE_REVERSE,
} from '@/common/constants';

export default {
  name: 'ContestList',
  data() {
    return {
      pageSize: 10,
      total: 0,
      contestList: [],
      keyword: '',
      loading: false,
      excludeAdmin: true,
      currentPage: 1,
      currentId: 1,
      downloadDialogVisible: false,
      CONTEST_TYPE_REVERSE: {},
    };
  },
  mounted() {
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.getContestList(this.currentPage);
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == 'true' ? true : false;
      if (refresh) {
        this.getContestList(1);
      }
    },
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getContestList(page);
    },
    getContestList(page) {
      this.loading = true;
      api.admin_getContestList(page, this.pageSize, this.keyword).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.contestList = res.data.data.records;
        },
        (res) => {
          this.loading = false;
        }
      );
    },
    openDownloadOptions(contestId) {
      this.downloadDialogVisible = true;
      this.currentId = contestId;
    },
    downloadSubmissions() {
      let url = `/file/download-contest-ac-submission?cid=${this.currentId}&excludeAdmin=${this.excludeAdmin}`;
      utils.downloadFile(url);
    },
    goEdit(contestId) {
      this.$router.push({ name: 'admin-edit-contest', params: { contestId } });
    },
    goContestAnnouncement(contestId) {
      this.$router.push({
        name: 'admin-contest-announcement',
        params: { contestId },
      });
    },
    goContestProblemList(contestId) {
      this.$router.push({
        name: 'admin-contest-problem-list',
        params: { contestId },
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
  },
};
</script>
<style scoped>
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
.el-tag--dark {
  border-color: #fff;
}
</style>
