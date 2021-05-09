<template>
  <div>
    <div class="container">
      <div class="title-article" style="text-align: left">
        <h1 class="title" id="sharetitle">
          <span>{{ discussion.title }}</span>
        </h1>
        <div class="title-msg">
          <span>
            <a
              class="c999"
              @click="getInfoByUsername(discussion.uid, discussion.author)"
              :title="discussion.author"
            >
              <avatar
                :username="discussion.author"
                :inline="true"
                :size="26"
                color="#FFF"
                class="user-avatar"
                :src="discussion.avatar"
              ></avatar>
              <span class="user-name">{{ discussion.author }}</span>
            </a>
          </span>
          <span
            class="role-root role"
            title="Super Administrator"
            v-if="discussion.role == 'root'"
            >SPA</span
          >
          <span
            class="role-admin role"
            title="Administrator"
            v-if="discussion.role == 'admin'"
            >ADM</span
          >
          <span class="c999" style="padding:0 6px;"
            ><i class="el-icon-folder-opened"></i
            ><a
              class="c999"
              @click="toAllDiscussionByCid(discussion.categoryId)"
            >
              分类：{{ discussion.categoryName }}</a
            ></span
          >
          <span class="c999"
            ><i class="fa fa-thumbs-o-up"></i
            ><span> 点赞：{{ discussion.likeNum }}</span></span
          >
          <span class="c999"
            ><i class="fa fa-eye"></i
            ><span> 浏览：{{ discussion.viewNum }}</span></span
          >

          <a href="javascript:void(0);" class="report" title="举报"
            ><i class="fa fa-envira"></i><span>举报</span></a
          >
          <a
            @click="toLikeDiscussion(discussion.id, true)"
            class="like"
            title="点赞"
            v-if="!discussion.hasLike"
          >
            <i class="fa fa-thumbs-o-up"></i> <span>点赞</span></a
          >
          <a
            @click="toLikeDiscussion(discussion.id, false)"
            class="like"
            title="已点赞"
            v-else
          >
            <i class="fa fa-thumbs-up"></i> <span>已点赞</span></a
          >

          <span>
            <i class="fa fa-clock-o"> 最后修改于：</i>
            <span>{{ discussion.gmtModified | localtime }}</span>
          </span>
        </div>
      </div>
      <div class="body-article">
        <div
          class="markdown-body"
          v-html="contentHtml"
          v-katex
          v-highlight
        ></div>
      </div>
    </div>
    <comment :did="$route.params.discussionID"></comment>
  </div>
</template>

<script>
import comment from '@/components/oj/comment/comment';
import api from '@/common/api';
import myMessage from '@/common/message';
import { addCodeBtn } from '@/common/codeblock';
import Avatar from 'vue-avatar';
import { mapGetters, mapActions } from 'vuex';

export default {
  components: {
    comment,
    Avatar,
  },
  data() {
    return {
      discussion: {
        author: 'HOJ',
        avatar: '',
      },
      query: {
        currentPage: 1,
        did: null,
      },
      discussionID: 0,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    ...mapActions(['changeDomTitle']),
    init() {
      this.routeName = this.$route.name;
      this.discussionID = this.$route.params.discussionID || '';

      api.getDiscussion(this.discussionID).then((res) => {
        this.discussion = res.data.data;
        this.changeDomTitle({ title: this.discussion.title });
        this.$nextTick((_) => {
          addCodeBtn();
        });
      });
    },

    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },

    toAllDiscussionByCid(cid) {
      this.$router.push({
        path: '/discussion',
        query: { cid },
      });
    },

    toLikeDiscussion(did, toLike) {
      api.toLikeDiscussion(did, toLike).then((res) => {
        myMessage.success(res.data.msg);
        if (toLike) {
          this.discussion.likeNum++;
          this.discussion.hasLike = true;
        } else {
          this.discussion.likeNum--;
          this.discussion.hasLike = false;
        }
      });
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'isAdminRole']),
    contentHtml() {
      if (this.discussion.content) {
        return this.$markDown.render(this.discussion.content);
      }
    },
  },
  watch: {
    isAuthenticated(newVal, oldVal) {
      if (newVal != oldVal) {
        this.init();
      }
    },
  },
};
</script>
<style scoped>
.container {
  box-sizing: border-box;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
}
.title-article {
  background: #fff;
  overflow: hidden;
  padding: 10px 20px;
  position: relative;
  text-align: center;
}
.title-article h1.title {
  font-size: 25px;
  font-weight: 600;
  color: #34495e;
  padding: 0 0 10px;
  width: 80%;
  line-height: 32px;
  word-break: break-all;
}
.title-article .title-msg {
  margin-bottom: 0px;
  font-size: 12px;
  color: #999;
}
.title-article .title-msg span {
  margin-right: 3px;
}
.title-article .title-msg span a.c999 {
  color: #999 !important;
}
.title-article .title-msg span a.c999:hover {
  color: #007bff !important;
  text-decoration: none;
}
.user-avatar {
  vertical-align: middle;
}
.user-name {
  margin: 0 0.25rem !important;
}
.title-article .title-msg a.report {
  position: absolute;
  top: 30px;
  right: 5px;
  color: #4caf50 !important;
  font-weight: bold;
  font-size: 14px;
}
.title-article .title-msg a.like {
  position: absolute;
  top: 30px;
  right: 60px;
  color: #ff6700 !important;
  font-weight: bold;
  font-size: 14px;
}
@media screen and (max-width: 768px) {
  .title-article .title-msg a.report {
    top: 50px !important;
    right: 12px !important;
  }
  .title-article .title-msg a.like {
    top: 24px !important;
    right: 12px !important;
  }
}

.body-article {
  background: #fff;
  overflow: hidden;
  width: 100%;
  padding: 20px 20px;
  text-align: left;
  font-size: 14px;
  line-height: 1.6;
}
</style>
