<template>
  <div>
    <vxe-table
      stripe
      auto-resize
      :data="adminDiscussionList"
      :loading="loading"
      align="center"
    >
      <vxe-table-column field="id" title="ID" width="60"></vxe-table-column>
      <vxe-table-column
        field="title"
        :title="$t('m.Title')"
        show-overflow
        min-width="130"
      ></vxe-table-column>
      <vxe-table-column
        field="author"
        :title="$t('m.Author')"
        min-width="130"
        show-overflow
      ></vxe-table-column>
      <vxe-table-column
        field="likeNum"
        :title="$t('m.Likes')"
        min-width="96"
      ></vxe-table-column>
      <vxe-table-column
        field="viewNum"
        :title="$t('m.Views')"
        min-width="96"
      ></vxe-table-column>
      <vxe-table-column
        field="gmtCreate"
        :title="$t('m.Created_Time')"
        min-width="150"
      >
        <template v-slot="{ row }">
          {{ row.gmtCreate | localtime }}
        </template>
      </vxe-table-column>
      <vxe-table-column field="status" :title="$t('m.Status')" min-width="100">
        <template v-slot="{ row }">
          <el-select
            v-model="row.status"
            @change="updateGroupDiscussion(row)"
            size="small"
          >
            <el-option :label="$t('m.Normal')" :value="0" :key="0"></el-option
            ><el-option
              :label="$t('m.Disable')"
              :value="1"
              :key="1"
            ></el-option>
          </el-select>
        </template>
      </vxe-table-column>
      <vxe-table-column
        min-width="100"
        field="topPriority"
        :title="$t('m.Top')"
      >
        <template v-slot="{ row }">
          <el-switch
            v-model="row.topPriority"
            active-text=""
            inactive-text=""
            :active-value="true"
            :inactive-value="false"
            @change="updateGroupDiscussion(row)"
          >
          </el-switch>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('m.Option')" min-width="130">
        <template v-slot="{ row }">
          <el-tooltip effect="dark" :content="$t('m.Delete')" placement="top">
            <el-button
              icon="el-icon-delete-solid"
              size="mini"
              @click.native="deleteGroupDiscussion(row.id)"
              type="danger"
            >
            </el-button>
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="$t('m.View_Discussion')"
            placement="top"
          >
            <el-button
              icon="el-icon-search"
              size="mini"
              @click.native="goGroupDiscussion(row.id)"
              type="primary"
            >
            </el-button>
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
  name: 'GroupDiscussionList',
  components: {
    Pagination,
    Editor,
  },
  data() {
    return {
      adminTotal: 0,
      currentPage: 1,
      limit: 10,
      adminDiscussionList: [],
      loading: false,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getGroupAdminDiscussionList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    getGroupAdminDiscussionList() {
      this.loading = true;
      api
        .getGroupAdminDiscussionList(
          this.currentPage,
          this.limit,
          this.$route.params.groupID
        )
        .then(
          (res) => {
            this.adminDiscussionList = res.data.data.records;
            this.adminTotal = res.data.data.total;
            this.loading = false;
          },
          (err) => {
            this.loading = false;
          }
        );
    },
    goGroupDiscussion(discussionId) {
      this.$router.push({
        name: 'GroupDiscussionDetails',
        params: {
          discussionID: discussionId,
          groupID: this.$route.params.groupID,
        },
      });
    },
    updateGroupDiscussion(row) {
      api.updateGroupDiscussion(row).then((res) => {
        mMessage.success(this.$i18n.t('m.Update_Successfully'));
        this.$emit('currentChange', 1);
        this.currentChange(1);
      });
    },
    deleteGroupDiscussion(did) {
      this.$confirm(
        this.$i18n.t('m.Delete_Discussion_Tips'),
        this.$i18n.t('m.Warning'),
        {
          confirmButtonText: this.$i18n.t('m.OK'),
          cancelButtonText: this.$i18n.t('m.Cancel'),
          type: 'warning',
        }
      )
        .then(() => {
          this.loading = true;
          api.deleteGroupDiscussion(did).then((res) => {
            this.loading = true;
            mMessage.success(this.$i18n.t('m.Delete_successfully'));
            this.$emit('currentChange', 1);
            this.currentChange(1);
          });
        })
        .catch(() => {
          this.loading = false;
        });
    },
  },
  computed: {
    ...mapGetters(['userInfo', 'isSuperAdmin', 'isGroupAdmin']),
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
