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
          <span
            @click="getInfoByUsername(item.senderId, item.senderUsername)"
            style="cursor: pointer;"
          >
            <avatar
              :username="item.senderUsername"
              :inline="true"
              :size="40"
              color="#FFF"
              :src="item.senderAvatar"
              :title="item.senderUsername"
            ></avatar>
          </span>
          <div class="title">
            <div>
              <span
                style="margin-right:3px;"
                class="user-name"
                @click="getInfoByUsername(item.senderId, item.senderUsername)"
                :title="item.senderUsername"
                >{{ item.senderUsername }}</span
              >
              <span class="msg-action">
                {{ $t('m.Action_' + item.action) }}
              </span>
            </div>
            <div @click="goMsgSourceUrl(item.url)" style="cursor: pointer;">
              <div
                class="content"
                v-if="item.sourceContent != null"
                v-html="item.sourceContent"
              ></div>

              <div
                class="orginal-reply"
                v-if="item.quoteContent != null"
                v-html="item.quoteContent"
              ></div>
            </div>
            <div class="extra-info">
              <span
                ><i class="el-icon-time">
                  <el-tooltip
                    :content="item.gmtCreate | localtime"
                    placement="top"
                  >
                    <span>&nbsp;{{ item.gmtCreate | fromNow }}</span>
                  </el-tooltip></i
                ></span
              >
              <span class="delete" @click="deleteMsg(item.id)"
                ><i class="el-icon-delete"> {{ $t('m.Delete') }}</i></span
              >
            </div>
          </div>
        </div>
        <div class="link-discussion">
          <span
            >{{
              item.sourceType == 'Discussion'
                ? $t('m.From_Discussion_Post')
                : $t('m.From_the_Contest')
            }}
            <span class="title" @click="goMsgSourceUrl(item.url)"
              >“{{ item.sourceTitle }}”</span
            ></span
          >
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
import Avatar from 'vue-avatar';
import api from '@/common/api';
import myMessage from '@/common/message';
import Pagination from '@/components/oj/common/Pagination';
export default {
  components: { Avatar, Pagination },
  data() {
    return {
      dataList: [],
      query: {
        currentPage: 1,
        limit: 6,
      },
      loading: false,
      total: 0,
      route_name: 'DiscussMsg',
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
        case 'DiscussMsg':
          countName = 'comment';
          break;
        case 'ReplyMsg':
          countName = 'reply';
          break;
        case 'LikeMsg':
          countName = 'like';
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
  display: flex;
}
.msg-list-item .title {
  color: #99a;
  font-size: 16px;
  margin-left: 13px;
}
.msg-list-item .title .content {
  color: #222;
  word-break: break-word;
  margin: 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 20px;
  max-height: 2.6em;
}
.user-name {
  color: #666;
  font-weight: bold;
}
.user-name:hover {
  cursor: pointer;
  color: #409eff;
}
.msg-action {
  font-size: 16px;
  margin-left: 5px;
}

.msg-list-item .orginal-reply {
  color: #999;
  border-left: 2px solid #e7e7e7;
  margin: 8px 0 5px;
  padding-left: 4px;
  font-size: 12px;
  line-height: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 12px;
  line-height: 16px;
  max-height: 2.6em;
}

.msg-list-item .extra-info {
  color: #999;
  font-size: 12px;
  line-height: 30px;
}
.msg-list-item .extra-info span {
  margin-right: 10px;
}
.msg-list-item .extra-info .delete:hover {
  cursor: pointer;
  color: red;
}
.link-discussion {
  color: #999;
  font-size: 15px;
  text-align: center;
}
.link-discussion .title {
  color: #409eff;
  font-weight: 700;
  cursor: pointer;
}

@media only screen and (max-width: 767px) {
  .link-discussion {
    text-align: left;
  }
  .msg-action {
    font-size: 13px;
    margin-left: 0px;
    display: block;
  }
  .msg-list-item .title .content {
    margin-left: -47px;
  }
  .msg-list-item .extra-info {
    margin-left: -47px;
  }
  .msg-list-item .orginal-reply {
    margin-left: -47px;
  }
}
</style>
