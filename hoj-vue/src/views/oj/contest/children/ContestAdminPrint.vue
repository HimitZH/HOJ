<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Admin_Print') }}</span>
      <div class="filter-row">
        <span>
          {{ $t('m.Auto_Refresh') }}(10s)
          <el-switch
            @change="handleAutoRefresh"
            v-model="autoRefresh"
          ></el-switch>
        </span>
        <span>
          <el-button
            type="primary"
            @click="getContestPrint(1)"
            size="small"
            icon="el-icon-refresh"
            :loading="btnLoading"
            >{{ $t('m.Refresh') }}</el-button
          >
        </span>
      </div>
    </div>

    <vxe-table
      border="inner"
      stripe
      auto-resize
      align="center"
      :data="printList"
    >
      <vxe-table-column
        field="username"
        :title="$t('m.Username')"
        min-width="150"
      >
        <template v-slot="{ row }">
          <span
            ><a
              @click="getUserTotalSubmit(row.username)"
              style="color:rgb(87, 163, 243);"
              >{{ row.username }}</a
            >
          </span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="realname"
        :title="$t('m.RealName')"
        min-width="150"
      ></vxe-table-column>
      <vxe-table-column
        field="gmtCreate"
        min-width="150"
        :title="$t('m.Submit_Time')"
      >
        <template v-slot="{ row }">
          <span>{{ row.gmtCreate | localtime }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column field="status" :title="$t('m.Status')" min-width="150">
        <template v-slot="{ row }">
          <el-tag effect="dark" color="#19be6b" v-if="row.status == 1">{{
            $t('m.Printed')
          }}</el-tag>
          <el-tag effect="dark" color="#f90" v-if="row.status == 0">{{
            $t('m.Not_Printed')
          }}</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column field="option" :title="$t('m.Option')" min-width="150">
        <template v-slot="{ row }">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-download"
            @click="downloadSubmissions(row.id)"
            round
            >{{ $t('m.Download') }}</el-button
          >
          <el-button
            type="success"
            size="small"
            icon="el-icon-circle-check"
            @click="updateStatus(row.id)"
            round
            >{{ $t('m.OK') }}</el-button
          >
        </template>
      </vxe-table-column>
    </vxe-table>
    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getContestPrint"
    ></Pagination>
  </el-card>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';
import utils from '@/common/utils';
const Pagination = () => import('@/components/oj/common/Pagination');

export default {
  name: 'Contest-Print-Admin',
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 15,
      total: 0,
      btnLoading: false,
      autoRefresh: false,
      contestID: null,
      printList: [],
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getContestPrint(1);
  },
  methods: {
    updateStatus(id) {
      let params = {
        id: id,
        cid: this.contestID,
      };
      api.updateContestPrintStatus(params).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
        this.getContestPrint(1);
      });
    },
    getContestPrint(page = 1) {
      let params = {
        cid: this.contestID,
        currentPage: page,
        limit: this.limit,
      };
      this.btnLoading = true;
      api
        .getContestPrintList(params)
        .then((res) => {
          this.btnLoading = false;
          this.printList = res.data.data.records;
          this.total = res.data.data.total;
        })
        .catch(() => {
          this.btnLoading = false;
        });
    },
    downloadSubmissions(id) {
      let url = `/api/file/download-contest-print-text?id=${id}`;
      utils.downloadFile(url);
    },
    handleAutoRefresh() {
      if (this.autoRefresh) {
        this.refreshFunc = setInterval(() => {
          this.page = 1;
          this.getContestPrint(1);
        }, 10000);
      } else {
        clearInterval(this.refreshFunc);
      }
    },
  },
  beforeDestroy() {
    clearInterval(this.refreshFunc);
  },
};
</script>

<style scoped>
.filter-row {
  float: right;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 2px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
/deep/ .el-tag--dark {
  border-color: #fff;
}
</style>
