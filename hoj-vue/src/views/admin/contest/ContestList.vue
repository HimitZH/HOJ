<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Contest_List') }}</span>
        <div class="filter-row">
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
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
        align="center"
      >
        <vxe-table-column field="id" width="80" title="ID"> </vxe-table-column>
        <vxe-table-column
          field="title"
          min-width="150"
          :title="$t('m.Title')"
          show-overflow
        >
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Type')" width="100">
          <template v-slot="{ row }">
            <el-tag type="gray">{{ row.type | parseContestType }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Auth')" width="100">
          <template v-slot="{ row }">
            <el-tooltip
              :content="$t('m.' + CONTEST_TYPE_REVERSE[row.auth].tips)"
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
        <vxe-table-column :title="$t('m.Status')" width="100">
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
        <vxe-table-column :title="$t('m.Visible')" min-width="80">
          <template v-slot="{ row }">
            <el-switch
              v-model="row.visible"
              :disabled="!isSuperAdmin && userInfo.uid != row.uid"
              @change="changeContestVisible(row.id, row.visible, row.uid)"
            >
            </el-switch>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="210" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>Start Time: {{ row.startTime | localtime }}</p>
            <p>End Time: {{ row.endTime | localtime }}</p>
            <p>Created Time: {{ row.gmtCreate | localtime }}</p>
            <p>Creator: {{ row.author }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <template v-if="isSuperAdmin || userInfo.uid == row.uid">
              <div style="margin-bottom:10px">
                <el-tooltip
                  effect="dark"
                  :content="$t('m.Edit')"
                  placement="top"
                >
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
                  :content="$t('m.View_Contest_Problem_List')"
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
              </div>
              <div style="margin-bottom:10px">
                <el-tooltip
                  effect="dark"
                  :content="$t('m.View_Contest_Announcement_List')"
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
                  :content="$t('m.Download_Contest_AC_Submission')"
                  placement="top"
                >
                  <el-button
                    icon="el-icon-download"
                    size="mini"
                    @click.native="openDownloadOptions(row.id)"
                    type="warning"
                  >
                  </el-button>
                </el-tooltip>
              </div>
            </template>
            <el-tooltip
              effect="dark"
              :content="$t('m.Delete')"
              placement="top"
              v-if="isSuperAdmin"
            >
              <el-button
                icon="el-icon-delete"
                size="mini"
                @click.native="deleteContest(row.id)"
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
          :current-page.sync="currentPage"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>
    <el-dialog
      :title="$t('m.Download_Contest_AC_Submission')"
      width="320px"
      :visible.sync="downloadDialogVisible"
    >
      <el-switch
        v-model="excludeAdmin"
        :active-text="$t('m.Exclude_admin_submissions')"
      ></el-switch>
      <el-radio-group v-model="splitType" style="margin-top:10px">
        <el-radio label="user">{{ $t('m.SplitType_User') }}</el-radio>
        <el-radio label="problem">{{ $t('m.SplitType_Problem') }}</el-radio>
      </el-radio-group>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="downloadSubmissions">{{
          $t('m.OK')
        }}</el-button>
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
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
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
      splitType: 'user',
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
  computed: {
    ...mapGetters(['isSuperAdmin', 'userInfo']),
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
      let url = `/api/file/download-contest-ac-submission?cid=${this.currentId}&excludeAdmin=${this.excludeAdmin}&splitType=${this.splitType}`;
      utils.downloadFile(url);
      this.downloadDialogVisible = false;
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
    deleteContest(contestId) {
      this.$confirm(this.$i18n.t('m.Delete_Contest_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.admin_deleteContest(contestId).then((res) => {
          myMessage.success(this.$i18n.t('m.Delete_successfully'));
          this.currentChange(1);
        });
      });
    },
    changeContestVisible(contestId, visible, uid) {
      api.admin_changeContestVisible(contestId, visible, uid).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
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
  .filter-row span div {
    width: 80% !important;
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
