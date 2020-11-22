<template>
  <el-card shadow :padding="10">
    <div slot="header">
      <span class="panel-title" v-if="isContest">{{ title }}</span>
      <span v-else class="home-title panel-title">{{ title}}</span>
      <span style="float: right">
        <el-button
          v-if="listVisible"
          type="primary"
          @click="init"
          size="small"
          icon="el-icon-refresh"
          :loading="btnLoading"
          >Refresh</el-button
        >
        <el-button v-else type="primary" icon="el-icon-back" @click="goBack" size="small"
          >Back</el-button
        >
      </span>
    </div>
    <transition-group name="el-zoom-in-bottom">
      <div
        class="no-announcement"
        v-if="!announcements.length"
        key="no-announcement"
      >
        <p>No Announcements</p>
      </div>
      <template v-if="listVisible">
        <ul class="announcements-container" key="list">
          <li v-for="announcement in announcements" :key="announcement.title">
            <div class="flex-container">
                <div class="title">
                <a class="entry" @click="goAnnouncement(announcement)">
                  {{ announcement.title }}</a
                >
                </div>
               
                <div class="info">
                    <span class="date">
                
                      <i class="el-icon-edit"></i>
                    {{ announcement.create_time | localtime }}
          
                  </span>
                    <span class="creator">
                     
                      <i class="el-icon-user"></i>
                    {{ announcement.created_by.username }}
          
                    </span>
                </div>
              </div>
          </li>
        </ul>
        <Pagination
          v-if="!isContest"
          key="page"
          :total="total"
          :page-size="limit"
          @on-change="getAnnouncementList"
        >
        </Pagination>
      </template>

      <template v-else>
        <div
          v-katex
          v-html="announcement.content"
          key="content"
          class="content-container markdown-body"
        ></div>
      </template>
    </transition-group>
  </el-card>
</template>

<script>
import api from "@/common/api";
import Pagination from "@/components/common/Pagination";

export default {
  name: "Announcement",
  components: {
    Pagination,
  },
  data() {
    return {
      limit: 10,
      total: 10,
      btnLoading: false,
      announcements: [{
                "id": 6,
                "created_by": {
                    "id": 1,
                    "username": "root",
                    "real_name": null
                },
                "title": "\u8d5e\u52a9\u672c\u7ad9\u7684\u670d\u52a1\u5668 O(\u2229_\u2229)O\u8c22\u8c22",
                "content": "<p><img alt=\"IMG_0264.JPG\" src=\"/public/upload/4cd6928f19.jpg\" width=\"425\" height=\"346\" /><br /></p><p><br /></p><ul><li>\u8d5e\u52a9\u672c\u7ad9\u8d2d\u4e70\u66f4\u597d\u7684\u670d\u52a1\u5668\uff0c\u9632\u6b62\u90e8\u5206\u9898\u76ee\u548c\u7f16\u8bd1\u5668\u975e\u9884\u671f\u7684\u8d85\u65f6</li><li>\u8d5e\u52a9\u672c\u7ad9\u7684CDN</li></ul><p><span style=\"color: rgb(65, 140, 175);\">O(\u2229_\u2229)O\u8c22\u8c22</span></p>",
                "create_time": "2019-06-06T11:37:14.950009Z",
                "last_update_time": "2019-10-10T14:08:25.887019Z",
                "visible": true
            },
            {
                "id": 5,
                "created_by": {
                    "id": 1,
                    "username": "root",
                    "real_name": null
                },
                "title": "\u672c\u7ad9\u5c06\u5728\u672a\u6765\u4e00\u5468\u5185\u968f\u65f6\u8fc1\u79fb\u90e8\u7f72\u673a\u5668\uff0c\u5982\u6709\u9700\u6c42\u8bf7\u63d0\u524d\u7533\u8bf7 [5.3 \u5df2\u7ecf\u5b8c\u6210]",
                "content": "<p>RT</p><p><br /></p><p>Update:5.3 \u5df2\u7ecf\u5b8c\u6210</p>",
                "create_time": "2019-04-28T02:29:40.726247Z",
                "last_update_time": "2019-05-03T02:23:10.504187Z",
                "visible": true
            },],
      announcement: "",
      listVisible: true,
    };
  },
  mounted() {
    // this.init();
  },
  methods: {
    init() {
      if (this.isContest) {
        this.getContestAnnouncementList();
      } else {
        this.getAnnouncementList();
      }
    },
    getAnnouncementList(page = 1) {
      this.btnLoading = true;
      api.getAnnouncementList((page - 1) * this.limit, this.limit).then(
        (res) => {
          this.btnLoading = false;
          this.announcements = res.data.data.results;
          this.total = res.data.data.total;
        },
        () => {
          this.btnLoading = false;
        }
      );
    },
    getContestAnnouncementList() {
      this.btnLoading = true;
      api.getContestAnnouncementList(this.$route.params.contestID).then(
        (res) => {
          this.btnLoading = false;
          this.announcements = res.data.data;
        },
        () => {
          this.btnLoading = false;
        }
      );
    },
    goAnnouncement(announcement) {
      this.announcement = announcement;
      this.listVisible = false;
    },
    goBack() {
      this.listVisible = true;
      this.announcement = "";
    },
  },
  computed: {
    title() {
      if (this.listVisible) {
        return this.isContest ? "Contest Announcements" : "Announcements";
      } else {
        return this.announcement.title;
      }
    },
    isContest() {
      return !!this.$route.params.contestID;
    },
  },
};
</script>

<style scoped>
.announcements-container {
  margin-top: -10px;
  margin-bottom: 10px;
}
.announcements-container li {
  padding-top: 15px;
  list-style: none;
  padding-bottom: 15px;
  margin-left: 20px;
  margin-top:10px;
  font-size: 16px;
  border: 1px solid rgba(187, 187, 187, 0.5);
  border-left: 2px solid #409EFF;
}
/* .announcements-container li:last-child {
  border-bottom: none;
} */
.flex-container{
  text-align: center;
}
.flex-container .info{
  margin-top: 5px;
}

.flex-container .title .entry {
  color: #495060;
  font-style: oblique;
}
.flex-container .title a:hover {
  color: #2d8cf0;
  border-bottom: 1px solid #2d8cf0;
}
.creator {
  width: 200px;
  text-align: center;
}
.date {
  width: 200px;
  text-align: center;
  margin-right: 5px;
}

.content-container {
  padding: 0 20px 20px 20px;
}

.no-announcement {
  text-align: center;
  font-size: 16px;
}

.announcement-animate-enter-active {
  animation: fadeIn 1s;
}
ul{
  list-style-type:none;
  padding-inline-start:0px;
}
</style>
