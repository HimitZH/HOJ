<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{
          contestId ? $t('m.Contest_Problem_List') : $t('m.Problem_List')
        }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateProblem"
              icon="el-icon-plus"
              >{{ $t('m.Create') }}
            </el-button>
            <el-button
              type="success"
              size="small"
              v-if="!contestId"
              @click="AddRemoteOJProblemDialogVisible = true"
              icon="el-icon-plus"
              >{{ $t('m.Add_Rmote_OJ_Problem') }}
            </el-button>
            <el-button
              v-if="contestId"
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="addProblemDialogVisible = true"
              >{{ $t('m.Add_From_Public_Problem') }}
            </el-button>
          </span>
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
        stripe
        auto-resize
        :data="problemList"
        ref="xTable"
        :loading="loading"
        @row-dblclick="handleDblclick"
        align="center"
      >
        <vxe-table-column min-width="100" field="problemId" title="ID">
        </vxe-table-column>
        <vxe-table-column field="title" min-width="150" :title="$t('m.Title')">
        </vxe-table-column>
        <vxe-table-column
          field="author"
          min-width="150"
          :title="$t('m.Author')"
        >
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="gmtCreate"
          :title="$t('m.Created_Time')"
        >
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="130" field="auth" :title="$t('m.Auth')">
          <template v-slot="{ row }">
            <el-select
              v-model="row.auth"
              @change="changeProblemAuth(row)"
              size="small"
              :disabled="row.auth == 3 && !contestId"
            >
              <el-option :label="$t('m.Public_Problem')" :value="1"></el-option>
              <el-option
                :label="$t('m.Private_Problem')"
                :value="2"
              ></el-option>
              <el-option
                :label="$t('m.Contest_Problem')"
                :value="3"
                :disabled="!contestId"
              ></el-option>
            </el-select>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Option" min-width="200">
          <template v-slot="{ row }">
            <el-tooltip effect="dark" :content="$t('m.Edit')" placement="top">
              <el-button
                icon="el-icon-edit-outline"
                size="mini"
                @click.native="goEdit(row.id)"
                type="primary"
              >
              </el-button>
            </el-tooltip>

            <el-tooltip
              effect="dark"
              :content="$t('m.Download_Testcase')"
              placement="top"
            >
              <el-button
                icon="el-icon-download"
                size="mini"
                @click.native="downloadTestCase(row.id)"
                type="success"
              >
              </el-button>
            </el-tooltip>

            <el-tooltip effect="dark" :content="$t('m.Delete')" placement="top">
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

      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next, sizes"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
          @size-change="onPageSizeChange"
          :page-sizes="[10, 30, 50, 100]"
        >
        </el-pagination>
      </div>
    </el-card>

    <el-dialog
      :title="$t('m.Add_Contest_Problem')"
      v-if="contestId"
      width="90%"
      :visible.sync="addProblemDialogVisible"
      @close-on-click-modal="false"
    >
      <ContestAddProblem
        :contestID="contestId"
        @on-change="getProblemList"
      ></ContestAddProblem>
    </el-dialog>

    <el-dialog
      :title="$t('m.Add_Rmote_OJ_Problem')"
      width="350px"
      :visible.sync="AddRemoteOJProblemDialogVisible"
      @close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.Remote_OJ')">
          <el-select v-model="otherOJName" size="small">
            <el-option
              :label="remoteOj.name"
              :value="remoteOj.key"
              v-for="(remoteOj, index) in REMOTE_OJ"
              :key="index"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('m.Problem_ID')">
          <el-input v-model="otherOJProblemId" size="small"></el-input>
        </el-form-item>
        <el-form-item style="text-align:center">
          <el-button
            type="primary"
            icon="el-icon-plus"
            @click="addRemoteOJProblem"
            :loading="addRemoteOJproblemLoading"
            >{{ $t('m.Add') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import ContestAddProblem from '@/components/admin/ContestAddProblem.vue';
import myMessage from '@/common/message';
import { REMOTE_OJ } from '@/common/constants';
export default {
  name: 'ProblemList',
  components: {
    ContestAddProblem,
  },
  data() {
    return {
      pageSize: 10,
      total: 0,
      problemList: [],
      keyword: '',
      loading: false,
      currentPage: 1,
      routeName: '',
      contestId: '',
      // for make public use
      currentProblemID: '',
      currentRow: {},
      addProblemDialogVisible: false,
      AddRemoteOJProblemDialogVisible: false,
      addRemoteOJproblemLoading: false,
      otherOJName: 'HDU',
      otherOJProblemId: '',
      REMOTE_OJ: {},
    };
  },
  mounted() {
    this.routeName = this.$route.name;
    this.contestId = this.$route.params.contestId;
    this.getProblemList(this.currentPage);
    this.REMOTE_OJ = Object.assign({}, REMOTE_OJ);
  },
  methods: {
    handleDblclick(row) {
      row.isEditing = true;
    },
    goEdit(problemId) {
      if (this.routeName === 'admin-problem-list') {
        this.$router.push({
          name: 'admin-edit-problem',
          params: { problemId },
        });
      } else if (this.routeName === 'admin-contest-problem-list') {
        this.$router.push({
          name: 'admin-edit-contest-problem',
          params: { problemId: problemId, contestId: this.contestId },
        });
      }
    },
    goCreateProblem() {
      if (this.routeName === 'admin-problem-list') {
        this.$router.push({ name: 'admin-create-problem' });
      } else if (this.routeName === 'admin-contest-problem-list') {
        this.$router.push({
          name: 'admin-create-contest-problem',
          params: { contestId: this.contestId },
        });
      }
    },
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getProblemList(page);
    },
    onPageSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.getProblemList(this.currentPage);
    },
    getProblemList(page = 1) {
      this.loading = true;
      let funcName =
        this.routeName === 'admin-problem-list'
          ? 'admin_getProblemList'
          : 'admin_getContestProblemList';
      let params = {
        limit: this.pageSize,
        currentPage: page,
        keyword: this.keyword,
        cid: this.contestId,
      };
      api[funcName](params).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          for (let problem of res.data.data.records) {
            problem.isEditing = false;
          }
          this.problemList = res.data.data.records;
        },
        (err) => {
          this.loading = false;
        }
      );
    },

    changeProblemAuth(row) {
      api.admin_changeProblemPublic(row).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },

    deleteProblem(id) {
      this.$confirm(this.$i18n.t('m.Delete_Problem_Tips'), 'Tips', {
        type: 'warning',
      }).then(
        () => {
          let funcName =
            this.routeName === 'admin-problem-list'
              ? 'admin_deleteProblem'
              : 'admin_deleteContestProblem';
          api[funcName](id)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.getProblemList(this.currentPage);
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    updateProblem(row) {
      let data = Object.assign({}, row);
      let funcName = '';
      if (this.contestId) {
        data.contest_id = this.contestId;
        funcName = 'admin_editContestProblem';
      } else {
        funcName = 'admin_editProblem';
      }
      api[funcName](data)
        .then((res) => {
          myMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.getProblemList(this.currentPage);
        })
        .catch(() => {});
    },
    downloadTestCase(problemID) {
      let url = '/api/file/download-testcase?pid=' + problemID;
      utils.downloadFile(url).then(() => {
        this.$alert(this.$i18n.t('m.Download_Testcase_Success'), 'Tips');
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    addRemoteOJProblem() {
      this.addRemoteOJproblemLoading = true;
      api
        .admin_addRemoteOJProblem(this.otherOJName, this.otherOJProblemId)
        .then(
          (res) => {
            this.addRemoteOJproblemLoading = false;
            this.AddRemoteOJProblemDialogVisible = false;
            myMessage.success(this.$i18n.t('m.Add_Successfully'));
            this.currentChange(1);
          },
          (err) => {
            this.addRemoteOJproblemLoading = false;
          }
        );
    },
  },
  watch: {
    $route(newVal, oldVal) {
      this.contestId = newVal.params.contestId;
      this.routeName = newVal.name;
      this.getProblemList(this.currentPage);
    },
  },
};
</script>

<style scoped>
.filter-row {
  margin-top: 10px;
}
.filter-row span {
  margin-bottom: 5px;
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
</style>
