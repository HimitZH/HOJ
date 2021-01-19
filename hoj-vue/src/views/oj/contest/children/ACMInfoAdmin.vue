<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">AC Info</span>
      <div class="filter-row">
        <span>
          Auto Refresh(10s)
          <el-switch
            @change="handleAutoRefresh"
            v-model="autoRefresh"
          ></el-switch>
        </span>
        <span>
          <el-button
            type="primary"
            @click="getACInfo(1)"
            size="small"
            icon="el-icon-refresh"
            :loading="btnLoading"
            >Refresh</el-button
          >
        </span>
      </div>
    </div>
    <vxe-table
      border="inner"
      stripe
      auto-resize
      align="center"
      :data="acInfoList"
    >
      <vxe-table-column field="submitTime" min-width="150" title="AC Time">
        <template v-slot="{ row }">
          <span>{{ row.submitTime | localtime }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="displayId"
        title="Problem ID"
        min-width="100"
      ></vxe-table-column>
      <vxe-table-column field="first_blood" title="AC Status" min-width="80">
        <template v-slot="{ row }">
          <el-tag effect="dark" color="#ed3f14" v-if="row.firstBlood"
            >First Blood</el-tag
          >
          <el-tag effect="dark" color="#19be6b" v-else>Accepet</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column field="username" title="Username" min-width="150">
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
        title="RealName"
        min-width="150"
      ></vxe-table-column>
      <vxe-table-column field="checked" title="Status" min-width="150">
        <template v-slot="{ row }">
          <el-tag effect="dark" color="#19be6b" v-if="row.checked"
            >Checked</el-tag
          >
          <el-tag effect="dark" color="#f90" v-else>Not Checked</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column field="option" title="Option" min-width="150">
        <template v-slot="{ row }">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-circle-check"
            @click="updateCheckedStatus(row)"
            round
            >Check It</el-button
          >
        </template>
      </vxe-table-column>
    </vxe-table>
    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getACInfo"
    ></Pagination>
  </el-card>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import moment from 'moment';
import Pagination from '@/components/oj/common/Pagination.vue';
import api from '@/common/api';
import myMessage from '@/common/message';

export default {
  name: 'ACM-Info-Admin',
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 30,
      total: 0,
      btnLoading: false,
      autoRefresh: false,
      acInfoList: [],
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getACInfo(1);
  },
  methods: {
    getUserTotalSubmit(username) {
      this.$router.push({
        name: 'ContestSubmissionList',
        query: { username: username },
      });
    },
    getACInfo(page = 1) {
      this.btnLoading = true;
      let params = {
        cid: this.$route.params.contestID,
        limit: this.limit,
        currentPage: page,
      };
      api
        .getACMACInfo(params)
        .then((res) => {
          this.btnLoading = false;
          this.acInfoList = res.data.data.records;
          this.total = res.data.data.total;
        })
        .catch(() => {
          this.btnLoading = false;
        });
    },
    updateCheckedStatus(row) {
      let data = {
        id: row.id,
        checked: !row.checked,
        cid: row.cid,
      };
      api
        .updateACInfoCheckedStatus(data)
        .then((res) => {
          myMessage.success(res.data.msg);
          this.getACInfo();
        })
        .catch(() => {});
    },
    handleAutoRefresh() {
      if (this.autoRefresh) {
        this.refreshFunc = setInterval(() => {
          this.page = 1;
          this.getACInfo();
        }, 10000);
      } else {
        clearInterval(this.refreshFunc);
      }
    },
  },
  computed: {
    ...mapState({
      contest: (state) => state.contest.contest,
    }),
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
