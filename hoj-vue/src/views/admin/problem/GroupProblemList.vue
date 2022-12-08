<template>
<div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Admin_Group_Apply_Problem')}}</span>
        <el-row :gutter="30">
          <el-col :xs="24" :md="6" style="margin-top:15px">
              <el-input v-model="query.keyword" 
              :placeholder="$t('m.Enter_keyword')" 
              size="small"
              @keyup.enter.native="pushRouter">
              </el-input>
          </el-col>
          <el-col :xs="24" :md="6" style="margin-top:15px">
              <el-input v-model="query.gid" 
              :placeholder="$t('m.Enter_Group_ID')" 
              type="number"
              size="small"
              @keyup.enter.native="pushRouter">
              </el-input>
           </el-col>
          <el-col :xs="24" :md="6" style="margin-top:15px">
            <el-button
              type="primary"
              size="small"
              @click="pushRouter"
              icon="el-icon-search"
              >{{ $t('m.Search') }}
            </el-button>
           </el-col>
        </el-row>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="problemList"
        ref="adminProblemList"
        :loading="loading"
        align="center"
      >
        <vxe-table-column
          field="gid"
          min-width="64"
          :title="$t('m.Group_Number')"
          show-overflow
        >
        <template v-slot="{ row }">
            <el-link
                type="primary"
                @click="goGroup(row.gid)"
                style="font-size: 13px;"
                >{{ row.gid }}
            </el-link>
        </template>
        </vxe-table-column>
        <vxe-table-column min-width="64" field="id" :title="$t('m.Problem')+' ID'">
        <template v-slot="{ row }">
            <el-link
                type="success"
                @click="goEdit(row.id)"
                style="font-size: 13px;"
                >{{ row.id }}
            </el-link>
        </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="100"
          field="problemId"
          :title="$t('m.Display_ID')"
        >
        </vxe-table-column>
         <vxe-table-column
          field="title"
          min-width="150"
          :title="$t('m.Title')"
          show-overflow
        >
        </vxe-table-column>
        <vxe-table-column
          field="type"
          min-width="64"
          :title="$t('m.Type')"
          show-overflow
        >
        <template v-slot="{ row }">
            <el-tag type="gray">{{ row.type | parseContestType }}</el-tag>
        </template>
        </vxe-table-column>
        <vxe-table-column
          field="judgeMode"
          min-width="64"
          :title="$t('m.Judge_Mode')"
          show-overflow
        >
        <template v-slot="{ row }">
            <span v-if="row.judgeMode == 'default'">{{ $t('m.General_Judge') }}</span>
            <span v-else-if="row.judgeMode == 'spj'">{{ $t('m.Special_Judge') }}</span>
            <span v-else-if="row.judgeMode == 'interactive'">{{ $t('m.Interactive_Judge') }}</span>
        </template>
        </vxe-table-column>
        <vxe-table-column
          field="author"
          min-width="130"
          :title="$t('m.Author')"
          show-overflow
        >
        <template v-slot="{ row }"
          ><el-link
            type="primary"
            @click="goUserHome(row.author)"
            style="font-size: 13px;"
            >{{ row.author }}</el-link
          >
        </template>
        </vxe-table-column>
         <vxe-table-column min-width="120" :title="$t('m.Examine')">
          <template v-slot="{ row }">
            <el-select
              v-model="row.applyPublicProgress"
              @change="changeProblemProgress(row.id,row.applyPublicProgress)"
              size="small"
            >
              <el-option
                :label="$t('m.Applying')"
                :value="1"
              ></el-option>
              <el-option
                :label="$t('m.Agreed')"
                :value="2"
              ></el-option>
              <el-option
                :label="$t('m.Refused')"
                :value="3"
              ></el-option>
            </el-select>
          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options">
        <el-pagination
          v-if="showPagination"
          class="page"
          layout="prev, pager, next, sizes"
          @current-change="currentChange"
          :page-size="query.limit"
          :total="total"
          :current-page.sync="query.currentPage"
          @size-change="onPageSizeChange"
          :page-sizes="[10, 30, 50, 100]"
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
  name: 'GroupProblemList',
  data() {
    return {
      total: 0,
      query: {
        gid: null,
        limit: 10,
        keyword: '',
        currentPage: 1,
      },
      problemList: [],
      loading: false,
      showPagination:false,
    };
  },
  mounted() {
    this.init();
  },
  methods:{
      init(){
        let query = this.$route.query;
        this.query.currentPage = query.currentPage || 1;
        this.query.limit = query.limit || 10;
        this.query.keyword = query.keyword;
        this.query.gid = query.gid || null;
        this.getProblemList();
      },
      getProblemList(){
         let params = {
            limit: this.query.limit,
            currentPage: this.query.currentPage,
            keyword: this.query.keyword,
            gid: this.query.gid,
        };
        this.loading = true;
        api.admin_getGroupApplyProblemList(params).then(
            (res) => {
            this.loading = false;
            this.total = res.data.data.total;
            this.problemList = res.data.data.records;
            this.showPagination = true;
          },
          (err) => {
            this.loading = false;
            this.showPagination = true;
          }
        )
      },
      currentChange(page) {
        this.query.currentPage = page;
        this.pushRouter();
      },
      onPageSizeChange(pageSize) {
        this.query.limit = pageSize;
        this.pushRouter();
      },
      pushRouter(){
        this.$router.push({
          name: 'admin-group-apply-problem',
          query: this.query
        });
      },
      changeProblemProgress(pid,progress) {
        let data = {
            pid,
            progress
        }
        api.admin_changeGroupProblemApplyProgress(data).then((res) => {
            myMessage.success(this.$i18n.t('m.Update_Successfully'));
        });
      },
      goUserHome(username) {
        window.open('/user-home?username='+username);
      },
      goEdit(problemId){
        this.$router.push({
          name: 'admin-edit-problem',
          params: { problemId },
          query: {
            back: this.$route.fullPath,
          },
        });
      },
      goGroup(gid){
        window.open('/group/'+gid);
      }
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
  }
}
</script>