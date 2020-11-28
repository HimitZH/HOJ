<template>
<div class="contest-body">
  <el-row>
    <el-col :xs="24" :md="24" :lg="24">
      <el-card shadow>
        <div class="contest-title">
          <div slot="header">
            <span class="panel-title">{{title}}</span>
          </div>
         </div>
         <el-row>
            <el-col :span="12" class="text-align:left">
              <el-tooltip :content="CONTEST_TYPE_REVERSE[auth].tips" placement="top">
                <el-tag
                    :type="CONTEST_TYPE_REVERSE[auth].color"
                    effect="plain">
                    {{CONTEST_TYPE_REVERSE[auth].name}}
                </el-tag>
              </el-tooltip>
            </el-col>
          <el-col :span="12" style="text-align:right">
              <el-button size="small">
                {{ rule_type }}
              </el-button>
            </el-col>
          </el-row>
         <div class="contest-time">
          <el-row>
            <el-col :xs="24" :md="12"  class="left">
              <p><i class="fa fa-hourglass-start" aria-hidden="true"></i> StartAt：{{startTime | localtime}}</p>
            </el-col>
            <el-col :xs="24" :md="12"  class="right">
              <p><i class="fa fa-hourglass-end" aria-hidden="true"></i> EndAt：{{endTime | localtime}}</p>
            </el-col>
          </el-row>
         </div>
        <div class="slider">
            <el-slider
              v-model="value"
              :format-tooltip="formatTooltip"
              :step="timeStep"
         ></el-slider>
        </div>
        <el-row>
          <el-col :span="24" style="text-align:center">
            <el-tag
                      effect="dark"
                      size="medium"
                    >
                      <i
                        class="fa fa-circle"
                        aria-hidden="true"
                      ></i>
                      -11:16:52
                      <!-- {{ CONTEST_STATUS_REVERSE[status].name }} -->
              </el-tag>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row>
    <div class="sub-menu" >
      <!-- 判断是否需要密码验证 -->
      <el-card v-if="passwordFormVisible" class="password-form-card" style="text-align:center">
        <div slot="header">
          <span class="panel-title">Password required</span>
        </div>
        <p class="password-form-tips">To enter the Private contest,please input the password!</p>
        <el-input v-model="contestPassword" type="password"
                        placeholder="Enter the contest password" 
                        @on-enter="checkPassword"/>
        <el-button type="primary" @click="checkPassword" style="float:right;margin:5px">Enter</el-button>
     </el-card>
     

      <el-tabs v-else @tab-click="tabClick" v-model="route_name" :lazy="true">
        <el-tab-pane name="contest-details">
          <span slot="label"><i class="el-icon-s-home"></i>&nbsp;Overview</span>
          <el-card class="box-card">
            <div v-html="description" class="markdown-body"></div>
          </el-card>
        </el-tab-pane>
        <el-tab-pane name="contest-problem-list">
          <span slot="label"><i class="fa fa-list" aria-hidden="true"></i>&nbsp;Problems</span>
          <transition name="el-zoom-in-bottom">
            <router-view ></router-view>
          </transition>
        </el-tab-pane>
        <el-tab-pane name="contest-submission-list">
          <span slot="label"><i class="el-icon-menu"></i>&nbsp;Status</span>
          <transition name="el-zoom-in-bottom">
            <router-view></router-view>
          </transition>
        </el-tab-pane>
        <el-tab-pane name="contest-rank">
          <span slot="label"><i class="fa fa-bar-chart" aria-hidden="true"></i>&nbsp;Rank</span>
          <transition name="el-zoom-in-bottom">
            <router-view></router-view>
          </transition>
        </el-tab-pane>  
        <el-tab-pane name="contest-announcement-list">
          <span slot="label"><i class="fa fa-bullhorn" aria-hidden="true"></i>&nbsp;Announcements</span>
          <transition name="el-zoom-in-bottom">
            <router-view></router-view>
          </transition>
        </el-tab-pane>
        <el-tab-pane>
          <span slot="label"><i class="fa fa-commenting" aria-hidden="true"></i>&nbsp;Comments</span>
          <transition name="el-zoom-in-bottom">
            <router-view></router-view>
          </transition>
        </el-tab-pane>
        <el-tab-pane name="contest-ac-info">
          <span slot="label"><i class="el-icon-s-help" aria-hidden="true"></i>&nbsp;AC Info</span>
          <transition name="el-zoom-in-bottom">
            <router-view></router-view>
          </transition>
        </el-tab-pane>
      </el-tabs>
    </div>
</div>  
</template>
<script>
import time from "@/common/time";
import moment from "moment";
import api from "@/common/api";
import { mapState, mapGetters, mapActions } from "vuex";
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_STATUS,
  CONTEST_TYPE_REVERSE,
} from "@/common/constants";
export default {
  name: "",
  data() {
    return {
      route_name: "contest-details",
      value: 0,
      duration: 0, // 比赛时长时间戳
      nowDurationMs: 0, // 比赛开始到现在的时间戳
      timer: null,
      CONTEST_STATUS: CONTEST_STATUS,
      CONTEST_STATUS_REVERSE: CONTEST_STATUS_REVERSE,
      CONTEST_TYPE_REVERSE: CONTEST_TYPE_REVERSE,
      btnLoading: false,
      contestID: "",
      contestPassword: "",
      status: 0,
      auth: "1",
      contest_type: "Public",
      title:"2020\u5e74 \u67e0\u6aac\u8336\u6b22\u4e50\u8d5b \u66a8 \u672c\u79d1\u751f\u521b\u65b0\u5b9e\u9a8c\u5ba4\u6708\u8d5b",
      description: "<p>\u7ef4\u4ed6\u5165\u6211\u5fc3\uff0c\u8d5b\u8fc7\u6d77\u6d1b\u56e0\uff0c\u5728\u8fd9\u4e2a\u5bd2\u51b7\u7684\u51ac\u5929\uff0c\u4e3a\u4ec0\u4e48\u4e0d\u6765\u4e00\u676f\u51b0\u723d\u67e0\u6aac\u8336\u5462\uff1f</p><p><img src=\"https://pic1.zhimg.com/d8db4bb6d8dfc727b6f0a7f12d7367a0_r.jpg?source=1940ef5c\" alt=\"\" /></p><h1>\u6bd4\u8d5b\u5956\u54c1\u8bf4\u660e:</h1><p>\u672c\u6b21\u6bd4\u8d5b\u5177\u6709\u4e30\u539a\u7684\u5956\u54c1 (\u91cd\u8981\u7684\u4e0d\u662f\u4ef7\u503c\uff0c\u800c\u662f<b>\u6b22\u4e50</b>\u4e0d\u662f\u4e48\uff1f) \uff0c\u5177\u4f53\u89c4\u5219\u5982\u4e0b\uff1a</p><p>\u4e00\u3001\u6240\u6709\u53c2\u8d5b\u9009\u624b\u5747\u83b7\u5f97<b>\u53c2\u8d5b\u7eaa\u5ff5\u5956</b>\u2014\u2014<b>\u9ea6\u65af\u5a01\u5c14\u5496\u5561\u4e00\u5305</b></p><p>\u4e8c\u3001\u672c\u6b21\u6bd4\u8d5b\u6392\u540d<b>\u7b2c\u4e00\u3001\u7b2c\u5341</b>\u7684\u9009\u624b\u5404\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u7ef4\u4ed6\u67e0\u6aac\u8336\u4e09\u74f6</b></p><p><span style=\"color: rgb(51, 51, 51);\">\u4e09\u3001\u672c\u6b21\u6bd4\u8d5b\u6392\u540d<b>\u7b2c\u4e8c\u3001\u7b2c\u5341\u4e00</b>\u7684\u9009\u624b\u5404\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u798f\u5efa\u9ad8\u7aef\u8336\u53f6\u4e09\u888b</b></span><br /></p><p>\u56db\u3001\u672c\u6b21\u6bd4\u8d5b<b>\u6700\u540e\u4e00\u4e2aAC</b>\u7684\u540c\u5b66\u5c06\u83b7\u5f97\u987d\u5f3a\u62fc\u640f\u5956\uff0c\u5956\u54c1\u795e\u79d8\u7684\u5f88\uff08ZXF\u8d5e\u52a9\uff09\uff0c<b>\u73b0\u573a\u63ed\u6653</b>\uff0c\u594b\u6597\u5427baby\u4eec.</p><p>\u4e94\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97<b>\u7b2c\u4e00\u540d</b>\u7684\u9009\u624b\u5c06\u83b7\u5f97ZYB\u5b66\u957f\u8d5e\u52a9\u7684<b>\u9ad8\u7aef\u8f6f\u76ae\u672c\u4e00\u4e2a</b></p><p><span style=\"color: rgb(51, 51, 51);\">\u516d\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97<b>\u7b2c\u4e8c\u3001\u4e09\u3001\u56db\u3001\u4e94\u540d</b>\u7684\u9009\u624b\u5c06\u6bcf\u4eba\u83b7\u5f97LZH\u5b66\u957f\u8d5e\u52a9\u7684<b>\u8f6f\u76ae\u672c\u4e00\u4e2a</b></span><br /></p><p><span style=\"color: rgb(51, 51, 51);\"><span style=\"color: rgb(51, 51, 51);\">\u4e03\u3001\u672c\u6b21\u6bd4\u8d5b\u83b7\u5f97</span><span style=\"color: rgb(51, 51, 51);\"><b>\u7b2c\u516d\u3001\u4e03\u3001\u516b\u3001\u4e5d\u540d</b></span><span style=\"color: rgb(51, 51, 51);\">\u7684\u9009\u624b\u5c06\u6bcf\u4eba\u83b7\u5f97CGX\u8d5e\u52a9\u7684<b>\u53cc\u6c47\u8089\u5757\u738b\u7279\u7ea7\u706b\u817f\u80a0</b></span><span style=\"color: rgb(51, 51, 51);\"><b>\u4e00\u6839</b></span><br /></span></p><p>\u516b\u3001\u6bd4\u8d5b\u7ed3\u675f\u540e\uff0c<b>\u968f\u673a\u62bd\u53d6\u4e09\u540d\u540c\u5b66</b>\u9001<b>\u725b\u5ba2\u886c\u886b</b>\uff0c\u6ee1\u8db3\u4f60\u6c38\u8fdc\u62bd\u4e0d\u5230\u886c\u886b\u7684\u975e\u914b\u7684\u613f\u671b\uff01</p><p><b><u>\u4ee5\u4e0a\u89c4\u5219\u6700\u7ec8\u89e3\u91ca\u6743\u5f52\u521b\u65b0\u5b9e\u9a8c\u5ba4\u6240\u6709</u></b>\u3002</p><h1><font>\u7f5a\u65f6\u89c4\u5219\u548c\u8fd4\u56de\u7ed3\u679c\u89e3\u91ca:</font></h1><p><font>1.\u63d0\u4ea4\u663e\u793aAccepted\u5373\u4e3a\u901a\u8fc7,\u82e5\u663e\u793a\u5176\u4ed6\u7ed3\u679c\u5373\u8868\u793a\u9519\u8bef.\u9519\u8bef\u4e00\u6b21\u603b\u7f5a\u65f6+20\u5206\u949f,\u8bf7\u8c28\u614e\u63d0\u4ea4\uff01</font></p><p><font>2.\u5173\u4e8e\u8fd4\u56de\u7ed3\u679c\u7684\u89e3\u91ca:</font></p><p><font>Pending & Juding : You solution will be judged soon, please wait for result</font></p><p><font>Compile Error : Failed to compile your source code. Click on the link to see compiler&#039;s output.</font></p><p><font>Accepted : Congratulations. Your solution is correct.</font></p><p><font>Wrong Answer : Your program&#039;s output doesn&#039;t match judger&#039;s answer.</font></p><p><font>Runtime Error : Your program terminated abnormally. Possible reasons are: segment fault, divided by zero or exited with code other than 0.OrThe memory your program actually used has exceeded limit.</font></p><p><font>Time Limit Exceeded : The CPU time your program used has exceeded limit. Java has a triple time limit.</font></p><p><font>Memory Limit Exceeded : The memory your program actually used has exceeded limit.</font></p><p><font>System Error : Oops, something has gone wrong with the judger. Please report this to administrator.</font></p><h1><font>\u6bd4\u8d5b\u8981\u6c42:</font></h1><p><font>\u6bd4\u8d5b\u8fc7\u7a0b\u4e2d<b>\u4e0d\u8bb8\u8ba8\u8bba</b>\u4e0d\u8bb8\u4e0a\u5916\u7f51,\u6709\u95ee\u9898\u53ef\u4ee5\u4e3e\u624b!</font></p><p><font><b>\u9898\u76ee\u96be\u5ea6\u4e0e\u9898\u76ee\u987a\u5e8f\u65e0\u5173,\u8bf7\u5408\u7406\u5b89\u6392\u505a\u9898\u65f6\u95f4!</b></font></p>",
      real_time_rank: true,
      rule_type: "ACM",
      startTime: "2018-04-17T11:16:52Z",
      endTime: "2018-04-22T11:16:36Z",
      now: "2020-11-17T15:16:22.013824Z",
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.route_name = this.$route.name;
    if(this.route_name =='contest-problem-details'){ //特殊判断
      this.route_name = 'contest-problem-list'
    }
    // this.$store.dispatch("getContest").then((res) => {
    //   this.changeDomTitle({ title: res.data.data.title });
    //   let data = res.data.data;
    //   let endTime = moment(data.end_time);
    //   if (endTime.isAfter(moment(data.now))) {
    //     this.timer = setInterval(() => {
    //       this.$store.commit("nowAdd1s");
    //     }, 1000);
    //   }
    // });
    let nowTime = new Date().getTime(); // 获取当前时间戳
    this.duration = time.durationMs(this.startTime, this.endTime);
    this.nowDurationMs = time.durationMs(this.startTime, nowTime);
    if (this.nowDurationMs >= this.duration) {
      // 比赛已经结束
      this.nowDurationMs = this.duration;
      this.value = 100;
    } else {
      this.timer = setInterval(() => {
        // let nowTime = new Date().getTime(); // 获取当前时间戳
        // this.nowDurationMs = time.durationMs(this.startTime, nowTime); // 获取当前时间距离比赛开始的时间戳
        if (this.nowDurationMs >= 0) {
          // 比赛开始进度条才开始计时
          this.value = (this.nowDurationMs / this.duration) * 100; // 赋予进度条新值
        }
        if (this.nowDurationMs >= this.duration) {
          this.nowDurationMs = this.duration;
          clearInterval(this.timer);
          return;
        }
        this.nowDurationMs += 1;
      }, 1000);
    }
  },
  methods: {
    ...mapActions(["changeDomTitle"]),
    formatTooltip(val) {
      if (this.nowDurationMs <= 0) {
        // 还未开始
        return "00:00:00";
      }
      return time.secondFormat(this.nowDurationMs); // 格式化时间
    },
    checkPassword() {
      if (this.contestPassword === "") {
        this.$error("Password can't be empty");
        return;
      }
      this.btnLoading = true;
      api.checkContestPassword(this.contestID, this.contestPassword).then(
        (res) => {
          this.$success("Succeeded");
          this.$store.commit(types.CONTEST_ACCESS, { access: true });
          this.btnLoading = false;
        },
        (res) => {
          this.btnLoading = false;
        }
      );
    },
    tabClick(tab){
        let name = tab.name;
        if(name !==this.$route.name){
          this.$router.push({name: name});
        }
    },
  },
  computed: {
    timeStep() {
      // 时间段平分滑条长度
      return 100 / this.duration;
    },
    ...mapState({
      showMenu: (state) => state.contest.itemVisible.menu,
      contest: (state) => state.contest.contest,
      contest_table: (state) => [state.contest.contest],
      // now: (state) => state.contest.now,
    }),
    ...mapGetters([
      "contestMenuDisabled",
      "contestRuleType",
      "contestStatus",
      "countdown",
      "isContestAdmin",
      "OIContestRealTimePermission",
      "passwordFormVisible",
    ]),
    countdownColor() {
      if (this.contestStatus) {
        return CONTEST_STATUS_REVERSE[this.contestStatus].color;
      }
    },
    showAdminHelper() {
      return this.isContestAdmin && this.contestRuleType === "ACM";
    },
  },
  watch: {
    $route(newVal) {
      this.route_name = newVal.name;
      this.contestID = newVal.params.contestID;
      this.changeDomTitle({ title: this.contest.title });
    },
  },
  filters: {
    localtime(value) {
      return time.utcToLocal(value);
    },
  },
  beforeDestroy() {
    clearInterval(this.timer);
    this.$store.commit("clearContest");
  },
};
</script>
<style scoped>
@media screen and (min-width: 768px) {
  .contest-body {
    padding: 0 8%;
  }
  .contest-time .left {
    text-align: left;
  }
  .contest-time .right {
    text-align: right;
  }
  .password-form-card{
    width: 400px;
    margin: 0 auto;
  }
}
@media screen and (max-width: 768px) {
  .contest-time .left,
  .contest-time .right {
    text-align: center;
  }
}
/deep/.el-slider__button {
  width: 20px !important;
  height: 20px !important;
  background-color: #409eff !important;
}
/deep/.el-slider__button-wrapper {
  z-index: 500;
}
/deep/.el-slider__bar {
  height: 10px !important;
  background-color: #09be24 !important;
}
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
/deep/.el-tabs__nav-wrap{
  background: #fff;
  border-radius: 3px;
}
/deep/.el-tabs--top .el-tabs__item.is-top:nth-child(2){
   padding-left: 20px;
 }
.contest-title{
   text-align: center;
}
.contest-time {
  width: 100%;
  font-size: 16px;
}
.el-tag--dark {
  border-color: #fff;
}
.el-tag {
  color: rgb(25, 190, 107);
  background: #fff;
  border: 1px solid #e9eaec;
  font-size: 18px;
}
.sub-menu {
  margin-top: 15px;
}
.password-form-tips{
  text-align: center;
  font-size: 14px;
}
</style>