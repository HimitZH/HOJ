<template>
  <el-card>
    <div class="filter-row">
      <el-row>
        <el-col :md="3" :xs="5">
          <span class="title">{{ $t('m.Group_Member') }}</span>
        </el-col>
        <el-col :md="18" :xs="19">
          <el-button
            v-if="isSuperAdmin || isGroupAdmin"
            :type="adminPage ? 'warning' : 'success'"
            size="small"
            @click="adminPage = !adminPage"
            :icon="adminPage ? 'el-icon-back' : 'el-icon-s-opportunity'"
            >{{ adminPage ? $t('m.Back') : $t('m.Member_Admin') }}</el-button
          >
        </el-col>
      </el-row>
    </div>
    <div v-if="!adminPage">
      <vxe-table
        stripe
        auto-resize
        :data="memberList"
        :loading="loading"
        align="center"
      >
        <vxe-table-column
          min-width="150"
          field="username"
          show-overflow
          :title="$t('m.Username')"
        >
          <template v-slot="{ row }"
            ><el-link
              type="primary"
              @click="goUserHome(row.username)"
              style="font-size: 13px;"
              >{{ row.username }}</el-link
            >
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="gmtCreate"
          :title="$t('m.Join_Time')"
        >
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="gmtModify"
          :title="$t('m.Change_Time')"
        >
          <template v-slot="{ row }">
            {{ row.gmtModify | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="auth"
          :title="$t('m.Member_Auth')"
        >
          <template v-slot="{ row }">
            <el-select v-model="row.auth" disabled size="small">
              <el-option :label="$t('m.Applying')" :value="1"></el-option>
              <el-option :label="$t('m.Refused')" :value="2"></el-option>
              <el-option :label="$t('m.General_Member')" :value="3"></el-option>
              <el-option :label="$t('m.Group_Admin')" :value="4"></el-option>
              <el-option :label="$t('m.Group_Root')" :value="5"></el-option>
            </el-select>
          </template>
        </vxe-table-column>
      </vxe-table>
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="currentChange"
        :current.sync="currentPage"
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </div>
    <MemberList v-if="adminPage" @currentChange="currentChange"></MemberList>
  </el-card>
</template>

<script>
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import api from '@/common/api';
import Editor from '@/components/admin/Editor.vue';
import MemberList from '@/components/oj/group/MemberList';
export default {
  name: 'GroupMemberList',
  components: {
    Pagination,
    Editor,
    MemberList,
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      memberList: [],
      loading: false,
      adminPage: false,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getGroupMemberList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    goUserHome(username) {
      this.$router.push({
        path: '/user-home',
        query: { username },
      });
    },
    getGroupMemberList() {
      this.loading = true;
      api
        .getGroupMemberList(
          this.currentPage,
          this.limit,
          this.$route.params.groupID
        )
        .then(
          (res) => {
            this.memberList = res.data.data.records;
            this.total = res.data.data.total;
            this.loading = false;
          },
          (err) => {
            this.loading = false;
          }
        );
    },
  },
  computed: {
    ...mapGetters(['userInfo', 'isSuperAdmin', 'isGroupAdmin']),
  },
};
</script>

<style scoped>
.title {
  font-size: 20px;
  vertical-align: middle;
  float: left;
}
.filter-row {
  margin-bottom: 5px;
  text-align: center;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-left: 5px;
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-left: 10px;
    margin-right: 10px;
  }
}
.title-input {
  margin-bottom: 20px;
}

.visible-box {
  margin-top: 10px;
  width: 205px;
  float: left;
}
.visible-box span {
  margin-right: 10px;
}
.el-form-item {
  margin-bottom: 2px !important;
}
/deep/.el-dialog__body {
  padding-top: 0 !important;
}
</style>
