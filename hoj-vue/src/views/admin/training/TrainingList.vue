<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Training_List') }}</span>
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
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateTraining"
              icon="el-icon-plus"
              >{{ $t('m.Create') }}
            </el-button>
          </span>
        </div>
      </div>
      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="trainingList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="id" width="80" title="ID"> </vxe-table-column>
        <vxe-table-column field="rank" width="80" :title="$t('m.Order_Number')">
        </vxe-table-column>
        <vxe-table-column
          field="title"
          min-width="150"
          :title="$t('m.Title')"
          show-overflow
        >
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Auth')" width="100">
          <template v-slot="{ row }">
            <el-tag :type="TRAINING_TYPE[row.auth]['color']" effect="dark">
              {{ row.auth }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Visible')" min-width="80">
          <template v-slot="{ row }">
            <el-switch
              v-model="row.status"
              :disabled="!isSuperAdmin && userInfo.username != row.author"
              @change="changeTrainingStatus(row.id, row.status, row.author)"
            >
            </el-switch>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="210" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>Created Time: {{ row.gmtCreate | localtime }}</p>
            <p>Update Time: {{ row.gmtModified | localtime }}</p>
            <p>Creator: {{ row.author }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <template v-if="isSuperAdmin || userInfo.username == row.author">
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
                  :content="$t('m.View_Training_Problem_List')"
                  placement="top"
                >
                  <el-button
                    icon="el-icon-tickets"
                    size="mini"
                    @click.native="goTrainingProblemList(row.id)"
                    type="success"
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
                @click.native="deleteTraining(row.id)"
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
  </div>
</template>

<script>
import api from '@/common/api';
import { TRAINING_TYPE } from '@/common/constants';
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
export default {
  name: 'TrainingList',
  data() {
    return {
      pageSize: 10,
      total: 0,
      trainingList: [],
      keyword: '',
      loading: false,
      excludeAdmin: true,
      currentPage: 1,
      currentId: 1,
      downloadDialogVisible: false,
      TRAINING_TYPE: {},
    };
  },
  mounted() {
    this.getTrainingList(this.currentPage);
    this.TRAINING_TYPE = Object.assign({}, TRAINING_TYPE);
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == 'true' ? true : false;
      if (refresh) {
        this.getTrainingList(1);
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
      this.getTrainingList(page);
    },
    getTrainingList(page) {
      this.loading = true;
      api.admin_getTrainingList(page, this.pageSize, this.keyword).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.trainingList = res.data.data.records;
        },
        (res) => {
          this.loading = false;
        }
      );
    },
    goEdit(trainingId) {
      this.$router.push({
        name: 'admin-edit-training',
        params: { trainingId },
      });
    },
    goTrainingProblemList(trainingId) {
      this.$router.push({
        name: 'admin-training-problem-list',
        params: { trainingId },
      });
    },
    deleteTraining(trainingId) {
      this.$confirm(this.$i18n.t('m.Delete_Training_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.admin_deleteTraining(trainingId).then((res) => {
          myMessage.success(this.$i18n.t('m.Delete_successfully'));
          this.currentChange(1);
        });
      });
    },
    changeTrainingStatus(trainingId, status, author) {
      api.admin_changeTrainingStatus(trainingId, status, author).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    goCreateTraining() {
      this.$router.push({ name: 'admin-create-training' });
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
