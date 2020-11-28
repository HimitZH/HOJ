<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">Announcement</span>
      </div>
      <div class="create">
      <el-button type="primary" size="small" @click="openAnnouncementDialog(null)" icon="el-icon-plus">Create</el-button>
      </div>
      <div class="list">
        <vxe-table
          :loading="loading"
          ref="table"
          :data="announcementList"
          auto-resize
          stripe
          >
          <vxe-table-column
            min-width="50"
            field="id"
            title="ID">
          </vxe-table-column>
          <vxe-table-column
           min-width="150"
            field="title"
            title="Title">
          </vxe-table-column>
          <vxe-table-column
            min-width="150"
            field="gmtCreate"
            title="Create Time">
            <template v-slot="{row}">
              {{ row.gmtCreate | localtime }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            min-width="150"
            field="gmtModified"
            title="Last Update Time">
            <template v-slot="{row}">
              {{row.gmtModified | localtime }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            min-width="150"
            field="username"
            title="Author">
          </vxe-table-column>
          <vxe-table-column
            min-width="100"
            field="status"
            title="Visible">
            <template v-slot="{row}">
              <el-switch v-model="row.status"
                         active-text=""
                         inactive-text=""
                         :active-value="0"
                         :inactive-value="1"
                         @change="handleVisibleSwitch(row)">
              </el-switch>
            </template>
          </vxe-table-column>
          <vxe-table-column
            title="Option"
            min-width="150">
            <template v-slot="row">
              <el-tooltip class="item" effect="dark" content="编辑公告" placement="top">
                <el-button  icon="el-icon-edit-outline" @click.native="openAnnouncementDialog(row)" size="mini" type="primary"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="删除公告" placement="top">
                <el-button  icon="el-icon-delete-solid" @click.native="deleteAnnouncement(row.id)" size="mini" type="danger"></el-button>
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
            :total="total">
          </el-pagination>
        </div>
      </div>
    </el-card>

    <!--编辑公告对话框-->
    <el-dialog :title="announcementDialogTitle" :visible.sync="showEditAnnouncementDialog" :fullscreen="true"
               @open="onOpenEditDialog">
      <el-form label-position="top" :model="announcement">
        <el-form-item label="公告标题" required>
          <el-input
            v-model="announcement.title"
            placeholder="请输入公告标题" class="title-input">
          </el-input>
        </el-form-item>
        <el-form-item label="公告内容" required>
          <Simditor v-model="announcement.content"></Simditor>
        </el-form-item>
        <div class="visible-box">
          <span>是否显示</span>
          <el-switch
            v-model="announcement.status"
            :active-value="0"
            :inactive-value="1"
            active-text=""
            inactive-text="">
          </el-switch>
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger"  @click.native="showEditAnnouncementDialog = false">Cancel</el-button>
        <el-button type="primary"  @click.native="submitAnnouncement">Save</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<script>
  import Simditor from '@/components/admin/Simditor.vue'
  import api from '@/common/api'

  export default {
    name: 'announcement',
    components: {
      Simditor
    },
    data () {
      return {
        contestID: '',
        // 显示编辑公告对话框
        showEditAnnouncementDialog: false,
        // 公告列表
        announcementList: [
          {id:1001,title:'测试公告',gmtCreate:'2020-11-11 12:22:22',content:"测试内容",
          gmtModified:'2020-11-11 12:53:22',username:'Himit_ZH',status:0}
        ],
        // 一页显示的公告数
        pageSize: 15,
        // 总公告数
        total: 0,
        mode: 'create',
        // 公告 (new | edit) model

        announcement: {
          title: '',
          status: 0,
          content: ''
        },
        // 对话框标题
        announcementDialogTitle: 'Edit Announcement',
        // 是否显示loading
        loading: false,
        // 当前页码
        currentPage: 0
      }
    },
    mounted () {
      // this.init()
    },
    methods: {
      init () {
        this.contestID = this.$route.params.contestId
        if (this.contestID) {
          this.getContestAnnouncementList()
        } else {
          this.getAnnouncementList(1)
        }
      },
      // 切换页码回调
      currentChange (page) {
        this.currentPage = page
        this.getAnnouncementList(page)
      },

      getAnnouncementList (page) {
        this.loading = true
        api.getAnnouncementList((page - 1) * this.pageSize, this.pageSize).then(res => {
          this.loading = false
          this.total = res.data.data.total
          this.announcementList = res.data.data.results
        }, res => {
          this.loading = false
        })
      },
      getContestAnnouncementList () {
        this.loading = true
        api.getContestAnnouncementList(this.contestID).then(res => {
          this.loading = false
          this.announcementList = res.data.data
        }).catch(() => {
          this.loading = false
        })
      },
      // 打开编辑对话框的回调
      onOpenEditDialog () {
        // todo 优化
        // 暂时解决 文本编辑器显示异常bug
        setTimeout(() => {
          if (document.createEvent) {
            let event = document.createEvent('HTMLEvents')
            event.initEvent('resize', true, true)
            window.dispatchEvent(event)
          } else if (document.createEventObject) {
            window.fireEvent('onresize')
          }
        }, 0)
      },
      // 提交编辑
      // 默认传入MouseEvent
      submitAnnouncement (data = undefined) {
        let funcName = ''
        if (!data.title) {
          data = {
            id: this.announcement.id,
            title: this.announcement.title,
            content: this.announcement.content,
            status: this.announcement.status
          }
        }
        if (this.contestID) {
          data.contest_id = this.contestID
          funcName = this.mode === 'edit' ? 'updateContestAnnouncement' : 'createContestAnnouncement'
        } else {
          funcName = this.mode === 'edit' ? 'updateAnnouncement' : 'createAnnouncement'
        }
        api[funcName](data).then(res => {
          this.showEditAnnouncementDialog = false
          this.init()
        }).catch()
      },

      // 删除公告
      deleteAnnouncement (announcementId) {
        this.$confirm('你确定要删除该公告?', 'Warning', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // then 为确定
          this.loading = true
          let funcName = this.contestID ? 'deleteContestAnnouncement' : 'deleteAnnouncement'
          api[funcName](announcementId).then(res => {
            this.loading = true
            this.init()
          })
        }).catch(() => {
          // catch 为取消
          this.loading = false
        })
      },

      openAnnouncementDialog (row) {
        this.showEditAnnouncementDialog = true
        if (row !== null) {
          this.announcementDialogTitle = 'Edit Announcement'
          this.announcement = Object.assign({},row.data[0])
          console.log(this.announcement)
        } else {
          this.announcementDialogTitle = 'Create Announcement'
          this.announcement.title = ''
          this.announcement.visible = 0
          this.announcement.content = ''
          this.mode = 'create'
        }
      },
      handleVisibleSwitch (row) {
        this.mode = 'edit'
        this.submitAnnouncement({
          id: row.data[0].id,
          title: row.data[0].title,
          content: row.data[0].content,
          status: row.data[0].status
        })
      }
    },
    watch: {
      $route () {
        this.init()
      }
    }
  }
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
  .visible-box span{
    margin-right:10px ;
  }
  .el-form-item {
   margin-bottom: 2px!important;
  }
  /deep/.el-dialog__body {
    padding-top: 0!important;
  }
  .create{
    margin-bottom: 5px;
  }
</style>
