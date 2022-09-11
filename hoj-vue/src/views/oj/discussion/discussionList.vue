<template>
  <div class="container">
    <el-row :gutter="20">
      <el-col
        :md="18"
        :xs="24"
        v-loading="loading.discussion"
      >
        <div class="discussion-header">
          <span style="padding: 16px;float:left;">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <template v-if="currentCategory">
                <el-breadcrumb-item :to="{ name: routeName, query: null }">
                  {{ query.onlyMine ? $t('m.Mine') : ''
                  }}{{ $t('m.All') }}</el-breadcrumb-item>
                <el-breadcrumb-item>{{ currentCategory }} ( {{ total }} )</el-breadcrumb-item>
              </template>
              <template v-else>
                <el-breadcrumb-item :to="{ name: routeName }">{{ query.onlyMine ? $t('m.Mine') : '' }}{{ $t('m.All') }} (
                  {{ total }} )</el-breadcrumb-item>
              </template>
            </el-breadcrumb>
          </span>
          <span class="search">
            <vxe-input
              v-model="query.keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              @keyup.enter.native="handleQueryChange"
              @search-click="handleQueryChange"
            ></vxe-input>
          </span>
        </div>
        <template v-if="discussionList.length > 0">
          <div
            class="title-article"
            v-for="(discussion, index) in discussionList"
            :key="index"
          >
            <el-card
              shadow="hover"
              class="list-card"
            >
              <span
                class="svg-top"
                v-if="discussion.topPriority"
              >
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
                <a @click="toDiscussionDetail(discussion.id)">{{
                  discussion.title
                }}</a>
                <el-button
                  type="primary"
                  size="mini"
                  style="margin-left:5px;"
                  v-if="discussion.pid"
                  @click="
                    pushRouter(
                      null,
                      { problemID: discussion.pid },
                      'ProblemDetails'
                    )
                  "
                >{{ $t('m.Go_to_problem') }}</el-button>
              </h1>
              <a
                @click="toDiscussionDetail(discussion.id)"
                class="article-hlink2"
              >
                <p>{{ discussion.description }}</p>
              </a>
              <div class="title-msg">
                <span>
                  <a
                    @click="
                      getInfoByUsername(discussion.uid, discussion.author)
                    "
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
                    <span class="pl">{{ discussion.author }}</span>
                  </a>
                  <span
                    class="role-root role"
                    title="Super Administrator"
                    v-if="discussion.role == 'root'"
                  >SPA</span>
                  <span
                    class="role-admin role"
                    title="Administrator"
                    v-if="discussion.role == 'admin'"
                  >ADM</span>
                </span>

                <span class="pr pl"><label class="fw"><i class="el-icon-chat-round"></i></label><span>
                    <span class="hidden-xs-only"> {{ $t('m.Comment') }}:</span>
                    {{ discussion.commentNum }}</span></span>

                <span class="pr"><label class="fw"><i class="fa fa-thumbs-o-up"></i></label><span>
                    <span class="hidden-xs-only"> {{ $t('m.Likes') }}:</span>
                    {{ discussion.likeNum }}</span></span>
                <span class="pr"><label class="fw"><i class="fa fa-eye"></i></label><span>
                    <span class="hidden-xs-only"> {{ $t('m.Views') }}:</span>
                    {{ discussion.viewNum }}</span></span>
                <span class="pr"><label class="fw"><i class="el-icon-folder-opened"></i></label>
                  <a @click="
                      pushRouter(
                        {
                          cid: discussion.categoryId,
                          onlyMine: query.onlyMine,
                        },
                        { problemID: query.pid },
                        routeName
                      )
                    ">
                    {{ cidMapName[discussion.categoryId] }}</a>
                </span>

                <span class="pr pl hidden-xs-only">
                  <label class="fw"><i class="fa fa-clock-o"></i></label><span>
                    {{ $t('m.Release_Time') }}：<el-tooltip
                      :content="discussion.gmtCreate | localtime"
                      placement="top"
                    >
                      <span>{{ discussion.gmtCreate | fromNow }}</span>
                    </el-tooltip></span>
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
                    >{{ $t('m.Edit') }}</el-dropdown-item>
                    <el-dropdown-item
                      icon="el-icon-delete"
                      :command="'delete:' + index"
                      v-show="discussion.uid === userInfo.uid || isAdminRole"
                    >{{ $t('m.Delete') }}</el-dropdown-item>
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
                      >{{ $t('m.Edit') }}</el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-delete"
                        :command="'delete:' + index"
                        v-show="discussion.uid === userInfo.uid || isAdminRole"
                      >{{ $t('m.Delete') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>

                  <span
                    class="pr"
                    style="float:right;margin-top:10px; "
                  ><label class="fw"><i class="fa fa-clock-o"></i></label><span> {{ discussion.gmtCreate | localtime }}</span></span>
                </div>
              </div>
            </el-card>
          </div>
        </template>
        <template v-else>
          <el-empty :description="$t('m.No_Data')"></el-empty>
        </template>
        <Pagination
          :total="total"
          :page-size="query.limit"
          @on-change="changeRoute"
          :current.sync="query.currentPage"
        ></Pagination>
      </el-col>
      <el-col
        :md="6"
        :xs="24"
      >
        <el-button
          class="btn"
          type="primary"
          @click="toEditDiscussion"
          style="width: 100%;"
        ><i class="el-icon-edit">
            {{
              this.query.pid == ''
                ? $t('m.Post_discussion')
                : $t('m.Post_problem_discussion')
            }}</i>
        </el-button>
        <el-button
          v-if="isAuthenticated"
          class="btn"
          type="danger"
          @click="toOnlyMyDiscussion(!query.onlyMine)"
          style="width: 100%;margin-left:0;margin-top:10px"
        ><i class="el-icon-search">
            {{ query.onlyMine ? $t('m.All') : $t('m.Mine') }}</i>
        </el-button>
        <template v-if="this.query.pid">
          <el-button
            class="btn"
            type="success"
            @click="toAllDiscussion"
            style="width: 100%;margin-left:0;margin-top:10px"
          ><i class="el-icon-s-home"> {{ $t('m.General_discussion') }}</i>
          </el-button>

          <el-button
            class="btn"
            type="warning"
            @click="
              pushRouter(
                { onlyMine: query.onlyMine },
                { problemID: query.pid },
                'ProblemDetails'
              )
            "
            style="width: 100%;margin-left:0;margin-top:10px"
          ><i class="el-icon-back"> {{ $t('m.Return') }} ({{ query.pid }})</i>
          </el-button>
        </template>
        <div class="category-body">
          <h3 class="title-sidebar">
            <a @click="
                pushRouter(
                  { onlyMine: query.onlyMine },
                  { problemID: query.pid },
                  routeName
                )
              "><i class="el-icon-folder-opened"></i> {{ $t('m.Category') }}</a>
            <span
              style="float:right"
              v-if="isSuperAdmin"
            >
              <a @click="upsertCategoryList">
                <i
                  class="el-icon-edit"
                  v-if="isViewCategoryList"
                > {{$t('m.Edit')}}</i>
                <i
                  class="el-icon-check"
                  v-if="!isViewCategoryList"
                > {{$t('m.OK')}}</i>
              </a>
            </span>
          </h3>
          <template v-if="isViewCategoryList">
            <el-row v-loading="loading.category">
              <el-col
                :span="24"
                class="category-item"
                :title="category.name"
                v-for="(category, index) in categoryList"
                :key="index"
              >
                <a
                  @click="
                  pushRouter(
                    { cid: category.id, onlyMine: query.onlyMine },
                    { problemID: query.pid },
                    routeName
                  )
                "
                  style="display: block"
                >
                  {{ category.name }}
                  <span
                    class="el-icon-arrow-right"
                    style="float:right;font-weight: 600!important;"
                  ></span>
                </a>
              </el-col>
            </el-row>
          </template>
          <template v-else>
            <el-row>
              <el-col
                :span="24"
                :title="category.name"
                v-for="(category, index) in categoryList"
                :key="index"
                style="margin-top: 10px"
              >
                <template v-if="category.id != null">
                  <el-input
                  v-model="category.name"
                  size="small"
                ></el-input>
                </template>
                <template v-else>
                  <el-input
                  v-model="category.name"
                  style="width: 75%;"
                  size="small"
                ></el-input>
                  <el-button
                  type="danger"
                  style="margin-left: 5%;width: 20%;"
                  size="small"
                  icon="el-icon-delete"
                  @click="removeCategory(index)"
                ></el-button>
                </template>
              </el-col>
              <el-col :span="24">
                <el-button
                  type="primary"
                  style="margin-top: 10px;width: 100%;"
                  size="small"
                  @click="addNewCategory"
                ><i class="el-icon-plus"></i></el-button>
              </el-col>
            </el-row>
          </template>
        </div>
      </el-col>
    </el-row>
    <!--编辑讨论对话框-->
    <el-dialog
      :title="discussionDialogTitle"
      :visible.sync="showEditDiscussionDialog"
      :fullscreen="true"
      @open="onOpenEditDialog"
    >
      <el-form
        label-position="top"
        :model="discussion"
      >
        <el-form-item
          :label="$t('m.Discussion_title')"
          required
        >
          <el-input
            v-model="discussion.title"
            :placeholder="$t('m.Discussion_title')"
          >
          </el-input>
        </el-form-item>
        <el-form-item
          :label="$t('m.Discussion_Desc')"
          required
        >
          <el-input
            v-model="discussion.description"
            :placeholder="$t('m.Discussion_Desc')"
          >
          </el-input>
        </el-form-item>
        <el-form-item
          :label="$t('m.Discussion_Category')"
          required
        >
          <el-select
            v-model="discussion.categoryId"
            placeholder="---"
          >
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
        <el-form-item
          :label="$t('m.Discussion_content')"
          required
        >
          <Editor :value.sync="discussion.content"></Editor>
        </el-form-item>
      </el-form>
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          type="danger"
          @click.native="showEditDiscussionDialog = false"
        >{{ $t('m.Cancel') }}</el-button>
        <el-button
          type="primary"
          @click.native="submitDiscussion"
        >{{
          $t('m.OK')
        }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
import api from "@/common/api";
import myMessage from "@/common/message";
import { mapGetters, mapActions } from "vuex";
import "element-ui/lib/theme-chalk/display.css";
import Pagination from "@/components/oj/common/Pagination";
const Editor = () => import("@/components/admin/Editor.vue");
export default {
  components: {
    Avatar,
    Editor,
    Pagination,
  },
  data() {
    return {
      total: 0,
      showEditDiscussionDialog: false,
      discussion: {
        id: null,
        pid: null, // 题目id 为null表示不关联
        title: "",
        content: "",
        description: "",
        categoryId: "",
        topPriority: false,
        uid: "",
        author: "",
        avatar: "",
      },
      backupDiscussion: {}, // 临时记录
      // 对话框标题
      discussionDialogTitle: "",
      discussionList: [],
      categoryList: [],
      cidMapName: {},
      currentCategory: "",
      query: {
        keyword: "",
        cid: "",
        currentPage: 1,
        limit: 10,
        pid: "",
        onlyMine: false,
      },
      routeName: "",
      loading: {
        discussion: true,
        category: true,
      },
      isViewCategoryList: true,
    };
  },
  created() {
    this.routeName = this.$route.name;
    let query = this.$route.query;
    // 必须在这里先获取当前页码，不然分页组件的显示有误
    this.query.currentPage = parseInt(query.currentPage) || 1;
    if (this.query.currentPage < 1) {
      this.query.currentPage = 1;
    }
  },
  mounted() {
    this.discussionDialogTitle = this.$i18n.t("m.Edit_Discussion");
    this.loading.category = true;
    api.getCategoryList().then(
      (res) => {
        this.categoryList = res.data.data;
        for (let i = 0; i < this.categoryList.length; i++) {
          this.cidMapName[this.categoryList[i].id] = this.categoryList[i].name;
        }
        this.loading.category = false;
        this.init();
      },
      (err) => {
        this.loading.category = false;
      }
    );
  },
  methods: {
    ...mapActions(["changeDomTitle"]),
    init() {
      this.routeName = this.$route.name;
      let query = this.$route.query;
      this.query.keyword = query.keyword || "";
      this.query.cid = query.cid || "";
      this.query.pid = this.$route.params.problemID || "";
      this.query.onlyMine = query.onlyMine + "" == "true" ? true : false; // 统一换成字符串判断
      if (this.query.cid) {
        this.currentCategory = this.cidMapName[this.query.cid];
      } else {
        this.currentCategory = "";
      }
      if (this.query.pid) {
        this.discussion.pid = this.query.pid;
        this.changeDomTitle({ title: this.query.pid + " Discussion" });
      } else {
        this.discussion.pid = null;
      }
      this.getDiscussionList();
    },

    upsertCategoryList(){
      if(this.isViewCategoryList){
        this.isViewCategoryList = false;
        return;
      }else{
        this.isViewCategoryList = true;
      }
      this.loading.category = true;
      api.upsertCategoryList(this.categoryList).then(
      (res) => {
        this.categoryList = res.data.data;
        for (let i = 0; i < this.categoryList.length; i++) {
          this.cidMapName[this.categoryList[i].id] = this.categoryList[i].name;
        }
        this.loading.category = false;
      },
      (err) => {
        this.loading.category = false;
      }
      );
    },
    getDiscussionList() {
      let queryParams = Object.assign({}, this.query);
      this.loading.discussion = true;
      api.getDiscussionList(this.query.limit, queryParams).then(
        (res) => {
          this.total = res.data.data.total;
          this.discussionList = res.data.data.records;
          this.loading.discussion = false;
        },
        (err) => {
          this.loading.discussion = false;
        }
      );
    },

    changeRoute(page) {
      this.query.currentPage = page;
      this.pushRouter(
        this.query,
        { problemID: this.query.pid },
        this.routeName
      );
    },

    getInfoByUsername(uid, username) {
      this.$router.push({
        path: "/user-home",
        query: { uid, username },
      });
    },

    toEditDiscussion() {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t("m.Please_login_first"));
        this.$store.dispatch("changeModalStatus", { visible: true });
      } else {
        this.discussionDialogTitle = this.$i18n.t("m.Create_Discussion");
        if (this.backupDiscussion) {
          this.discussion = this.backupDiscussion;
          // 避免监听覆盖
          this.discussion.pid = this.query.pid || null;
        } else {
          this.discussion = {
            id: null,
            pid: this.query.pid || null,
            title: "",
            content: "",
            description: "",
            categoryId: "",
            topPriority: false,
            uid: "",
            author: "",
            avatar: "",
          };
        }
        this.showEditDiscussionDialog = true;
      }
    },

    handleQueryChange() {
      this.pushRouter(
        this.query,
        { problemID: this.query.pid },
        this.routeName
      );
    },
    toDiscussionDetail(discussionID) {
      this.$router.push({
        name: "DiscussionDetails",
        params: { discussionID: discussionID },
      });
    },

    toAllDiscussion() {
      this.$router.push({
        path: "/discussion",
      });
    },

    toOnlyMyDiscussion(onlyMine) {
      this.$router.push({
        path: this.$route.path,
        query: {
          onlyMine: onlyMine,
        },
      });
    },

    pushRouter(query, params, name) {
      this.$router.push({
        name: name,
        query: query,
        params: params,
      });
    },

    // 打开编辑对话框的回调
    onOpenEditDialog() {
      // todo 优化
      // 暂时解决 文本编辑器显示异常bug
      setTimeout(() => {
        if (document.createEvent) {
          let event = document.createEvent("HTMLEvents");
          event.initEvent("resize", true, true);
          window.dispatchEvent(event);
        } else if (document.createEventObject) {
          window.fireEvent("onresize");
        }
      }, 0);
    },

    submitDiscussion() {
      // 默认为题目的讨论添加题号格式
      let discussion = Object.assign({}, this.discussion);
      if (this.discussionDialogTitle == this.$i18n.t("m.Create_Discussion")) {
        if (discussion.pid) {
          discussion.title = "[" + discussion.pid + "] " + discussion.title;
        }
        api.addDiscussion(discussion).then((res) => {
          myMessage.success(this.$i18n.t("m.Post_successfully"));
          this.showEditDiscussionDialog = false;
          this.init();
        });
      } else {
        api.updateDiscussion(discussion).then((res) => {
          myMessage.success(this.$i18n.t("m.Update_Successfully"));
          this.showEditDiscussionDialog = false;
          this.init();
        });
      }
    },
    handleCommand(command) {
      let tmpArr = command.split(":");
      switch (tmpArr[0]) {
        case "edit":
          this.discussionDialogTitle = this.$i18n.t("m.Edit_Discussion");
          this.discussion = Object.assign(
            {},
            this.discussionList[parseInt(tmpArr[1])]
          );
          this.showEditDiscussionDialog = true;
          break;
        case "delete":
          this.$confirm(this.$i18n.t("m.Delete_Discussion_Tips"), "Tips", {
            confirmButtonText: this.$i18n.t("m.OK"),
            cancelButtonText: this.$i18n.t("m.Cancel"),
            type: "warning",
          }).then(() => {
            api
              .deleteDiscussion(this.discussionList[parseInt(tmpArr[1])].id)
              .then((res) => {
                myMessage.success(this.$i18n.t("m.Delete_successfully"));
                this.init();
              });
          });
          break;
      }
    },
    addNewCategory(){
      this.categoryList.push({
        id: null,
        name: ''
      });
    },
    removeCategory(index){
      this.categoryList.splice(index, 1);
    }
  },

  watch: {
    $route(newVal, oldVal) {
      if (newVal != oldVal) {
        this.init();
      }
    },
    discussion(newVal, oldVal) {
      if (
        this.discussionDialogTitle == this.$i18n.t("m.Create_Discussion") &&
        newVal != oldVal
      ) {
        this.backupDiscussion = this.discussion;
      }
    },
  },

  computed: {
    ...mapGetters([
      "isAuthenticated",
      "userInfo",
      "isAdminRole",
      "isSuperAdmin",
    ]),
  },
};
</script>
<style>
.role-root {
  background-color: #f9d681 !important;
  color: #ff503f !important;
  font-weight: 600;
}

.role-admin {
  background-color: #409eff !important;
  color: #fff !important;
}

.role {
  display: inline-block;
  font-size: 0.75rem;
  padding: 0.1875rem 0.25rem;
  line-height: 1;
}
</style>
<style scoped>
/deep/ .el-card__body {
  padding: 0 !important;
}
.discussion-header {
  background-color: #fff;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 10px;
  height: 41px;
}
.discussion-header .search {
  margin-top: 3px;
  margin-right: 6px;
  float: right;
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

.category-body {
  background: #fff;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 6px;
  overflow: hidden;
  margin-top: 12px;
}
.category-body .title-sidebar {
  border-bottom: 1px solid #eee;
  width: 100%;
  color: #34495e;
  font-size: 14px;
  font-weight: 600;
  padding-bottom: 10px;
  margin-bottom: 10px;
  overflow: hidden;
}
.category-body .title-sideba a {
  color: #34495e;
}
.category-body h3 {
  margin: 0;
  padding: 0;
}
.category-item {
  height: 30px;
  font-size: 14px;
  padding: 3px 10px;
  margin-bottom: 5px;
}
.category-item a {
  color: #34495e;
}
.category-item:hover {
  background-color: #eff3f5 !important;
  font-weight: bold;
  color: #222;
}
</style>
