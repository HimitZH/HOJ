<template>
  <div>
    <vxe-table
      stripe
      auto-resize
      :data="contestList"
      :loading="loading"
      align="center"
      v-if="!editPage"
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
            <el-tag :type="CONTEST_TYPE_REVERSE[row.auth].color" effect="plain">
              {{ $t('m.' + CONTEST_TYPE_REVERSE[row.auth].name) }}
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
            {{ $t('m.' + CONTEST_STATUS_REVERSE[row.status]['name']) }}
          </el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('m.Visible')" min-width="80">
        <template v-slot="{ row }">
          <el-switch
            v-model="row.visible"
            :disabled="!isGroupRoot && userInfo.uid != row.uid"
            @change="changeContestVisible(row.id, row.visible)"
          >
          </el-switch>
        </template>
      </vxe-table-column>
      <vxe-table-column min-width="210" :title="$t('m.Info')">
        <template v-slot="{ row }">
          <p>{{ $t('m.Start_Time') }}: {{ row.startTime | localtime }}</p>
          <p>{{ $t('m.End_Time') }}: {{ row.endTime | localtime }}</p>
          <p>{{ $t('m.Created_Time') }}: {{ row.gmtCreate | localtime }}</p>
          <p>{{ $t('m.Creator') }}: {{ row.author }}</p>
        </template>
      </vxe-table-column>
      <vxe-table-column min-width="150" :title="$t('m.Option')">
        <template v-slot="{ row }">
          <el-tooltip
            effect="dark"
            :content="$t('m.Edit')"
            placement="top"
            v-if="isGroupRoot || userInfo.uid == row.uid"
          >
            <el-button
              icon="el-icon-edit"
              size="mini"
              @click.native="goEditContest(row.id)"
              type="primary"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.View_Contest_Problem_List')"
            placement="top"
            v-if="isGroupRoot || userInfo.uid == row.uid"
          >
            <el-button
              icon="el-icon-tickets"
              size="mini"
              @click.native="goContestProblemList(row.id)"
              type="success"
            >
            </el-button>
          </el-tooltip>
          <p></p>
          <el-tooltip
            effect="dark"
            :content="$t('m.View_Contest_Announcement_List')"
            placement="top"
            v-if="isGroupRoot || userInfo.uid == row.uid"
          >
            <el-button
              icon="el-icon-info"
              size="mini"
              @click.native="goContestAnnouncementList(row.id)"
              type="info"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.Download_Contest_AC_Submission')"
            placement="top"
            v-if="isGroupRoot || userInfo.uid == row.uid"
          >
            <el-button
              icon="el-icon-download"
              size="mini"
              @click.native="openDownloadOptions(row.id)"
              type="warning"
            >
            </el-button>
          </el-tooltip>
          <p></p>
          <el-tooltip
            effect="dark"
            :content="$t('m.Delete')"
            placement="top"
            v-if="isGroupRoot || userInfo.uid == row.uid"
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
    <Pagination
      v-if="!editPage"
      :total="total"
      :page-size="limit"
      @on-change="currentChange"
      :current.sync="currentPage"
      @on-page-size-change="onPageSizeChange"
      :layout="'prev, pager, next, sizes'"
    ></Pagination>
    <Contest
      v-if="editPage"
      mode="edit"
      :title="$t('m.Edit_Contest')"
      apiMethod="updateGroupContest"
      :cid="cid"
      @handleEditPage="handleEditPage"
      @currentChange="currentChange"
    ></Contest>
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
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import api from '@/common/api';
import mMessage from '@/common/message';
import utils from '@/common/utils';
import Contest from '@/components/oj/group/Contest';
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
    Contest,
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      contestList: [],
      loading: false,
      routeName: '',
      cid: null,
      editPage: false,
      downloadDialogVisible: false,
      excludeAdmin: true,
      splitType: 'user',
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
      this.getAdminContestList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    handleEditPage() {
      this.editPage = false;
      this.$emit('currentChange', 1);
      this.$emit('handleEditPage');
    },
    getAdminContestList() {
      this.loading = true;
      api
        .getGroupAdminContestList(
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
    openDownloadOptions(contestId) {
      this.downloadDialogVisible = true;
      this.currentId = contestId;
    },
    downloadSubmissions() {
      let url = `/api/file/download-contest-ac-submission?cid=${this.currentId}&excludeAdmin=${this.excludeAdmin}&splitType=${this.splitType}`;
      utils.downloadFile(url);
      this.downloadDialogVisible = false;
    },
    goEditContest(contestId) {
      this.editPage = true;
      this.cid = contestId;
      this.$emit('handleEditPage');
    },
    goContestProblemList(contestId) {
      this.$emit('handleProblemPage', contestId);
    },
    goContestAnnouncementList(contestId) {
      this.$emit('handleAnnouncementPage', contestId);
    },
    changeContestVisible(cid, visible) {
      api.changeGroupContestVisible(cid, visible).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
        this.$emit('currentChange', 1);
        this.currentChange(1);
      });
    },
    deleteContest(id) {
      this.$confirm(
        this.$i18n.t('m.Delete_Contest_Tips'),
        this.$i18n.t('m.Warning'),
        {
          type: 'warning',
        }
      ).then(
        () => {
          api
            .deleteGroupContest(id, this.$route.params.groupID)
            .then((res) => {
              mMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.$emit('currentChange', 1);
              this.currentChange(1);
            })
            .catch(() => {});
        },
        () => {}
      );
    },
  },
  computed: {
    ...mapGetters(['isGroupRoot', 'userInfo']),
  },
};
</script>
