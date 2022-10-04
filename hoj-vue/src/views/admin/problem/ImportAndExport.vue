<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Export_Problem') }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="exportProblems"
              icon="el-icon-arrow-down"
              >{{ $t('m.Export') }}
            </el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @keyup.enter.native="filterByKeyword"
              @search-click="filterByKeyword"
            ></vxe-input>
          </span>
        </div>
      </div>
      <vxe-table
        :data="problems"
        stripe
        auto-resize
        ref="xTable"
        :loading="loadingProblems"
        :checkbox-config="{ labelField: '', highlight: true, range: true }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"> </vxe-table-column>
        <vxe-table-column title="ID" min-width="100" field="id">
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Title')" field="title">
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="author"
          :title="$t('m.Author')"
        >
        </vxe-table-column>

        <vxe-table-column field="gmtCreate" :title="$t('m.Created_Time')">
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next, sizes"
          @current-change="getProblems"
          :page-size="limit"
          :page-sizes="[10, 50, 100, 500]"
           @size-change="handleSizeChange"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Import_Problem') }}</span>
      </div>
      <el-upload
        ref="HOJ"
        action="/api/file/import-problem"
        name="file"
        :file-list="fileList1"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile1Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
      >
        <el-button
          size="small"
          :loading="loading.HOJ"
          type="primary"
          slot="trigger"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('HOJ')"
          :loading="loading.HOJ"
          :disabled="!fileList1.length"
          icon="el-icon-upload"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Import_QDOJ_Problem')
        }}</span>
      </div>
      <el-upload
        ref="QDOJ"
        action="/api/file/import-qdoj-problem"
        name="file"
        :file-list="fileList2"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile2Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
      >
        <el-button
          size="small"
          type="primary"
          slot="trigger"
          :loading="loading.QDOJ"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('QDOJ')"
          :loading="loading.QDOJ"
          icon="el-icon-upload"
          :disabled="!fileList2.length"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Import_FPS_Problem')
        }}</span>
      </div>
      <el-upload
        ref="FPS"
        action="/api/file/import-fps-problem"
        name="file"
        :file-list="fileList3"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile3Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
      >
        <el-button
          size="small"
          type="primary"
          slot="trigger"
          :loading="loading.FPS"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('FPS')"
          :loading="loading.FPS"
          icon="el-icon-upload"
          :disabled="!fileList3.length"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>
  </div>
</template>
<script>
import api from '@/common/api';
import utils from '@/common/utils';
import myMessage from '@/common/message';
export default {
  name: 'import_and_export',
  data() {
    return {
      fileList1: [],
      fileList2: [],
      fileList3: [],
      page: 1,
      limit: 10,
      total: 0,
      loadingProblems: false,
      loadingImporting: false,
      keyword: '',
      problems: [],
      selected_problems: [],
      loading: {
        HOJ: false,
        qdoj: false,
        fps: false,
      },
    };
  },
  mounted() {
    this.getProblems();
  },
  methods: {
    // 题目表部分勾选 改变选中的内容
    handleSelectionChange({ records }) {
      this.selected_problems = records;
    },

    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      this.selected_problems = this.$refs.xTable.getCheckboxRecords();
    },

    handleSizeChange(pageSize){
      this.limit = pageSize;
      this.getProblems();
    },

    getProblems(page = 1) {
      let params = {
        keyword: this.keyword,
        currentPage: page,
        limit: this.limit,
        oj: 'Mine',
      };
      this.loadingProblems = true;
      api.admin_getProblemList(params).then((res) => {
        this.problems = res.data.data.records;
        this.total = res.data.data.total;
        this.loadingProblems = false;
      });
    },
    exportProblems() {
      let params = [];
      if (this.selected_problems.length <= 0) {
        myMessage.error(this.$i18n.t('m.Export_Problem_NULL_Tips'));
        return;
      }
      for (let p of this.selected_problems) {
        params.push('pid=' + p.id);
      }
      let url = '/api/file/export-problem?' + params.join('&');
      utils.downloadFile(url);
    },
    submitUpload(ref) {
      this.loading[ref] = true;
      this.$refs[ref].submit();
    },
    onFile1Change(file, fileList) {
      this.fileList1 = fileList.slice(-1);
    },
    onFile2Change(file, fileList) {
      this.fileList2 = fileList.slice(-1);
    },
    onFile3Change(file, fileList) {
      this.fileList3 = fileList.slice(-1);
    },
    uploadSucceeded(response, file, fileList) {
      this.loading.HOJ = false;
      this.loading.QDOJ = false;
      this.loading.FPS = false;
      if (response.status != 200) {
        myMessage.error(response.msg);
        this.$notify.error({
          title: this.$i18n.t('m.Error'),
          message: response.msg,
          duration: 8000
        });
      } else {
        myMessage.success(this.$i18n.t('m.Upload_Problem_Succeeded'));
        this.getProblems();
      }
    },
    uploadFailed() {
      this.loading.HOJ = false;
      this.loading.QDOJ = false;
      this.loading.FPS = false;
      myMessage.error(this.$i18n.t('m.Upload_Problem_Failed'));
    },
    filterByKeyword() {
      this.getProblems();
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
