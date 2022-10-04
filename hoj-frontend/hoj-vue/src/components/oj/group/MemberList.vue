<template>
  <div>
    <vxe-table
      stripe
      auto-resize
      :data="adminMemberList"
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
        :title="$t('m.Created_Time')"
      >
        <template v-slot="{ row }">
          {{ row.gmtCreate | localtime }}
        </template>
      </vxe-table-column>
      <vxe-table-column
        min-width="150"
        field="gmtModify"
        :title="$t('m.Modified_Time')"
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
          <el-select
            v-model="row.auth"
            @change="updateGroupMember(row)"
            size="small"
            :disabled="!isSuperAdmin && !isGroupOwner && row.auth >= userAuth"
          >
            <el-option
              :label="$t('m.Applying')"
              :value="1"
              :disabled="!isSuperAdmin && !isGroupOwner && 1 >= userAuth"
            ></el-option>
            <el-option
              :label="$t('m.Refused')"
              :value="2"
              :disabled="!isSuperAdmin && !isGroupOwner && 2 >= userAuth"
            ></el-option>
            <el-option
              :label="$t('m.General_Member')"
              :value="3"
              :disabled="!isSuperAdmin && !isGroupOwner && 3 >= userAuth"
            ></el-option>
            <el-option
              :label="$t('m.Group_Admin')"
              :value="4"
              :disabled="!isSuperAdmin && !isGroupOwner && 4 >= userAuth"
            ></el-option>
            <el-option
              :label="$t('m.Group_Root')"
              :value="5"
              :disabled="!isSuperAdmin && !isGroupOwner && 5 >= userAuth"
            ></el-option>
          </el-select>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('m.Option')" min-width="150">
        <template v-slot="{ row }">
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('m.View_Reason')"
            placement="top"
          >
            <el-button
              icon="el-icon-search"
              @click.native="viewReason(row.reason)"
              size="mini"
              type="primary"
            ></el-button>
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('m.Delete_Member')"
            placement="top"
            v-if="
              (isGroupOwner || row.auth < userAuth) &&
                userInfo.uid != row.uid &&
                row.uid != group.owner
            "
          >
            <el-button
              icon="el-icon-delete-solid"
              @click.native="deleteMember(row.uid, row.gid)"
              size="mini"
              type="danger"
            ></el-button>
          </el-tooltip>
        </template>
      </vxe-table-column>
    </vxe-table>
    <Pagination
      :total="adminTotal"
      :page-size="limit"
      @on-change="currentChange"
      :current.sync="currentPage"
      @on-page-size-change="onPageSizeChange"
      :layout="'prev, pager, next, sizes'"
    ></Pagination>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import api from '@/common/api';
import mMessage from '@/common/message';
import Editor from '@/components/admin/Editor.vue';
export default {
  name: 'GroupMemberList',
  components: {
    Pagination,
    Editor,
  },
  data() {
    return {
      adminTotal: 0,
      currentPage: 1,
      limit: 10,
      adminMemberList: [],
      member: {
        id: null,
        username: '',
        auth: null,
      },
      loading: false,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getGroupAdminMemberList();
    },
    goUserHome(username) {
      this.$router.push({
        path: '/user-home',
        query: { username },
      });
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    getGroupAdminMemberList() {
      this.loading = true;
      api
        .getGroupApplyList(
          this.currentPage,
          this.limit,
          this.$route.params.groupID
        )
        .then(
          (res) => {
            this.adminMemberList = res.data.data.records;
            this.adminTotal = res.data.data.total;
            this.loading = false;
          },
          (err) => {
            this.loading = false;
          }
        );
    },
    updateGroupMember(data) {
      api
        .updateGroupMember(data)
        .then((res) => {
          mMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.$emit('currentChange', 1);
          this.$store.dispatch('getGroup');
          this.currentChange(1);
        })
        .catch(() => {});
    },
    viewReason(value) {
      this.$alert(value, this.$t('m.Apply_Reason'), {
        confirmButtonText: this.$t('m.OK'),
      });
    },
    deleteMember(uid, gid) {
      this.$confirm(
        this.$i18n.t('m.Delete_Member_Tips'),
        this.$i18n.t('m.Warning'),
        {
          confirmButtonText: this.$i18n.t('m.OK'),
          cancelButtonText: this.$i18n.t('m.Cancel'),
          type: 'warning',
        }
      )
        .then(() => {
          this.loading = true;
          api
            .deleteGroupMember(uid, gid)
            .then((res) => {
              this.loading = false;
              mMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.$emit('currentChange', 1);
              this.$store.dispatch('getGroup');
              this.currentChange(1);
            })
            .catch(() => {
              this.loading = false;
            });
        })
        .catch(() => {
          this.loading = false;
        });
    },
  },
  computed: {
    ...mapGetters([
      'userInfo',
      'isSuperAdmin',
      'userAuth',
      'isGroupOwner',
      'group',
    ]),
  },
};
</script>

<style scoped>
.filter-row {
  margin-bottom: 5px;
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
