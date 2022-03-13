<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.General_Announcement')
        }}</span>
      </div>
      <div class="create">
        <el-button
          type="primary"
          size="small"
          @click="openAnnouncementDialog(null)"
          icon="el-icon-plus"
          >{{ $t('m.Create') }}</el-button
        >
      </div>
      <div class="list">
        <vxe-table
          :loading="loading"
          ref="table"
          :data="announcementList"
          auto-resize
          stripe
        >
          <vxe-table-column min-width="50" field="id" title="ID">
          </vxe-table-column>
          <vxe-table-column
            min-width="150"
            field="title"
            show-overflow
            :title="$t('m.Announcement_Title')"
          >
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
            field="gmtModified"
            :title="$t('m.Modified_Time')"
          >
            <template v-slot="{ row }">
              {{ row.gmtModified | localtime }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            min-width="150"
            field="username"
            show-overflow
            :title="$t('m.Author')"
          >
          </vxe-table-column>
          <vxe-table-column
            min-width="100"
            field="status"
            :title="$t('m.Announcement_visible')"
          >
            <template v-slot="{ row }">
              <el-switch
                v-model="row.status"
                active-text=""
                inactive-text=""
                :active-value="0"
                :inactive-value="1"
                @change="handleVisibleSwitch(row)"
              >
              </el-switch>
            </template>
          </vxe-table-column>
          <vxe-table-column title="Option" min-width="150">
            <template v-slot="row">
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('m.Edit_Announcement')"
                placement="top"
              >
                <el-button
                  icon="el-icon-edit-outline"
                  @click.native="openAnnouncementDialog(row.row)"
                  size="mini"
                  type="primary"
                ></el-button>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('m.Delete_Announcement')"
                placement="top"
              >
                <el-button
                  icon="el-icon-delete-solid"
                  @click.native="deleteAnnouncement(row.row.id)"
                  size="mini"
                  type="danger"
                ></el-button>
              </el-tooltip>
            </template>
          </vxe-table-column>
        </vxe-table>

        <div class="panel-options">
          <el-pagination
            v-if="!contestID"
            class="page"
            layout="prev, pager, next"
            @current-change="currentChange"
            :page-size="pageSize"
            :total="total"
          >
          </el-pagination>
        </div>
      </div>
    </el-card>

    <!--编辑公告对话框-->
    <el-dialog
      :title="announcementDialogTitle"
      :visible.sync="showEditAnnouncementDialog"
      :fullscreen="true"
      @open="onOpenEditDialog"
    >
      <el-form label-position="top" :model="announcement">
        <el-form-item :label="$t('m.Announcement_Title')" required>
          <el-input
            v-model="announcement.title"
            :placeholder="$t('m.Announcement_Title')"
            class="title-input"
          >
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Announcement_Content')" required>
          <Editor :value.sync="announcement.content"></Editor>
        </el-form-item>
        <div class="visible-box">
          <span>{{ $t('m.Announcement_visible') }}</span>
          <el-switch
            v-model="announcement.status"
            :active-value="0"
            :inactive-value="1"
            active-text=""
            inactive-text=""
          >
          </el-switch>
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button
          type="danger"
          @click.native="showEditAnnouncementDialog = false"
          >{{ $t('m.Cancel') }}</el-button
        >
        <el-button type="primary" @click.native="submitAnnouncement">{{
          $t('m.OK')
        }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';
import { mapGetters } from 'vuex';
const Editor = () => import('@/components/admin/Editor.vue');
export default {
  name: 'announcement',
  components: {
    Editor,
  },
  data() {
    return {
      contestID: '',
      // 显示编辑公告对话框
      showEditAnnouncementDialog: false,
      // 公告列表
      announcementList: [],
      // 一页显示的公告数
      pageSize: 15,
      // 总公告数
      total: 0,
      mode: 'create',
      // 公告 (new | edit) model

      announcement: {
        id: null,
        title: '',
        content: '',
        status: 0,
        uid: '',
      },
      // 对话框标题
      announcementDialogTitle: 'Edit Announcement',
      // 是否显示loading
      loading: false,
      // 当前页码
      currentPage: 0,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.contestID = this.$route.params.contestId;
      if (this.contestID) {
        this.getContestAnnouncementList(1);
      } else {
        this.getAnnouncementList(1);
      }
    },
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      if (this.contestID) {
        this.getContestAnnouncementList(page);
      } else {
        this.getAnnouncementList(page);
      }
    },

    getAnnouncementList(page) {
      this.loading = true;
      api.admin_getAnnouncementList(page, this.pageSize).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.announcementList = res.data.data.records;
        },
        (res) => {
          this.loading = false;
        }
      );
    },
    getContestAnnouncementList(page) {
      this.loading = true;
      api
        .admin_getContestAnnouncementList(this.contestID, page, this.pageSize)
        .then((res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.announcementList = res.data.data.records;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    // 打开编辑对话框的回调
    onOpenEditDialog() {
      // todo 优化
      // 暂时解决 文本编辑器显示异常bug
      setTimeout(() => {
        if (document.createEvent) {
          let event = document.createEvent('HTMLEvents');
          event.initEvent('resize', true, true);
          window.dispatchEvent(event);
        } else if (document.createEventObject) {
          window.fireEvent('onresize');
        }
      }, 0);
    },
    // 提交编辑
    // 默认传入MouseEvent
    submitAnnouncement(data = undefined) {
      let funcName = '';
      if (!data.id) {
        data = this.announcement;
      }
      let requestData;
      if (this.contestID) {
        let announcement = {
          announcement: data,
          cid: this.contestID,
        };
        requestData = announcement;
        funcName =
          this.mode === 'edit'
            ? 'admin_updateContestAnnouncement'
            : 'admin_createContestAnnouncement';
      } else {
        funcName =
          this.mode === 'edit'
            ? 'admin_updateAnnouncement'
            : 'admin_createAnnouncement';
        requestData = data;
      }
      api[funcName](requestData)
        .then((res) => {
          this.showEditAnnouncementDialog = false;
          myMessage.success(this.$i18n.t('m.Post_successfully'));
          this.init();
        })
        .catch();
    },

    // 删除公告
    deleteAnnouncement(announcementId) {
      this.$confirm(this.$i18n.t('m.Delete_Announcement_Tips'), 'Warning', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      })
        .then(() => {
          // then 为确定
          this.loading = true;
          let funcName = this.contestID
            ? 'admin_deleteContestAnnouncement'
            : 'admin_deleteAnnouncement';
          api[funcName](announcementId).then((res) => {
            this.loading = true;
            myMessage.success(this.$i18n.t('m.Delete_successfully'));
            this.init();
          });
        })
        .catch(() => {
          // catch 为取消
          this.loading = false;
        });
    },

    openAnnouncementDialog(row) {
      this.showEditAnnouncementDialog = true;
      if (row !== null) {
        this.announcementDialogTitle = this.$i18n.t('m.Edit_Announcement');
        this.announcement = Object.assign({}, row);
        this.mode = 'edit';
      } else {
        this.announcementDialogTitle = this.$i18n.t('m.Create_Announcement');
        this.announcement.title = '';
        this.announcement.status = 0;
        this.announcement.content = '';
        this.announcement.uid = this.userInfo.uid;
        this.announcement.username = this.userInfo.username;
        this.mode = 'create';
      }
    },
    handleVisibleSwitch(row) {
      this.mode = 'edit';
      this.submitAnnouncement({
        id: row.id,
        title: row.title,
        content: row.content,
        status: row.status,
        uid: row.uid,
      });
    },
  },
  watch: {
    $route() {
      this.init();
    },
  },
  computed: {
    ...mapGetters(['userInfo']),
  },
};
</script>

<style scoped>
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
.create {
  margin-bottom: 5px;
}
</style>
