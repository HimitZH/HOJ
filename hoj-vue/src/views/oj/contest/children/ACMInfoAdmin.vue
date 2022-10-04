<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Admin_Helper') }}</span>
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
            @click="getACInfo(1)"
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
      :data="acInfoList"
    >
      <vxe-table-column
        field="submitTime"
        min-width="150"
        :title="$t('m.AC_Time')"
      >
        <template v-slot="{ row }">
          <span>{{ row.submitTime | localtime }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="displayId"
        :title="$t('m.Problem_ID')"
        min-width="100"
      >
        <template v-slot="{ row }">
          <span
            style="vertical-align: top;"
            v-if="disPlayIdMapColor[row.displayId]"
          >
            <svg
              t="1633685184463"
              class="icon"
              viewBox="0 0 1088 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="5840"
              width="25"
              height="25"
            >
              <path
                d="M575.872 849.408c-104.576 0-117.632-26.56-119.232-31.808-6.528-22.528 32.896-70.592 63.744-96.768l-1.728-2.624c137.6-42.688 243.648-290.112 243.648-433.472A284.544 284.544 0 0 0 478.016 0a284.544 284.544 0 0 0-284.288 284.736c0 150.4 116.352 415.104 263.744 438.336-25.152 29.568-50.368 70.784-39.104 108.928 12.608 43.136 62.72 63.232 157.632 63.232 7.872 0 11.52 9.408 4.352 19.52-21.248 29.248-77.888 63.424-167.68 63.424V1024c138.944 0 215.936-74.816 215.936-126.528a46.72 46.72 0 0 0-16.32-36.608 56.32 56.32 0 0 0-36.416-11.456zM297.152 297.472c0 44.032-38.144 25.344-38.144-38.656 0-108.032 85.248-195.712 190.592-195.712 62.592 0 81.216 39.232 38.08 39.232-105.152 0.064-190.528 87.04-190.528 195.136z"
                :fill="disPlayIdMapColor[row.displayId]"
                p-id="5841"
              ></path>
            </svg>
          </span>
          <span>{{ row.displayId }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        field="first_blood"
        :title="'AC' + $t('m.Status')"
        min-width="80"
      >
        <template v-slot="{ row }">
          <el-tag effect="dark" color="#ed3f14" v-if="row.firstBlood">{{
            $t('m.First_Blood')
          }}</el-tag>
          <el-tag effect="dark" color="#19be6b" v-else>{{
            $t('m.Accepted')
          }}</el-tag>
        </template>
      </vxe-table-column>
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
      <vxe-table-column field="checked" :title="$t('m.Status')" min-width="150">
        <template v-slot="{ row }">
          <el-tag effect="dark" color="#19be6b" v-if="row.checked">{{
            $t('m.Checked')
          }}</el-tag>
          <el-tag effect="dark" color="#f90" v-else>{{
            $t('m.Not_Checked')
          }}</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column field="option" :title="$t('m.Option')" min-width="150">
        <template v-slot="{ row }">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-circle-check"
            @click="updateCheckedStatus(row)"
            round
            >{{ $t('m.Check_It') }}</el-button
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
import api from '@/common/api';
import myMessage from '@/common/message';
import { mapState } from 'vuex';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'ACM-Info-Admin',
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 20,
      total: 0,
      btnLoading: false,
      autoRefresh: false,
      acInfoList: [],
    };
  },
  computed: {
    ...mapState({
      disPlayIdMapColor: (state) => state.contest.disPlayIdMapColor,
    }),
  },
  beforeCreate() {
    if (this.$store.state.contest.contestProblems.length === 0) {
      this.$store.dispatch('getContestProblems');
    }
  },
  mounted() {
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
          myMessage.success(this.$i18n.t('m.Update_Successfully'));
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
