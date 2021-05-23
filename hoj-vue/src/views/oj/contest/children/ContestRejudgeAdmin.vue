<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">Admin Contest Rejudge</span>
    </div>
    <vxe-table
      border="inner"
      stripe
      auto-resize
      align="center"
      :data="contestProblems"
    >
      <vxe-table-column field="pid" min-width="50" title="Problem ID">
      </vxe-table-column>
      <vxe-table-column
        field="displayId"
        title="Display ID"
        min-width="100"
      ></vxe-table-column>
      <vxe-table-column field="displayTitle" title="Title" min-width="200">
      </vxe-table-column>
      <vxe-table-column field="ac" title="AC" min-width="80">
      </vxe-table-column>
      <vxe-table-column
        field="total"
        title="Total"
        min-width="80"
      ></vxe-table-column>
      <vxe-table-column field="option" title="Option" min-width="150">
        <template v-slot="{ row }">
          <el-button
            type="primary"
            size="small"
            :loading="btnLoading"
            icon="el-icon-refresh-right"
            @click="rejudgeProblem(row)"
            round
            >Rejudge All</el-button
          >
        </template>
      </vxe-table-column>
    </vxe-table>
  </el-card>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import api from '@/common/api';
import myMessage from '@/common/message';

export default {
  name: 'Contest-Rejudge-Admin',
  data() {
    return {
      btnLoading: false,
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    if (this.contestProblems.length == 0) {
      this.getContestProblems();
    }
  },
  methods: {
    ...mapActions(['getContestProblems']),
    rejudgeProblem(row) {
      this.$confirm('你是否确定将该题的所有提交全部重判？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        let params = {
          pid: row.pid,
          cid: row.cid,
        };
        this.btnLoading = true;
        api
          .ContestRejudgeProblem(params)
          .then((res) => {
            myMessage.success(res.data.msg);
            this.btnLoading = false;
          })
          .catch(() => {
            this.btnLoading = false;
          });
      });
    },
  },
  computed: {
    ...mapState({
      contest: (state) => state.contest.contest,
      contestProblems: (state) => state.contest.contestProblems,
    }),
  },
};
</script>
<style scoped></style>
