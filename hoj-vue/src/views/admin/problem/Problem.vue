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
                <el-option label="比赛中" :value="3"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
           <el-col :md="4" :xs="24">
            <el-form-item label="Code Shareable">
              <el-switch
                v-model="problem.code_share"
                active-text=""
                inactive-text="">
              </el-switch>
            </el-form-item>
          </el-col>
          <el-col :md="16" :xs="24">
            <el-form-item label="Tags" required>
              <el-tag
                v-for="tag in problem.tags"
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
                :trigger-on-focus="false"
                @keyup.enter.native="addTag"
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
              <el-checkbox-group v-model="problem.languages">
                <el-tooltip
                  class="spj-radio"
                  v-for="lang in allLanguage.languages"
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
          <p>题目样例：请最好不要超过2个题目样例，题面样例不纳入评测数据。</p>
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
            v-for="(sample, index) in problem.samples"
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
          <p>1. 题目要求的输出结果可能不唯一，允许不同结果存在</p>
          <p>2. 题目最终要求输出一个浮点数，而且会告诉只要答案和标准答案相差不超过某个较小的数就可以。
            例如题目要求保留几位小数，输出结果后几位小数不相同也是正确的。</p>
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
              <el-radio-group v-model="problem.spj_language">
                <el-tooltip
                  class="spj-radio"
                  v-for="lang in allLanguage.spj_languages"
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
                icon="el-icon-fa-random"
                @click="compileSPJ"
                :loading="loadingCompile"
                style="margin-left:10px"
              >Complie
              </el-button>
            </template>
            <code-mirror
              v-model="problem.spj_code"
              :mode="spjMode"
            ></code-mirror>
          </Accordion>
        </el-form-item>

        <el-form-item style="margin-top: 20px" label="Hint">
          <Simditor v-model="problem.hint"></Simditor>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="4">
            <el-form-item label="Type">
              <el-radio-group
                v-model="problem.type"
                :disabled="disableRuleType"
              >
                <el-radio :label="0" >ACM</el-radio>
                <el-radio :label="1" >OI</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="6">
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

            <vxe-table stripe auto-resize :data="problem.test_case_score">
              <vxe-table-column field="input_name" title="Input" min-width="150">
              </vxe-table-column>
              <vxe-table-column field="output_name" title="Output" min-width="150">
              </vxe-table-column>
              <vxe-table-column field="score" title="Score" min-width="150">
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
import Simditor from "@/components/admin/Simditor";
import Accordion from "@/components/admin/Accordion";
import CodeMirror from "@/components/admin/CodeMirror";
import api from "@/common/api";

export default {
  name: "Problem",
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
          message: "Title is required",
          trigger: "blur",
        },
        input_description: {
          required: true,
          message: "Input Description is required",
          trigger: "blur",
        },
        output_description: {
          required: true,
          message: "Output Description is required",
          trigger: "blur",
        },
      },
      loadingCompile: false,
      mode: "", // 该题目是编辑或者创建
      contest: {},
      problem: {
        id: "",
        title: "",
        description: "",
        input_description: "",
        output_description: "",
        time_limit: 1000,
        memory_limit: 256,
        difficulty: 0,
        auth: 1,
        code_share: true,
        tags: [
          {
            id:1001,
            name:'模拟题'
          },
          {
            id:1002,
            name:'递归题'
          }
        ],
        languages: [],
        examples:[{ input: "", output: "" }], // 题面上的样例输入输出
        samples: [{ input: "", output: "" }], // 判题机使用的样例
        spj: false,
        spj_language: "",
        spj_code: "",
        spj_compile_ok: false,
        test_case_id: "",
        test_case_score: [
          {input_name:'1.in',output_name:'1.out',score:100}
        ],
        type: 0,
        hint: "",
        source: "",
      },

      reProblem: {
        languages: [],
      },
      testCaseUploaded: false,
      allLanguage: {
          languages:[
              {
            content_type: "text/x-csrc",
            description: "GCC 5.4",
            name: "C",
            compile_command: "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {src_path} -lm -o {exe_path}",
            template: "#include <stdio.h>\nint add(int a, int b) {\n    return a+b;\n}\nint main() {\n    printf(\"%d\", add(1, 2));\n    return 0;\n}"
          },
          {
            content_type: "text/x-c++src",
            description: "G++ 5.4",
            name: "C++",
            compile_command: "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {src_path} -lm -o {exe_path}",
            template: "#include <iostream>\nint add(int a, int b) {\n    return a+b;\n}\nint main() {\n    std::cout << add(1, 2);\n    return 0;\n}",
          },  
          { content_type: "text/x-java",
            description: "OpenJDK 1.8",
            name: "Java",
            compile_command: "/usr/bin/javac {src_path} -d {exe_dir} -encoding UTF8",
            template: "import java.util.Scanner;\npublic class Main{\n    public static void main(String[] args){\n        Scanner in=new Scanner(System.in);\n        int a=in.nextInt();\n        int b=in.nextInt();\n        System.out.println((a+b));\n    }\n}"
          },
          {
            content_type: "text/x-python",
            description: "Python 3.7",
            name: "Python3",
            template: "a, b = map(int, input().split())\nprint(a + b)",
            compile_command: "/usr/bin/python3 -m py_compile {src_path}"
          }
        ],
        spj_languages:[
          {
            content_type: "text/x-csrc",
            description: "GCC 5.4",
            name: "C",
            compile_command: "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {src_path} -lm -o {exe_path}",
            template: "#include <stdio.h>\nint add(int a, int b) {\n    return a+b;\n}\nint main() {\n    printf(\"%d\", add(1, 2));\n    return 0;\n}"
          },
          {
            content_type: "text/x-c++src",
            description: "G++ 5.4",
            name: "C++",
            compile_command: "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {src_path} -lm -o {exe_path}",
            template: "#include <iostream>\nint add(int a, int b) {\n    return a+b;\n}\nint main() {\n    std::cout << add(1, 2);\n    return 0;\n}",
          },  
        ]
      },
      inputVisible: false,
      tagInput: '',
      title: "",
      spjMode: "",
      disableRuleType: false,
      routeName: "",
      error: {
        tags: "",
        spj: "",
        languages: "",
        testCase: "",
      },
    };
  },
  mounted() {
    this.routeName = this.$route.name;
    if (
      this.routeName === "edit-problem" ||
      this.routeName === "edit-contest-problem"
    ) {
      this.mode = "edit";
    } else {
      this.mode = "add";
    }
    api.getLanguages().then((res) => {
      this.problem = this.reProblem = {
        id: "",
        title: "",
        description: "",
        input_description: "",
        output_description: "",
        time_limit: 1000,
        memory_limit: 256,
        difficulty: 0,
        visible: true,
        share_submission: false,
        tags: [],
        languages: [],
        samples: [{ input: "", output: "" }],
        spj: false,
        spj_language: "",
        spj_code: "",
        spj_compile_ok: false,
        test_case_id: "",
        test_case_score: [],
        type: 0,
        hint: "",
        source: "",
      };
      let contestID = this.$route.params.contestId;
      if (contestID) {
        this.problem.contest_id = this.reProblem.contest_id = contestID;
        this.disableRuleType = true;
        api.getContest(contestID).then((res) => {
          this.problem.type = this.reProblem.type =
            res.data.data.type;
          this.contest = res.data.data;
        });
      }

      this.problem.spj_language = "C";
      let allLanguage = res.data.data;
      this.allLanguage = allLanguage;

      // get problem after getting languages list to avoid find undefined value in `watch problem.languages`
      if (this.mode === "edit") {
        this.title = "Edit Problem";
        let funcName = {
          "admin-edit-problem": "getProblem",
          "admin-edit-contest-problem": "getContestProblem",
        }[this.routeName];
        // api[funcName](this.$route.params.problemId).then((problemRes) => {
        //   let data = problemRes.data.data;
        //   if (!data.spj_code) {
        //     data.spj_code = "";
        //   }
        //   data.spj_language = data.spj_language || "C";
        //   this.problem = data;
        //   this.testCaseUploaded = true;
        // });
      } else {
        this.title = "Add Problem";
        for (let item of allLanguage.languages) {
          this.problem.languages.push(item.name);
        }
      }
    });
  },
  watch: {
    $route() {
      this.$refs.form.resetFields();
      this.problem = this.reProblem;
    },

    "problem.spj_language"(newVal) {
      this.spjMode = this.allLanguage.spj_languages.find((item) => {
        return item.name === this.problem.spj_language;
      }).content_type;
    },
  },
  methods: {
    switchSpj() {
      if (this.testCaseUploaded) {
        this.$confirm(
          "If you change problem judge method, you need to re-upload test cases",
          "Warning",
          {
            confirmButtonText: "Yes",
            cancelButtonText: "Cancel",
            type: "warning",
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
    querySearch(queryString, callback) {
      // api
      //   .getProblemTagList()
      //   .then((res) => {
      //     let tagList = [];
      //     for (let tag of res.data.data) {
      //       tagList.push({ value: tag.name});
      //     }
      //     callback(tagList);
      //   })
      //   .catch(() => {});
      let tagList = [{value:'简单题'}];
      callback(tagList);
    },
    resetTestCase() {
      this.testCaseUploaded = false;
      this.problem.test_case_score = [];
      this.problem.test_case_id = "";
    },
    addTag() {
      let newTag ={
        name:this.tagInput,
      }
      if (newTag) {
        this.problem.tags.push(newTag);
      }
      this.inputVisible = false;
      this.tagInput = '';
    },

    // 根据tagID从题目的tags列表中移除
    closeTag(tag) {
      this.problem.tags.splice(this.problem.tags.indexOf(tag), 1);
    },
    // 添加题目样例
    addExample(){
      this.problem.examples.push({ input: "", output: "" });
    },
  // 添加判题机的测试样例
    addSample() {
      this.problem.samples.push({ input: "", output: "" });
    },
  //根据下标删除特定的题目样例
    deleteExample(index){
      this.problem.examples.splice(index, 1);
    },
  //根据下标删除特定的判题机测试样例
    deleteSample(index) {
      this.problem.samples.splice(index, 1);
    },
    uploadSucceeded(response) {
      if (response.error) {
        this.$error(response.data);
        return;
      }
      let fileList = response.data.info;
      for (let file of fileList) {
        file.score = (100 / fileList.length).toFixed(0);
        if (!file.output_name && this.problem.spj) {
          file.output_name = "-";
        }
      }
      this.problem.test_case_score = fileList;
      this.testCaseUploaded = true;
      this.problem.test_case_id = response.data.id;
    },
    uploadFailed() {
      this.$error("Upload failed");
    },

    compileSPJ() {
      let data = {
        id: this.problem.id,
        spj_code: this.problem.spj_code,
        spj_language: this.problem.spj_language,
      };
      this.loadingCompile = true;
      api.compileSPJ(data).then(
        (res) => {
          this.loadingCompile = false;
          this.problem.spj_compile_ok = true;
          this.error.spj = "";
        },
        (err) => {
          this.loadingCompile = false;
          this.problem.spj_compile_ok = false;
          const h = this.$createElement;
          this.$msgbox({
            title: "Compile Error",
            type: "error",
            message: h("pre", err.data.data),
            showCancelButton: false,
            closeOnClickModal: false,
            customClass: "dialog-compile-error",
          });
        }
      );
    },

    submit() {
      if (!this.problem.samples.length) {
        this.$error("Sample is required");
        return;
      }
      for (let sample of this.problem.samples) {
        if (!sample.input || !sample.output) {
          this.$error("Sample input and output is required");
          return;
        }
      }
      if (!this.problem.tags.length) {
        this.error.tags = "Please add at least one tag";
        this.$error(this.error.tags);
        return;
      }

      if (this.problem.spj) {
        if (!this.problem.spj_code) {
          this.error.spj = "Spj code is required";
          this.$error(this.error.spj);
        } else if (!this.problem.spj_compile_ok) {
          this.error.spj = "SPJ code has not been successfully compiled";
        }
        if (this.error.spj) {
          this.$error(this.error.spj);
          return;
        }
      }

      if (!this.problem.languages.length) {
        this.error.languages =
          "Please choose at least one language for problem";
        this.$error(this.error.languages);
        return;
      }
      if (!this.testCaseUploaded) {
        this.error.testCase = "Test case is not uploaded yet";
        this.$error(this.error.testCase);
        return;
      }

      if (this.problem.type === "OI") {
        for (let item of this.problem.test_case_score) {
          try {
            if (parseInt(item.score) <= 0) {
              this.$error("Invalid test case score");
              return;
            }
          } catch (e) {
            this.$error("Test case score must be an integer");
            return;
          }
        }
      }
      this.problem.languages = this.problem.languages.sort();
      let funcName = {
        "create-problem": "createProblem",
        "edit-problem": "editProblem",
        "create-contest-problem": "createContestProblem",
        "edit-contest-problem": "editContestProblem",
      }[this.routeName];
      // edit contest problem 时, contest_id会被后来的请求覆盖掉
      if (funcName === "editContestProblem") {
        this.problem.contest_id = this.contest.id;
      }
      api[funcName](this.problem)
        .then((res) => {
          if (
            this.routeName === "create-contest-problem" ||
            this.routeName === "edit-contest-problem"
          ) {
            this.$router.push({
              name: "contest-problem-list",
              params: { contestId: this.$route.params.contestId },
            });
          } else {
            this.$router.push({ name: "problem-list" });
          }
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped>
/deep/.el-form-item__label {
  padding: 0!important;
}
.el-form-item {
  margin-bottom: 10px!important;
}
.difficulty-select {
  width: 120px;
}
.input-new-tag {
  width: 78px;
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
  border:0px;
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
  border:0px;
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
