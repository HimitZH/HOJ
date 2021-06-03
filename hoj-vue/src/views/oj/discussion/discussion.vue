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
            ><i class="el-icon-folder-opened"> 分类：</i
            ><a
              class="c999"
              @click="toAllDiscussionByCid(discussion.categoryId)"
              >{{ discussion.categoryName }}</a
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

          <a @click="showReportDialog = true" class="report" title="举报"
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
            <i class="fa fa-clock-o"> 创建时间：</i>
            <span>
              <el-tooltip
                :content="discussion.gmtCreate | localtime"
                placement="top"
              >
                <span>{{ discussion.gmtCreate | fromNow }}</span>
              </el-tooltip>
            </span>
          </span>

          <span style="padding:0 6px;" v-show="userInfo.uid == discussion.uid"
            ><a style="color:#8fb0c9" @click="showEditDiscussionDialog = true"
              ><i class="el-icon-edit-outline"> 编辑</i></a
            ></span
          >
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
    <el-dialog title="举报" :visible.sync="showReportDialog" width="350px">
      <el-form label-position="top" :model="report">
        <el-form-item label="标签" required>
          <el-checkbox-group v-model="report.tagList">
            <el-checkbox label="垃圾广告"></el-checkbox>
            <el-checkbox label="违法违规"></el-checkbox>
            <el-checkbox label="色情低俗"></el-checkbox>
            <el-checkbox label="赌博诈骗"></el-checkbox>
            <el-checkbox label="恶意骂战"></el-checkbox>
            <el-checkbox label="恶意抄袭"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="理由" required>
          <el-input
            type="textarea"
            v-model="report.content"
            placeholder="请写下举报的理由"
            maxlength="200"
            show-word-limit
            :rows="4"
          >
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger" @click.native="showReportDialog = false"
          >取消</el-button
        >
        <el-button type="primary" @click.native="submitReport">提交</el-button>
      </span>
    </el-dialog>

    <!--编辑讨论对话框-->
    <el-dialog
      :title="discussionDialogTitle"
      :visible.sync="showEditDiscussionDialog"
      :fullscreen="true"
      @open="onOpenEditDialog"
    >
      <el-form label-position="top" :model="discussion">
        <el-form-item label="讨论标题" required>
          <el-input
            v-model="discussion.title"
            placeholder="请输入讨论标题"
            class="title-input"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="讨论简介" required>
          <el-input
            v-model="discussion.description"
            placeholder="请输入讨论简介"
            class="title-input"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="讨论分类" required>
          <el-select
            v-model="discussion.categoryId"
            placeholder="请选择"
            disabled
          >
            <el-option
              :label="discussion.categoryName"
              :value="discussion.categoryId"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否置顶" required v-if="isAdminRole">
          <el-switch v-model="discussion.topPriority"> </el-switch>
        </el-form-item>
        <el-form-item label="讨论内容" required>
          <Editor :value.sync="discussion.content"></Editor>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button
          type="danger"
          @click.native="showEditDiscussionDialog = false"
          >取消</el-button
        >
        <el-button type="primary" @click.native="submitDiscussion"
          >发布</el-button
        >
      </span>
    </el-dialog>
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
import Editor from '@/components/admin/Editor.vue';

export default {
  components: {
    comment,
    Avatar,
    Editor,
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
      showEditDiscussionDialog: false,
      discussionDialogTitle: 'Edit Discussion',
      showReportDialog: false,
      report: {
        tagList: [],
        content: '',
      },
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
      if (!this.isAuthenticated) {
        myMessage.warning('请先登录');
        return;
      }
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
    submitDiscussion() {
      // 默认为题目的讨论添加题号格式
      let discussion = Object.assign({}, this.discussion);
      // 不要影响动态数据
      delete discussion.viewNum;
      delete discussion.likeNum;
      api.updateDiscussion(discussion).then((res) => {
        myMessage.success(res.data.msg);
        this.showEditDiscussionDialog = false;
        this.init();
      });
    },
    submitReport() {
      if (!this.isAuthenticated) {
        myMessage.warning('请先登录');
        return;
      }
      if (this.report.tagList.length == 0 && !this.report.content) {
        myMessage.warning('举报标签和理由不能都为空');
        return;
      }
      var reportMsg = '';
      for (let i = 0; i < this.report.tagList.length; i++) {
        reportMsg += '#' + this.report.tagList[i] + '# ';
      }
      reportMsg += this.report.content;
      let discussionReport = {
        reporter: this.userInfo.username,
        content: reportMsg,
        did: this.discussionID,
      };
      api.toReportDiscussion(discussionReport).then((res) => {
        myMessage.success(res.data.msg);
        this.showReportDialog = false;
      });
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'isAdminRole', 'userInfo']),
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
/deep/ .el-dialog__body {
  padding: 0px 20px;
}
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
