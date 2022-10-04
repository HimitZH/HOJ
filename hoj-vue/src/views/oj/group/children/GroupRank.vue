 <template>
    <div>
    <el-card :padding="10">
        <div style="text-align: center;">
            <span>
            <el-input
            :placeholder="$t('m.Rank_Search_Placeholder')"
            v-model="query.searchUser"
            @keyup.enter.native="filterByUserOrType"
            >
            <el-button
                slot="append"
                icon="el-icon-search"
                class="search-btn"
                @click="filterByUserOrType"
            ></el-button>
            </el-input>
            </span>
        </div>
        <div class="swtich-type">
            <el-switch
                v-model="query.type"
                :active-value="0"
                :inactive-value="1"
                :active-text="$t('m.Group_ACM_Rank_Type')"
                :inactive-text="$t('m.Group_OI_Rank_Type')"
                @change="filterByUserOrType">
            </el-switch>
        </div>
    </el-card>
    <vxe-table
        :data="dataRank"
        :loading="loadingTable"
        align="center"
        highlight-hover-row
        :seq-config="{ seqMethod }"
        auto-resize
        style="font-weight: 500;"
      >
        <vxe-table-column type="seq" min-width="50"></vxe-table-column>
        <vxe-table-column
          field="username"
          :title="$t('m.User')"
          min-width="200"
          show-overflow
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row.username"
              :inline="true"
              :size="25"
              color="#FFF"
              :src="row.avatar"
              class="user-avatar"
            ></avatar>
            <a
              @click="getInfoByUsername(row.uid, row.username)"
              style="color:#2d8cf0;"
              >{{ row.username }}</a
            >
            <span style="margin-left:2px" v-if="row.titleName">
              <el-tag effect="dark" size="small" :color="row.titleColor">
                {{ row.titleName }}
              </el-tag>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="nickname"
          :title="$t('m.Nickname')"
          width="160"
        >
          <template v-slot="{ row }">
            <el-tag
              effect="plain"
              size="small"
              v-if="row.nickname"
              :type="nicknameColor(row.nickname)"
            >
              {{ row.nickname }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column field="ac" :title="$t('m.AC')" min-width="80">
          <template v-slot="{ row }">
            <span>
              <a
                @click="goUserACStatus(row.username)"
                style="color:rgb(87, 163, 243);"
                >{{ row.ac }}</a
              >
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Total')" min-width="100" field="total">
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Score')" min-width="80">
          <template v-slot="{ row }">
            <span>{{ row.score }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Rating')" min-width="80">
          <template v-slot="{ row }">
            <span>{{ getACRate(row.ac, row.total) }}</span>
          </template>
        </vxe-table-column>
    </vxe-table>

    <Pagination
        :total="total"
        :page-size.sync="query.limit"
        :current.sync="query.page"
        @on-change="currentChange"
        show-sizer
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
    ></Pagination>
    </div>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import { mapGetters } from 'vuex';
import Avatar from 'vue-avatar';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'group-rank',
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      query:{
        page: 1,
        limit: 30,
        searchUser: null,
        gid:null,
        type:0,
      },
      total: 0,
      loadingTable: false,
      dataRank: [],
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init(){
      let route = this.$route.query;
      this.query.searchUser = route.searchUser || '';
      this.query.gid = this.$route.params.groupID;
      this.query.page = route.page || 1;
      this.query.limit = route.limit || 30;
      this.query.type = route.type || 0;
      this.getRankData();
    },
    onPageSizeChange(pageSize) {
      this.query.limit = pageSize;
      this.handleRouter();
    },
    currentChange(page) {
      this.query.page = page;
      this.handleRouter();
    },
    filterByUserOrType() {
      this.query.page = 1;
      this.handleRouter();
    },
    handleRouter(){
       this.$router.push({
        path: this.$route.path,
        query: this.query,
      });
    },
    getRankData() {
      this.loadingTable = true;
      api
        .getGroupRank(this.query.page, this.query.limit, this.query.gid, this.query.type, this.query.searchUser)
        .then((res) => {
          this.loadingTable = false;
          this.total = res.data.data.total;
          this.dataRank = res.data.data.records;
        })
        .catch(() => {
          this.loadingTable = false;
        });
    },
    seqMethod({ rowIndex }) {
      return this.query.limit * (this.query.page - 1) + rowIndex + 1;
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },
    goUserACStatus(username) {
      this.$router.push({
        name: 'GroupSubmissionList',
        query: {
          username,
          status: 0
        },
      });
    },
    getACRate(ac, total) {
      return utils.getACRate(ac, total);
    },
    nicknameColor(nickname) {
      let typeArr = ['', 'success', 'info', 'danger', 'warning'];
      let index = nickname.length % 5;
      return typeArr[index];
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'userInfo']),
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
  }
};
</script>

<style scoped>
.swtich-type{
    float: right;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
  .swtich-type{
    margin-top:10px;
    margin-bottom: 10px;
    float:none;
    text-align: center;
  } 
}
@media screen and (min-width: 768px) {
  .el-input-group {
    width: 50%;
  }
}

@media screen and (min-width: 1050px) {
  .el-input-group {
    width: 30%;
  }
}
</style>
<style>
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
.search-btn {
  color: #fff !important;
  background-color: #409eff !important;
  border-color: #409eff !important;
}
</style>
