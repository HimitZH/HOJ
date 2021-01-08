<template>
  <div class="flex-container">
    <div id="problem-main">
      <!--problem main-->
      <el-row >
        <el-col :sm="24" :md="24" :lg="12" class="problem-detail">
          <el-card :padding="10" shadow>
            <div slot="header" class="panel-title">
              <span>{{ problemData.problem.title }}</span><br>
              <span><el-tag v-for="tag in problemData.tags" :key="tag" effect="plain" size="small" style="margin-right:10px;">{{ tag }}</el-tag></span>
                <div class="problem-menu">
                  <span> <el-link type="primary" :underline="false" @click="graphVisible = !graphVisible"><i class="fa fa-pie-chart" aria-hidden="true"></i> Statistic</el-link></span>
                  <span> <el-link type="primary" :underline="false" @click="goProblemSubmission"><i class="fa fa-bars" aria-hidden="true"></i> Submissions</el-link></span>
                </div>
              <div class="question-intr">
                <span>Time Limit：{{problemData.problem.timeLimit}}MS</span><br>
                <span>Memory Limit：{{problemData.problem.memoryLimit}}MB</span><br>
                <span>Level：{{problemData.problem.difficulty | parseProblemLevel }}</span><span v-if="problemData.problem.type==1" style="margin-left: 10px;">Score：{{problemData.problem.ioScore}}</span><br>
                <span v-show="problemData.problem.author">Create By：{{problemData.problem.author}}</span><br>
              </div>
            </div>

            <div id="problem-content" class="markdown-body" v-katex>
              <p class="title">Description</p>
              <p class="content" v-html="problemData.problem.description"></p>
              <!-- {{$t('m.music')}} -->
              <p class="title">Input</p>
              <p class="content" v-html="problemData.problem.input"></p>

              <p class="title">Output</p>
              <p class="content" v-html="problemData.problem.output"></p>

              <div v-for="(example, index) of problemData.problem.examples" :key="index">
                <div class="flex-container example">
                  <div class="example-input">
                    <p class="title">
                      Sample Input {{ index + 1 }}
                      <a
                        class="copy"
                        v-clipboard:copy="example.input"
                        v-clipboard:success="onCopy"
                        v-clipboard:error="onCopyError"
                      >
                        <i class="el-icon-document-copy"></i>
                      </a>
                    </p>
                    <pre>{{ example.input }}</pre>
                  </div>
                  <div class="example-output">
                    <p class="title">Sample Output {{ index + 1 }}
                      <a
                        class="copy"
                        v-clipboard:copy="example.output"
                        v-clipboard:success="onCopy"
                        v-clipboard:error="onCopyError"
                      >
                        <i class="el-icon-document-copy"></i>
                      </a>

                    </p>
                    <pre>{{ example.output }}</pre>
                  </div>
                </div>
              </div>

              <div v-if="problemData.problem.hint">
                <p class="title">Hint</p>
                <el-card dis-hover>
                  <div class="content" v-html="problemData.problem.hint"></div>
                </el-card>
              </div>

              <div v-if="problemData.problem.source">
                <p class="title">Source</p>
                <p class="content">{{problemData.problem.source }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :sm="24" :md="24" :lg="12" class="submit-detail">
          <el-card :padding="10" id="submit-code" shadow="always">
            <CodeMirror
              :value.sync="code"
              :languages="problemData.problem.languages"
              :language="language"
              :theme="theme"
              @resetCode="onResetToTemplate"
              @changeTheme="onChangeTheme"
              @changeLang="onChangeLang"
            ></CodeMirror>
            <el-row>
              <el-col :sm="24" :md="10" :lg="10" style="margin-top:4px;">
                <div v-if="!isAuthenticated">
                  <el-alert type="info" show-icon effect="dark" :closable="false"
                    >Please login first</el-alert
                  >
                </div>
                <div class="status" v-if="statusVisible">
                  <template v-if="!this.contestID ||(this.contestID && OIContestRealTimePermission)">
                    <span>Status:</span>
                    <el-tag
                      effect="dark"
                      :color="submissionStatus.color"
                      @click.native="handleRoute('/submission-detail/' + submissionId)"
                    >
                    <i class="fa fa-circle" aria-hidden="true"></i>
                      {{ submissionStatus.text }}
                    </el-tag>
                  </template>
                  <template
                    v-else-if="this.contestID && !OIContestRealTimePermission"
                  >
                    <el-alert type="success" show-icon effect="dark" :closable="false"
                      >Submitted successfully</el-alert
                    >
                  </template>
                </div>
                <div v-else-if="problemData.myStatus == 0">
                  <el-alert type="success" show-icon effect="dark" :closable="false"
                    >You have solved the problem</el-alert
                  >
                </div>
                <div
                  v-else-if="
                    this.contestID &&
                    !OIContestRealTimePermission &&
                    submissionExists
                  "
                >
                  <el-alert type="success" show-icon effect="dark" :closable="false"
                    >You have submitted a solution</el-alert
                  >
                </div>
                <div v-if="contestEnded">
                  <el-alert type="warning" show-icon effect="dark" :closable="false"
                    >Contest has ended</el-alert
                  >
                </div>
              </el-col>

              <el-col :sm="24" :md="14" :lg="14" style="margin-top:4px;">
                <template v-if="captchaRequired">
                  <div class="captcha-container">
                    <el-tooltip
                      v-if="captchaRequired"
                      content="Click to refresh"
                      placement="top"
                    >
                      <img :src="captchaSrc" @click="getCaptchaSrc" />
                    </el-tooltip>
                    <el-input v-model="captchaCode" class="captcha-code" />
                  </div>
                </template>
                <el-button
                  type="primary"
                  icon="el-icon-edit-outline"
                  size="small"
                  :loading="submitting"
                  @click="submitCode"
                  :disabled="problemSubmitDisabled || submitted"
                  class="fl-right"
                >
                  <span v-if="submitting">Submitting</span>
                  <span v-else>Submit</span>
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog :visible.sync="graphVisible" width="400px">
      <div id="pieChart-detail">
        <ECharts :options="largePie" :initOptions="largePieInitOpts"></ECharts>
      </div>
      <div slot="footer">
        <el-button type="ghost" @click="graphVisible=false" size="small">Close</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import CodeMirror from "@/components/oj/common/CodeMirror.vue";
import storage from "@/common/storage";
import utils from "@/common/utils"
import {
  JUDGE_STATUS,
  CONTEST_STATUS,
  JUDGE_STATUS_RESERVE,
  buildProblemCodeKey,
} from "@/common/constants";
import {pie, largePie} from './chartData'
import api from "@/common/api";
import myMessage from '@/common/message'

// 只显示这些状态的图形占用
const filtedStatus = ["wa", "ce", "ac", "tle", "mle", "re", "pe"];

export default {
  name: "Problem",
  components: {
    CodeMirror,
  },
  data() {
    return {
      statusVisible: false,
      captchaRequired: false,
      graphVisible: false,
      submissionExists: false,
      captchaCode: "",
      captchaSrc: "",
      contestID: 0,
      problemID: 1000,
      submitting: false,
      code: "",
      language: "C++",
      theme: "monokai",
      submissionId: "",
      submitted: false,
      result: {
        status: 9,
      },
      problemData: {
        problem:{},
        problemCount:{},
        tags:[],
        languages:[]
      },
      pie: pie,
      largePie: largePie,
      // echarts 无法获取隐藏dom的大小，需手动指定
      largePieInitOpts: {
        width: '380',
        height: '380'
      },
      JUDGE_STATUS_RESERVE:{}
    };
  },
  // 获取缓存中的该题的做题代码，代码语言，代码风格
  beforeRouteEnter (to, from, next) {
    let problemCode = storage.get(buildProblemCodeKey(to.params.problemID, to.params.contestID))
    if (problemCode) {
      next(vm => {
        vm.language = problemCode.language
        vm.code = problemCode.code
        vm.theme = problemCode.theme
      })
    } else {
      next()
    }
  },
  mounted() {
    this.init();
    this.JUDGE_STATUS_RESERVE = Object.assign({},JUDGE_STATUS_RESERVE);
  },
  methods: {
    ...mapActions(["changeDomTitle"]),
    init() {
      if(this.$route.params.contestID){
        this.contestID = this.$route.params.contestID;
      }
      this.problemID = this.$route.params.problemID;
      let func =this.$route.name === "problemDetails"
          ? "getProblem"
          : "getContestProblem";
      api[func](this.problemID, this.contestID).then(
        (res) => {
          let result = res.data.data;
          this.changeDomTitle({ title: result.problem.title });
          result['myStatus'] = -10 // 设置默认值
          if(this.isAuthenticated){
            let pidList = [result.problem.id]
            api.getUserProblemStatus(pidList).then(res=>{
              let statusMap = res.data.data
              if(statusMap[result.problem.id]!=-10){
                this.submissionExists = true
                result.myStatus = statusMap[result.problem.id]
              }else{
                this.submissionExists = false
              }
            })
          }
          result.problem.examples = utils.stringToExamples(result.problem.examples);
          this.problemData = result;
          this.changePie(result.problemCount);

          // 在beforeRouteEnter中修改了, 说明本地有code,不用更改配置
          if (this.code !== "") {
            return;
          }
          this.language = this.problemData.languages[0];
          
        },
        () => {
     
        }
      );
    },
     changePie (problemData) {
      let total = problemData.total
      let acNum = problemData.ac
      // 该状态结果数为0的不显示,同时一些无关参数也排除
      for (let k in problemData) {
        if (problemData[k]==0 || filtedStatus.indexOf(k)===-1) {
          delete problemData[k]
        }
      }

      let data = [
        {name: 'WA', value: total - acNum},
        {name: 'AC', value: acNum}
      ]
      this.pie.series[0].data = data
      // 只把大图的AC selected下，这里需要做一下deepcopy
      let data2 = JSON.parse(JSON.stringify(data))
      data2[1].selected = true
      this.largePie.series[1].data = data2

      // 根据结果设置legend,没有提交过的legend不显示
      let legend = Object.keys(problemData).map(ele => (ele+'').toUpperCase())
      if (legend.length === 0) {
        legend.push('AC', 'WA')
      }
      this.largePie.legend.data = legend

      
      // 把ac的数据提取出来放在最后
      let acCount = problemData.ac
      delete problemData.ac

      let largePieData = []
      Object.keys(problemData).forEach(ele => {
        largePieData.push({name: (ele+'').toUpperCase(), value: problemData[ele]})
      })
      largePieData.push({name: 'AC', value: acCount})
      this.largePie.series[0].data = largePieData
    },

    goProblemSubmission(){
      if(this.contestID){
        this.$router.push({
          name:"contestSubmissionList",
          params:{contestID:this.contestID},
          query:{problemID:this.problemID}
        })
      }else{
        this.$router.push({
          name:"SubmissionList",
          query:{problemID:this.problemID}
        })
      }
    },

    handleRoute(route) {
      this.$router.push(route);
    },
    onChangeLang(newLang) {
      this.language = newLang;
    },
    onChangeTheme(newTheme) {
      this.theme = newTheme;
    },
    onResetToTemplate() {
      this.$confirm("你是否确定要重置你的代码？", "提示", {
        cancelButtonText: "取消",
        confirmButtonText: "确定",
        type: "warning",
      })
        .then(() => {
          this.code = "";
        })
        .catch(() => {});
    },
    checkSubmissionStatus() {
      // 使用setTimeout避免一些问题
      if (this.refreshStatus) {
        // 如果之前的提交状态检查还没有停止,则停止,否则将会失去timeout的引用造成无限请求
        clearTimeout(this.refreshStatus);
      }
      const checkStatus = () => {
        let submitId = this.submissionId;
        api.getSubmission(submitId).then(
          (res) => {
            this.result.status = res.data.data.submission.status;
            if (Object.keys(res.data.data.submission).length !== 0) {
              // status不为判题和排队中才表示此次判题结束
              if(res.data.data.submission.status != JUDGE_STATUS_RESERVE['Pending']&&res.data.data.submission.status != JUDGE_STATUS_RESERVE['Judging']){
                this.submitting = false;
                this.submitted = false;
                clearTimeout(this.refreshStatus);
                this.init();
              }else{
                this.refreshStatus = setTimeout(checkStatus, 2000);
              }
            } else {
              this.refreshStatus = setTimeout(checkStatus, 2000);
            }
          },
          (res) => {
            this.submitting = false;
            clearTimeout(this.refreshStatus);
          }
        );
      };
      // 设置每2秒检查一下该题的提交结果
      this.refreshStatus = setTimeout(checkStatus, 2000);
    },
    submitCode() {
      if (this.code.trim() === "") {
        myMessage.error("提交的代码不能为空！");
        return;
      }
      this.submissionId = "";
      this.result = { status: 9 };
      this.submitting = true;
      let data = {
        pid: this.problemData.problem.id,
        language: this.language,
        code: this.code,
        cid: this.contestID,
      };
      if (this.captchaRequired) {
        data.captcha = this.captchaCode;
      }
      const submitFunc = (data, detailsVisible) => {
        this.statusVisible = true;
        api.submitCode(data).then(
          (res) => {
            this.submissionId = res.data.data && res.data.data.submitId;
            // 定时检查状态
            this.submitting = false;
            this.submissionExists = true;
            if (!detailsVisible) {
              this.$Modal.success({
                title: "Success",
                content: "代码提交成功！",
              });
              return;
            }
            this.submitted = true;
            this.checkSubmissionStatus();
          },
          (res) => {
            this.getCaptchaSrc();
            if (res.data.data.startsWith("Captcha is required")) {
              this.captchaRequired = true;
            }
            this.submitting = false;
            this.statusVisible = false;
          }
        );
      };

      if (this.contestRuleType === "OI" && !this.OIContestRealTimePermission) {
        if (this.submissionExists) {
          this.$Modal.confirm({
            title: "",
            content:
              "<h3>" +
              "你已经有该题目的提交了，确定要再一次提交覆盖之前的提交记录？" +
              "<h3>",
            onOk: () => {
              // 暂时解决对话框与后面提示对话框冲突的问题(否则一闪而过）
              setTimeout(() => {
                submitFunc(data, false);
              }, 1000);
            },
            onCancel: () => {
              this.submitting = false;
            },
          });
        } else {
          submitFunc(data, false);
        }
      } else {
        submitFunc(data, true);
      }
    },
    onCopy(event) {
      myMessage.success('Sample copied successfully');
    },
    onCopyError(e) {
       myMessage.success('Sample copy failed');
    },
  },
  computed: {
    ...mapGetters([
      "problemSubmitDisabled",
      "contestRuleType",
      "OIContestRealTimePermission",
      "contestStatus",
      'isAuthenticated',
    ]),
    contest() {
      return this.$store.state.contest.contest;
    },
    contestEnded() {
      return this.contestStatus === CONTEST_STATUS.ENDED;
    },
    submissionStatus() {
      return {
        text: JUDGE_STATUS[this.result.status]["name"],
        color: JUDGE_STATUS[this.result.status]["rgb"],
      };
    },
    submissionRoute() {
      if (this.contestID) {
        return {
          name: "contest-submission-list",
          query: { problemID: this.problemID },
        };
      } else {
        return {
          name: "submission-list",
          query: { problemID: this.problemID },
        };
      }
    },
  },
  beforeRouteLeave(to, from, next) {
    // 防止切换组件后仍然不断请求
    clearInterval(this.refreshStatus);
    storage.set(buildProblemCodeKey(this.problemData.problem.id, from.params.contestID), {
      code: this.code,
      language: this.language,
      theme: this.theme,
    });
    next();
  },
  watch: {
    $route() {
      this.init();
    },
    isAuthenticated (newVal) {
        if (newVal === true) {
          this.init()
        }
    }
  },
};
</script>

<style scoped>
#problem-main {
  flex: auto;
}
.problem-menu{
  float: right;
}

.problem-menu span{
  margin-left: 10px;
}
.el-link{
  font-size: 16px!important;
}
.question-intr {
  margin-top: 30px;
  border-radius: 4px;
  border: 1px solid #ddd;
  border-left: 2px solid #3498db;
  background: #fafafa;
  padding: 10px;
  line-height: 1.8;
  margin-bottom: 10px;
  font-size: 14px;
}

.submit-detail{
  height:100%
}

@media screen and (min-width: 768px) {
  .problem-detail {
    height: 672px;
    overflow-y: auto;
  }
  .submit-detail{
    height: 672px;
    overflow-y: auto;
  }
  .problem-menu span{
    margin-left: 20px;
  }
  .question-intr {
    margin-top: 6px;
  }
}

@media screen and (max-width: 1080px) {
  .submit-detail{
    padding-top:20px;
  }
  .submit-detail{
    height:100%
  }
}
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
#right-column {
  flex: none;
  width: 220px;
}

#problem-content {
  margin-top: -50px;
}
#problem-content .title {
  font-size: 20px;
  font-weight: 400;
  margin: 25px 0 8px 0;
  color: #3091f2;
}
#problem-content .copy {
  padding-left: 8px;
}

p.content {
  margin-left: 25px;
  margin-right: 20px;
  font-size: 15px;
}
.flex-container {
  display: flex;
  width: 100%;
  max-width: 100%;
  justify-content: space-around;
  align-items: flex-start;
  flex-flow: row nowrap;
}
.example {
  align-items: stretch;
}
.example-input,
.example-output {
  width: 50%;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  margin-right: 5%;
}
.example pre {
  flex: 1 1 auto;
  align-self: stretch;
  border-style: solid;
  background: transparent;
}
#submit-code{
  height: auto;
}
#submit-code .status {
  float: left;
}
#submit-code .status span {
  margin-right: 10px;
  margin-left: 10px;
}
.captcha-container {
  display: inline-block;
}
.captcha-container .captcha-code {
  width: auto;
  margin-top: -20px;
  margin-left: 20px;
}

.fl-right {
  float: right;
}
/deep/.el-dialog__body {
    padding: 10px 10px!important;
}
#pieChart .echarts {
  height: 250px;
  width: 210px;
}
#pieChart #detail {
  position: absolute;
  right: 10px;
  top: 10px;
}
/deep/.echarts {
  width: 350px;
  height: 350px;
}
#pieChart-detail {
  /* margin-top: 20px; */
  height: 350px;
}
</style>

