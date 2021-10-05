<template>
  <div class="msg-wrap" v-loading="loading">
    <h3 class="msg-list-header">
      <span class="ft">{{ $t('m.' + route_name) }}</span>
      <span class="fr"
        >{{ $t('m.Msg_Total') + ' ' + total + ' ' + $t('m.Msg_Messages') }}
        <span class="clear-all" @click="deleteMsg()">{{
          $t('m.Clean_All')
        }}</span></span
      >
    </h3>
    <template v-if="dataList.length > 0">
      <el-card class="box-card" v-for="(item, index) in dataList" :key="index">
        <div class="msg-list-item">
          <span class="svg-lt" v-if="!item.state">
            <svg
              t="1633158277197"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="4745"
              width="32"
              height="32"
            >
              <path
                d="M512 322c-104.92 0-190 85.08-190 190s85.08 190 190 190 190-85.06 190-190-85.08-190-190-190z"
                p-id="4746"
                fill="#d81e06"
              ></path>
            </svg>
          </span>
          <div class="top">
            <span class="title">{{ item.title }}</span>
            <span class="extra"
              ><el-tooltip
                :content="item.gmtCreate | localtime"
                placement="top"
              >
                <span>&nbsp;{{ item.gmtCreate | fromNow }}</span>
              </el-tooltip></span
            >
            <span class="extra delete"
              ><i class="el-icon-delete" @click="deleteMsg(item.id)">
                {{ $t('m.Delete') }}</i
              ></span
            >
          </div>

          <div class="bottom">
            <span
              class="content markdown-body"
              v-highlight
              v-html="$markDown.render(item.content)"
            >
            </span>
          </div>
        </div>
      </el-card>
    </template>
    <template v-else
      ><el-empty :description="$t('m.No_Data')"></el-empty>
    </template>
    <Pagination
      :total="total"
      :page-size="query.limit"
      @on-change="changeRoute"
      :current.sync="query.currentPage"
    ></Pagination>
  </div>
</template>
<script>
import Pagination from '@/components/oj/common/Pagination';
import api from '@/common/api';
import myMessage from '@/common/message';
export default {
  components: { Pagination },
  data() {
    return {
      dataList: [],
      loading: false,
      total: 0,
      query: { limit: 8, currentPage: 1 },
      route_name: 'SysMsg',
    };
  },
  created() {
    this.route_name = this.$route.name;
  },
  mounted() {
    let query = this.$route.query;
    this.query.currentPage = parseInt(query.currentPage) || 1;
    if (this.query.currentPage < 1) {
      this.query.currentPage = 1;
    }
    this.getMsgList();
  },
  methods: {
    getMsgList() {
      let queryParams = Object.assign({}, this.query);
      this.loading = true;
      api.getMsgList(this.route_name, queryParams).then(
        (res) => {
          this.total = res.data.data.total;
          this.dataList = res.data.data.records;
          this.loading = false;
          this.substractUnreadMsgNum();
        },
        (err) => {
          this.loading = false;
        }
      );
    },
    changeRoute(page) {
      this.query.currentPage = page;
      this.getMsgList();
    },
    goMsgSourceUrl(url) {
      this.$router.push({
        path: url,
      });
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },
    deleteMsg(id = undefined) {
      this.$confirm(this.$i18n.t('m.Delete_Msg_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        api.cleanMsg(this.route_name, id).then((res) => {
          myMessage.success(this.$i18n.t('m.Delete_successfully'));
          this.getMsgList();
        });
      });
    },
    substractUnreadMsgNum() {
      let countName;
      switch (this.route_name) {
        case 'SysMsg':
          countName = 'sys';
          break;
        case 'MineMsg':
          countName = 'mine';
          break;
      }
      let needSubstractMsg = {
        name: countName,
        num: this.limit,
      };
      this.$store.dispatch('substractUnreadMessageCount', needSubstractMsg);
    },
  },
};
</script>
<style scoped>
.box-card {
  margin-bottom: 15px;
  position: relative;
}
.clear-all {
  cursor: pointer;
  color: #409eff;
}
.clear-all:hover {
  color: red;
  font-weight: bolder;
}
.msg-wrap {
  padding: 20px;
  padding-top: 0px;
  overflow: hidden;
}
@media only screen and (max-width: 767px) {
  .msg-wrap {
    padding: 0px;
  }
}
.msg-list-header {
  height: 35px;
  border-bottom: 3px solid #eff3f5;
  position: relative;
  top: -10px;
}
.svg-lt {
  position: absolute;
  top: 0px;
  right: 0px;
}
.fl {
  float: left;
}
.fr {
  float: right;
}
.msg-list-item {
  line-height: 30px;
}
.title {
  color: #333;
  font-weight: 700;
  font-size: 14px;
}
.extra {
  color: #999;
  font-size: 12px;
  line-height: 22px;
  margin: 0 8px;
}
.bottom {
  color: #666;
  padding-left: 8px;
}
.text {
  word-break: break-word;
}
.delete:hover {
  cursor: pointer;
  color: red;
  font-weight: bolder;
}
</style>
