<template>
  <div style="text-align:center">
    <vxe-input
      v-model="keyword"
      placeholder="Enter keyword"
      type="search"
      size="medium"
      @search-click="filterByKeyword"
      @keyup.enter.native="filterByKeyword"
      style="margin-bottom:10px"
    ></vxe-input>
    <vxe-table
      :data="problems"
      :loading="loading"
      auto-resize
      stripe
      align="center"
    >
      <vxe-table-column title="ID" min-width="100" field="problemId">
      </vxe-table-column>
      <vxe-table-column min-width="150" title="Title" field="title">
      </vxe-table-column>
      <vxe-table-column title="option" align="center" min-width="100">
        <template v-slot="{ row }">
          <el-tooltip
            effect="dark"
            content="添加该题目到比赛中"
            placement="top"
          >
            <el-button
              icon="el-icon-plus"
              size="mini"
              @click.native="handleAddProblem(row.id)"
              type="primary"
            >
            </el-button>
          </el-tooltip>
        </template>
      </vxe-table-column>
    </vxe-table>

    <el-pagination
      class="page"
      layout="prev, pager, next"
      @current-change="getPublicProblem"
      :page-size="limit"
      :total="total"
    >
    </el-pagination>
  </div>
</template>
<script>
import api from '@/common/api';
import myMessage from '@/common/message';
export default {
  name: 'add-problem-from-public',
  props: ['contestID'],
  data() {
    return {
      page: 1,
      limit: 10,
      total: 0,
      loading: false,
      problems: [],
      contest: {},
      keyword: '',
    };
  },
  mounted() {
    api
      .admin_getContest(this.contestID)
      .then((res) => {
        this.contest = res.data.data;
        this.getPublicProblem();
      })
      .catch(() => {});
  },
  methods: {
    getPublicProblem(page) {
      this.loading = true;
      let params = {
        keyword: this.keyword,
        currentPage: page,
        limit: this.limit,
        problemType: this.contest.type,
        cid: this.contest.id,
      };
      api
        .admin_getContestProblemList(params)
        .then((res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.problems = res.data.data.records;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleAddProblem(problemID) {
      this.$prompt('请输入该题目在比赛中显示的序号ID', '确认').then(
        ({ value }) => {
          let data = {
            pid: problemID,
            cid: this.contestID,
            displayId: value,
          };
          api.admin_addProblemFromPublic(data).then(
            (res) => {
              this.$emit('on-change');
              myMessage.success(res.data.msg);
              this.getPublicProblem(this.page);
            },
            () => {}
          );
        },
        () => {}
      );
    },
    filterByKeyword() {
      this.getPublicProblem(this.page);
    },
  },
};
</script>
<style scoped>
.page {
  margin-top: 20px;
  text-align: right;
}
</style>
