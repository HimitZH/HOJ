<template>
  <div>
    <vxe-table
      stripe
      auto-resize
      :data="trainingList"
      :loading="loading"
      align="center"
      v-if="!editPage"
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
            {{ $t('m.Training_' + row.auth) }}
          </el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('m.Visible')" min-width="80">
        <template v-slot="{ row }">
          <el-switch
            v-model="row.status"
            :disabled="!isGroupRoot && userInfo.username != row.author"
            @change="changeTrainingStatus(row.id, row.status)"
          >
          </el-switch>
        </template>
      </vxe-table-column>
      <vxe-table-column min-width="210" :title="$t('m.Info')">
        <template v-slot="{ row }">
          <p>{{ $t('m.Created_Time') }}: {{ row.gmtCreate | localtime }}</p>
          <p>{{ $t('m.Update_Time') }}: {{ row.gmtModified | localtime }}</p>
          <p>{{ $t('m.Creator') }}: {{ row.author }}</p>
        </template>
      </vxe-table-column>
      <vxe-table-column min-width="150" :title="$t('m.Option')">
        <template v-slot="{ row }">
          <el-tooltip
            effect="dark"
            :content="$t('m.Edit')"
            placement="top"
            v-if="isGroupRoot || userInfo.username == row.author"
          >
            <el-button
              icon="el-icon-edit"
              size="mini"
              @click.native="goEditTraining(row.id)"
              type="primary"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.View_Training_Problem_List')"
            placement="top"
            v-if="isGroupRoot || userInfo.username == row.author"
          >
            <el-button
              icon="el-icon-tickets"
              size="mini"
              @click.native="goTrainingProblemList(row.id)"
              type="success"
            >
            </el-button>
          </el-tooltip>
          <p></p>
          <el-tooltip
            effect="dark"
            :content="$t('m.Delete')"
            placement="top"
            v-if="isGroupRoot || userInfo.username == row.author"
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
    <Pagination
      v-if="!editPage"
      :total="total"
      :page-size="limit"
      @on-change="currentChange"
      :current.sync="currentPage"
      @on-page-size-change="onPageSizeChange"
      :layout="'prev, pager, next, sizes'"
    ></Pagination>
    <Training
      v-if="editPage"
      mode="edit"
      :title="$t('m.Edit_Training')"
      apiMethod="updateGroupTraining"
      :tid="tid"
      @handleEditPage="handleEditPage"
      @currentChange="currentChange"
    ></Training>
  </div>
</template>

<script>
import { TRAINING_TYPE } from '@/common/constants';
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import api from '@/common/api';
import mMessage from '@/common/message';
import Training from '@/components/oj/group/Training'
export default {
  name: 'GroupTrainingList',
  components: {
    Pagination,
    Training
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      trainingList: [],
      loading: false,
      routeName: '',
      tid: null,
      editPage: false,
    };
  },
  mounted() {
    this.TRAINING_TYPE = Object.assign({}, TRAINING_TYPE);
    this.init();
  },
  methods: {
    init() {
      this.getAdminTrainingList();
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
      this.$emit("currentChange", 1);
      this.$emit("handleEditPage");
    },
    getAdminTrainingList() {
      this.loading = true;
      api.getGroupAdminTrainingList(this.currentPage, this.limit, this.$route.params.groupID).then(
        (res) => {
          this.trainingList = res.data.data.records;
          this.total = res.data.data.total;
          this.loading = false;
        },
        (err) => {
          this.loading = false;
        }
      );
    },
    goEditTraining(trainingId) {
      this.$emit("handleEditPage");
      this.editPage = true;
      this.tid = trainingId;
    },
    goTrainingProblemList(trainingId) {
      this.$emit("handleProblemPage", trainingId);
    },
    changeTrainingStatus(tid, status) {
      api.changeGroupTrainingStatus(tid, status).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
        this.$emit("currentChange", 1);
        this.currentChange(1);
      });
    },
    deleteTraining(id) {
      this.$confirm(this.$i18n.t('m.Delete_Training_Tips'), this.$i18n.t('m.Warning'), {
        type: 'warning',
      }).then(() => {
          api.deleteGroupTraining(id, this.$route.params.groupID)
            .then((res) => {
              mMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.$emit("currentChange", 1);
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
  },
};
</script>