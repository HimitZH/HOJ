<template>
  <div>
    <vxe-table
      stripe
      auto-resize
      :data="problemList"
      :loading="loading"
      align="center"
      v-if="!editPage"
    >
      <vxe-table-column min-width="64" field="id" title="ID">
      </vxe-table-column>
      <vxe-table-column
        min-width="100"
        field="problemId"
        :title="$t('m.Display_ID')"
        v-if="!contestId"
      >
      </vxe-table-column>
      <vxe-table-column
        min-width="150"
        :title="$t('m.Original_Display')"
        v-else
        align="left"
      >
        <template v-slot="{ row }">
          <p v-if="contestId">{{ $t('m.Display_ID') }}：{{ row.problemId }}</p>
          <p v-if="contestId">{{ $t('m.Title') }}：{{ row.title }}</p>
          <span v-else>{{ row.problemId }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="title"
        min-width="150"
        :title="$t('m.Title')"
        show-overflow
        v-if="!contestId"
      >
      </vxe-table-column>
      <vxe-table-column
        min-width="150"
        :title="$t('m.Contest_Display')"
        v-else
        align="left"
      >
        <template v-slot="{ row }">
          <p v-if="contestProblemMap[row.id]">
            {{ $t('m.Display_ID') }}：{{
              contestProblemMap[row.id]['displayId']
            }}
          </p>
          <p v-if="contestProblemMap[row.id]">
            {{ $t('m.Title') }}：{{ contestProblemMap[row.id]['displayTitle'] }}
          </p>
          <span v-if="contestProblemMap[row.id]">
            {{ $t('m.Balloon_Color') }}：<el-color-picker
              v-model="contestProblemMap[row.id].color"
              show-alpha
              :predefine="predefineColors"
              size="small"
              style="vertical-align: middle;"
              @change="changeProblemColor(contestProblemMap[row.id])"
            >
            </el-color-picker>
          </span>
          <span v-else>{{ row.title }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="author"
        min-width="130"
        :title="$t('m.Author')"
        show-overflow
      >
      </vxe-table-column>
      <vxe-table-column
        min-width="130"
        :title="$t('m.Created_Time')"
        v-if="!contestId"
      >
        <template v-slot="{ row }">
          {{ row.gmtCreate | localtime }}
        </template>
      </vxe-table-column>
      <vxe-table-column
        min-width="96"
        field="modifiedUser"
        :title="$t('m.Modified_User')"
        show-overflow
      >
      </vxe-table-column>
      <vxe-table-column min-width="120" :title="$t('m.Auth')">
        <template v-slot="{ row }">
          <el-select
            v-model="row.auth"
            @change="changeProblemAuth(row.id, row.auth)"
            size="small"
            :disabled="
              row.gid != gid ||
                (!isGroupRoot && userInfo.username != row.author)
            "
          >
            <el-option :label="$t('m.Public_Problem')" :value="1"></el-option>
            <el-option :label="$t('m.Private_Problem')" :value="2"></el-option>
            <el-option
              :label="$t('m.Contest_Problem')"
              :value="3"
              :disabled="!contestId"
            ></el-option>
          </el-select>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('m.Option')" min-width="200">
        <template v-slot="{ row }">
          <el-tooltip
            effect="dark"
            :content="$t('m.Edit')"
            placement="top"
            v-if="
              row.gid == gid && (isGroupRoot || userInfo.username == row.author)
            "
          >
            <el-button
              icon="el-icon-edit-outline"
              size="mini"
              @click.native="goEditProblem(row.id)"
              type="primary"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.Download_Testcase')"
            placement="top"
            v-if="
              row.gid == gid && (isGroupRoot || userInfo.username == row.author)
            "
          >
            <el-button
              icon="el-icon-download"
              size="mini"
              @click.native="downloadTestCase(row.id)"
              type="success"
            >
            </el-button>
          </el-tooltip>
          <p></p>
          <el-tooltip
            effect="dark"
            :content="getApplyContent(row.applyPublicProgress)"
            placement="top"
            v-if="
              !contestId && row.gid == gid && (isGroupRoot || userInfo.username == row.author)
            "
          >
            <el-button
              :icon="getApplyIcon(row.applyPublicProgress)"
              size="mini"
              @click.native="applyPublic(row.id,row.applyPublicProgress)"
              type="warning"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.Remove')"
            placement="top"
            v-if="contestId"
          >
            <el-button
              icon="el-icon-close"
              size="mini"
              @click.native="removeProblem(row.id)"
              type="warning"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.Delete')"
            placement="top"
            v-if="
              row.gid == gid && (isGroupRoot || userInfo.username == row.author)
            "
          >
            <el-button
              icon="el-icon-delete-solid"
              size="mini"
              @click.native="deleteProblem(row.id)"
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
    <Problem
      v-if="editPage"
      mode="edit"
      :title="$t('m.Edit_Problem')"
      apiMethod="updateGroupProblem"
      :pid="pid"
      :contestId="contestId"
      @handleEditPage="handleEditPage"
      @currentChange="currentChange"
    ></Problem>
  </div>
</template>

<script>
import utils from '@/common/utils';
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import api from '@/common/api';
import mMessage from '@/common/message';
import Problem from '@/components/oj/group/Problem';
export default {
  name: 'GroupProblemList',
  components: {
    Pagination,
    Problem,
  },
  props: {
    contestId: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      problemList: [],
      contestProblemMap: {},
      loading: false,
      pid: null,
      editPage: false,
      predefineColors: null,
      gid: null,
    };
  },
  mounted() {
    this.gid = this.$route.params.groupID;
    this.init();
  },
  methods: {
    init() {
      this.getAdminProblemList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    getAdminProblemList() {
      this.loading = true;
      if (!this.contestId) {
        api
          .getGroupAdminProblemList(
            this.currentPage,
            this.limit,
            this.$route.params.groupID
          )
          .then(
            (res) => {
              this.problemList = res.data.data.records;
              this.total = res.data.data.total;
              this.loading = false;
            },
            (err) => {
              this.loading = false;
            }
          );
      } else {
        let params = {
          cid: this.contestId,
        };
        api
          .getGroupContestProblemList(this.currentPage, this.limit, params)
          .then(
            (res) => {
              this.total = res.data.data.problemList.total;
              this.problemList = res.data.data.problemList.records;
              this.contestProblemMap = res.data.data.contestProblemMap;
              this.loading = false;
            },
            (err) => {
              this.loading = false;
            }
          );
      }
    },
    handleEditPage() {
      this.editPage = false;
      this.$emit('currentChange', 1);
      if (this.contestId) {
        this.$emit('handleEditProblemPage');
      } else {
        this.$emit('handleEditPage');
      }
    },
    goEditProblem(id) {
      this.pid = id;
      this.editPage = !this.editPage;
      if (this.contestId) {
        this.$emit('handleEditProblemPage');
      } else {
        this.$emit('handleEditPage');
      }
    },
    changeProblemColor(contestProblem) {
      api.updateGroupContestProblem(contestProblem).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },
    changeProblemAuth(pid, auth) {
      api.changeGroupProblemAuth(pid, auth).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
        this.$emit('currentChange', 1);
      });
    },
    getApplyContent(progress){
      if(progress == null){
        return this.$i18n.t('m.Group_Problem_Apply_Public');
      }else if(progress == 1){
        return this.$i18n.t('m.Applying_for_Publicity');
      }else if(progress == 2){
        return this.$i18n.t('m.Already_Public_Problem');
      }else if(progress == 3){
        return this.$i18n.t('m.Refused');
      }
    },
    getApplyIcon(progress){
      if(progress == null){
        return 'el-icon-upload2';
      }else if(progress == 1){
        return 'el-icon-loading';
      }else if(progress == 2){
        return 'el-icon-circle-check';
      }else if(progress == 3){
        return 'el-icon-circle-close';
      }
    },
    applyPublic(pid, progress){
      if(progress == null){
        this.$confirm(
        this.$i18n.t('m.Group_Problem_Apply_Public_Tips'),
        this.$i18n.t('m.Tips'),
        {
          type: 'info',
        }
        ).then(
          () => {
            api
              .applyGroupProblemPublic(pid, true)
              .then((res) => {
                mMessage.success('success');
                this.$emit('currentChange', 1);
                this.currentChange(1);
              })
              .catch(() => {});
          },
          () => {}
        );
      }else{
        this.$confirm(
        this.$i18n.t('m.Cancel_Group_Problem_Apply_Public_Tips'),
        this.$i18n.t('m.Warning'),
        {
          type: 'warning',
        }
        ).then(
          () => {
            api
              .applyGroupProblemPublic(pid, false)
              .then((res) => {
                mMessage.success('success');
                this.$emit('currentChange', 1);
                this.currentChange(1);
              })
              .catch(() => {});
          },
          () => {}
        );
      }

    },
    removeProblem(pid) {
      this.$confirm(
        this.$i18n.t('m.Remove_Contest_Problem_Tips'),
        this.$i18n.t('m.Warning'),
        {
          type: 'warning',
        }
      ).then(
        () => {
          api
            .deleteGroupContestProblem(pid, this.contestId)
            .then((res) => {
              mMessage.success('success');
              this.$emit('currentChangeProblem');
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    downloadTestCase(problemID) {
      let url = '/api/file/download-testcase?pid=' + problemID;
      utils.downloadFile(url).then(() => {
        this.$alert(
          this.$i18n.t('m.Download_Testcase_Success'),
          this.$i18n.t('m.Tips')
        );
      });
    },
    deleteProblem(id) {
      this.$confirm(
        this.$i18n.t('m.Delete_Problem_Tips'),
        this.$i18n.t('m.Warning'),
        {
          type: 'warning',
        }
      ).then(
        () => {
          api
            .deleteGroupProblem(id, this.$route.params.groupID)
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
    ...mapGetters(['userInfo', 'isGroupRoot']),
    isContest() {
      return !!this.contestId;
    },
  },
};
</script>
