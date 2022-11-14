<template>
  <el-card>
    <div class="filter-row">
      <el-row>
        <el-col :md="5" :xs="24">
          <span class="title">{{
            this.problemID
              ? this.problemID + ' ' + $t('m.Problem_Discussion')
              : $t('m.Group_Discussion')
          }}</span>
        </el-col>
        <el-col :md="15" :xs="24">
          <el-button
            type="primary"
            size="small"
            @click="goCreateDiscussion"
            icon="el-icon-plus"
          >
            {{
              this.problemID ? $t('m.Post_problem_discussion') : $t('m.Create')
            }}</el-button
          >
          <el-button
            v-if="problemID"
            type="success"
            @click="goGroupAllDiscussion"
            size="small"
            icon="el-icon-back"
            ><i class="el-icon-s-home"> {{ $t('m.Group_Discussion') }}</i>
          </el-button>
          <el-button
            v-if="isSuperAdmin || isGroupAdmin"
            :type="adminPage ? 'warning' : 'success'"
            size="small"
            @click="handleAdminPage"
            :icon="adminPage ? 'el-icon-back' : 'el-icon-s-opportunity'"
            >{{
              adminPage ? $t('m.Back') : $t('m.Discussion_Admin')
            }}</el-button
          >
        </el-col>
      </el-row>
    </div>
    <div v-if="!adminPage">
      <el-empty
        v-if="discussionList.length == 0"
        :description="$t('m.No_Discussion')"
      ></el-empty>
      <div
        class="title-article"
        v-for="(discussion, index) in discussionList"
        :key="index"
      >
        <el-card
          shadow="always"
          class="list-card"
          :body-style="{ padding: '0px' }"
        >
          <span class="svg-top" v-if="discussion.topPriority">
            <svg
              t="1620283436433"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="10095"
              width="48"
              height="48"
            >
              <path
                d="M989.9222626666667 444.3410103333334L580.1490096666668 34.909091333333336H119.41107066666666l870.511192 870.596525V444.3410103333334z"
                fill="#F44336"
                p-id="10096"
              ></path>
              <path
                d="M621.3675956666667 219.39846433333332l-43.832889-43.770828-126.663111 126.841535-32.826182-32.780929 126.663112-126.841535-43.734627-43.673859 26.739071-26.775273 120.396283 120.224324-26.741657 26.776565zM582.6055756666667 284.67587833333334c24.030384-24.065293 50.614303-36.636444 79.751758-37.71604 29.134869-1.07701 55.240404 9.903838 78.31402 32.945131 21.950061 21.91903 32.323232 46.86998 31.120808 74.851556s-13.257697 53.441939-36.167111 76.383677c-23.901091 23.934707-50.254869 36.406303-79.057455 37.41608-28.806465 1.012364-54.481455-9.739636-77.024969-32.252121-22.016-21.98497-32.689131-47.067798-32.014223-75.244606 0.672323-28.179394 12.365576-53.638465 35.077172-76.383677z m36.196849 32.57794c-14.921697 14.943677-23.517091 30.756202-25.783596 47.438869-2.269091 16.68396 2.880646 31.297939 15.441454 43.841939 12.825859 12.807758 27.34804 18.234182 43.566546 16.271515 16.217212-1.960081 31.985778-10.608485 47.303111-25.947798 15.976727-15.998707 25.133253-32.109899 27.46699-48.332283 2.333737-16.221091-2.813414-30.637253-15.441455-43.247192-12.827152-12.809051-27.67903-18.133333-44.558222-15.972848-16.879192 2.157899-32.877899 10.808889-47.994828 25.947798zM780.1276766666667 524.3048083333333l-53.476848 53.553131-32.726627-32.681374 153.400889-153.616808 52.858829 52.783839c38.213818 38.159515 41.146182 73.44097 8.79709 105.83402-15.71297 15.737535-34.076444 22.586182-55.086545 20.552404-21.012687-2.032485-39.97996-11.897535-56.905697-29.591273l-16.861091-16.833939z m74.572283-74.67701l-49.516606 49.586424 14.182141 14.161454c19.240081 19.211636 37.209212 20.455434 53.913859 3.728809 16.305131-16.329697 14.941091-34.002747-4.101172-53.016566L854.6999596666667 449.6277983333334z"
                fill="#FFFFFF"
                p-id="10097"
              ></path>
            </svg>
          </span>
          <h1 class="article-hlink">
            <a @click="goGroupDiscussion(discussion.id)">{{
              discussion.title
            }}</a>
            <el-button
              type="primary"
              size="mini"
              style="margin-left:5px;"
              v-if="discussion.pid"
              @click="goGroupProblemDetails(discussion.pid)"
              >{{ $t('m.Go_to_problem') }}</el-button
            >
          </h1>
          <a @click="goGroupDiscussion(discussion.id)" class="article-hlink2">
            <p>{{ discussion.description }}</p>
          </a>
          <div class="title-msg">
            <span>
              <a
                @click="getInfoByUsername(discussion.uid, discussion.author)"
                :title="discussion.author"
              >
                <avatar
                  :username="discussion.author"
                  :inline="true"
                  :size="24"
                  color="#FFF"
                  class="user-avatar"
                  :src="discussion.avatar"
                ></avatar>
                <span class="pl">{{ discussion.author }}</span></a
              >
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
            </span>

            <span class="pr pl"
              ><label class="fw"><i class="el-icon-chat-round"></i></label
              ><span>
                <span class="hidden-xs-only"> {{ $t('m.Comment') }}:</span>
                {{ discussion.commentNum }}</span
              ></span
            >

            <span class="pr"
              ><label class="fw"><i class="fa fa-thumbs-o-up"></i></label
              ><span>
                <span class="hidden-xs-only"> {{ $t('m.Likes') }}:</span>
                {{ discussion.likeNum }}</span
              ></span
            >
            <span class="pr"
              ><label class="fw"><i class="fa fa-eye"></i></label
              ><span>
                <span class="hidden-xs-only"> {{ $t('m.Views') }}:</span>
                {{ discussion.viewNum }}</span
              ></span
            >
            <span class="pr">
              <label class="fw">
                <i class="el-icon-folder-opened"></i>
                {{ cidMapName[discussion.categoryId] }}
              </label>
            </span>
            <span class="pr pl hidden-xs-only">
              <label class="fw"><i class="fa fa-clock-o"></i></label
              ><span>
                {{ $t('m.Release_Time') }}：<el-tooltip
                  :content="discussion.gmtCreate | localtime"
                  placement="top"
                >
                  <span>{{ discussion.gmtCreate | fromNow }}</span>
                </el-tooltip></span
              >
            </span>

            <el-dropdown
              style="float:right;"
              class="hidden-xs-only"
              v-show="
                isAuthenticated &&
                  (discussion.uid === userInfo.uid || isAdminRole)
              "
              @command="handleCommand"
            >
              <span class="el-dropdown-link">
                <i class="el-icon-more"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  icon="el-icon-edit-outline"
                  :command="'edit:' + index"
                  v-show="discussion.uid === userInfo.uid"
                  >{{ $t('m.Edit') }}</el-dropdown-item
                >
                <el-dropdown-item
                  icon="el-icon-delete"
                  :command="'delete:' + index"
                  v-show="discussion.uid === userInfo.uid || isAdminRole"
                  >{{ $t('m.Delete') }}</el-dropdown-item
                >
              </el-dropdown-menu>
            </el-dropdown>

            <div class="hidden-sm-and-up">
              <el-dropdown
                style="float:right;margin-top:10px; "
                v-show="
                  isAuthenticated &&
                    (discussion.uid === userInfo.uid || isAdminRole)
                "
                @command="handleCommand"
              >
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    icon="el-icon-edit-outline"
                    :command="'edit:' + index"
                    v-show="discussion.uid === userInfo.uid"
                    >{{ $t('m.Edit') }}</el-dropdown-item
                  >
                  <el-dropdown-item
                    icon="el-icon-delete"
                    :command="'delete:' + index"
                    v-show="discussion.uid === userInfo.uid || isAdminRole"
                    >{{ $t('m.Delete') }}</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown>

              <span class="pr" style="float:right;margin-top:10px; "
                ><label class="fw"><i class="fa fa-clock-o"></i></label
                ><span> {{ discussion.gmtCreate | localtime }}</span></span
              >
            </div>
          </div>
        </el-card>
      </div>
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="currentChange"
        :current.sync="currentPage"
        @on-page-size-change="onPageSizeChange"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </div>
    <DiscussionList
      v-if="adminPage"
      @currentChange="currentChange"
    ></DiscussionList>
    <el-dialog
      :title="title"
      :visible.sync="showEditDiscussionDialog"
      :fullscreen="true"
      @open="onOpenEditDialog"
      style="text-align: left"
    >
      <el-form label-position="top" :model="discussion">
        <el-form-item :label="$t('m.Discussion_title')" required>
          <el-input
            v-model="discussion.title"
            :placeholder="$t('m.Discussion_title')"
            class="title-input"
          >
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Discussion_Desc')" required>
          <el-input
            v-model="discussion.description"
            :placeholder="$t('m.Discussion_Desc')"
            class="title-input"
          >
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Discussion_Category')" required>
          <el-select v-model="discussion.categoryId" placeholder="---">
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('m.Discussion_top')"
          required
          v-if="isAdminRole"
        >
          <el-switch v-model="discussion.topPriority"> </el-switch>
        </el-form-item>
        <el-form-item :label="$t('m.Discussion_content')" required>
          <Editor :value.sync="discussion.content"></Editor>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button
          type="danger"
          @click.native="showEditDiscussionDialog = false"
          >{{ $t('m.Cancel') }}</el-button
        >
        <el-button type="primary" @click.native="submitDiscussion">{{
          $t('m.OK')
        }}</el-button>
      </span>
    </el-dialog>
  </el-card>
</template>

<script>
import Avatar from 'vue-avatar';
import { mapGetters } from 'vuex';
import Pagination from '@/components/oj/common/Pagination';
import DiscussionList from '@/components/oj/group/DiscussionList';
import Editor from '@/components/admin/Editor.vue';
import api from '@/common/api';
import mMessage from '@/common/message';
export default {
  name: 'GroupDiscussionList',
  components: {
    Pagination,
    DiscussionList,
    Avatar,
    Editor,
  },
  data() {
    return {
      total: 0,
      currentPage: 1,
      limit: 10,
      showEditDiscussionDialog: false,
      discussion: {
        id: null,
        pid: null,
        title: '',
        content: '',
        description: '',
        categoryId: '',
        topPriority: false,
        uid: '',
        author: '',
        avatar: '',
      },
      backupDiscussion: null,
      discussionList: [],
      categoryList: [],
      cidMapName:{},
      showTags: false,
      loading: false,
      adminPage: false,
      title: '',
      problemID: null,
      groupID: null,
    };
  },
  mounted() {
    api.getCategoryList().then(
      (res) => {
        this.categoryList = res.data.data;
        for (let i = 0; i < this.categoryList.length; i++) {
          this.cidMapName[this.categoryList[i].id] = this.categoryList[i].name;
        }
      },
      () => {}
    );
    this.init();
  },
  methods: {
    init() {
      this.problemID = this.$route.params.problemID;
      this.groupID = this.$route.params.groupID;
      this.getGroupDiscussionList();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    getGroupDiscussionList() {
      this.loading = true;
      api
        .getGroupDiscussionList(
          this.currentPage,
          this.limit,
          this.groupID,
          this.problemID
        )
        .then(
          (res) => {
            this.discussionList = res.data.data.records;
            this.total = res.data.data.total;
            this.loading = false;
          },
          (err) => {
            this.loading = false;
          }
        );
    },
    goGroupDiscussion(discussionId) {
      this.$router.push({
        name: 'GroupDiscussionDetails',
        params: {
          discussionID: discussionId,
          groupID: this.groupID,
        },
      });
    },
    goGroupAllDiscussion() {
      this.$router.push({
        name: 'GroupDiscussionList',
        params: {
          groupID: this.groupID,
        },
      });
    },
    goGroupProblemDetails(pid) {
      this.$router.push({
        name: 'GroupProblemDetails',
        params: {
          groupID: this.groupID,
          problemID: pid,
        },
      });
    },
    handleAdminPage() {
      this.adminPage = !this.adminPage;
    },
    onOpenEditDialog() {
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
    goCreateDiscussion() {
      this.showEditDiscussionDialog = true;
      this.title = this.$i18n.t('m.Create_Discussion');
    },
    submitDiscussion() {
      let discussion = Object.assign({}, this.discussion);

      if (this.problemID) {
        discussion.pid = this.problemID;
      }

      if (this.title == this.$i18n.t('m.Create_Discussion')) {
        if (discussion.pid) {
          discussion.title = '[' + discussion.pid + '] ' + discussion.title;
        }
        discussion.gid = this.$route.params.groupID;
        api.addGroupDiscussion(discussion).then((res) => {
          mMessage.success(this.$i18n.t('m.Post_successfully'));
          this.showEditDiscussionDialog = false;
          this.currentChange(1);
        });
      } else {
        api.updateGroupDiscussion(discussion).then((res) => {
          mMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.showEditDiscussionDialog = false;
          this.currentChange(1);
        });
      }
    },
    handleCommand(command) {
      let tmpArr = command.split(':');
      switch (tmpArr[0]) {
        case 'edit':
          this.title = this.$i18n.t('m.Edit_Discussion');
          this.discussion = Object.assign(
            {},
            this.discussionList[parseInt(tmpArr[1])]
          );
          this.showEditDiscussionDialog = true;
          break;
        case 'delete':
          this.$confirm(this.$i18n.t('m.Delete_Discussion_Tips'), 'Tips', {
            confirmButtonText: this.$i18n.t('m.OK'),
            cancelButtonText: this.$i18n.t('m.Cancel'),
            type: 'warning',
          }).then(() => {
            api
              .deleteGroupDiscussion(
                this.discussionList[parseInt(tmpArr[1])].id
              )
              .then((res) => {
                mMessage.success(this.$i18n.t('m.Delete_successfully'));
                this.currentChange(1);
              });
          });
          break;
      }
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },
  },
  computed: {
    ...mapGetters([
      'isAuthenticated',
      'isSuperAdmin',
      'isGroupAdmin',
      'userInfo',
      'isAdminRole',
    ]),
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal != oldVal) {
        this.init();
      }
    },
  },
};
</script>

<style scoped>
.title {
  font-size: 20px;
  vertical-align: middle;
  float: left;
}
.filter-row {
  margin-bottom: 5px;
  text-align: center;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-left: 5px;
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-left: 10px;
    margin-right: 10px;
  }
}
.list-card {
  border-radius: 6px;
  margin-bottom: 10px;
  padding: 15px;
  text-align: left;
  position: relative;
}
.list-card p {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.list-card .article-hlink {
  overflow: hidden;
  display: block;
}
.svg-top {
  position: absolute;
  top: 0px;
  right: 0px;
}
.article-hlink {
  margin: 0;
  padding: 0;
}
.article-hlink a {
  font-size: 16px;
  font-weight: 600;
  color: #34495e;
  margin-top: 5px;
}
a {
  color: #34495e;
  text-decoration: none;
}

.article-hlink2 p {
  margin-bottom: 10px;
  color: #888;
  font-size: 12px;
  margin: 0;
  padding: 0;
}
.title-article .title-msg {
  margin-top: 15px;
  font-size: 12px;
  color: #999 !important;
}
.title-article .title-msg a {
  color: #999;
  text-decoration: none;
}
.user-avatar {
  vertical-align: middle;
}

.title-article .title-msg span {
  margin-right: 3px;
}
.title-article .title-msg .pl {
  padding-left: 0.3rem !important;
}
.title-article .title-msg .pr {
  padding-right: 0.3rem !important;
}
</style>
