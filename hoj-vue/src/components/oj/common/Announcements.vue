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
import Pagination from "@/components/oj/common/Pagination";

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
                "title": "测试标题",
                "content": "<p>\u7ef4\u4ed6\u5165\u6211\u5fc3\uff0c\u8d5b\u8fc7\u6d77\u6d1b\u56e0\uff0c\u5728\u8fd9\u4e2a\u5bd2\u51b7\u7684\u51ac\u5929\uff0c\u4e3a\u4ec0\u4e48\u4e0d\u6765\u4e00\u676f\u51b0\u723d\u67e0\u6aac\u8336\u5462\uff1f</p><p><img src=\"https://pic1.zhimg.com/d8db4bb6d8dfc727b6f0a7f12d7367a0_r.jpg?source=1940ef5c\" alt=\"\" /></p><h1>\u6bd4\u8d5b\u5956\u54c1\u8bf4\u660e:</h1><p>\u672c\u6b21\u6bd4\u8d5b\u5177\u6709\u4e30\u539a\u7684\u5956\u54c1 (\u91cd\u8981\u7684\u4e0d\u662f\u4ef7\u503c\uff0c\u800c\u662f<b>\u6b22\u4e50</b>\u4e0d\u662f\u4e48\uff1f) \uff0c\u5177\u4f53\u89c4\u5219\u5982\u4e0b\uff1a</p><p>\u4e00\u3001\u6240\u6709\u53c2\u8d5b\u9009\u624b\u5747\u83b7\u5f97<b>\u53c2\u8d5b\u7eaa\u5ff5\u5956</b>\u2014\u2014<b>\u9ea6\u65af\u5a01\u5c14\u5496\u5561\u4e00\u5305</b></p><p>\u4e8c\u3001\u672c\u6b21\u6bd4\u8d5b\u6392\u540d<b>\u7b2c\u4e00\u3001\u7b2c\u5341</b>\u7684\u9009\u624b\u5404\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u7ef4\u4ed6\u67e0\u6aac\u8336\u4e09\u74f6</b></p><p><span style=\"color: rgb(51, 51, 51);\">\u4e09\u3001\u672c\u6b21\u6bd4\u8d5b\u6392\u540d<b>\u7b2c\u4e8c\u3001\u7b2c\u5341\u4e00</b>\u7684\u9009\u624b\u5404\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u798f\u5efa\u9ad8\u7aef\u8336\u53f6\u4e09\u888b</b></span><br /></p><p>\u56db\u3001\u672c\u6b21\u6bd4\u8d5b<b>\u6700\u540e\u4e00\u4e2aAC</b>\u7684\u540c\u5b66\u5c06\u83b7\u5f97\u987d\u5f3a\u62fc\u640f\u5956\uff0c\u5956\u54c1\u795e\u79d8\u7684\u5f88\uff08ZXF\u8d5e\u52a9\uff09\uff0c<b>\u73b0\u573a\u63ed\u6653</b>\uff0c\u594b\u6597\u5427baby\u4eec.</p><p>\u4e94\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97<b>\u7b2c\u4e00\u540d</b>\u7684\u9009\u624b\u5c06\u83b7\u5f97ZYB\u5b66\u957f\u8d5e\u52a9\u7684<b>\u9ad8\u7aef\u8f6f\u76ae\u672c\u4e00\u4e2a</b></p><p><span style=\"color: rgb(51, 51, 51);\">\u516d\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97<b>\u7b2c\u4e8c\u3001\u4e09\u3001\u56db\u3001\u4e94\u540d</b>\u7684\u9009\u624b\u5c06\u6bcf\u4eba\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u8f6f\u76ae\u672c\u4e00\u4e2a</b></span><br /></p><p><span style=\"color: rgb(51, 51, 51);\"><span style=\"color: rgb(51, 51, 51);\">\u4e03\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97</span><span style=\"color: rgb(51, 51, 51);\"><b>\u7b2c\u516d\u3001\u4e03\u3001\u516b\u3001\u4e5d\u540d</b></span><span style=\"color: rgb(51, 51, 51);\">\u7684\u9009\u624b\u5c06\u6bcf\u4eba\u83b7\u5f97CGX\u8d5e\u52a9\u7684<b>\u53cc\u6c47\u8089\u5757\u738b\u7279\u7ea7\u706b\u817f\u80a0</b></span><span style=\"color: rgb(51, 51, 51);\"><b>\u4e00\u6839</b></span><br /></span></p><p>\u516b\u3001\u6bd4\u8d5b\u7ed3\u675f\u540e\uff0c<b>\u968f\u673a\u62bd\u53d6\u4e09\u540d\u540c\u5b66</b>\u9001<b>\u725b\u5ba2\u886c\u886b</b>\uff0c\u6ee1\u8db3\u4f60\u6c38\u8fdc\u62bd\u4e0d\u5230\u886c\u886b\u7684\u975e\u914b\u7684\u613f\u671b\uff01</p><p><b><u>\u4ee5\u4e0a\u89c4\u5219\u6700\u7ec8\u89e3\u91ca\u6743\u5f52\u521b\u65b0\u5b9e\u9a8c\u5ba4\u6240\u6709</u></b>\u3002</p><h1><font>\u7f5a\u65f6\u89c4\u5219\u548c\u8fd4\u56de\u7ed3\u679c\u89e3\u91ca:</font></h1><p><font>1.\u63d0\u4ea4\u663e\u793aAccepted\u5373\u4e3a\u901a\u8fc7,\u82e5\u663e\u793a\u5176\u4ed6\u7ed3\u679c\u5373\u8868\u793a\u9519\u8bef.\u9519\u8bef\u4e00\u6b21\u603b\u7f5a\u65f6+20\u5206\u949f,\u8bf7\u8c28\u614e\u63d0\u4ea4\uff01</font></p><p><font>2.\u5173\u4e8e\u8fd4\u56de\u7ed3\u679c\u7684\u89e3\u91ca:</font></p><p><font>Pending & Juding : You solution will be judged soon, please wait for result</font></p><p><font>Compile Error : Failed to compile your source code. Click on the link to see compiler&#039;s output.</font></p><p><font>Accepted : Congratulations. Your solution is correct.</font></p><p><font>Wrong Answer : Your program&#039;s output doesn&#039;t match judger&#039;s answer.</font></p><p><font>Runtime Error : Your program terminated abnormally. Possible reasons are: segment fault, divided by zero or exited with code other than 0.OrThe memory your program actually used has exceeded limit.</font></p><p><font>Time Limit Exceeded : The CPU time your program used has exceeded limit. Java has a triple time limit.</font></p><p><font>Memory Limit Exceeded : The memory your program actually used has exceeded limit.</font></p><p><font>System Error : Oops, something has gone wrong with the judger. Please report this to administrator.</font></p><h1><font>\u6bd4\u8d5b\u8981\u6c42:</font></h1><p><font>\u6bd4\u8d5b\u8fc7\u7a0b\u4e2d<b>\u4e0d\u8bb8\u8ba8\u8bba</b>\u4e0d\u8bb8\u4e0a\u5916\u7f51,\u6709\u95ee\u9898\u53ef\u4ee5\u4e3e\u624b!</font></p><p><font><b>\u9898\u76ee\u96be\u5ea6\u4e0e\u9898\u76ee\u987a\u5e8f\u65e0\u5173,\u8bf7\u5408\u7406\u5b89\u6392\u505a\u9898\u65f6\u95f4!</b></font></p>",
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
