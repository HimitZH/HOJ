<template>
  <div class="flex-container">
    <div id="problem-main">
      <!--problem main-->
      <el-row >
        <el-col :sm="24" :md="12" :lg="12" class="problem-detail">
          <el-card :padding="10" shadow="always">
            <div slot="header" class="panel-title">
              <span>{{ problem.title }}</span><br>
              <span><el-tag v-for="tag in problem.tags" :key="tag" effect="plain" size="small" style="margin-right:10px;">{{ tag }}</el-tag></span>
                <div class="problem-menu">
                  <span v-if="this.contestID"> <el-link type="primary" :underline="false"><i class="fa fa-home" aria-hidden="true"></i> Contest</el-link></span>
                  <span> <el-link type="primary" :underline="false" @click="graphVisible = !graphVisible"><i class="fa fa-pie-chart" aria-hidden="true"></i> Statistic</el-link></span>
                  <span> <el-link type="primary" :underline="false"><i class="fa fa-bars" aria-hidden="true"></i> Submissions</el-link></span>
                </div>
              <div class="question-intr">
                <span>Time Limit：{{problem.time_limit}}</span><br>
                <span>Memory Limit：{{problem.memory_limit}}</span><br>
                <span>Level：{{problem.difficulty}}</span><span style="margin-left: 10px;">Score：{{problem.score}}</span><br>
                <span v-show="problem.author">Create By：{{problem.author}}</span><br>
              </div>
            </div>

            <div id="problem-content" class="markdown-body" v-katex>
              <p class="title">Description</p>
              <p class="content" v-html="problem.description"></p>
              <!-- {{$t('m.music')}} -->
              <p class="title">Input</p>
              <p class="content" v-html="problem.input_description"></p>

              <p class="title">Output</p>
              <p class="content" v-html="problem.output_description"></p>

              <div v-for="(sample, index) of problem.samples" :key="index">
                <div class="flex-container sample">
                  <div class="sample-input">
                    <p class="title">
                      Sample Input {{ index + 1 }}
                      <a
                        class="copy"
                        v-clipboard:copy="sample.input"
                        v-clipboard:success="onCopy"
                        v-clipboard:error="onCopyError"
                      >
                        <i class="el-icon-document-copy"></i>
                      </a>
                    </p>
                    <pre>{{ sample.input }}</pre>
                  </div>
                  <div class="sample-output">
                    <p class="title">Sample Output {{ index + 1 }}
                      <a
                        class="copy"
                        v-clipboard:copy="sample.output"
                        v-clipboard:success="onCopy"
                        v-clipboard:error="onCopyError"
                      >
                        <i class="el-icon-document-copy"></i>
                      </a>

                    </p>
                    <pre>{{ sample.output }}</pre>
                  </div>
                </div>
              </div>

              <div v-if="problem.hint">
                <p class="title">Hint</p>
                <el-card dis-hover>
                  <div class="content" v-html="problem.hint"></div>
                </el-card>
              </div>

              <div v-if="problem.source">
                <p class="title">Source</p>
                <p class="content">{{ problem.source }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :sm="24" :md="12" :lg="12" class="submit-detail">
          <!--problem main end-->
          <el-card :padding="20" id="submit-code" shadow="always">
            <CodeMirror
              :value.sync="code"
              :languages="problem.languages"
              :language="language"
              :theme="theme"
              @resetCode="onResetToTemplate"
              @changeTheme="onChangeTheme"
              @changeLang="onChangeLang"
            ></CodeMirror>
            <el-row>
              <el-col :sm="24" :md="10" :lg="10" style="margin-top:4px;">
                <div class="status" v-if="statusVisible">
                  <template
                    v-if="
                      !this.contestID ||
                      (this.contestID && OIContestRealTimePermission)
                    "
                  >
                    <span>Status</span>
                    <el-tag
                      type="dot"
                      :color="submissionStatus.color"
                      @click.native="handleRoute('/status/' + submissionId)"
                    >
                      {{ submissionStatus.text.replace(/ /g, "_") }}
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
                <div v-else-if="problem.my_status === 0">
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
import {
  JUDGE_STATUS,
  CONTEST_STATUS,
  buildProblemCodeKey,
} from "@/common/constants";
import {pie, largePie} from './chartData'
import api from "@/common/api";
import mMessage from '@/common/message'

// 只显示这些状态的图形占用
const filtedStatus = ["-1", "-2", "0", "1", "2", "3", "4", "8"];

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
      contestID: "",
      problemID: 1000,
      submitting: false,
      code: "",
      language: "C++",
      theme: "monokai",
      submissionId: "",
      submitted: false,
      result: {
        result: 9,
      },
      problem: {
        title: "A + B Problem",
        description:
          "<p>请计算两个整数的和并输出结果。</p><p>注意不要有不必要的输出，比如&quot;请输入 a 和 b 的值: &quot;，示例代码见隐藏部分。</p>",
        hint:
          '<p>C \u8bed\u8a00\u5b9e\u73b0:</p><pre><code class="lang-c++">#include &lt;stdio.h&gt;    \nint main(){\n    int a, b;\n    scanf(&quot;%d%d&quot;, &a, &b);\n    printf(&quot;%d\\n&quot;, a+b);\n    return 0;\n}</code></pre><p>Java \u5b9e\u73b0:</p><pre><code class="lang-java">import java.util.Scanner;\npublic class Main{\n    public static void main(String[] args){\n        Scanner in=new Scanner(System.in);\n        int a=in.nextInt();\n        int b=in.nextInt();\n        System.out.println((a+b));  \n    }\n}</code></pre>',
        input_description:
          "<p>\u4e24\u4e2a\u7528\u7a7a\u683c\u5206\u5f00\u7684\u6574\u6570.</p>",
        output_description: "<p>\u4e24\u6570\u4e4b\u548c</p>",
        my_status: 0,
        time_limit:'1000MS',
        memory_limit:'32MB',
        score:100,
        template: {},
        languages: ["C++", "Python3","C"],
        author:'Himit_ZH',
        rule_type: "OI",
        difficulty: "Mid",
        source: "\u7ecf\u5178\u9898\u76ee",
        samples: [
          {
            input: "1 1",
            output: "2",
          },
          {
            input: "2 3",
            output: "5",
          },
        ],
        tags: ['简单题','模拟题'],
      },
      pie: pie,
      largePie: largePie,
      // echarts 无法获取隐藏dom的大小，需手动指定
      largePieInitOpts: {
        width: '380',
        height: '380'
      }
    };
  },
  // beforeRouteEnter (to, from, next) {
  //   let problemCode = storage.get(buildProblemCodeKey(to.params.problemID, to.params.contestID))
  //   if (problemCode) {
  //     next(vm => {
  //       vm.language = problemCode.language
  //       vm.code = problemCode.code
  //       vm.theme = problemCode.theme
  //     })
  //   } else {
  //     next()
  //   }
  // },
  mounted() {
    this.$store.commit("changeContestItemVisible", { menu: false });
    this.init();
  },
  methods: {
    ...mapActions(["changeDomTitle"]),
    init() {
      // this.$Loading.start()
      this.contestID = this.$route.params.contestID;
      this.problemID = this.$route.params.problemID;
      let func =
        this.$route.name === "problem-details"
          ? "getProblem"
          : "getContestProblem";
      api[func](this.problemID, this.contestID).then(
        (res) => {
          this.$Loading.finish();
          let problem = res.data.data;
          this.changeDomTitle({ title: problem.title });
          api.submissionExists(problem.id).then((res) => {
            this.submissionExists = res.data.data;
          });
          problem.languages = problem.languages.sort();
          this.problem = problem;
          this.changePie(problem);

          // 在beforeRouteEnter中修改了, 说明本地有code，无需加载template
          if (this.code !== "") {
            return;
          }
          // try to load problem template
          this.language = this.problem.languages[0];
          let template = this.problem.template;
          if (template && template[this.language]) {
            this.code = template[this.language];
          }
        },
        () => {
          this.$Loading.error();
        }
      );
    },
    // changePie (problemData) {
    //   // 只显示特定的一些状态
    //   for (let k in problemData.statistic_info) {
    //     if (filtedStatus.indexOf(k) === -1) {
    //       delete problemData.statistic_info[k]
    //     }
    //   }
    //   let acNum = problemData.accepted_number
    //   let data = [
    //     {name: 'WA', value: problemData.submission_number - acNum},
    //     {name: 'AC', value: acNum}
    //   ]
    //   this.pie.series[0].data = data
    //   // 只把大图的AC selected下，这里需要做一下deepcopy
    //   let data2 = JSON.parse(JSON.stringify(data))
    //   data2[1].selected = true
    //   this.largePie.series[1].data = data2

    //   // 根据结果设置legend,没有提交过的legend不显示
    //   let legend = Object.keys(problemData.statistic_info).map(ele => JUDGE_STATUS[ele].short)
    //   if (legend.length === 0) {
    //     legend.push('AC', 'WA')
    //   }
    //   this.largePie.legend.data = legend

    //   // 把ac的数据提取出来放在最后
    //   let acCount = problemData.statistic_info['0']
    //   delete problemData.statistic_info['0']

    //   let largePieData = []
    //   Object.keys(problemData.statistic_info).forEach(ele => {
    //     largePieData.push({name: JUDGE_STATUS[ele].short, value: problemData.statistic_info[ele]})
    //   })
    //   largePieData.push({name: 'AC', value: acCount})
    //   this.largePie.series[0].data = largePieData
    // },
    handleRoute(route) {
      this.$router.push(route);
    },
    onChangeLang(newLang) {
      if (this.problem.template[newLang]) {
        if (this.code.trim() === "") {
          this.code = this.problem.template[newLang];
        }
      }
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
          let template = this.problem.template;
          if (template && template[this.language]) {
            this.code = template[this.language];
          } else {
            this.code = "";
          }
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
        let id = this.submissionId;
        api.getSubmission(id).then(
          (res) => {
            this.result = res.data.data;
            if (Object.keys(res.data.data.statistic_info).length !== 0) {
              this.submitting = false;
              this.submitted = false;
              clearTimeout(this.refreshStatus);
              this.init();
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
      this.refreshStatus = setTimeout(checkStatus, 2000);
    },
    submitCode() {
      if (this.code.trim() === "") {
        this.$error("m.Code_can_not_be_empty");
        return;
      }
      this.submissionId = "";
      this.result = { result: 9 };
      this.submitting = true;
      let data = {
        problem_id: this.problem.id,
        language: this.language,
        code: this.code,
        contest_id: this.contestID,
      };
      if (this.captchaRequired) {
        data.captcha = this.captchaCode;
      }
      const submitFunc = (data, detailsVisible) => {
        this.statusVisible = true;
        api.submitCode(data).then(
          (res) => {
            this.submissionId = res.data.data && res.data.data.submission_id;
            // 定时检查状态
            this.submitting = false;
            this.submissionExists = true;
            if (!detailsVisible) {
              this.$Modal.success({
                title: "Success",
                content: "Submit_code_successfully",
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
              "You_have_submission_in_this_problem_sure_to_cover_it" +
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
      mMessage.success('Sample copied successfully');
    },
    onCopyError(e) {
       mMessage.success('Sample copy failed');
    },
  },
  computed: {
    ...mapGetters([
      "problemSubmitDisabled",
      "contestRuleType",
      "OIContestRealTimePermission",
      "contestStatus",
    ]),
    contest() {
      return this.$store.state.contest.contest;
    },
    contestEnded() {
      return this.contestStatus === CONTEST_STATUS.ENDED;
    },
    submissionStatus() {
      return {
        text: JUDGE_STATUS[this.result.result]["name"],
        color: JUDGE_STATUS[this.result.result]["color"],
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

    this.$store.commit("changeContestItemVisible", { menu: true });
    storage.set(buildProblemCodeKey(this.problem._id, from.params.contestID), {
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
@media screen and (min-width: 768px) {
  .problem-detail {
    height: 672px;
    overflow-y: auto;
  }
  .problem-menu span{
    margin-left: 20px;
  }
  .question-intr{
    margin-top: 5px;
  }
}
.submit-detail{
  height:100%
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
.sample {
  align-items: stretch;
}
.sample-input,
.sample-output {
  width: 50%;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  margin-right: 5%;
}
.sample pre {
  flex: 1 1 auto;
  align-self: stretch;
  border-style: solid;
  background: transparent;
}

/* #submit-code {
    margin-top: 20px;
    margin-bottom: 20px;
  } */
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

