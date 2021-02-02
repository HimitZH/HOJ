<template>
  <div class="problem">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ title }}</span>
      </div>
      <el-form
        ref="form"
        :model="problem"
        :rules="rules"
        label-position="top"
        label-width="70px"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item prop="title" label="Title" required>
              <el-input
                placeholder="Enter the title of problem"
                v-model="problem.title"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="contestID">
          <el-col :md="12" :xs="24">
            <el-form-item label="Display Title">
              <el-input
                placeholder="Enter the display title of problem in contest"
                v-model="contestProblem.displayTitle"
              ></el-input>
            </el-form-item>
          </el-col>

          <el-col :md="12" :xs="24">
            <el-form-item label="Display ID">
              <el-input
                placeholder="Enter the display ID of problem in contest"
                v-model="contestProblem.displayId"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item prop="description" label="Description" required>
              <Simditor v-model="problem.description"></Simditor>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :md="8" :xs="24">
            <el-form-item label="Time Limit(ms)" required>
              <el-input
                type="Number"
                placeholder="Enter the time limit of problem"
                v-model="problem.timeLimit"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Memory Limit(mb)" required>
              <el-input
                type="Number"
                placeholder="Enter the memory limit of problem"
                v-model="problem.memoryLimit"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Level" required>
              <el-select
                class="difficulty-select"
                placeholder="Enter the level of problem"
                v-model="problem.difficulty"
              >
                <el-option label="Easy" :value="0"></el-option>
                <el-option label="Mid" :value="1"></el-option>
                <el-option label="Hard" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item
              prop="input_description"
              label="Input Description"
              required
            >
              <Simditor v-model="problem.input"></Simditor>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item
              prop="output_description"
              label="Output Description"
              required
            >
              <Simditor v-model="problem.output"></Simditor>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :md="4" :xs="24">
            <el-form-item label="Auth">
              <el-select v-model="problem.auth" size="small">
                <el-option label="公开" :value="1"></el-option>
                <el-option label="私有" :value="2"></el-option>
                <el-option label="比赛题目" :value="3"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="4" :xs="24">
            <el-form-item label="Code Shareable">
              <el-switch
                v-model="problem.codeShare"
                active-text=""
                inactive-text=""
              >
              </el-switch>
            </el-form-item>
          </el-col>
          <el-col :md="16" :xs="24">
            <el-form-item label="Tags" required>
              <el-tag
                v-for="tag in problemTags"
                closable
                :close-transition="false"
                :key="tag.name"
                size="small"
                @close="closeTag(tag.name)"
                style="margin-right: 7px;margin-top:4px"
                >{{ tag.name }}</el-tag
              >
              <!-- 输入时建议，回车，选择，光标消失触发更新 -->
              <el-autocomplete
                v-if="inputVisible"
                size="mini"
                class="input-new-tag"
                v-model="tagInput"
                :trigger-on-focus="true"
                @keyup.enter.native="addTag"
                @click="selectTag"
                @blur="addTag"
                @select="addTag"
                :fetch-suggestions="querySearch"
              >
              </el-autocomplete>
              <el-tooltip
                effect="dark"
                content="添加新标签"
                placement="top"
                v-else
              >
                <el-button
                  class="button-new-tag"
                  size="small"
                  @click="inputVisible = true"
                  icon="el-icon-plus"
                ></el-button>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :md="24" :xs="24">
            <el-form-item label="Language" :error="error.languages" required>
              <el-checkbox-group v-model="problemLanguages">
                <el-tooltip
                  class="spj-radio"
                  v-for="lang in allLanguage"
                  :key="lang.name"
                  effect="dark"
                  :content="lang.description"
                  placement="top-start"
                >
                  <el-checkbox :label="lang.name"></el-checkbox>
                </el-tooltip>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
        </el-row>

        <div>
          <div class="panel-title home-title">
            Problem Examples
            <el-popover placement="right" trigger="hover">
              <p>
                题目样例：请最好不要超过2个题目样例，题面样例不纳入评测数据。
              </p>
              <i slot="reference" class="el-icon-question"></i>
            </el-popover>
          </div>
          <el-form-item
            v-for="(example, index) in problem.examples"
            :key="'example' + index"
          >
            <Accordion :title="'Example' + (index + 1)">
              <el-button
                type="danger"
                size="small"
                icon="el-icon-delete"
                slot="header"
                @click="deleteExample(index)"
              >
                Delete
              </el-button>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Input Example" required>
                    <el-input
                      :rows="5"
                      type="textarea"
                      placeholder="Input Example"
                      v-model="example.input"
                      style="white-space: pre-line"
                    >
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Output Example" required>
                    <el-input
                      :rows="5"
                      type="textarea"
                      placeholder="Output Example"
                      v-model="example.output"
                    >
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </Accordion>
          </el-form-item>
        </div>

        <div class="add-example-btn">
          <el-button
            class="add-examples"
            @click="addExample()"
            icon="el-icon-plus"
            type="small"
            >Add Example
          </el-button>
        </div>

        <div>
          <div class="panel-title home-title">
            Judge Samples
            <el-popover placement="right" trigger="hover">
              <p>评测数据：判题机对该题目的相关提交进行评测的数据来源。</p>
              <i slot="reference" class="el-icon-question"></i>
            </el-popover>
          </div>
          <el-form-item
            v-for="(sample, index) in problemSamples"
            :key="'sample' + index"
          >
            <Accordion :title="'Sample' + (index + 1)">
              <el-button
                type="danger"
                size="small"
                icon="el-icon-delete"
                slot="header"
                @click="deleteSample(index)"
              >
                Delete
              </el-button>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Input Sample" required>
                    <el-input
                      :rows="5"
                      type="textarea"
                      placeholder="Input Sample"
                      v-model="sample.input"
                    >
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Output Sample" required>
                    <el-input
                      :rows="5"
                      type="textarea"
                      placeholder="Output Sample"
                      v-model="sample.output"
                    >
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </Accordion>
          </el-form-item>
        </div>
        <div class="add-sample-btn">
          <el-button
            class="add-samples"
            @click="addSample()"
            icon="el-icon-plus"
            type="small"
            >Add Sample
          </el-button>
        </div>

        <div class="panel-title home-title">
          Special Judge
          <el-popover placement="right" trigger="hover">
            <p>使用特殊判题的原因：</p>
            <p>1. 题目要求的输出结果可能不唯一，允许不同结果存在。</p>
            <p>
              2.
              题目最终要求输出一个浮点数，而且会告诉只要答案和标准答案相差不超过某个较小的数就可以。
              例如题目要求保留几位小数，输出结果后几位小数不相同也是正确的。
            </p>
            <i slot="reference" class="el-icon-question"></i>
          </el-popover>
        </div>
        <el-form-item label="" :error="error.spj">
          <el-col :span="24">
            <el-checkbox
              v-model="problem.spj"
              @click.native.prevent="switchSpj()"
              >Use Special Judge</el-checkbox
            >
          </el-col>
        </el-form-item>
        <el-form-item v-if="problem.spj">
          <Accordion title="Special Judge Code">
            <template slot="header">
              <span style="margin-right:5px;">SPJ language：</span>
              <el-radio-group v-model="problem.spjLanguage">
                <el-tooltip
                  class="spj-radio"
                  v-for="lang in allSpjLanguage"
                  :key="lang.name"
                  effect="dark"
                  :content="lang.description"
                  placement="top-start"
                >
                  <el-radio :label="lang.name">{{ lang.name }}</el-radio>
                </el-tooltip>
              </el-radio-group>
              <el-button
                type="primary"
                size="small"
                icon="el-icon-folder-checked"
                @click="compileSPJ"
                :loading="loadingCompile"
                style="margin-left:10px"
                >Complie
              </el-button>
            </template>
            <code-mirror
              v-model="problem.spjCode"
              :mode="spjMode"
            ></code-mirror>
          </Accordion>
        </el-form-item>

        <el-form-item style="margin-top: 20px" label="Hint">
          <Simditor v-model="problem.hint"></Simditor>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Type">
              <el-radio-group
                v-model="problem.type"
                :disabled="disableRuleType"
              >
                <el-radio :label="0">ACM</el-radio>
                <el-radio :label="1">OI</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item
              label="Total Score"
              v-show="problem.type == 1"
              :required="problem.type == 1"
            >
              <el-input
                v-model="problem.ioScore"
                placeholder="Total Score"
              ></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="TestCase" :error="error.testcase">
              <el-upload
                action="/api/admin/test_case"
                name="file"
                :data="{ spj: problem.spj }"
                :show-file-list="true"
                :on-success="uploadSucceeded"
                :on-error="uploadFailed"
              >
                <el-button size="small" type="primary" icon="el-icon-upload"
                  >Choose File</el-button
                >
              </el-upload>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <vxe-table
              stripe
              auto-resize
              :data="problem.test_case_score"
              align="center"
            >
              <vxe-table-column
                field="input_name"
                title="Input"
                min-width="100"
              >
              </vxe-table-column>
              <vxe-table-column
                field="output_name"
                title="Output"
                min-width="100"
              >
              </vxe-table-column>
              <vxe-table-column field="score" title="Score" min-width="100">
                <template v-slot="{ row }">
                  <el-input
                    size="small"
                    placeholder="Score"
                    v-model="row.score"
                    :disabled="problem.type !== 'OI'"
                  >
                  </el-input>
                </template>
              </vxe-table-column>
            </vxe-table>
          </el-col>
        </el-row>

        <el-form-item label="Source">
          <el-input
            placeholder="Enter the problem where from"
            v-model="problem.source"
          ></el-input>
        </el-form-item>
        <el-button type="primary" @click.native="submit()" size="small"
          >Save</el-button
        >
      </el-form>
    </el-card>
  </div>
</template>

<script>
import Simditor from '@/components/admin/Simditor';
import Accordion from '@/components/admin/Accordion';
import CodeMirror from '@/components/admin/CodeMirror';
import utils from '@/common/utils';
import { mapGetters } from 'vuex';
import api from '@/common/api';
import myMessage from '@/common/message';
export default {
  name: 'Problem',
  components: {
    Simditor,
    Accordion,
    CodeMirror,
  },
  data() {
    return {
      rules: {
        title: {
          required: true,
          message: 'Title is required',
          trigger: 'blur',
        },
        input_description: {
          required: true,
          message: 'Input Description is required',
          trigger: 'blur',
        },
        output_description: {
          required: true,
          message: 'Output Description is required',
          trigger: 'blur',
        },
      },
      loadingCompile: false,
      mode: '', // 该题目是编辑或者创建
      contest: {},
      pid: null, // 题目id，如果为创建模式则为null
      contestID: null, // 比赛id
      contestProblem: {
        displayId: null,
        displayTitle: null,
        cid: null,
        pid: null,
      }, // 比赛题目的相关属性
      problem: {
        id: null,
        title: '',
        description: '',
        input: '',
        output: '',
        timeLimit: 1000,
        memoryLimit: 256,
        difficulty: 0,
        auth: 1,
        codeShare: true,
        examples: [], // 题面上的样例输入输出
        spj: false,
        spjLanguage: '',
        spjCode: '',
        spj_compile_ok: false,
        test_case_id: '',
        test_case_score: [],
        type: 0,
        hint: '',
        source: '',
        cid: null,
      },
      problemTags: [], //指定问题的标签列表
      problemLanguages: [], //指定问题的编程语言列表
      problemSamples: [], // 判题机使用的样例
      reProblem: {},
      testCaseUploaded: false,
      allLanguage: [], //所有编程语言
      allSpjLanguage: [], // 所有可以用特殊判题的语言
      allTags: [],
      inputVisible: false,
      tagInput: '',
      title: '',
      spjMode: '',
      disableRuleType: false,
      routeName: '',
      error: {
        tags: '',
        spj: '',
        languages: '',
        testCase: '',
      },
    };
  },
  mounted() {
    this.routeName = this.$route.name;
    if (
      this.routeName === 'admin-edit-problem' ||
      this.routeName === 'admin-edit-contest-problem'
    ) {
      this.mode = 'edit';
    } else {
      this.mode = 'add';
    }
    api.getLanguages().then((res) => {
      this.problem = this.reProblem = {
        id: null,
        title: '',
        description: '',
        input: '',
        output: '',
        timeLimit: 1000,
        memoryLimit: 256,
        difficulty: 0,
        auth: 1,
        codeShare: true,
        examples: [],
        spj: false,
        spjLanguage: '',
        spjCode: '',
        spj_compile_ok: false,
        test_case_id: '',
        test_case_score: [],
        contestProblem: {},
        type: 0,
        hint: '',
        source: '',
        cid: null,
      };
      let contestID = this.$route.params.contestId;
      this.contestID = contestID;
      if (contestID) {
        this.problem.cid = this.reProblem.cid = contestID;
        this.problem.auth = this.reProblem.auth = 3;
        this.disableRuleType = true;
        api.admin_getContest(contestID).then((res) => {
          this.problem.type = this.reProblem.type = res.data.data.type;
          this.contest = res.data.data;
        });
      }
      this.problem.spjLanguage = 'C';
      let allLanguage = res.data.data;
      this.allLanguage = allLanguage;
      for (let i = 0; i < allLanguage.length; i++) {
        if (allLanguage[i].isSpj == true) {
          this.allSpjLanguage.push(allLanguage[i]);
        }
      }

      // get problem after getting languages list to avoid find undefined value in `watch problemLanguages`
      this.init();
    });
  },
  watch: {
    $route() {
      this.routeName = this.$route.name;
      if (
        this.routeName === 'admin-edit-problem' ||
        this.routeName === 'admin-edit-contest-problem'
      ) {
        this.mode = 'edit';
      } else {
        this.mode = 'add';
      }
      this.$refs.form.resetFields();
      this.problem = this.reProblem;
      this.problemTags = []; //指定问题的标签列表
      this.problemLanguages = []; //指定问题的编程语言列表
      this.problemSamples = [];
      this.init();
    },

    'problem.spjLanguage'(newVal) {
      this.spjMode = this.allLanguage.find((item) => {
        return item.name === this.problem.spjLanguage && item.isSpj == true;
      }).content_type;
    },
  },
  methods: {
    init() {
      if (this.mode === 'edit') {
        this.pid = this.$route.params.problemId;
        this.title = 'Edit Problem';
        let funcName = {
          'admin-edit-problem': 'admin_getProblem',
          'admin-edit-contest-problem': 'admin_getContestProblem',
        }[this.routeName];
        api[funcName](this.pid).then((problemRes) => {
          let data = problemRes.data.data;
          (data.spj_compile_ok = false),
            (data.test_case_id = ''),
            (data.test_case_score = []);
          data.spj = true;
          if (!data.spjCode) {
            data.spjCode = '';
            data.spj = false;
          }
          data.spjLanguage = data.spjLanguage || 'C';
          this.problem = data;
          this.problem['examples'] = utils.stringToExamples(data.examples);
          this.testCaseUploaded = true;
        });
        if (funcName === 'admin_getContestProblem') {
          api
            .admin_getContestProblemInfo(this.pid, this.contestID)
            .then((res) => {
              this.contestProblem = res.data.data;
            });
        }
        api.getProblemLanguages(this.pid).then((res) => {
          let Languages = res.data.data;
          for (let i = 0; i < Languages.length; i++) {
            this.problemLanguages.push(Languages[i].name);
          }
        });
        api.admin_getProblemCases(this.pid).then((res) => {
          this.problemSamples = res.data.data;
        });
        api.admin_getProblemTags(this.pid).then((res) => {
          this.problemTags = res.data.data;
        });
      } else {
        this.title = 'Create Problem';
        for (let item of this.allLanguage) {
          this.problemLanguages.push(item.name);
        }
      }
    },
    switchSpj() {
      if (this.testCaseUploaded) {
        this.$confirm(
          '如果你想改变该题目的判题方法，那么你需要重新上传测试数据。',
          '注意',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
          .then(() => {
            this.problem.spj = !this.problem.spj;
            this.resetTestCase();
          })
          .catch(() => {});
      } else {
        this.problem.spj = !this.problem.spj;
      }
    },
    querySearch(queryString, cb) {
      api
        .admin_getAllProblemTagList()
        .then((res) => {
          let tagList = [];
          this.allTags = res.data.data;
          for (let tag of res.data.data) {
            tagList.push({ value: tag.name });
          }
          cb(tagList);
        })
        .catch(() => {});
    },
    resetTestCase() {
      this.testCaseUploaded = false;
      this.problem.test_case_score = [];
      this.problem.test_case_id = '';
    },
    selectTag(item) {
      for (var i = 0; i < this.problemTags.length; i++) {
        if (this.problemTags[i].name == item.value) {
          myMessage.warning('该标签已添加，请不要重复添加！');
          this.tagInput = '';
          return;
        }
      }
      this.tagInput = item.value;
    },
    addTag(item) {
      let newTag = {
        name: this.tagInput,
      };
      if (this.tagInput) {
        for (var i = 0; i < this.problemTags.length; i++) {
          if (this.problemTags[i].name == this.tagInput) {
            myMessage.warning('该标签已添加，请不要重复添加！');
            this.tagInput = '';
            return;
          }
        }
        this.problemTags.push(newTag);
        this.inputVisible = false;
        this.tagInput = '';
      }
    },

    // 根据tagID从题目的tags列表中移除
    closeTag(tag) {
      this.problemTags.splice(this.problemTags.indexOf(tag), 1);
    },
    // 添加题目样例
    addExample() {
      this.problem.examples.push({ input: '', output: '' });
    },
    // 添加判题机的测试样例
    addSample() {
      this.problemSamples.push({ input: '', output: '', pid: this.pid });
    },
    //根据下标删除特定的题目样例
    deleteExample(index) {
      this.problem.examples.splice(index, 1);
    },
    //根据下标删除特定的判题机测试样例
    deleteSample(index) {
      this.problemSamples.splice(index, 1);
    },
    uploadSucceeded(response) {
      if (response.error) {
        myMessage.error(response.data);
        return;
      }
      let fileList = response.data.info;
      for (let file of fileList) {
        file.score = (100 / fileList.length).toFixed(0);
        if (!file.output_name && this.problem.spj) {
          file.output_name = '-';
        }
      }
      this.problem.test_case_score = fileList;
      this.testCaseUploaded = true;
      this.problem.test_case_id = response.data.id;
    },
    uploadFailed() {
      myMessage.error('Upload failed');
    },

    compileSPJ() {
      let data = {
        id: this.problem.id,
        spjCode: this.problem.spjCode,
        spjLanguage: this.problem.spjLanguage,
      };
      this.loadingCompile = true;
      api.compileSPJ(data).then(
        (res) => {
          this.loadingCompile = false;
          this.problem.spj_compile_ok = true;
          this.error.spj = '';
        },
        (err) => {
          this.loadingCompile = false;
          this.problem.spj_compile_ok = false;
          const h = this.$createElement;
          this.$msgbox({
            title: 'Compile Error',
            type: 'error',
            message: h('pre', err.data.data),
            showCancelButton: false,
            closeOnClickModal: false,
            customClass: 'dialog-compile-error',
          });
        }
      );
    },

    submit() {
      if (!this.problem.examples.length) {
        myMessage.error('题面测试数据是不能为空！至少输入一项！');
        return;
      }
      for (let example of this.problem.examples) {
        if (!example.input || !example.output) {
          myMessage.error('每一项题面测试数据的输入输出都不能为空！');
          return;
        }
      }
      if (!this.problemSamples.length) {
        myMessage.error('评测数据是不能为空！');
        return;
      }
      for (let sample of this.problemSamples) {
        if (!sample.input || !sample.output) {
          myMessage.error('每一项评测数据的输入输出都不能为空！');
          return;
        }
      }
      if (!this.problemTags.length) {
        this.error.tags = '请添加至少一个题目标签！';
        myMessage.error(this.error.tags);
        return;
      }

      if (this.problem.spj) {
        if (!this.problem.spjCode) {
          this.error.spj = '特殊判题的程序代码不能为空！';
          myMessage.error(this.error.spj);
        } else if (!this.problem.spj_compile_ok) {
          this.error.spj = '特殊判题的程序没有编译成功，请重新编译！';
        }
        if (this.error.spj) {
          myMessage.error(this.error.spj);
          return;
        }
      }

      if (!this.problemLanguages.length) {
        this.error.languages = '请至少给题目选择一项编程语言！';
        myMessage.error(this.error.languages);
        return;
      }
      // if (!this.testCaseUploaded) {
      //   this.error.testCase = "评测数据还没有成功上传！";
      //   myMessage.error(this.error.testCase);
      //   return;
      // }

      if (this.problem.type === 1) {
        for (let item of this.problem.test_case_score) {
          try {
            if (parseInt(item.score) <= 0) {
              myMessage.error('测评得分小于0是无效的！');
              return;
            }
          } catch (e) {
            myMessage.error('测评得分的结果必须是数字类型！');
            return;
          }
        }
      }

      let funcName = {
        'admin-create-problem': 'admin_createProblem',
        'admin-edit-problem': 'admin_editProblem',
        'admin-create-contest-problem': 'admin_createContestProblem',
        'admin-edit-contest-problem': 'admin_editContestProblem',
      }[this.routeName];
      // edit contest problem 时, contest_id会被后来的请求覆盖掉
      if (funcName === 'editContestProblem') {
        this.problem.contest_id = this.contest.id;
      }
      if (
        funcName === 'admin_createProblem' ||
        funcName === 'admin_createContestProblem'
      ) {
        this.problem.author = this.userInfo.username;
      }

      let problemTagList = Object.assign([], this.problemTags);
      for (let i = 0; i < problemTagList.length; i++) {
        //避免后台插入违反唯一性
        for (let tag2 of this.allTags) {
          if (problemTagList[i].name == tag2.name) {
            problemTagList[i] = tag2;
            break;
          }
        }
      }
      let problemLanguageList = Object.assign([], this.problemLanguages); // 深克隆 防止影响
      for (let i = 0; i < problemLanguageList.length; i++) {
        problemLanguageList[i] = { name: problemLanguageList[i] };
        for (let lang of this.allLanguage) {
          if (problemLanguageList[i].name == lang.name) {
            problemLanguageList[i] = lang;
            break;
          }
        }
      }

      let problemDto = {}; // 上传给后台的数据
      problemDto['problem'] = Object.assign({}, this.problem); // 深克隆
      problemDto.problem.examples = utils.examplesToString(
        this.problem.examples
      ); // 需要转换格式

      problemDto['tags'] = problemTagList;
      problemDto['languages'] = problemLanguageList;
      problemDto['samples'] = this.problemSamples;

      api[funcName](problemDto)
        .then((res) => {
          if (
            this.routeName === 'admin-create-contest-problem' ||
            this.routeName === 'admin-edit-contest-problem'
          ) {
            if (res.data.data) {
              // 新增题目操作 需要使用返回来的pid
              this.contestProblem['pid'] = res.data.data.pid;
              this.contestProblem['cid'] = this.$route.params.contestId;
            }
            api.admin_setContestProblemInfo(this.contestProblem).then((res) => {
              myMessage.success(res.data.msg);
              this.$router.push({
                name: 'admin-contest-problem-list',
                params: { contestId: this.$route.params.contestId },
              });
            });
          } else {
            myMessage.success(res.data.msg);
            this.$router.push({ name: 'admin-problem-list' });
          }
        })
        .catch(() => {});
    },
  },
  computed: {
    ...mapGetters(['userInfo']),
  },
};
</script>

<style scoped>
/deep/.el-form-item__label {
  padding: 0 !important;
}
.el-form-item {
  margin-bottom: 10px !important;
}
.difficulty-select {
  width: 120px;
}
.input-new-tag {
  width: 120px;
}
.button-new-tag {
  height: 24px;
  line-height: 22px;
  padding-top: 0;
  padding-bottom: 0;
}

.accordion {
  margin-bottom: 10px;
}

.add-examples {
  width: 100%;
  background-color: #fff;
  border: 1px dashed #2d8cf0;
  outline: none;
  cursor: pointer;
  color: #2d8cf0;
  height: 35px;
  font-size: 14px;
}
.add-examples i {
  margin-right: 10px;
}
.add-examples:hover {
  border: 0px;
  background-color: #2d8cf0;
  color: #fff;
}
.add-example-btn {
  margin-bottom: 10px;
}

.add-samples {
  width: 100%;
  background-color: #fff;
  border: 1px dashed #19be6b;
  outline: none;
  cursor: pointer;
  color: #19be6b;
  height: 35px;
  font-size: 14px;
}
.add-samples i {
  margin-right: 10px;
}
.add-samples:hover {
  border: 0px;
  background-color: #19be6b;
  color: #fff;
}
.add-sample-btn {
  margin-bottom: 10px;
}

.dialog-compile-error {
  width: auto;
  max-width: 80%;
  overflow-x: scroll;
}
</style>
