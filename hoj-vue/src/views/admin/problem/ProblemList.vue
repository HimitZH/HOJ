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
          </span>
          <span v-if="contestId">
            <el-button
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="addProblemDialogVisible = true"
              >{{ $t('m.Add_From_Public_Problem') }}
            </el-button>
          </span>
          <span>
            <el-button
              type="success"
              size="small"
              @click="AddRemoteOJProblemDialogVisible = true"
              icon="el-icon-plus"
              >{{ $t('m.Add_Rmote_OJ_Problem') }}
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

          <span>
            <el-select
              v-model="oj"
              @change="ProblemListChangeFilter"
              size="small"
              style="width: 180px;"
            >
              <el-option
                :label="$t('m.All_Problem')"
                :value="'All'"
              ></el-option>
              <el-option :label="$t('m.My_OJ')" :value="'Mine'"></el-option>
              <el-option
                :label="remoteOj.name"
                :key="index"
                :value="remoteOj.key"
                v-for="(remoteOj, index) in REMOTE_OJ"
              ></el-option>
            </el-select>
          </span>

          <span v-if="!contestId">
            <el-select
              v-model="problemListAuth"
              @change="ProblemListChangeFilter"
              size="small"
              style="width: 180px;"
            >
              <el-option :label="$t('m.All_Problem')" :value="0"></el-option>
              <el-option :label="$t('m.Public_Problem')" :value="1"></el-option>
              <el-option
                :label="$t('m.Private_Problem')"
                :value="2"
              ></el-option>
              <el-option
                :label="$t('m.Contest_Problem')"
                :value="3"
              ></el-option>
            </el-select>
          </span>
        </div>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="problemList"
        ref="adminProblemList"
        :loading="loading"
        align="center"
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
          field="title"
          min-width="150"
          :title="$t('m.Title')"
          show-overflow
          v-if="!contestId"
        >
        </vxe-table-column>

        <vxe-table-column
          min-width="150"
          :title="$t('m.Original_Display')"
          v-if="isContest"
          align="left"
        >
          <template v-slot="{ row }">
            <p v-if="contestId">
              {{ $t('m.Display_ID') }}：{{ row.problemId }}
            </p>
            <p v-if="contestId">{{ $t('m.Title') }}：{{ row.title }}</p>
            <span v-else>{{ row.problemId }}</span>
          </template>
        </vxe-table-column>

        <vxe-table-column
          min-width="150"
          :title="$t('m.Contest_Display')"
          v-if="isContest"
          align="left"
        >
          <template v-slot="{ row }">
            <p v-if="contestProblemMap[row.id]">
              {{ $t('m.Display_ID') }}：{{
                contestProblemMap[row.id]['displayId']
              }}
            </p>
            <p v-if="contestProblemMap[row.id]">
              {{ $t('m.Title') }}：{{
                contestProblemMap[row.id]['displayTitle']
              }}
            </p>
            <span v-if="contestProblemMap[row.id]">
              {{ $t('m.Balloon_Color') }}：<el-color-picker
                v-model="contestProblemMap[row.id].color"
                show-alpha
                :predefine="predefineColors"
                size="small"
                style="vertical-align: middle;"
                @change="
                  changeContestProblemColor(
                    contestProblemMap[row.id].id,
                    contestProblemMap[row.id].color
                  )
                "
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
        <vxe-table-column min-width="120" :title="$t('m.Created_Time')">
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
        <vxe-table-column min-width="100" :title="$t('m.Auth')">
          <template v-slot="{ row }">
            <el-select
              v-model="row.auth"
              @change="changeProblemAuth(row)"
              size="small"
              :disabled="!isSuperAdmin && !isProblemAdmin && !contestId"
            >
              <el-option
                :label="$t('m.Public_Problem')"
                :value="1"
                :disabled="!isSuperAdmin && !isProblemAdmin"
              ></el-option>
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
              v-if="isSuperAdmin || isProblemAdmin"
            >
              <el-button
                icon="el-icon-download"
                size="mini"
                @click.native="downloadTestCase(row.id)"
                type="success"
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
              v-if="isSuperAdmin || isProblemAdmin"
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
        <el-form-item :label="$t('m.Problem_ID')" required>
          <el-input v-model="otherOJProblemId" size="small"></el-input>
        </el-form-item>

        <el-form-item
          v-if="contestId"
          :label="$t('m.Enter_The_Problem_Display_ID_in_the_Contest')"
          required
        >
          <el-input v-model="displayId" size="small"></el-input>
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
import { mapGetters } from 'vuex';
export default {
  name: 'ProblemList',
  components: {
    ContestAddProblem,
  },
  data() {
    return {
      problemListAuth: 0,
      oj: 'All',
      pageSize: 10,
      total: 0,
      problemList: [],
      contestProblemMap: {},
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
      displayId: '',

      predefineColors: [
        '#ff4500',
        '#ff8c00',
        '#ffd700',
        '#90ee90',
        '#00ced1',
        '#1e90ff',
        '#c71585',
      ],
    };
  },
  mounted() {
    this.init();
  },
  computed: {
    ...mapGetters(['userInfo', 'isSuperAdmin', 'isProblemAdmin']),
    isContest() {
      return !(this.routeName == 'admin-problem-list' && !this.contestId);
    },
  },
  methods: {
    init() {
      this.routeName = this.$route.name;
      this.contestId = this.$route.params.contestId;
      this.getProblemList(this.currentPage);
      this.REMOTE_OJ = Object.assign({}, REMOTE_OJ);
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
      let params = {
        limit: this.pageSize,
        currentPage: page,
        keyword: this.keyword,
        cid: this.contestId,
        oj: this.oj,
      };
      if (this.problemListAuth != 0) {
        params['auth'] = this.problemListAuth;
      }
      if (this.routeName === 'admin-problem-list') {
        api.admin_getProblemList(params).then(
          (res) => {
            this.loading = false;
            this.total = res.data.data.total;
            this.problemList = res.data.data.records;
          },
          (err) => {
            this.loading = false;
          }
        );
      } else {
        api.admin_getContestProblemList(params).then(
          (res) => {
            this.loading = false;
            this.total = res.data.data.problemList.total;
            this.problemList = res.data.data.problemList.records;
            this.contestProblemMap = res.data.data.contestProblemMap;
          },
          (err) => {
            this.loading = false;
          }
        );
      }
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
          api[funcName](id, null)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.getProblemList(this.currentPage);
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    removeProblem(pid) {
      this.$confirm(this.$i18n.t('m.Remove_Problem_Tips'), 'Tips', {
        type: 'warning',
      }).then(
        () => {
          api
            .admin_deleteContestProblem(pid, this.contestId)
            .then((res) => {
              myMessage.success('success');
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
    ProblemListChangeFilter() {
      this.currentChange(1);
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    addRemoteOJProblem() {
      if (!this.otherOJProblemId) {
        myMessage.error(this.$i18n.t('m.Problem_ID_is_required'));
        return;
      }

      if (!this.displayId && this.contestId) {
        myMessage.error(
          this.$i18n.t('m.The_Problem_Display_ID_in_the_Contest_is_required')
        );
        return;
      }

      this.addRemoteOJproblemLoading = true;
      let funcName = '';
      if (this.contestId) {
        funcName = 'admin_addContestRemoteOJProblem';
      } else {
        funcName = 'admin_addRemoteOJProblem';
      }
      api[funcName](
        this.otherOJName,
        this.otherOJProblemId,
        this.contestId,
        this.displayId
      ).then(
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
    changeContestProblemColor(id, color) {
      let data = {
        id: id,
        color: color,
      };
      api.admin_setContestProblemInfo(data).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Balloon_Color_Successfully'));
      });
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (
        newVal.params.contestId != oldVal.params.contestId ||
        newVal.name != oldVal.name
      ) {
        this.init();
      }
    },
  },
};
</script>

<style scoped>
.filter-row span button {
  margin-top: 5px;
  margin-bottom: 5px;
}
.filter-row span div {
  margin-top: 8px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
  .filter-row span div {
    width: 80%;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
</style>
