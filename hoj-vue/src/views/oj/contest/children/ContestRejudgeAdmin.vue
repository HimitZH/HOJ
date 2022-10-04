<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Contest_Rejudge') }}</span>
    </div>
    <vxe-table
      border="inner"
      stripe
      auto-resize
      align="center"
      :data="contestProblems"
    >
      <vxe-table-column field="pid" width="60" :title="$t('m.ID')">
      </vxe-table-column>
      <vxe-table-column
        field="displayId"
        :title="$t('m.Problem_ID')"
        min-width="100"
      ></vxe-table-column>
      <vxe-table-column
        field="displayTitle"
        :title="$t('m.Title')"
        min-width="150"
        show-overflow
      >
      </vxe-table-column>
      <vxe-table-column field="ac" :title="$t('m.AC')" min-width="80">
      </vxe-table-column>
      <vxe-table-column
        field="total"
        :title="$t('m.Total')"
        min-width="80"
      ></vxe-table-column>
      <vxe-table-column field="option" :title="$t('m.Option')" min-width="150">
        <template v-slot="{ row }">
          <el-button
            type="primary"
            size="small"
            :loading="btnLoading"
            icon="el-icon-refresh-right"
            @click="rejudgeProblem(row)"
            round
            >{{ $t('m.Rejudge_All') }}</el-button
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
      this.$confirm(this.$i18n.t('m.Contest_Rejudge_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(
        () => {
          let params = {
            pid: row.pid,
            cid: row.cid,
          };
          this.btnLoading = true;
          api
            .ContestRejudgeProblem(params)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Rejudge_successfully'));
              this.btnLoading = false;
            })
            .catch(() => {
              this.btnLoading = false;
            });
        },
        () => {}
      );
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
<style scoped>
@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
</style>
