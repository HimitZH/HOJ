<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Discussion_Admin')
        }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="danger"
              icon="el-icon-delete-solid"
              @click="deleteDiscussion(null)"
              size="small"
              >{{ $t('m.Delete') }}
            </el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
        </div>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="discussionList"
        ref="xTable"
        align="center"
        :loading="discussionLoadingTable"
        :checkbox-config="{ highlight: true, range: true }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column field="id" title="ID" width="60"></vxe-table-column>
        <vxe-table-column
          field="title"
          :title="$t('m.Title')"
          show-overflow
          min-width="150"
        ></vxe-table-column>
        <vxe-table-column
          field="author"
          :title="$t('m.Author')"
          min-width="150"
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
        <vxe-table-column
          field="status"
          :title="$t('m.Status')"
          min-width="100"
        >
          <template v-slot="{ row }">
            <el-select
              v-model="row.status"
              @change="changeDiscussionStatus(row)"
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
              @change="handleTopSwitch(row)"
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
                @click.native="deleteDiscussion([row.id])"
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
                @click.native="toDiscussion(row.id, row.gid)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="discussionCurrentChange"
          :page-size="pageSize"
          :total="discussionTotal"
        >
        </el-pagination>
      </div>
    </el-card>
    <el-card style="margin-top:20px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Discussion_Report')
        }}</span>
      </div>
      <vxe-table
        :loading="discussionReportLoadingTable"
        ref="table"
        align="center"
        :data="discussionReportList"
        auto-resize
        stripe
      >
        <vxe-table-column min-width="60" field="id" title="ID">
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="did"
          :title="$t('m.Discussion_ID')"
        >
        </vxe-table-column>
         <vxe-table-column
          field="discussionTitle"
          :title="$t('m.Title')"
          show-overflow
          min-width="150"
        ></vxe-table-column>
        <vxe-table-column
          field="discussionAuthor"
          :title="$t('m.Author')"
          min-width="150"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="reporter"
          show-overflow
          :title="$t('m.Reporter')"
        >
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="gmtCreate"
          :title="$t('m.Report_Time')"
        >
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="status"
          :title="$t('m.Checked')"
        >
          <template v-slot="{ row }">
            <el-switch
              v-model="row.status"
              active-text=""
              inactive-text=""
              :active-value="true"
              :inactive-value="false"
              @change="handleCheckedSwitch(row)"
            >
            </el-switch>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Option')" min-width="150">
          <template v-slot="{ row }">
            <el-tooltip
              class="item"
              effect="dark"
              :content="$t('m.View_Report_content')"
              placement="top"
            >
              <el-button
                icon="el-icon-document"
                @click.native="openReportDialog(row.content)"
                size="mini"
                type="success"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="$t('m.View_Discussion')"
              placement="top"
            >
              <el-button
                icon="el-icon-search"
                size="mini"
                @click.native="toDiscussion(row.did, row.gid)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="discussionReportCurrentChange"
          :page-size="pageSize"
          :total="discussionReportTotal"
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>
<script>
import api from '@/common/api';
import myMessage from '@/common/message';
export default {
  name: 'discussion',
  data() {
    return {
      pageSize: 10,
      discussionTotal: 0,
      discussionList: [],
      selectedDiscussions: [],
      keyword: '',
      discussionLoadingTable: false,
      discussionCurrentPage: 1,

      discussionReportList: [],
      discussionReportTotal: 0,
      discussionReportCurrentPage: 1,
      discussionReportLoadingTable: false,
    };
  },
  mounted() {
    this.getDiscussionList(1);
    this.getDiscussionReportList();
  },
  methods: {
    discussionCurrentChange(page) {
      this.discussionCurrentPage = page;
      this.getDiscussionList(page);
    },
    discussionReportCurrentChange(page) {
      this.discussionReportCurrentPage = page;
      this.getDiscussionReportList();
    },
    getDiscussionList(page) {
      this.discussionLoadingTable = true;
      let searchParams = {
        currentPage: page,
        keyword: this.keyword,
        admin: true,
      };
      api.getDiscussionList(this.pageSize, searchParams).then(
        (res) => {
          this.discussionLoadingTable = false;
          this.discussionTotal = res.data.data.total;
          this.discussionList = res.data.data.records;
        },
        (res) => {
          this.discussionLoadingTable = false;
        }
      );
    },
    getDiscussionReportList() {
      this.discussionReportLoadingTable = true;
      api
        .admin_getDiscussionReport(
          this.discussionReportCurrentPage,
          this.pageSize
        )
        .then(
          (res) => {
            this.discussionReportLoadingTable = false;
            this.discussionReportList = res.data.data.records;
            this.discussionReportTotal = res.data.data.total;
          },
          (err) => {
            this.discussionReportLoadingTable = false;
          }
        );
    },
    filterByKeyword() {
      this.discussionCurrentChange(1);
      this.keyword = '';
    },
    // 用户表部分勾选 改变选中的内容
    handleSelectionChange({ records }) {
      this.selectedDiscussions = [];
      for (let num = 0; num < records.length; num++) {
        this.selectedDiscussions.push(records[num].id);
      }
    },
    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      let discussion = this.$refs.xTable.getCheckboxRecords();
      this.selectedDiscussions = [];
      for (let num = 0; num < discussion.length; num++) {
        this.selectedDiscussions.push(discussion[num].id);
      }
    },
    changeDiscussionStatus(row) {
      let discussion = {
        id: row.id,
        status: row.status,
      };
      api.admin_updateDiscussion(discussion).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },
    handleTopSwitch(row) {
      let discussion = {
        id: row.id,
        topPriority: row.topPriority,
      };
      api.admin_updateDiscussion(discussion).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },

    handleCheckedSwitch(row) {
      let discussionReport = {
        id: row.id,
        status: row.status,
      };
      api.admin_updateDiscussionReport(discussionReport).then((res) => {
        myMessage.success(this.$i18n.t('m.Update_Successfully'));
      });
    },

    toDiscussion(did, gid) {
      if(gid != null){
        window.open('/group/'+ gid +'/discussion-detail/' + did);
      }else{
        window.open('/discussion-detail/' + did);
      }
    },

    deleteDiscussion(didList) {
      if (!didList) {
        didList = this.selectedDiscussions;
      }
      if (didList.length > 0) {
        this.$confirm(this.$i18n.t('m.Delete_Discussion_Tips'), 'Tips', {
          type: 'warning',
        }).then(
          () => {
            api
              .admin_deleteDiscussion(didList)
              .then((res) => {
                myMessage.success(this.$i18n.t('m.Delete_successfully'));
                this.selectedDiscussions = [];
                this.getDiscussionList(this.currentPage);
              })
              .catch(() => {
                this.selectedDiscussions = [];
                this.getDiscussionList(this.currentPage);
              });
          },
          () => {}
        );
      } else {
        myMessage.warning(
          this.$i18n.t('m.The_number_of_discussions_selected_cannot_be_empty')
        );
      }
    },
    openReportDialog(content) {
      let reg = '#(.*?)# ';
      let re = RegExp(reg, 'g');
      let tmp;
      let showContent = '<strong>' + this.$i18n.t('m.Tags') + '</strong>：';
      while ((tmp = re.exec(content))) {
        showContent += tmp[1] + ' ';
      }
      showContent +=
        '<br><br><strong>' +
        this.$i18n.t('m.Content') +
        '</strong>：' +
        content.replace(/#(.*?)# /g, '');
      this.$alert(showContent, this.$i18n.t('m.Report_Content'), {
        confirmButtonText: this.$i18n.t('m.OK'),
        dangerouslyUseHTMLString: true,
      });
    },
  },
};
</script>
<style scoped>
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
</style>
