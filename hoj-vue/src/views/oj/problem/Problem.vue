<template>
  <div>
    <div id="problem-main">
      <!--problem main-->
      <el-row class="problem-box">
        <el-col :sm="24" :md="24" :lg="12" class="problem-left">
          <el-tabs
            v-model="activeName"
            type="border-card"
            @tab-click="handleClickTab"
          >
            <el-tab-pane name="problemDetail" v-loading="loading">
              <span slot="label"
                ><i class="fa fa-list-alt"></i>
                {{ $t('m.Problem_Description') }}</span
              >
              <div :padding="10" shadow class="problem-detail">
                <div slot="header" class="panel-title">
                  <span>{{ problemData.problem.title }}</span
                  ><br />
                  <span v-if="contestID && !contestEnded"
                    ><el-tag effect="plain" size="small">{{
                      $t('m.Contest_Problem')
                    }}</el-tag></span
                  >
                  <div
                    v-else-if="problemData.tags.length > 0"
                    class="problem-tag"
                  >
                    <el-popover
                      placement="right-start"
                      width="60"
                      trigger="hover"
                    >
                      <el-tag
                        slot="reference"
                        size="small"
                        type="primary"
                        style="cursor: pointer;"
                        effect="plain"
                        >{{ $t('m.Show_Tags') }}</el-tag
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
                  <div
                    v-else-if="problemData.tags.length == 0"
                    class="problem-tag"
                  >
                    <el-tag effect="plain" size="small">{{
                      $t('m.No_tag')
                    }}</el-tag>
                  </div>
                  <div class="problem-menu">
                    <span v-if="!contestID">
                      <el-link
                        type="primary"
                        :underline="false"
                        @click="goProblemDiscussion"
                        ><i class="fa fa-comments" aria-hidden="true"></i>
                        {{ $t('m.Problem_Discussion') }}</el-link
                      ></span
                    >
                    <span>
                      <el-link
                        type="primary"
                        :underline="false"
                        @click="graphVisible = !graphVisible"
                        ><i class="fa fa-pie-chart" aria-hidden="true"></i>
                        {{ $t('m.Statistic') }}</el-link
                      ></span
                    >
                    <span>
                      <el-link
                        type="primary"
                        :underline="false"
                        @click="goProblemSubmission"
                        ><i class="fa fa-bars" aria-hidden="true"></i>
                        {{ $t('m.Solutions') }}</el-link
                      ></span
                    >
                  </div>
                  <div class="question-intr">
                    <template v-if="!isCFProblem">
                      <span
                        >{{ $t('m.Time_Limit') }}：C/C++
                        {{ problemData.problem.timeLimit }}MS，{{
                          $t('m.Other')
                        }}
                        {{ problemData.problem.timeLimit * 2 }}MS</span
                      ><br />
                      <span
                        >{{ $t('m.Memory_Limit') }}：C/C++
                        {{ problemData.problem.memoryLimit }}MB，{{
                          $t('m.Other')
                        }}
                        {{ problemData.problem.memoryLimit * 2 }}MB</span
                      ><br />
                    </template>

                    <template v-else>
                      <span
                        >{{ $t('m.Time_Limit') }}：{{
                          problemData.problem.timeLimit
                        }}MS</span
                      >
                      <br />
                      <span
                        >{{ $t('m.Memory_Limit') }}：{{
                          problemData.problem.memoryLimit
                        }}MB</span
                      ><br />
                    </template>
                    <template v-if="problemData.problem.difficulty != null">
                      <span
                        >{{ $t('m.Level') }}：{{
                          PROBLEM_LEVEL[problemData.problem.difficulty]['name']
                        }}</span
                      >
                      <br />
                    </template>
                    <template v-if="problemData.problem.type == 1">
                      <span
                        >{{ $t('m.Score') }}：{{ problemData.problem.ioScore }}
                      </span>
                      <span v-if="!contestID" style="margin-left:5px;">
                        {{ $t('m.OI_Rank_Score') }}：{{
                          calcOIRankScore(
                            problemData.problem.ioScore,
                            problemData.problem.difficulty
                          )
                        }}(0.1*{{ $t('m.Score') }}+2*{{ $t('m.Level') }})
                      </span>
                      <br />
                    </template>

                    <template v-if="problemData.problem.author">
                      <span
                        >{{ $t('m.Created') }}：{{
                          problemData.problem.author
                        }}</span
                      ><br />
                    </template>
                  </div>
                </div>

                <div id="problem-content">
                  <p class="title">{{ $t('m.Description') }}</p>
                  <p
                    class="content markdown-body"
                    v-html="problemData.problem.description"
                    v-katex
                    v-highlight
                  ></p>
                  <p class="title">{{ $t('m.Input') }}</p>
                  <p
                    class="content markdown-body"
                    v-html="problemData.problem.input"
                    v-katex
                    v-highlight
                  ></p>

                  <p class="title">{{ $t('m.Output') }}</p>
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
                          {{ $t('m.Sample_Input') }} {{ index + 1 }}
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
                          {{ $t('m.Sample_Output') }} {{ index + 1 }}
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

                  <template v-if="problemData.problem.hint">
                    <p class="title">{{ $t('m.Hint') }}</p>
                    <el-card dis-hover>
                      <p
                        class="hint-content markdown-body"
                        v-html="problemData.problem.hint"
                        v-katex
                        v-highlight
                      ></p>
                    </el-card>
                  </template>

                  <template v-if="problemData.problem.source && !contestID">
                    <p class="title">{{ $t('m.Source') }}</p>
                    <p class="content" v-html="problemData.problem.source"></p>
                  </template>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane name="mySubmission">
              <span slot="label"
                ><i class="el-icon-time"></i> {{ $t('m.My_Submission') }}</span
              >
              <template v-if="!isAuthenticated">
                <div style="margin:50px 0px;margin-left:-20px;">
                  <el-alert
                    :title="$t('m.Please_login_first')"
                    type="warning"
                    center
                    :closable="false"
                    :description="$t('m.Login_to_view_your_submission_history')"
                    show-icon
                  >
                  </el-alert>
                </div>
              </template>
              <template v-else>
                <div style="margin:20px 0px;margin-right:10px;">
                  <vxe-table
                    align="center"
                    :data="mySubmissions"
                    stripe
                    auto-resize
                    border="inner"
                    :loading="loadingTable"
                  >
                    <vxe-table-column
                      :title="$t('m.Submit_Time')"
                      min-width="96"
                    >
                      <template v-slot="{ row }">
                        <span
                          ><el-tooltip
                            :content="row.submitTime | localtime"
                            placement="top"
                          >
                            <span>{{ row.submitTime | fromNow }}</span>
                          </el-tooltip></span
                        >
                      </template>
                    </vxe-table-column>
                    <vxe-table-column
                      field="status"
                      :title="$t('m.Status')"
                      min-width="160"
                    >
                      <template v-slot="{ row }">
                        <span :class="getStatusColor(row.status)">{{
                          JUDGE_STATUS[row.status].name
                        }}</span>
                      </template>
                    </vxe-table-column>
                    <vxe-table-column :title="$t('m.Time')" min-width="96">
                      <template v-slot="{ row }">
                        <span>{{ submissionTimeFormat(row.time) }}</span>
                      </template>
                    </vxe-table-column>
                    <vxe-table-column :title="$t('m.Memory')" min-width="96">
                      <template v-slot="{ row }">
                        <span>{{ submissionMemoryFormat(row.memory) }}</span>
                      </template>
                    </vxe-table-column>
                    <vxe-table-column
                      :title="$t('m.Score')"
                      min-width="64"
                      v-if="problemData.problem.type == 1"
                    >
                      <template v-slot="{ row }">
                        <template v-if="contestID && row.score != null">
                          <el-tag
                            effect="plain"
                            size="medium"
                            :type="JUDGE_STATUS[row.status]['type']"
                            >{{ row.score }}</el-tag
                          >
                        </template>
                        <template v-else-if="row.score != null">
                          <el-tooltip placement="top">
                            <div slot="content">
                              {{ $t('m.Problem_Score') }}：{{
                                row.score != null ? row.score : $t('m.Unknown')
                              }}<br />{{ $t('m.OI_Rank_Score') }}：{{
                                row.oiRankScore != null
                                  ? row.oiRankScore
                                  : $t('m.Unknown')
                              }}<br />
                              {{
                                $t('m.OI_Rank_Calculation_Rule')
                              }}：(score*0.1+difficulty*2)*(ac_cases/sum_cases)
                            </div>
                            <el-tag
                              effect="plain"
                              size="medium"
                              :type="JUDGE_STATUS[row.status]['type']"
                              >{{ row.score }}</el-tag
                            >
                          </el-tooltip>
                        </template>
                        <template
                          v-else-if="
                            row.status == JUDGE_STATUS_RESERVE['Pending'] ||
                              row.status == JUDGE_STATUS_RESERVE['Compiling'] ||
                              row.status == JUDGE_STATUS_RESERVE['Judging']
                          "
                        >
                          <el-tag
                            effect="plain"
                            size="medium"
                            :type="JUDGE_STATUS[row.status]['type']"
                          >
                            <i class="el-icon-loading"></i>
                          </el-tag>
                        </template>
                        <template v-else>
                          <el-tag
                            effect="plain"
                            size="medium"
                            :type="JUDGE_STATUS[row.status]['type']"
                            >--</el-tag
                          >
                        </template>
                      </template>
                    </vxe-table-column>
                    <vxe-table-column
                      field="language"
                      :title="$t('m.Language')"
                      show-overflow
                      min-width="130"
                    >
                      <template v-slot="{ row }">
                        <el-tooltip
                          class="item"
                          effect="dark"
                          :content="$t('m.View_submission_details')"
                          placement="top"
                        >
                          <el-button
                            type="text"
                            @click="showSubmitDetail(row)"
                            >{{ row.language }}</el-button
                          >
                        </el-tooltip>
                      </template>
                    </vxe-table-column>
                  </vxe-table>
                  <Pagination
                    :total="mySubmission_total"
                    :page-size="mySubmission_limit"
                    @on-change="getMySubmission"
                    :current.sync="mySubmission_currentPage"
                  ></Pagination>
                </div>
              </template>
            </el-tab-pane>
          </el-tabs>
        </el-col>
        <div
          class="problem-resize hidden-md-and-down"
          :title="$t('m.Shrink_Sidebar')"
        >
          <span>⋮</span>
          <el-tooltip
            :content="
              toWatchProblem
                ? $t('m.View_Problem_Content')
                : $t('m.Only_View_Problem')
            "
            placement="right"
          >
            <el-button
              icon="el-icon-caret-right"
              circle
              class="right-fold fold"
              @click.native="onlyWatchProblem"
              size="mini"
            ></el-button>
          </el-tooltip>
          <el-tooltip
            :content="$t('m.Put_away_the_full_screen_and_write_the_code')"
            placement="left"
          >
            <el-button
              icon="el-icon-caret-left"
              circle
              class="left-fold fold"
              @click.native="resetWatch(false)"
              size="mini"
              v-show="toResetWatch"
            ></el-button>
          </el-tooltip>
        </div>
        <el-col :sm="24" :md="24" :lg="12" class="problem-right">
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
                    >{{ $t('m.Please_login_first') }}</el-alert
                  >
                </div>
                <div class="status" v-if="statusVisible">
                  <template v-if="result.status == JUDGE_STATUS_RESERVE['sf']">
                    <span>{{ $t('m.Status') }}:</span>
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
                    v-else-if="result.status == JUDGE_STATUS_RESERVE['snr']"
                  >
                    <el-alert
                      type="warning"
                      show-icon
                      effect="dark"
                      :closable="false"
                      >{{ $t('m.Submitted_Not_Result') }}</el-alert
                    >
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
                    <span>{{ $t('m.Status') }}:</span>
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
                      >{{ $t('m.Submitted_successfully') }}</el-alert
                    >
                  </template>
                </div>
                <div
                  v-else-if="
                    (!this.contestID ||
                      this.contestRuleType == RULE_TYPE.ACM) &&
                      problemData.myStatus == JUDGE_STATUS_RESERVE.ac
                  "
                >
                  <el-alert
                    type="success"
                    show-icon
                    effect="dark"
                    :closable="false"
                    >{{ $t('m.You_have_solved_the_problem') }}</el-alert
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
                    >{{ $t('m.You_have_submitted_a_solution') }}</el-alert
                  >
                </div>
                <div v-if="contestEnded">
                  <el-alert
                    type="warning"
                    show-icon
                    effect="dark"
                    :closable="false"
                    >{{ $t('m.Contest_has_ended') }}</el-alert
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
                  <span v-if="submitting">{{ $t('m.Submitting') }}</span>
                  <span v-else>{{ $t('m.Submit') }}</span>
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
        <el-button type="ghost" @click="graphVisible = false" size="small">{{
          $t('m.Close')
        }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="submitPwdVisible" width="340px">
      <el-form>
        <el-form-item :label="$t('m.Enter_the_contest_password')" required>
          <el-input
            :placeholder="$t('m.Enter_the_contest_password')"
            v-model="submitPwd"
            show-password
          ></el-input>
        </el-form-item>
        <el-button
          type="primary"
          round
          style="margin-left:130px"
          @click="checkContestPassword"
        >
          {{ $t('m.Submit') }}
        </el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
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
import { addCodeBtn } from '@/common/codeblock';
const CodeMirror = () => import('@/components/oj/common/CodeMirror.vue');
import Pagination from '@/components/oj/common/Pagination';
// 只显示这些状态的图形占用
const filtedStatus = ['wa', 'ce', 'ac', 'pa', 'tle', 'mle', 're', 'pe'];

export default {
  name: 'ProblemDetails',
  components: {
    CodeMirror,
    Pagination,
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
      JUDGE_STATUS: {},
      PROBLEM_LEVEL: {},
      RULE_TYPE: {},
      toResetWatch: false,
      toWatchProblem: false,
      activeName: 'problemDetail',
      loadingTable: false,
      mySubmission_total: 0,
      mySubmission_limit: 15,
      mySubmission_currentPage: 1,
      mySubmissions: [],
      loading: false,
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
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
    this.RULE_TYPE = Object.assign({}, RULE_TYPE);
  },
  mounted() {
    this.init();
    this.dragControllerDiv();
  },
  methods: {
    ...mapActions(['changeDomTitle']),
    handleClickTab({ name }) {
      if (name == 'mySubmission') {
        this.getMySubmission();
      }
    },
    getMySubmission() {
      let params = {
        onlyMine: true,
        currentPage: this.mySubmission_currentPage,
        problemID: this.problemID,
        contestID: this.contestID,
        limit: this.mySubmission_limit,
      };
      if (this.contestID) {
        if (this.contestStatus == CONTEST_STATUS.SCHEDULED) {
          params.beforeContestSubmit = true;
        } else {
          params.beforeContestSubmit = false;
        }
      }
      let func = this.contestID
        ? 'getContestSubmissionList'
        : 'getSubmissionList';
      this.loadingTable = true;
      api[func](this.mySubmission_limit, utils.filterEmptyValue(params))
        .then(
          (res) => {
            let data = res.data.data;
            this.mySubmissions = data.records;
            this.mySubmission_total = data.total;
            this.loadingTable = false;
          },
          (err) => {
            this.loadingTable = false;
          }
        )
        .catch(() => {
          this.loadingTable = false;
        });
    },
    getStatusColor(status) {
      return 'el-tag el-tag--medium status-' + JUDGE_STATUS[status].color;
    },
    submissionTimeFormat(time) {
      return utils.submissionTimeFormat(time);
    },

    submissionMemoryFormat(memory) {
      return utils.submissionMemoryFormat(memory);
    },

    showSubmitDetail(row) {
      if (row.cid != 0) {
        // 比赛提交详情
        this.$router.push({
          name: 'ContestSubmissionDeatil',
          params: {
            contestID: this.$route.params.contestID,
            problemID: row.displayId,
            submitID: row.submitId,
          },
        });
      } else {
        this.$router.push({
          name: 'SubmissionDeatil',
          params: { submitID: row.submitId },
        });
      }
    },

    dragControllerDiv() {
      var resize = document.getElementsByClassName('problem-resize');
      var left = document.getElementsByClassName('problem-left');
      var right = document.getElementsByClassName('problem-right');
      var box = document.getElementsByClassName('problem-box');
      const _this = this;
      for (let i = 0; i < resize.length; i++) {
        // 鼠标按下事件
        resize[i].onmousedown = function(e) {
          //颜色改变提醒
          resize[i].style.background = '#818181';
          var startX = e.clientX;
          // 鼠标拖动事件
          document.onmousemove = function(e) {
            resize[i].left = startX;
            var endX = e.clientX;
            var moveLen = resize[i].left + (endX - startX); // （endx-startx）=移动的距离。resize[i].left+移动的距离=左边区域最后的宽度
            var maxT = box[i].clientWidth - resize[i].offsetWidth; // 容器宽度 - 左边区域的宽度 = 右边区域的宽度
            if (moveLen < 420) moveLen = 0; // 左边区域的最小宽度为420px
            if (moveLen > maxT - 580) moveLen = maxT - 580; //右边区域最小宽度为580px
            resize[i].style.left = moveLen; // 设置左侧区域的宽度
            for (let j = 0; j < left.length; j++) {
              left[j].style.width = moveLen + 'px';
              let tmp = box[i].clientWidth - moveLen - 11;
              right[j].style.width = tmp + 'px';
              if (tmp > 0) {
                _this.toResetWatch = false;
              }
              if (moveLen == 0) {
                _this.toWatchProblem = true;
              }
            }
          };
          // 鼠标松开事件
          document.onmouseup = function(evt) {
            //颜色恢复
            resize[i].style.background = '#d6d6d6';
            document.onmousemove = null;
            document.onmouseup = null;
            resize[i].releaseCapture && resize[i].releaseCapture(); //当你不在需要继续获得鼠标消息就要应该调用ReleaseCapture()释放掉
          };
          resize[i].setCapture && resize[i].setCapture(); //该函数在属于当前线程的指定窗口里设置鼠标捕获
          return false;
        };
      }
    },
    onlyWatchProblem() {
      if (this.toWatchProblem) {
        this.resetWatch(true);
        this.toWatchProblem = false;
        return;
      }
      var resize = document.getElementsByClassName('problem-resize');
      var left = document.getElementsByClassName('problem-left');
      var right = document.getElementsByClassName('problem-right');
      var box = document.getElementsByClassName('problem-box');
      for (let i = 0; i < resize.length; i++) {
        resize[i].style.left = box[i].clientWidth - 11;
        for (let j = 0; j < left.length; j++) {
          left[j].style.width = box[i].clientWidth - 11 + 'px';
          right[j].style.width = '0px';
        }
      }
      this.toResetWatch = true;
    },
    resetWatch(minLeft = false) {
      var resize = document.getElementsByClassName('problem-resize');
      var left = document.getElementsByClassName('problem-left');
      var right = document.getElementsByClassName('problem-right');
      var box = document.getElementsByClassName('problem-box');
      for (let i = 0; i < resize.length; i++) {
        let leftWidth = 0;
        if (minLeft) {
          leftWidth = 431; // 恢复左边最小420px+滑块11px
        } else {
          leftWidth = box[i].clientWidth - 580; // 右边最小580px
        }
        resize[i].style.left = leftWidth - 11;
        for (let j = 0; j < left.length; j++) {
          left[j].style.width = leftWidth - 11 + 'px';
          right[j].style.width = box[i].clientWidth - leftWidth + 'px';
        }
      }
      this.toResetWatch = false;
    },
    init() {
      if (this.$route.params.contestID) {
        this.contestID = this.$route.params.contestID;
      }
      this.problemID = this.$route.params.problemID;
      let func =
        this.$route.name === 'ProblemDetails'
          ? 'getProblem'
          : 'getContestProblem';
      this.loading = true;
      api[func](this.problemID, this.contestID).then(
        (res) => {
          let result = res.data.data;
          this.changeDomTitle({ title: result.problem.title });
          result['myStatus'] = -10; // 设置默认值

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

          this.loading = false;

          if (this.isAuthenticated) {
            let pidList = [result.problem.id];
            let isContestProblemList = this.contestID ? true : false;
            api
              .getUserProblemStatus(
                pidList,
                isContestProblemList,
                this.contestID
              )
              .then((res) => {
                let statusMap = res.data.data;
                if (statusMap[result.problem.id].status != -10) {
                  this.submissionExists = true;
                  this.problemData.myStatus =
                    statusMap[result.problem.id].status;
                } else {
                  this.submissionExists = false;
                }
              });
          }

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
          this.$nextTick((_) => {
            addCodeBtn();
          });
        },
        (err) => {
          this.loading = false;
        }
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
          query: { problemID: this.problemID, completeProblemID: true },
        });
      } else {
        this.$router.push({
          name: 'SubmissionList',
          query: { problemID: this.problemID, completeProblemID: true },
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
      this.$confirm(
        this.$i18n.t('m.Are_you_sure_you_want_to_reset_your_code'),
        'Tips',
        {
          cancelButtonText: this.$i18n.t('m.Cancel'),
          confirmButtonText: this.$i18n.t('m.OK'),
          type: 'warning',
        }
      )
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

    checkContestPassword() {
      // 密码为空，需要重新输入
      if (!this.submitPwd) {
        myMessage.warning(this.$i18n.t('m.Enter_the_contest_password'));
        return;
      }
      api.registerContest(this.contestID + '', this.submitPwd).then(
        (res) => {
          this.$store.commit('contestSubmitAccess', { submitAccess: true });
          this.submitPwdVisible = false;
          this.submitCode();
        },
        (res) => {}
      );
    },

    submitCode() {
      if (this.code.trim() === '') {
        myMessage.error(this.$i18n.t('m.Code_can_not_be_empty'));
        return;
      }

      // 比赛题目需要检查是否有权限提交
      if (!this.canSubmit && this.$route.params.contestID) {
        this.submitPwdVisible = true;
        return;
      }

      this.submissionId = '';
      this.result = { status: 9 };
      this.submitting = true;
      let data = {
        pid: this.problemID, // 如果是比赛题目就为display_id
        language: this.language,
        code: this.code,
        cid: this.contestID,
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
                content: this.$i18n.t('m.Submit_code_successfully'),
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
            this.$i18n.t(
              'm.You_have_submission_in_this_problem_sure_to_cover_it'
            ),
            'Warning',
            {
              confirmButtonText: this.$i18n.t('m.OK'),
              cancelButtonText: this.$i18n.t('m.Cancel'),
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

    calcOIRankScore(score, difficulty) {
      return 0.1 * score + 2 * difficulty;
    },

    onCopy(event) {
      myMessage.success(this.$i18n.t('m.Copied_successfully'));
    },
    onCopyError(e) {
      myMessage.success(this.$i18n.t('m.Copied_failed'));
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
    isCFProblem() {
      if (this.problemID.indexOf('CF-') == 0) {
        return true;
      } else {
        return false;
      }
    },
  },
  beforeRouteLeave(to, from, next) {
    // 防止切换组件后仍然不断请求
    clearInterval(this.refreshStatus);
    storage.set(buildProblemCodeKey(this.problemID, from.params.contestID), {
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
    isAuthenticated(newVal) {
      if (newVal === true) {
        this.init();
      }
    },
  },
};
</script>
<style>
.katex .katex-mathml {
  display: none;
}
</style>

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

/deep/.el-tabs--border-card > .el-tabs__content {
  padding-top: 0px;
  padding-right: 0px;
}
.problem-detail {
  padding-right: 15px;
}
@media screen and (min-width: 768px) {
  .problem-detail {
    height: 700px !important;
    overflow-y: auto;
  }
  .submit-detail {
    height: 755px !important;
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

@media screen and (min-width: 1050px) {
  .problem-box {
    width: 100%;
    height: 100%;
    overflow: hidden;
  }
  .problem-left {
    width: calc(50% - 10px); /*左侧初始化宽度*/
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    float: left;
  }
  .problem-resize {
    cursor: col-resize;
    float: left;
    position: relative;
    top: 330px;
    background-color: #d6d6d6;
    border-radius: 5px;
    margin-top: -10px;
    width: 10px;
    height: 50px;
    background-size: cover;
    background-position: center;
    /*z-index: 99999;*/
    font-size: 32px;
    color: white;
  }
  .problem-resize:hover .right-fold {
    display: block;
  }
  .problem-resize:hover .fold:before {
    content: '';
    position: absolute;
    display: block;
    width: 6px;
    height: 24px;
    left: -6px;
  }
  .right-fold {
    position: absolute;
    display: none;
    font-weight: bolder;
    margin-left: 15px;
    margin-top: -35px;
    cursor: pointer;
    z-index: 1000;
    text-align: center;
  }
  .left-fold {
    position: absolute;
    font-weight: bolder;
    margin-left: -40px;
    margin-top: 10px;
    cursor: pointer;
    z-index: 1000;
    text-align: center;
  }
  .fold:hover {
    color: #409eff;
    background: #fff;
  }

  /*拖拽区鼠标悬停样式*/
  .problem-resize:hover {
    color: #444444;
  }
  .problem-right {
    height: 100%;
    float: left;
    width: 50%;
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
  margin-top: -40px;
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
