<template>
  <div class="flex-container">
    <div id="problem-main">
      <!--problem main-->
      <el-row>
        <el-col :sm="24" :md="24" :lg="12">
          <el-card :padding="10" shadow class="problem-detail">
            <div slot="header" class="panel-title">
              <span>{{ problemData.problem.title }}</span
              ><br />
              <span v-if="contestID && !contestEnded"
                ><el-tag effect="plain" size="small">比赛题目</el-tag></span
              >
              <div v-else-if="problemData.tags.length > 0" class="problem-tag">
                <el-popover placement="right-start" width="60" trigger="hover">
                  <el-tag
                    slot="reference"
                    size="small"
                    type="primary"
                    style="cursor: pointer;"
                    effect="plain"
                    >Show Tags</el-tag
                  >
                  <el-tag
                    v-for="tag in problemData.tags"
                    :key="tag"
                    effect="plain"
                    size="small"
                    style="margin-right:5px;margin-top:2px"
                    >{{ tag }}</el-tag
                  >
                </el-popover>
              </div>
              <div v-else-if="problemData.tags.length == 0" class="problem-tag">
                <el-tag effect="plain" size="small">暂无标签</el-tag>
              </div>
              <div class="problem-menu">
                <span v-if="!contestID">
                  <el-link
                    type="primary"
                    :underline="false"
                    @click="goProblemDiscussion"
                    ><i class="fa fa-comments" aria-hidden="true"></i>
                    Discussion</el-link
                  ></span
                >
                <span>
                  <el-link
                    type="primary"
                    :underline="false"
                    @click="graphVisible = !graphVisible"
                    ><i class="fa fa-pie-chart" aria-hidden="true"></i>
                    Statistic</el-link
                  ></span
                >
                <span>
                  <el-link
                    type="primary"
                    :underline="false"
                    @click="goProblemSubmission"
                    ><i class="fa fa-bars" aria-hidden="true"></i>
                    Solution</el-link
                  ></span
                >
              </div>
              <div class="question-intr">
                <span
                  >Time Limit：C/C++
                  {{ problemData.problem.timeLimit }}MS，Other
                  {{ problemData.problem.timeLimit * 2 }}MS</span
                ><br />
                <span
                  >Memory Limit：C/C++
                  {{ problemData.problem.memoryLimit }}MB，Other
                  {{ problemData.problem.memoryLimit * 2 }}MB</span
                ><br />
                <span
                  >Level：{{
                    PROBLEM_LEVEL[problemData.problem.difficulty]['name']
                  }}</span
                ><span
                  v-if="problemData.problem.type == 1"
                  style="margin-left: 10px;"
                  >Score：{{ problemData.problem.ioScore }}</span
                ><br />
                <span v-show="problemData.problem.author"
                  >Create By：{{ problemData.problem.author }}</span
                ><br />
              </div>
            </div>

            <div id="problem-content">
              <p class="title">Description</p>
              <p
                class="content markdown-body"
                v-html="problemData.problem.description"
                v-katex
                v-highlight
              ></p>
              <p class="title">Input</p>
              <p
                class="content markdown-body"
                v-html="problemData.problem.input"
                v-katex
                v-highlight
              ></p>

              <p class="title">Output</p>
              <p
                class="content markdown-body"
                v-html="problemData.problem.output"
                v-katex
                v-highlight
              ></p>

              <div
                v-for="(example, index) of problemData.problem.examples"
                :key="index"
              >
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
                    <p class="title">
                      Sample Output {{ index + 1 }}
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
                  <div
                    class="hint-content"
                    v-html="problemData.problem.hint"
                    v-katex
                    v-highlight
                  ></div>
                </el-card>
              </div>

              <div v-if="problemData.problem.source && !contestID">
                <p class="title">Source</p>
                <p class="content" v-html="problemData.problem.source"></p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :sm="24" :md="24" :lg="12">
          <el-card
            :padding="10"
            id="submit-code"
            shadow="always"
            class="submit-detail"
          >
            <CodeMirror
              :value.sync="code"
              :languages="problemData.languages"
              :language.sync="language"
              :theme.sync="theme"
              @resetCode="onResetToTemplate"
              @changeTheme="onChangeTheme"
              @changeLang="onChangeLang"
            ></CodeMirror>
            <el-row>
              <el-col :sm="24" :md="10" :lg="10" style="margin-top:4px;">
                <div v-if="!isAuthenticated">
                  <el-alert
                    type="info"
                    show-icon
                    effect="dark"
                    :closable="false"
                    >Please login first</el-alert
                  >
                </div>
                <div class="status" v-if="statusVisible">
                  <template v-if="result.status == JUDGE_STATUS_RESERVE['sf']">
                    <span>Status:</span>
                    <el-tag
                      effect="dark"
                      :color="submissionStatus.color"
                      @click.native="reSubmit(submissionId)"
                    >
                      <i class="el-icon-refresh"></i>
                      {{ submissionStatus.text }}
                    </el-tag>
                  </template>
                  <template
                    v-else-if="
                      !this.contestID ||
                        (this.contestID &&
                          ContestRealTimePermission &&
                          this.contestRuleType == RULE_TYPE.OI) ||
                        (this.contestID &&
                          this.contestRuleType == RULE_TYPE.ACM)
                    "
                  >
                    <span>Status:</span>
                    <el-tag
                      effect="dark"
                      :color="submissionStatus.color"
                      @click.native="submissionRoute"
                    >
                      <i class="fa fa-circle" aria-hidden="true"></i>
                      {{ submissionStatus.text }}
                    </el-tag>
                  </template>
                  <template
                    v-else-if="
                      this.contestID &&
                        !ContestRealTimePermission &&
                        this.contestRuleType == RULE_TYPE.OI
                    "
                  >
                    <el-alert
                      type="success"
                      show-icon
                      effect="dark"
                      :closable="false"
                      >Submitted successfully</el-alert
                    >
                  </template>
                </div>
                <div
                  v-else-if="
                    !this.contestID &&
                      problemData.myStatus == JUDGE_STATUS_RESERVE.ac
                  "
                >
                  <el-alert
                    type="success"
                    show-icon
                    effect="dark"
                    :closable="false"
                    >You have solved the problem</el-alert
                  >
                </div>
                <div
                  v-else-if="
                    this.contestID &&
                      !ContestRealTimePermission &&
                      this.contestRuleType == RULE_TYPE.OI &&
                      submissionExists
                  "
                >
                  <el-alert
                    type="success"
                    show-icon
                    effect="dark"
                    :closable="false"
                    >You have submitted a solution</el-alert
                  >
                </div>
                <div v-if="contestEnded">
                  <el-alert
                    type="warning"
                    show-icon
                    effect="dark"
                    :closable="false"
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
        <el-button type="ghost" @click="graphVisible = false" size="small"
          >Close</el-button
        >
      </div>
    </el-dialog>

    <el-dialog :visible.sync="submitPwdVisible" width="340px">
      <el-form>
        <el-form-item label="Please Enter the Contest Protect Password">
          <el-input
            placeholder="Please Enter the Contest Protect Password"
            v-model="submitPwd"
            show-password
          ></el-input>
        </el-form-item>
        <el-button
          type="primary"
          round
          style="margin-left:130px"
          @click="submitCode"
        >
          OK
        </el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
import CodeMirror from '@/components/oj/common/CodeMirror.vue';
import storage from '@/common/storage';
import utils from '@/common/utils';
import {
  JUDGE_STATUS,
  CONTEST_STATUS,
  JUDGE_STATUS_RESERVE,
  buildProblemCodeKey,
  RULE_TYPE,
  PROBLEM_LEVEL,
} from '@/common/constants';
import { pie, largePie } from './chartData';
import api from '@/common/api';
import myMessage from '@/common/message';

// 只显示这些状态的图形占用
const filtedStatus = ['wa', 'ce', 'ac', 'tle', 'mle', 're', 'pe'];

export default {
  name: 'ProblemDetails',
  components: {
    CodeMirror,
  },
  data() {
    return {
      statusVisible: false,
      captchaRequired: false,
      graphVisible: false,
      submissionExists: false,
      captchaCode: '',
      captchaSrc: '',
      contestID: 0,
      problemID: '',
      submitting: false,
      code: '',
      language: 'C',
      isRemote: false,

      theme: 'solarized',
      submissionId: '',
      submitted: false,
      submitPwdVisible: false,
      submitPwd: '',
      result: {
        status: 9,
      },
      problemData: {
        problem: {
          difficulty: 0,
        },
        problemCount: {},
        tags: [],
        languages: [],
        codeTemplate: {},
      },
      pie: pie,
      largePie: largePie,
      // echarts 无法获取隐藏dom的大小，需手动指定
      largePieInitOpts: {
        width: '380',
        height: '380',
      },
      JUDGE_STATUS_RESERVE: {},
      PROBLEM_LEVEL: {},
      RULE_TYPE: {},
    };
  },
  // 获取缓存中的该题的做题代码，代码语言，代码风格
  beforeRouteEnter(to, from, next) {
    let problemCode = storage.get(
      buildProblemCodeKey(to.params.problemID, to.params.contestID)
    );
    if (problemCode) {
      next((vm) => {
        vm.language = problemCode.language;
        vm.code = problemCode.code;
        vm.theme = problemCode.theme;
      });
    } else {
      next();
    }
  },
  created() {
    this.JUDGE_STATUS_RESERVE = Object.assign({}, JUDGE_STATUS_RESERVE);
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
    this.RULE_TYPE = Object.assign({}, RULE_TYPE);
  },
  mounted() {
    this.init();
  },
  methods: {
    ...mapActions(['changeDomTitle']),
    init() {
      if (this.$route.params.contestID) {
        this.contestID = this.$route.params.contestID;
      }
      this.problemID = this.$route.params.problemID;
      let func =
        this.$route.name === 'ProblemDetails'
          ? 'getProblem'
          : 'getContestProblem';
      api[func](this.problemID, this.contestID).then(
        (res) => {
          let result = res.data.data;
          this.changeDomTitle({ title: result.problem.title });
          result['myStatus'] = -10; // 设置默认值
          if (this.isAuthenticated) {
            let pidList = [result.problem.id];
            let isContestProblemList = false;
            api
              .getUserProblemStatus(pidList, isContestProblemList)
              .then((res) => {
                let statusMap = res.data.data;
                if (statusMap[result.problem.id] != -10) {
                  this.submissionExists = true;
                  result.myStatus = statusMap[result.problem.id];
                } else {
                  this.submissionExists = false;
                }
              });
          }
          result.problem.examples = utils.stringToExamples(
            result.problem.examples
          );
          if (result.problem.description) {
            result.problem.description = this.$markDown.render(
              result.problem.description.toString()
            );
          }
          if (result.problem.input) {
            result.problem.input = this.$markDown.render(
              result.problem.input.toString()
            );
          }
          if (result.problem.output) {
            result.problem.output = this.$markDown.render(
              result.problem.output.toString()
            );
          }
          if (result.problem.hint) {
            result.problem.hint = this.$markDown.render(
              result.problem.hint.toString()
            );
          }
          this.problemData = result;
          this.isRemote = result.problem.isRemote;
          this.changePie(result.problemCount);

          // 在beforeRouteEnter中修改了, 说明本地有code，无需加载template
          if (this.code !== '') {
            return;
          }
          // try to load problem template
          this.language = this.problemData.languages[0];
          let codeTemplate = this.problemData.codeTemplate;
          if (codeTemplate && codeTemplate[this.language]) {
            this.code = codeTemplate[this.language];
          }
        },
        () => {}
      );
    },
    changePie(problemData) {
      let total = problemData.total;
      let acNum = problemData.ac;
      // 该状态结果数为0的不显示,同时一些无关参数也排除
      for (let k in problemData) {
        if (problemData[k] == 0 || filtedStatus.indexOf(k) === -1) {
          delete problemData[k];
        }
      }

      let data = [
        { name: 'WA', value: total - acNum },
        { name: 'AC', value: acNum },
      ];
      this.pie.series[0].data = data;
      // 只把大图的AC selected下，这里需要做一下deepcopy
      let data2 = JSON.parse(JSON.stringify(data));
      data2[1].selected = true;
      this.largePie.series[1].data = data2;

      // 根据结果设置legend,没有提交过的legend不显示
      let legend = Object.keys(problemData).map((ele) =>
        (ele + '').toUpperCase()
      );
      if (legend.length === 0) {
        legend.push('AC', 'WA');
      }
      this.largePie.legend.data = legend;

      // 把ac的数据提取出来放在最后
      let acCount = problemData.ac;
      delete problemData.ac;

      let largePieData = [];
      Object.keys(problemData).forEach((ele) => {
        largePieData.push({
          name: (ele + '').toUpperCase(),
          value: problemData[ele],
        });
      });
      largePieData.push({ name: 'AC', value: acCount });
      this.largePie.series[0].data = largePieData;
    },

    goProblemSubmission() {
      if (this.contestID) {
        this.$router.push({
          name: 'ContestSubmissionList',
          params: { contestID: this.contestID },
          query: { problemID: this.problemID },
        });
      } else {
        this.$router.push({
          name: 'SubmissionList',
          query: { problemID: this.problemID },
        });
      }
    },
    goProblemDiscussion() {
      this.$router.push({
        name: 'ProblemDiscussion',
        params: { problemID: this.problemID },
      });
    },

    handleRoute(route) {
      this.$router.push(route);
    },
    onChangeLang(newLang) {
      if (this.code.trim() != '') {
        if (this.code == this.problemData.codeTemplate[this.language]) {
          //原语言模板未变化，只改变语言
          if (this.problemData.codeTemplate[newLang]) {
            this.code = this.problemData.codeTemplate[newLang];
          } else {
            this.code = '';
          }
        }
      }
      this.language = newLang;
    },
    onChangeTheme(newTheme) {
      this.theme = newTheme;
    },
    onResetToTemplate() {
      this.$confirm('是否确定要重置代码模板？', '提示', {
        cancelButtonText: '取消',
        confirmButtonText: '确定',
        type: 'warning',
      })
        .then(() => {
          let codeTemplate = this.problemData.codeTemplate;
          if (codeTemplate && codeTemplate[this.language]) {
            this.code = codeTemplate[this.language];
          } else {
            this.code = '';
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
        let submitId = this.submissionId;
        api.getSubmission(submitId).then(
          (res) => {
            this.result.status = res.data.data.submission.status;
            if (Object.keys(res.data.data.submission).length !== 0) {
              // status不为判题和排队中才表示此次判题结束
              if (
                res.data.data.submission.status !=
                  JUDGE_STATUS_RESERVE['Pending'] &&
                res.data.data.submission.status !=
                  JUDGE_STATUS_RESERVE['Compiling'] &&
                res.data.data.submission.status !=
                  JUDGE_STATUS_RESERVE['Judging']
              ) {
                this.submitting = false;
                this.submitted = false;
                clearTimeout(this.refreshStatus);
                this.init();
              } else {
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
      if (this.code.trim() === '') {
        myMessage.error('提交的代码不能为空！');
        return;
      }

      // 比赛题目需要检查是否有权限提交
      if (!this.canSubmit && this.$route.params.contestID) {
        // 密码为空，需要重新输入
        if (!this.submitPwd) {
          this.submitPwdVisible = true;
          return;
        } else {
          this.submitPwdVisible = false;
        }
      }

      this.submissionId = '';
      this.result = { status: 9 };
      this.submitting = true;
      let data = {
        pid: this.problemID, // 如果是比赛题目就为display_id
        language: this.language,
        code: this.code,
        cid: this.contestID,
        protectContestPwd: this.submitPwd,
        isRemote: this.isRemote,
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
                title: 'Success',
                content: '代码提交成功！',
              });
              return;
            } else {
              myMessage.success(res.data.msg);
            }
            // 更新store的可提交权限
            if (!this.canSubmit) {
              this.$store.commit('contestIntoAccess', { access: true });
            }
            this.submitted = true;
            this.checkSubmissionStatus();
          },
          (res) => {
            // this.getCaptchaSrc();
            // if (res.data.data.startsWith('Captcha is required')) {
            //   this.captchaRequired = true;
            // }
            this.submitting = false;
            this.statusVisible = false;
          }
        );
      };

      if (
        this.contestRuleType === RULE_TYPE.OI &&
        !this.ContestRealTimePermission
      ) {
        if (this.submissionExists) {
          this.$confirm(
            '你已经有该题目的提交了，确定要再一次提交覆盖之前的提交记录？',
            '警告',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
          )
            .then(() => {
              // 暂时解决对话框与后面提示对话框冲突的问题(否则一闪而过）
              setTimeout(() => {
                submitFunc(data, false);
              }, 1000);
            })
            .catch(() => {
              this.submitting = false;
            });
        } else {
          submitFunc(data, false);
        }
      } else {
        submitFunc(data, true);
      }
    },

    reSubmit(submitId) {
      this.result = { status: 9 };
      this.submitting = true;
      api.reSubmitRemoteJudge(submitId).then(
        (res) => {
          myMessage.success(res.data.msg);
          this.submitted = true;
          this.checkSubmissionStatus();
        },
        (err) => {
          this.submitting = false;
          this.statusVisible = false;
        }
      );
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
      'problemSubmitDisabled',
      'contestRuleType',
      'ContestRealTimePermission',
      'contestStatus',
      'isAuthenticated',
      'canSubmit',
    ]),
    contest() {
      return this.$store.state.contest.contest;
    },
    contestEnded() {
      return this.contestStatus === CONTEST_STATUS.ENDED;
    },
    submissionStatus() {
      return {
        text: JUDGE_STATUS[this.result.status]['name'],
        color: JUDGE_STATUS[this.result.status]['rgb'],
      };
    },
    submissionRoute() {
      if (this.contestID) {
        // 比赛提交详情
        this.$router.push({
          name: 'ContestSubmissionDeatil',
          params: {
            contestID: this.contestID,
            problemID: this.problemID,
            submitID: this.submissionId,
          },
        });
      } else {
        this.$router.push({
          name: 'SubmissionDeatil',
          params: { submitID: this.submissionId },
        });
      }
    },
  },
  beforeRouteLeave(to, from, next) {
    // 防止切换组件后仍然不断请求
    clearInterval(this.refreshStatus);
    storage.set(
      buildProblemCodeKey(
        this.problemData.problem.problemId,
        from.params.contestID
      ),
      {
        code: this.code,
        language: this.language,
        theme: this.theme,
      }
    );
    next();
  },
  watch: {
    $route() {
      this.init();
    },
    isAuthenticated(newVal) {
      if (newVal === true) {
        this.init();
      }
    },
  },
};
</script>

<style scoped>
#problem-main {
  flex: auto;
}
.problem-menu {
  float: left;
}
a {
  color: #3091f2 !important ;
}
.problem-menu span {
  margin-left: 5px;
}
.el-link {
  font-size: 16px !important;
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

.submit-detail {
  height: 100%;
}

@media screen and (min-width: 768px) {
  .problem-detail {
    height: 700px !important;
    overflow-y: auto;
  }
  .submit-detail {
    height: 700px !important;
    overflow-y: auto;
  }
  .problem-tag {
    display: inline;
  }
  .problem-menu {
    float: right;
  }
  .problem-menu span {
    margin-left: 10px;
  }
  .question-intr {
    margin-top: 6px;
  }
}

@media screen and (max-width: 1080px) {
  .submit-detail {
    padding-top: 20px;
  }
  .submit-detail {
    height: 100%;
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
  font-size: 16px;
  font-weight: 600;
  margin: 25px 0 8px 0;
  color: #3091f2;
}
#problem-content .copy {
  padding-left: 8px;
}

.hint-content {
  font-size: 15px !important;
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
  padding: 5px 10px;
  white-space: pre-wrap;
  margin-top: 15px;
  margin-bottom: 15px;
  background: #f8f8f9;
  border: 1px dashed #e9eaec;
}
#submit-code {
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
  padding: 10px 10px !important;
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
