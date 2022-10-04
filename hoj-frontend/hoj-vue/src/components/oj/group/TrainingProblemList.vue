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
      >
      </vxe-table-column>
      <vxe-table-column
        field="title"
        min-width="150"
        :title="$t('m.Title')"
        show-overflow
      >
      </vxe-table-column>

      <vxe-table-column
        field="author"
        min-width="100"
        :title="$t('m.Author')"
        show-overflow
      >
      </vxe-table-column>
      <vxe-table-column
        min-width="200"
        :title="$t('m.Training_Problem_Rank')"
      >
        <template v-slot="{ row }">
          <el-input-number
            v-model="trainingProblemMap[row.id].rank"
            @change="handleChangeRank(trainingProblemMap[row.id])"
            :min="0"
            :max="2147483647"
          ></el-input-number>
        </template>
      </vxe-table-column>
      <vxe-table-column min-width="120" :title="$t('m.Auth')">
        <template v-slot="{ row }">
          <el-select
            v-model="row.auth"
            @change="changeProblemAuth(row.id, row.auth)"
            :disabled="row.gid != gid"
            size="small"
          >
            <el-option
              :label="$t('m.Public_Problem')"
              :value="1"
            ></el-option>
            <el-option
              :label="$t('m.Private_Problem')"
              :value="2"
            ></el-option>
            <el-option
              :label="$t('m.Contest_Problem')"
              :value="3"
              :disabled="true"
            ></el-option>
          </el-select>
        </template>
      </vxe-table-column>
      <vxe-table-column title="Option" min-width="250">
        <template v-slot="{ row }">
          <el-tooltip
            effect="dark"
            :content="$t('m.Edit')"
            placement="top"
            v-if="row.gid == gid"
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
            v-if="row.gid == gid"
          >
            <el-button
              icon="el-icon-download"
              size="mini"
              @click.native="downloadTestCase(row.id)"
              type="success"
            >
            </el-button>
          </el-tooltip>

          <el-tooltip effect="dark" :content="$t('m.Remove')" placement="top">
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
            v-if="row.gid == gid"
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
      @handleEditPage="handleEditPage"
      @currentChange="currentChange"
    ></Problem>
  </div>
</template>

<script>
import api from '@/common/api';
import mMessage from '@/common/message';
import Pagination from '@/components/oj/common/Pagination';
import Problem from '@/components/oj/group/Problem'
import { mapGetters } from 'vuex';
import utils from '@/common/utils';
export default {
  name: 'GroupTrainingProblemList',
  components: {
    Pagination,
    Problem
  },
  props: {
    trainingId: {
      type: Number,
      default: null
    },
  },
  data() {
    return {
      oj: 'All',
      limit: 10,
      currentPage: 1,
      total: 0,
      problemList: [],
      trainingProblemMap: {},
      loading: false,
      currentProblemID: '',
      currentRow: {},
      editPage: false,
      pid: null,
      gid: null,
    };
  },
  mounted() {
    this.gid = this.$route.params.groupID;
    this.init();
  },
  computed: {
    ...mapGetters(['userInfo', 'isSuperAdmin']),
  },
  methods: {
    init() {
      this.getProblemList();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    getProblemList() {
      this.loading = true;
      let params = {
        tid: this.trainingId,
      }
      api.getGroupTrainingProblemList(this.currentPage, this.limit, params).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.problemList.total;
          this.problemList = res.data.data.problemList.records;
          this.trainingProblemMap = res.data.data.trainingProblemMap;
        },
        (err) => {
          this.loading = false;
        }
      );
    },
    handleChangeRank(data) {
      api.updateGroupTrainingProblem(data).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
        this.getProblemList(1);
      });
    },
    changeProblemAuth(pid, auth) {
      api.changeGroupProblemAuth(pid, auth).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },
    handleEditPage() {
      this.editPage = false;
      this.$emit("handleEditProblemPage");
    },
    goEditProblem(id) {
      this.pid = id;
      this.editPage = true;
      this.$emit("handleEditProblemPage");
    },
    deleteProblem(id) {
      this.$confirm(this.$i18n.t('m.Delete_Problem_Tips'), this.$i18n.t('m.Warning'), {
        type: 'warning',
      }).then(
        () => {
          api
            .deleteGroupProblem(id)
            .then((res) => {
              mMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.$emit("currentChangeProblem");
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    removeProblem(pid) {
      this.$confirm(this.$i18n.t('m.Remove_Training_Problem_Tips'), this.$i18n.t('m.Warning'), {
        type: 'warning',
      }).then(
        () => {
          api
            .deleteGroupTrainingProblem(pid, this.trainingId)
            .then((res) => {
              mMessage.success('success');
              this.$emit("currentChangeProblem");
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    downloadTestCase(problemID) {
      let url = '/api/file/download-testcase?pid=' + problemID;
      utils.downloadFile(url).then(() => {
        this.$alert(this.$i18n.t('m.Download_Testcase_Success'), this.$i18n.t('m.Tips'));
      });
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (
        newVal.params.trainingId != oldVal.params.trainingId ||
        newVal.name != oldVal.name
      ) {
        this.init();
      }
    },
  },
};
</script>
