<template>
  <div>
    <el-card class="contest">
      <div slot="header" class="clearfix title">
        <el-link @click="goContest" :underline="false">比赛标题</el-link>
      </div>
      <el-carousel
        indicator-position="outside"
        height="350px"
        :interval="interval"
      >
        <el-carousel-item v-for="(contest, index) in contests" :key="index">
          <div class="contest-info">
            <div class="contest-tags">
              <el-button type="primary" round size="mini" style="margin-top: 4px;"
                ><i class="fa fa-calendar"></i>
                {{ contest.beginTime | localtime }}
              </el-button>
              <el-button type="success" round size="mini" style="margin-top: 4px;"
                ><i class="fa fa-clock-o"></i>
                {{ contest.duration }}
              </el-button>
              <el-button type="warning" round size="mini" style="margin-top: 4px;"
                ><i class="fa fa-trophy"></i>
                {{ contest.type }}
              </el-button>
            </div>
            <div class="contest-description">
              <blockquote v-html="contest.description"></blockquote>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>
    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <Announcements></Announcements>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span class="panel-title home-title"
              >Other OnlineJudge Contest</span
            >
          </div>
          <vxe-table
            border="inner"
            stripe
            auto-resize
            :data="otherContests">
            <vxe-table-column field="oj" title="OJ" min-width="100"></vxe-table-column>
            <vxe-table-column field="title" title="Title" min-width="200"></vxe-table-column>
            <vxe-table-column field="beginTime" title="Begin Time" min-width="150">
              <template v-slot="{ row }" >
                <span>{{row.beginTime| localtime}}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="endTime" title="End Time" min-width="150">
              <template v-slot="{ row }" >
                <span>{{row.endTime| localtime}}</span>
              </template>
            </vxe-table-column>
          </vxe-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import time from "@/common/time";
import Announcements from "@/components/oj/common/Announcements.vue";
export default {
  name: "home",
  components: {
    Announcements,
  },
  data() {
    return {
      interval: 6000,
      otherContests: [
        {
          oj: "Codeforces",
          title:
            "Codeforces Round #680 (Div. 1, based on VK Cup 2020-2021 - Final)",
          beginTime: "2020-11-08T05:00:00Z",
          endTime: "2020-11-08T08:00:00Z",
        },
      ],
      contests: [
        {
          beginTime: "2020-11-08T05:00:00Z",
          duration: "5hours",
          type: "ACM",
          description: "<p>\u7ef4\u4ed6\u5165\u6211\u5fc3\uff0c\u8d5b\u8fc7\u6d77\u6d1b\u56e0\uff0c\u5728\u8fd9\u4e2a\u5bd2\u51b7\u7684\u51ac\u5929\uff0c\u4e3a\u4ec0\u4e48\u4e0d\u6765\u4e00\u676f\u51b0\u723d\u67e0\u6aac\u8336\u5462\uff1f</p><p><img src=\"https://pic1.zhimg.com/d8db4bb6d8dfc727b6f0a7f12d7367a0_r.jpg?source=1940ef5c\" alt=\"\" /></p><h1>\u6bd4\u8d5b\u5956\u54c1\u8bf4\u660e:</h1><p>\u672c\u6b21\u6bd4\u8d5b\u5177\u6709\u4e30\u539a\u7684\u5956\u54c1 (\u91cd\u8981\u7684\u4e0d\u662f\u4ef7\u503c\uff0c\u800c\u662f<b>\u6b22\u4e50</b>\u4e0d\u662f\u4e48\uff1f) \uff0c\u5177\u4f53\u89c4\u5219\u5982\u4e0b\uff1a</p><p>\u4e00\u3001\u6240\u6709\u53c2\u8d5b\u9009\u624b\u5747\u83b7\u5f97<b>\u53c2\u8d5b\u7eaa\u5ff5\u5956</b>\u2014\u2014<b>\u9ea6\u65af\u5a01\u5c14\u5496\u5561\u4e00\u5305</b></p><p>\u4e8c\u3001\u672c\u6b21\u6bd4\u8d5b\u6392\u540d<b>\u7b2c\u4e00\u3001\u7b2c\u5341</b>\u7684\u9009\u624b\u5404\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u7ef4\u4ed6\u67e0\u6aac\u8336\u4e09\u74f6</b></p><p><span style=\"color: rgb(51, 51, 51);\">\u4e09\u3001\u672c\u6b21\u6bd4\u8d5b\u6392\u540d<b>\u7b2c\u4e8c\u3001\u7b2c\u5341\u4e00</b>\u7684\u9009\u624b\u5404\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u798f\u5efa\u9ad8\u7aef\u8336\u53f6\u4e09\u888b</b></span><br /></p><p>\u56db\u3001\u672c\u6b21\u6bd4\u8d5b<b>\u6700\u540e\u4e00\u4e2aAC</b>\u7684\u540c\u5b66\u5c06\u83b7\u5f97\u987d\u5f3a\u62fc\u640f\u5956\uff0c\u5956\u54c1\u795e\u79d8\u7684\u5f88\uff08ZXF\u8d5e\u52a9\uff09\uff0c<b>\u73b0\u573a\u63ed\u6653</b>\uff0c\u594b\u6597\u5427baby\u4eec.</p><p>\u4e94\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97<b>\u7b2c\u4e00\u540d</b>\u7684\u9009\u624b\u5c06\u83b7\u5f97ZYB\u5b66\u957f\u8d5e\u52a9\u7684<b>\u9ad8\u7aef\u8f6f\u76ae\u672c\u4e00\u4e2a</b></p><p><span style=\"color: rgb(51, 51, 51);\">\u516d\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97<b>\u7b2c\u4e8c\u3001\u4e09\u3001\u56db\u3001\u4e94\u540d</b>\u7684\u9009\u624b\u5c06\u6bcf\u4eba\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u8f6f\u76ae\u672c\u4e00\u4e2a</b></span><br /></p><p><span style=\"color: rgb(51, 51, 51);\"><span style=\"color: rgb(51, 51, 51);\">\u4e03\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97</span><span style=\"color: rgb(51, 51, 51);\"><b>\u7b2c\u516d\u3001\u4e03\u3001\u516b\u3001\u4e5d\u540d</b></span><span style=\"color: rgb(51, 51, 51);\">\u7684\u9009\u624b\u5c06\u6bcf\u4eba\u83b7\u5f97CGX\u8d5e\u52a9\u7684<b>\u53cc\u6c47\u8089\u5757\u738b\u7279\u7ea7\u706b\u817f\u80a0</b></span><span style=\"color: rgb(51, 51, 51);\"><b>\u4e00\u6839</b></span><br /></span></p><p>\u516b\u3001\u6bd4\u8d5b\u7ed3\u675f\u540e\uff0c<b>\u968f\u673a\u62bd\u53d6\u4e09\u540d\u540c\u5b66</b>\u9001<b>\u725b\u5ba2\u886c\u886b</b>\uff0c\u6ee1\u8db3\u4f60\u6c38\u8fdc\u62bd\u4e0d\u5230\u886c\u886b\u7684\u975e\u914b\u7684\u613f\u671b\uff01</p><p><b><u>\u4ee5\u4e0a\u89c4\u5219\u6700\u7ec8\u89e3\u91ca\u6743\u5f52\u521b\u65b0\u5b9e\u9a8c\u5ba4\u6240\u6709</u></b>\u3002</p><h1><font>\u7f5a\u65f6\u89c4\u5219\u548c\u8fd4\u56de\u7ed3\u679c\u89e3\u91ca:</font></h1><p><font>1.\u63d0\u4ea4\u663e\u793aAccepted\u5373\u4e3a\u901a\u8fc7,\u82e5\u663e\u793a\u5176\u4ed6\u7ed3\u679c\u5373\u8868\u793a\u9519\u8bef.\u9519\u8bef\u4e00\u6b21\u603b\u7f5a\u65f6+20\u5206\u949f,\u8bf7\u8c28\u614e\u63d0\u4ea4\uff01</font></p><p><font>2.\u5173\u4e8e\u8fd4\u56de\u7ed3\u679c\u7684\u89e3\u91ca:</font></p><p><font>Pending & Juding : You solution will be judged soon, please wait for result</font></p><p><font>Compile Error : Failed to compile your source code. Click on the link to see compiler&#039;s output.</font></p><p><font>Accepted : Congratulations. Your solution is correct.</font></p><p><font>Wrong Answer : Your program&#039;s output doesn&#039;t match judger&#039;s answer.</font></p><p><font>Runtime Error : Your program terminated abnormally. Possible reasons are: segment fault, divided by zero or exited with code other than 0.OrThe memory your program actually used has exceeded limit.</font></p><p><font>Time Limit Exceeded : The CPU time your program used has exceeded limit. Java has a triple time limit.</font></p><p><font>Memory Limit Exceeded : The memory your program actually used has exceeded limit.</font></p><p><font>System Error : Oops, something has gone wrong with the judger. Please report this to administrator.</font></p><h1><font>\u6bd4\u8d5b\u8981\u6c42:</font></h1><p><font>\u6bd4\u8d5b\u8fc7\u7a0b\u4e2d<b>\u4e0d\u8bb8\u8ba8\u8bba</b>\u4e0d\u8bb8\u4e0a\u5916\u7f51,\u6709\u95ee\u9898\u53ef\u4ee5\u4e3e\u624b!</font></p><p><font><b>\u9898\u76ee\u96be\u5ea6\u4e0e\u9898\u76ee\u987a\u5e8f\u65e0\u5173,\u8bf7\u5408\u7406\u5b89\u6392\u505a\u9898\u65f6\u95f4!</b></font></p>",
        },
        {
          beginTime: "2020-11-08T05:00:00Z",
          duration: "5hours",
          type: "ACM",
          description:"<p>测试</p>"
        }
      ],
    };
  },
  methods: {
    goContest() {},
  },
  filters: {
    localtime(value) {
      return time.utcToLocal(value);
    },
  },
};
</script>
<style scoped>
.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
}
.el-carousel__item:nth-child(n) {
  background-color: #fff;
}
.el-carousel__item {
  overflow-y: auto!important;
}
.el-col {
  margin-top: 25px;
}
.contest {
  text-align: center;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

.contest .title .el-link {
  font-size: 25px;
  font-weight: 500;
  color: #444;
}
.clearfix h2 {
  color: #409eff;
}
.el-link.el-link--default:hover {
  color: #409eff;
  transition: all 0.28s ease;
}
.contest .content-info {
  padding: 0 70px 40px 70px;
}
.contest .contest-description {
  margin-top: 25px;
  /* text-align: left; */
}

</style>
