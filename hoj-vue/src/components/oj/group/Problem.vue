<template>
  <el-row>
    <el-col :span="24">
      <el-card shadow="never">
        <div slot="header">
          <span class="panel-title home-title">{{ title }}</span>
        </div>
        <el-form
          ref="form"
          :model="problem"
          :rules="rules"
          label-position="top"
        >
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item
                prop="problemId"
                :label="$t('m.Problem_Display_ID')"
                required
              >
                <el-input
                  :placeholder="$t('m.Problem_Display_ID')"
                  v-model="problem.problemId"
                  :disabled="problem.isRemote"
                >
                  <template slot="prepend">
                    {{ group.shortName.toUpperCase() }}
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item
                prop="title"
                :label="$t('m.Title')"
                required
              >
                <el-input
                  :placeholder="$t('m.Title')"
                  v-model="problem.title"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row
            :gutter="20"
            v-if="contestId"
          >
            <el-col
              :md="12"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Contest_Display_Title')"
                required
              >
                <el-input
                  :placeholder="$t('m.Contest_Display_Title')"
                  v-model="contestProblem.displayTitle"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col
              :md="12"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Contest_Display_ID')"
                required
              >
                <el-input
                  :placeholder="$t('m.Contest_Display_ID')"
                  v-model="contestProblem.displayId"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item
                prop="description"
                :label="$t('m.Description')"
                required
              >
                <Editor :value.sync="problem.description"></Editor>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col
              :md="6"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Time_Limit') + '(ms)'"
                required
              >
                <el-input
                  type="Number"
                  :placeholder="$t('m.Time_Limit')"
                  v-model="problem.timeLimit"
                  :disabled="problem.isRemote"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col
              :md="6"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Memory_Limit') + '(mb)'"
                required
              >
                <el-input
                  type="Number"
                  :placeholder="$t('m.Memory_Limit')"
                  v-model="problem.memoryLimit"
                  :disabled="problem.isRemote"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col
              :md="6"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Stack_Limit') + '(mb)'"
                required
              >
                <el-input
                  type="Number"
                  :placeholder="$t('m.Stack_Limit')"
                  v-model="problem.stackLimit"
                  :disabled="problem.isRemote"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col
              :md="6"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Level')"
                required
              >
                <el-select
                  class="difficulty-select"
                  placeholder="Enter the level of problem"
                  v-model="problem.difficulty"
                >
                  <el-option
                    :label="getLevelName(key)"
                    :value="parseInt(key)"
                    v-for="(value, key, index) in PROBLEM_LEVEL"
                    :key="index"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item
                prop="input_description"
                :label="$t('m.Input')"
                required
              >
                <Editor :value.sync="problem.input"></Editor>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item
                prop="output_description"
                :label="$t('m.Output')"
                required
              >
                <Editor :value.sync="problem.output"></Editor>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item
                style="margin-top: 20px"
                :label="$t('m.Hint')"
              >
                <Editor :value.sync="problem.hint"></Editor>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col
              :md="4"
              :xs="24"
            >
              <el-form-item :label="$t('m.Auth')">
                <el-select
                  v-model="problem.auth"
                  size="small"
                >
                  <el-option
                    :label="$t('m.Public_Problem')"
                    :value="1"
                  ></el-option>
                  <el-option
                    :label="$t('m.Private_Problem')"
                    :value="2"
                  ></el-option>
                  <el-option
                    :label="$t('m.Contest_Problem')"
                    :value="3"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col
              :md="4"
              :xs="24"
            >
              <el-form-item :label="$t('m.Type')">
                <el-radio-group
                  v-model="problem.type"
                  :disabled="disableRuleType || problem.isRemote"
                  @change="problemTypeChange"
                >
                  <el-radio :label="0">ACM</el-radio>
                  <el-radio :label="1">OI</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col
              :md="4"
              :xs="24"
            >
              <el-form-item :label="$t('m.Code_Shareable')">
                <el-switch
                  v-model="problem.codeShare"
                  active-text=""
                  inactive-text=""
                >
                </el-switch>
              </el-form-item>
            </el-col>
            <el-col
              :md="12"
              :xs="24"
            >
              <el-form-item :label="$t('m.Tags')">
                <el-tag
                  v-for="tag in problemTags"
                  closable
                  :close-transition="false"
                  :key="tag.name"
                  size="small"
                  @close="closeTag(tag.name)"
                  style="margin-right: 7px;margin-top:4px"
                >{{ tag.name }}</el-tag>
                <!-- 输入时建议，回车，选择，光标消失触发更新 -->
                <el-autocomplete
                  v-if="inputVisible"
                  size="mini"
                  class="input-new-tag"
                  v-model="tagInput"
                  :trigger-on-focus="true"
                  @keyup.enter.native="addTag"
                  @click="selectTag"
                  @select="addTag"
                  :fetch-suggestions="querySearch"
                >
                </el-autocomplete>
                <el-tooltip
                  effect="dark"
                  :content="$t('m.Add')"
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
            <el-col
              :md="24"
              :xs="24"
            >
              <el-form-item
                :label="$t('m.Languages')"
                :error="error.languages"
                required
              >
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
              {{ $t('m.Problem_Examples') }}
              <el-popover
                placement="right"
                trigger="hover"
              >
                <p>
                  {{ $t('m.Problem_Examples_Desc') }}
                </p>
                <i
                  slot="reference"
                  class="el-icon-question"
                ></i>
              </el-popover>
            </div>
            <el-form-item
              v-for="(example, index) in problem.examples"
              :key="'example' + index"
            >
              <Accordion
                :title="$t('m.Problem_Example') + (index + 1)"
                :isOpen="example.isOpen ? true : false"
                :index="index"
                @changeVisible="changeExampleVisible"
              >
                <el-button
                  type="danger"
                  size="small"
                  icon="el-icon-delete"
                  slot="header"
                  @click="deleteExample(index)"
                >
                  {{ $t('m.Delete') }}
                </el-button>
                <el-row :gutter="20">
                  <el-col
                    :xs="24"
                    :md="12"
                  >
                    <el-form-item
                      :label="$t('m.Example_Input')"
                      required
                    >
                      <el-input
                        :rows="5"
                        type="textarea"
                        :placeholder="$t('m.Example_Input')"
                        v-model="example.input"
                        style="white-space: pre-line"
                      >
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col
                    :xs="24"
                    :md="12"
                  >
                    <el-form-item
                      :label="$t('m.Example_Output')"
                      required
                    >
                      <el-input
                        :rows="5"
                        type="textarea"
                        :placeholder="$t('m.Example_Output')"
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
            >{{ $t('m.Add_Example') }}
            </el-button>
          </div>
          <template v-if="!problem.isRemote">
            <div class="panel-title home-title">
              {{ $t('m.Judge_Extra_File') }}
              <el-popover
                placement="right"
                trigger="hover"
              >
                <p>{{ $t('m.Judge_Extra_File_Tips1') }}</p>
                <p>{{ $t('m.Judge_Extra_File_Tips2') }}</p>
                <i
                  slot="reference"
                  class="el-icon-question"
                ></i>
              </el-popover>
            </div>
            <el-row :gutter="20">
              <el-col
                :md="12"
                :xs="24"
              >
                <el-form-item>
                  <el-checkbox v-model="addUserExtraFile">{{
                    $t('m.User_Program')
                  }}</el-checkbox>
                </el-form-item>
                <el-form-item v-if="addUserExtraFile">
                  <AddExtraFile
                    :files.sync="userExtraFile"
                    type="user"
                    @upsertFile="upsertFile"
                    @deleteFile="deleteFile"
                  ></AddExtraFile>
                </el-form-item>
              </el-col>
              <el-col
                :md="12"
                :xs="24"
              >
                <el-form-item>
                  <el-checkbox v-model="addJudgeExtraFile">{{
                    $t('m.SPJ_Or_Interactive_Program')
                  }}</el-checkbox>
                </el-form-item>
                <el-form-item v-if="addJudgeExtraFile">
                  <AddExtraFile
                    :files.sync="judgeExtraFile"
                    type="judge"
                    @upsertFile="upsertFile"
                    @deleteFile="deleteFile"
                  ></AddExtraFile>
                </el-form-item>
              </el-col>
            </el-row>
          </template>
          <template v-if="!problem.isRemote">
            <div class="panel-title home-title">
              {{ $t('m.Judge_Mode') }}
              <el-popover
                placement="right"
                trigger="hover"
              >
                <p>1. {{ $t('m.General_Judge_Mode_Tips') }}</p>
                <p>2. {{ $t('m.Special_Judge_Mode_Tips') }}</p>
                <p>3. {{ $t('m.Interactive_Judge_Mode_Tips') }}</p>
                <i
                  slot="reference"
                  class="el-icon-question"
                ></i>
              </el-popover>
            </div>
            <el-form-item
              label=""
              :error="error.spj"
            >
              <el-col :span="24">
                <el-radio-group
                  v-model="problem.judgeMode"
                  @change="switchMode"
                >
                  <el-radio label="default">{{
                    $t('m.General_Judge')
                  }}</el-radio>
                  <el-radio label="spj">{{ $t('m.Special_Judge') }}</el-radio>
                  <el-radio label="interactive">{{
                    $t('m.Interactive_Judge')
                  }}</el-radio>
                </el-radio-group>
              </el-col>
            </el-form-item>
            <el-form-item v-if="problem.judgeMode != 'default'">
              <Accordion :title="
                  problem.judgeMode == 'spj'
                    ? $t('m.Special_Judge_Code')
                    : $t('m.Interactive_Judge_Code')
                ">
                <template slot="header">
                  <span style="margin-right:5px;">{{
                      problem.judgeMode == 'spj'
                        ? $t('m.SPJ_Language')
                        : $t('m.Interactive_Language')
                    }}：</span>
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
                  >{{ $t('m.Compile') }}
                  </el-button>
                </template>
                <code-mirror
                  v-model="problem.spjCode"
                  :mode="spjMode"
                ></code-mirror>
              </Accordion>
            </el-form-item>
          </template>
          <div class="panel-title home-title">{{ $t('m.Code_Template') }}</div>
          <el-form-item>
            <el-row>
              <el-col
                :span="24"
                v-for="(v, k) in codeTemplate"
                :key="'template' + k"
              >
                <el-form-item>
                  <el-checkbox v-model="v.status">{{ k }}</el-checkbox>
                  <div v-if="v.status">
                    <code-mirror
                      v-model="v.code"
                      :mode="v.mode"
                    ></code-mirror>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form-item>
          <el-row
            :gutter="20"
            v-if="!problem.isRemote"
          >
            <div class="panel-title home-title">
              {{ $t('m.Judge_Samples') }}
              <el-popover
                placement="right"
                trigger="hover"
              >
                <p>{{ $t('m.Sample_Tips') }}</p>
                <i
                  slot="reference"
                  class="el-icon-question"
                ></i>
              </el-popover>
            </div>

            <el-form-item required>
              <el-radio-group
                v-model="problem.judgeCaseMode"
                @change="switchJudgeCaseMode"
              >
                <el-radio :label="JUDGE_CASE_MODE.DEFAULT">
                  {{ problem.type == 1 ? $t('m.OI_Judge_Case_Default_Mode'): $t('m.ACM_Judge_Case_Default_Mode')}}
                </el-radio>
                <template v-if="problem.type == 1">
                  <el-radio :label="JUDGE_CASE_MODE.SUBTASK_LOWEST">{{$t('m.Judge_Case_Subtask_Lowest_Mode')}}</el-radio>
                  <el-radio :label="JUDGE_CASE_MODE.SUBTASK_AVERAGE">{{$t('m.Judge_Case_Subtask_Average_Mode')}}</el-radio>
                </template>
                <template v-else>
                  <el-radio :label="JUDGE_CASE_MODE.ERGODIC_WITHOUT_ERROR">{{$t('m.Judge_Case_Ergodic_Without_Error_Mode')}}</el-radio>
                </template>
              </el-radio-group>
            </el-form-item>

            <el-switch
              v-model="problem.isUploadCase"
              :active-text="$t('m.Use_Upload_File')"
              :inactive-text="$t('m.Use_Manual_Input')"
              style="margin: 10px 0"
            >
            </el-switch>

            <div v-show="problem.isUploadCase">
              <el-col :span="24">
                <el-form-item :error="error.testcase">
                  <el-upload
                    :action="uploadFileUrl+'&mode='+problem.judgeCaseMode"
                    name="file"
                    :show-file-list="true"
                    :on-success="uploadSucceeded"
                    :on-error="uploadFailed"
                  >
                    <el-button
                      size="small"
                      type="primary"
                      icon="el-icon-upload"
                    >{{ $t('m.Choose_File') }}</el-button>
                  </el-upload>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <vxe-table
                  ref="xTable"
                  stripe
                  auto-resize
                  :data="problem.testCaseScore"
                  align="center"
                  :sort-config="{trigger: 'cell', 
                  defaultSort: {field: 'groupNum', order: 'asc'}, 
                  orders: ['desc', 'asc', null],
                  sortMethod: customSortMethod}"
                >
                  <vxe-table-column
                    field="index"
                    title="#"
                    width="60"
                    sortable
                  ></vxe-table-column>
                  <vxe-table-column
                    field="input"
                    :title="$t('m.Sample_Input_File')"
                    min-width="100"
                  >
                  </vxe-table-column>
                  <vxe-table-column
                    field="output"
                    :title="$t('m.Sample_Output_File')"
                    min-width="100"
                  >
                  </vxe-table-column>
                  <vxe-table-column
                  v-if="problem.judgeCaseMode == JUDGE_CASE_MODE.SUBTASK_LOWEST 
                    || problem.judgeCaseMode == JUDGE_CASE_MODE.SUBTASK_AVERAGE"
                    field="groupNum"
                    :title="$t('m.Sample_Group_Num')"
                    sortable
                    min-width="100"
                  >
                    <template v-slot="{ row }">
                      <el-input
                        size="small"
                        v-model="row.groupNum"
                        @change="sortTestCaseList"
                        type="number"
                      ></el-input>
                    </template>
                  </vxe-table-column>
                  <vxe-table-column
                    field="score"
                    :title="$t('m.Score')"
                    v-if="problem.type == 1"
                    min-width="100"
                  >
                    <template v-slot="{ row }">
                      <el-input
                        size="small"
                        :placeholder="$t('m.Score')"
                        v-model="row.score"
                        :disabled="problem.type != 1"
                        type="number"
                      >
                      </el-input>
                    </template>
                  </vxe-table-column>
                </vxe-table>
              </el-col>
            </div>
            <div v-show="!problem.isUploadCase">
              <el-form-item
                v-for="(sample, index) in problemSamples"
                :key="'sample' + index"
              >
                <Accordion
                  :title="$t('m.Problem_Sample') + (sample.index)"
                  :isOpen="sample.isOpen ? true : false"
                  :index="index"
                  @changeVisible="changeSampleVisible"
                >
                  <el-button
                    type="danger"
                    size="small"
                    icon="el-icon-delete"
                    slot="header"
                    @click="deleteSample(index)"
                  >
                    {{ $t('m.Delete') }}
                  </el-button>
                  <el-row :gutter="20">
                    <el-col
                      :xs="24"
                      :md="12"
                    >
                      <el-form-item
                        :label="$t('m.Sample_Input')"
                        required
                      >
                        <el-input
                          :rows="5"
                          type="textarea"
                          :placeholder="$t('m.Sample_Input')"
                          v-model="sample.input"
                        >
                        </el-input>
                      </el-form-item>
                    </el-col>
                    <el-col
                      :xs="24"
                      :md="12"
                    >
                      <el-form-item
                        :label="$t('m.Sample_Output')"
                        required
                      >
                        <el-input
                          :rows="5"
                          type="textarea"
                          :placeholder="$t('m.Sample_Output')"
                          v-model="sample.output"
                        >
                        </el-input>
                      </el-form-item>
                    </el-col>
                    <el-col
                      :span="24"
                      v-show="problem.type == 1"
                    >
                      <el-form-item :label="$t('m.Score')">
                        <el-input
                          type="number"
                          size="small"
                          :placeholder="$t('m.Score')"
                          v-model="sample.score"
                        >
                        </el-input>
                      </el-form-item>
                    </el-col>
                    <el-col
                      :span="24"
                      v-show="problem.judgeCaseMode == JUDGE_CASE_MODE.SUBTASK_LOWEST 
                    || problem.judgeCaseMode == JUDGE_CASE_MODE.SUBTASK_AVERAGE"
                    >
                      <el-form-item :label="$t('m.Sample_Group_Num')">
                        <el-input
                          type="number"
                          size="small"
                          :placeholder="$t('m.Sample_Group_Num')"
                          v-model="sample.groupNum"
                          @change="sortManualProblemSampleList"
                        >
                        </el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </Accordion>
              </el-form-item>
              <div class="add-sample-btn">
                <el-button
                  class="add-samples"
                  @click="addSample()"
                  icon="el-icon-plus"
                  type="small"
                >{{ $t('m.Add_Sample') }}
                </el-button>
              </div>
            </div>
          </el-row>
          <el-form-item :label="$t('m.Source')">
            <el-input
              :placeholder="$t('m.Source')"
              v-model="problem.source"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('m.Auto_Remove_the_Blank_at_the_End_of_Code')"
            v-if="!problem.isRemote"
          >
            <el-switch
              v-model="problem.isRemoveEndBlank"
              active-text=""
              inactive-text=""
            >
            </el-switch>
          </el-form-item>
          <el-form-item :label="$t('m.Publish_the_Judging_Result_of_Test_Data')">
            <el-switch
              v-model="problem.openCaseResult"
              active-text=""
              inactive-text=""
            >
            </el-switch>
          </el-form-item>
          <el-button
            type="primary"
            @click.native="submit()"
            size="small"
          >{{
            $t('m.Save')
          }}</el-button>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import utils from "@/common/utils";
import { mapGetters } from "vuex";
import api from "@/common/api";
import mMessage from "@/common/message";
import { PROBLEM_LEVEL, JUDGE_CASE_MODE } from "@/common/constants";
import Editor from "@/components/admin/Editor.vue";
import Accordion from "@/components/admin/Accordion.vue";
import AddExtraFile from "@/components/admin/AddExtraFile.vue";
import CodeMirror from "@/components/admin/CodeMirror.vue";
export default {
  name: "GroupProblem",
  components: {
    Accordion,
    AddExtraFile,
    CodeMirror,
    Editor,
  },
  props: {
    mode: {
      type: String,
      default: "edit",
    },
    title: {
      type: String,
      default: "Edit Problem",
    },
    apiMethod: {
      type: String,
      default: "addGroupProblem",
    },
    pid: {
      type: Number,
      default: null,
    },
    contestId: {
      type: Number,
      default: null,
    },
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
      contest: {},
      codeTemplate: {},
      contestProblem: {
        displayId: null,
        displayTitle: null,
        cid: null,
        pid: null,
      },
      problem: {
        id: null,
        title: "",
        problemId: "",
        description: "",
        input: "",
        output: "",
        timeLimit: 1000,
        memoryLimit: 256,
        stackLimit: 128,
        difficulty: 0,
        auth: 1,
        codeShare: true,
        examples: [],
        spjLanguage: "",
        spjCode: "",
        spjCompileOk: false,
        uploadTestcaseDir: "",
        testCaseScore: [],
        isRemote: false,
        isUploadCase: true,
        type: 0,
        hint: "",
        source: "",
        cid: null,
        isRemoveEndBlank:false,
        openCaseResult: true,
        judgeMode: "default",
        judgeCaseMode: "default",
        userExtraFile: "",
        judgeExtraFile: "",
        gid: null,
      },
      problemTags: [],
      problemLanguages: [],
      problemSamples: [],
      problemCodeTemplate: [],
      reProblem: {},
      testCaseUploaded: false,
      allLanguage: [],
      allSpjLanguage: [],
      allTags: [],
      allTagsTmp: [],
      inputVisible: false,
      tagInput: "",
      spjMode: "",
      disableRuleType: false,
      routeName: "",
      uploadTestcaseDir: "",
      uploadFileUrl: "",
      error: {
        tags: "",
        spj: "",
        languages: "",
        testCase: "",
      },
      PROBLEM_LEVEL: {},
      JUDGE_CASE_MODE: {},
      spjRecord: {
        spjCode: "",
        spjLanguage: "",
      },
      addUserExtraFile: false,
      addJudgeExtraFile: false,
      userExtraFile: null,
      judgeExtraFile: null,
      judgeCaseModeRecord: "default",
      sampleIndex: 1,
    };
  },
  mounted() {
    let gid = this.$route.params.groupID;
    this.PROBLEM_LEVEL = Object.assign({}, PROBLEM_LEVEL);
    this.JUDGE_CASE_MODE = Object.assign({}, JUDGE_CASE_MODE);
    this.uploadFileUrl = "/api/file/upload-testcase-zip?gid=" + gid;
    api
      .getGroupProblemTagList(gid)
      .then((res) => {
        this.allTags = res.data.data;
        for (let tag of res.data.data) {
          this.allTagsTmp.push({ value: tag.name, oj: tag.oj });
        }
      })
      .catch(() => {});
    api.getLanguages(this.$route.params.problemId, false).then((res) => {
      let allLanguage = res.data.data;
      this.allLanguage = allLanguage;
      for (let i = 0; i < allLanguage.length; i++) {
        if (allLanguage[i].isSpj == true) {
          this.allSpjLanguage.push(allLanguage[i]);
        }
      }
      this.problem = this.reProblem = {
        id: null,
        problemId: "",
        title: "",
        description: "",
        input: "",
        output: "",
        timeLimit: 1000,
        memoryLimit: 256,
        stackLimit: 128,
        difficulty: 0,
        auth: 1,
        codeShare: true,
        examples: [],
        spjLanguage: "",
        spjCode: "",
        spjCompileOk: false,
        isUploadCase: true,
        uploadTestcaseDir: "",
        testCaseScore: [],
        contestProblem: {},
        type: 0,
        hint: "",
        source: "",
        cid: null,
        isRemoveEndBlank:false,
        openCaseResult: true,
        judgeMode: "default",
        judgeCaseMode: "default",
        userExtraFile: null,
        judgeExtraFile: null,
      };
      if (this.contestId) {
        this.problem.cid = this.reProblem.cid = this.contestId;
        this.problem.auth = this.reProblem.auth = 3;
        this.disableRuleType = true;
        api.getGroupContest(this.contestId).then((res) => {
          this.problem.type = this.reProblem.type = res.data.data.type;
          this.contest = res.data.data;
        });
      }
      this.problem.spjLanguage = "C";
      this.init();
    });
  },
  watch: {
    $route() {
      this.$refs.form.resetFields();
      this.problem = this.reProblem;
      this.problemTags = [];
      this.problemLanguages = [];
      this.problemSamples = [];
      this.problemCodeTemplate = [];
      this.codeTemplate = [];
      this.init();
    },
    problemLanguages(newVal) {
      let data = {};
      let languages = JSON.parse(JSON.stringify(newVal)).sort();
      for (let item of languages) {
        if (this.codeTemplate[item] === undefined) {
          let langConfig = this.allLanguage.find((lang) => {
            return lang.name === item;
          });
          let codeTemp;
          let problemCodeTemplate = this.problemCodeTemplate;
          if (problemCodeTemplate) {
            codeTemp = problemCodeTemplate.find((temp) => {
              return temp.lid == langConfig.id;
            });
          }
          if (codeTemp == undefined) {
            data[item] = {
              id: null,
              status: false,
              code: langConfig.codeTemplate,
              mode: langConfig.contentType,
            };
          } else {
            data[item] = {
              id: codeTemp.id,
              status: true,
              code: codeTemp.code,
              mode: langConfig.contentType,
            };
          }
        } else {
          data[item] = this.codeTemplate[item];
        }
      }
      this.codeTemplate = data;
    },
    "problem.spjLanguage"(newVal) {
      if (this.allSpjLanguage.length) {
        this.spjMode = this.allSpjLanguage.find((item) => {
          return item.name == this.problem.spjLanguage && item.isSpj == true;
        })["contentType"];
      }
    },
  },
  methods: {
    init() {
      if (this.mode === "edit") {
        api.getGroupProblem(this.pid).then((problemRes) => {
          let data = problemRes.data.data;
          data.spjCompileOk = false;
          data.uploadTestcaseDir = "";
          data.testCaseScore = [];
          if (!data.spjCode) {
            data.spjCode = "";
          }
          data.spjLanguage = data.spjLanguage || "C";
          data.problemId = data.problemId.slice(this.group.shortName.length);
          this.spjRecord.spjLanguage = data.spjLanguage;
          this.spjRecord.spjCode = data.spjCode;
          this.judgeCaseModeRecord = data.judgeCaseModeRecord;
          this.problem = data;
          this.problem["examples"] = utils.stringToExamples(data.examples);
          this.problem["examples"][0]["isOpen"] = true;
          this.testCaseUploaded = true;
          if (this.problem.userExtraFile) {
            this.addUserExtraFile = true;
            this.userExtraFile = JSON.parse(this.problem.userExtraFile);
          }
          if (this.problem.judgeExtraFile) {
            this.addJudgeExtraFile = true;
            this.judgeExtraFile = JSON.parse(this.problem.judgeExtraFile);
          }
          api
            .getGroupProblemCases(this.pid, this.problem.isUploadCase)
            .then((res) => {
              if (this.problem.isUploadCase) {
                this.problem.testCaseScore = res.data.data;
                this.problem.testCaseScore.forEach((item, index) => {
                  item.index = index + 1;
                });
                this.$refs.xTable.sort("groupNum", "asc");
              } else {
                this.problemSamples = res.data.data;
                if (
                  this.problemSamples != null &&
                  this.problemSamples.length > 0
                ) {
                  this.problemSamples[0]["isOpen"] = true;
                  this.problemSamples.forEach((item, index) => {
                    item.index = index + 1;
                  });
                  this.sampleIndex = this.problemSamples.length + 1;
                }
              }
            });
        });
        if (this.contestId) {
          api.getGroupContestProblem(this.pid, this.contestId).then((res) => {
            this.contestProblem = res.data.data;
          });
        }
        this.getProblemCodeTemplateAndLanguage();
        api.getGroupProblemTags(this.pid).then((res) => {
          this.problemTags = res.data.data;
        });
      } else {
        this.addExample();
        this.testCaseUploaded = false;
        for (let item of this.allLanguage) {
          this.problemLanguages.push(item.name);
        }
      }
    },
    async getProblemCodeTemplateAndLanguage() {
      const that = this;
      await api.getProblemCodeTemplate(that.pid).then((res) => {
        that.problemCodeTemplate = res.data.data;
      });
      api.getProblemLanguages(that.pid).then((res) => {
        let Languages = res.data.data;
        for (let i = 0; i < Languages.length; i++) {
          that.problemLanguages.push(Languages[i].name);
        }
      });
    },
    switchMode(mode) {
      let modeName = "General_Judge";
      let modeTips = "General_Judge_Mode_Tips";
      if (mode == "spj") {
        modeName = "Special_Judge";
        modeTips = "Special_Judge_Mode_Tips";
      } else if (mode == "interactive") {
        modeName = "Interactive_Judge";
        modeTips = "Interactive_Judge_Mode_Tips";
      }
      const h = this.$createElement;
      this.$msgbox({
        title: this.$i18n.t("m." + modeName),
        message: h("div", null, [
          h(
            "p",
            { style: "text-align: center;font-weight:bolder;color:red" },
            this.$i18n.t("m.Change_Judge_Mode")
          ),
          h("br", null, null),
          h(
            "p",
            { style: "font-weight:bolder" },
            this.$i18n.t("m." + modeTips)
          ),
        ]),
      });
    },
    querySearch(queryString, cb) {
      var ojName = "ME";
      if (this.problem.isRemote) {
        ojName = this.problem.problemId.split("-")[0];
      }
      var restaurants = this.allTagsTmp.filter((item) => item.oj == ojName);
      var results = queryString
        ? restaurants.filter(
            (item) =>
              item.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
          )
        : restaurants;
      cb(results);
    },
    changeContent(newVal) {
      this.announcement.content = newVal;
    },
    getLevelName(difficulty) {
      return utils.getLevelName(difficulty);
    },
    selectTag(item) {
      for (var i = 0; i < this.problemTags.length; i++) {
        if (this.problemTags[i].name == item.value) {
          mMessage.warning(this.$i18n.t("m.Add_Tag_Error"));
          this.tagInput = "";
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
            mMessage.warning(this.$i18n.t("m.Add_Tag_Error"));
            this.tagInput = "";
            return;
          }
        }
        this.problemTags.push(newTag);
        this.inputVisible = false;
        this.tagInput = "";
      }
    },
    closeTag(tag) {
      this.problemTags.splice(
        this.problemTags.map((item) => item.name).indexOf(tag),
        1
      );
    },
    deleteFile(type, name) {
      if (type == "user") {
        this.$delete(this.userExtraFile, name);
      } else {
        this.$delete(this.judgeExtraFile, name);
      }
    },
    upsertFile(type, name, oldname, content) {
      if (type == "user") {
        if (oldname && oldname != name) {
          this.$delete(this.userExtraFile, oldname);
        }
        if (!this.userExtraFile) {
          this.userExtraFile = {};
        }
        this.userExtraFile[name] = content;
      } else {
        if (oldname && oldname != name) {
          this.$delete(this.judgeExtraFile, name);
        }
        if (!this.judgeExtraFile) {
          this.judgeExtraFile = {};
        }
        this.judgeExtraFile[name] = content;
      }
    },
    problemTypeChange(type) {
      if (type == 1) {
        let length = this.problemSamples.length;
        let aver = parseInt(100 / length);
        let add_1_num = 100 - aver * length;
        for (let i = 0; i < length; i++) {
          if (i >= length - add_1_num) {
            this.problemSamples[i].score = aver + 1;
          } else {
            this.problemSamples[i].score = aver;
          }
        }
      }
    },
    addExample() {
      this.problem.examples.push({ input: "", output: "", isOpen: true });
    },
    changeExampleVisible(index, isOpen) {
      this.problem.examples[index]["isOpen"] = isOpen;
    },
    addSample() {
      let len = this.sampleIndex;
      if (this.mode === "edit") {
        this.problemSamples.push({
          input: "",
          output: "",
          score: this.problem.type == 0 ? null : 0,
          groupNum: this.problem.type == 0 ? null : len,
          pid: this.pid,
          isOpen: true,
          index: len,
        });
      } else {
        this.problemSamples.push({
          input: "",
          output: "",
          score: this.problem.type == 0 ? null : 0,
          groupNum: this.problem.type == 0 ? null : len,
          pid: this.pid,
          isOpen: true,
          index: len,
        });
      }
      this.sampleIndex = len + 1;
      this.sortManualProblemSampleList();
    },

    deleteExample(index) {
      this.problem.examples.splice(index, 1);
    },
    deleteSample(index) {
      this.problemSamples.splice(index, 1);
    },
    changeSampleVisible(index, isOpen) {
      this.problemSamples[index]["isOpen"] = isOpen;
    },
    uploadSucceeded(response) {
      if (response.status != 200) {
        mMessage.error(response.msg);
        this.testCaseUploaded = false;
        return;
      }
      mMessage.success(this.$i18n.t("m.Upload_Testcase_Successfully"));
      let fileList = response.data.fileList;
      let averSorce = parseInt(100 / fileList.length);
      let add_1_num = 100 - averSorce * fileList.length;
      for (let i = 0; i < fileList.length; i++) {
        if (averSorce) {
          if (i >= fileList.length - add_1_num) {
            fileList[i].score = averSorce + 1;
          } else {
            fileList[i].score = averSorce;
          }
        }
        if (!fileList[i].output) {
          fileList[i].output = "-";
        }
        fileList[i].pid = this.problem.id;
      }
      this.problem.testCaseScore = fileList;
      this.problem.testCaseScore.forEach((item, index) => {
        item.index = index + 1;
      });
      this.testCaseUploaded = true;
      this.problem.uploadTestcaseDir = response.data.fileListDir;
    },
    uploadFailed() {
      mMessage.error(this.$i18n.t("m.Upload_Testcase_Failed"));
    },
    compileSPJ() {
      let data = {
        pid: this.problem.id,
        code: this.problem.spjCode,
        language: this.problem.spjLanguage,
        extraFiles: this.judgeExtraFile,
      };
      this.loadingCompile = true;
      let apiMethodName = "groupCompileSpj";
      if (this.problem.judgeMode == "interactive") {
        apiMethodName = "groupCompileInteractive";
      }
      api[apiMethodName](data, this.$route.params.groupID).then(
        (res) => {
          this.loadingCompile = false;
          this.problem.spjCompileOk = true;
          this.error.spj = "";
          mMessage.success(this.$i18n.t("m.Compiled_Successfully"));
        },
        (err) => {
          this.loadingCompile = false;
          this.problem.spjCompileOk = false;
          const h = this.$createElement;
          this.$msgbox({
            title: "Compile Error",
            type: "error",
            message: h("pre", err.data.msg),
            showCancelButton: false,
            closeOnClickModal: false,
            customClass: "dialog-compile-error",
          });
        }
      );
    },
    sortTestCaseList() {
      this.$refs.xTable.clearSort();
      this.$refs.xTable.sort("groupNum", "asc");
    },
    customSortMethod({ data, sortList }) {
      const sortItem = sortList[0];
      const { property, order } = sortItem;
      let list = [];
      list = data.sort(function (a, b) {
        var value1 = a.groupNum,
          value2 = b.groupNum;
        if (value1 === value2) {
          return a.index - b.index;
        }
        if (order == "desc") {
          return value2 - value1;
        } else {
          return value1 - value2;
        }
      });
      return list;
    },
    sortManualProblemSampleList() {
      this.problemSamples = this.problemSamples.sort(function (a, b) {
        var value1 = a.groupNum,
          value2 = b.groupNum;
        if (value1 === value2) {
          return a.index - b.index;
        }
        return value1 - value2;
      });
    },
    submit() {
      if (!this.problem.problemId) {
        mMessage.error(
          this.$i18n.t("m.Problem_Display_ID") +
            " " +
            this.$i18n.t("m.is_required")
        );
        return;
      }
      if (this.contestId) {
        if (!this.contestProblem.displayId) {
          mMessage.error(
            this.$i18n.t("m.Contest_Display_ID") +
              " " +
              this.$i18n.t("m.is_required")
          );
          return;
        }
        if (!this.contestProblem.displayTitle) {
          mMessage.error(
            this.$i18n.t("m.Contest_Display_Title") +
              " " +
              this.$i18n.t("m.is_required")
          );
          return;
        }
      }
      if (!this.problem.isRemote) {
        if (!this.problem.isUploadCase) {
          if (!this.problemSamples.length) {
            mMessage.error(
              this.$i18n.t("m.Judge_Samples") +
                " " +
                this.$i18n.t("m.is_required")
            );
            return;
          }
          for (let sample of this.problemSamples) {
            if (!sample.input && !sample.output) {
              mMessage.error(
                this.$i18n.t("m.Sample_Input") +
                  " or " +
                  this.$i18n.t("m.Sample_Output") +
                  " " +
                  this.$i18n.t("m.is_required")
              );
              return;
            }
          }
          if (this.problem.type == 1) {
            for (let i = 0; i < this.problemSamples.length; i++) {
              if (this.problemSamples[i].score == "") {
                mMessage.error(
                  this.$i18n.t("m.Problem_Sample") +
                    this.problemSamples[i].index +
                    " " +
                    this.$i18n.t("m.Score_must_be_an_integer")
                );
                return;
              }
              try {
                if (parseInt(this.problemSamples[i].score) < 0) {
                  mMessage.error(
                    this.$i18n.t("m.Problem_Sample") +
                      this.problemSamples[i].index +
                      " " +
                      this.$i18n.t("m.Score_must_be_greater_than_or_equal_to_0")
                  );
                  return;
                }
              } catch (e) {
                mMessage.error(this.$i18n.t("m.Score_must_be_an_integer"));
                return;
              }
              if (
                (this.problem.judgeCaseMode == this.JUDGE_CASE_MODE.SUBTASK_LOWEST 
                  || this.problem.judgeCaseMode == this.JUDGE_CASE_MODE.SUBTASK_AVERAGE
                ) && this.problemSamples[i].groupNum == ""
              ) {
                mMessage.error(
                  this.$i18n.t("m.Problem_Sample") +
                    this.problemSamples[i].index +
                    "：" +
                    this.$i18n.t(
                      "m.Non_Default_Judge_Case_Mode_And_Group_Num_IS_NULL"
                    )
                );
                return;
              }
            }
          }
        } else {
          if (!this.testCaseUploaded) {
            this.error.testCase =
              this.$i18n.t("m.Judge_Samples") +
              " " +
              this.$i18n.t("m.is_required");
            mMessage.error(this.error.testCase);
            return;
          }
          if (this.problem.type == 1) {
            let problemSamples = this.problem.testCaseScore;
            for (let i = 0; i < problemSamples.length; i++) {
              if (problemSamples[i].score == "") {
                mMessage.error(
                  this.$i18n.t("m.Problem_Sample") +
                    (i + 1) +
                    " " +
                    this.$i18n.t("m.Score_must_be_an_integer")
                );
                return;
              }
              try {
                if (parseInt(problemSamples[i].score) < 0) {
                  mMessage.error(
                    this.$i18n.t("m.Problem_Sample") +
                      (i + 1) +
                      " " +
                      this.$i18n.t("m.Score_must_be_greater_than_or_equal_to_0")
                  );
                  return;
                }
              } catch (e) {
                mMessage.error(this.$i18n.t("m.Score_must_be_an_integer"));
                return;
              }
              if (
                (this.problem.judgeCaseMode == this.JUDGE_CASE_MODE.SUBTASK_LOWEST 
                  || this.problem.judgeCaseMode == this.JUDGE_CASE_MODE.SUBTASK_AVERAGE
                ) && problemSamples[i].groupNum == ""
              ) {
                mMessage.error(
                  this.$i18n.t("m.Problem_Sample") +
                    (i + 1) +
                    "：" +
                    this.$i18n.t(
                      "m.Non_Default_Judge_Case_Mode_And_Group_Num_IS_NULL"
                    )
                );
                return;
              }
            }
          }
        }
      }
      // 允许题目标签为空
      // if (!this.problemTags.length) {
      //   this.error.tags =
      //     this.$i18n.t('m.Tags') + ' ' + this.$i18n.t('m.is_required');
      //   mMessage.error(this.error.tags);
      //   return;
      // }
      let isChangeModeCode =
        this.spjRecord.spjLanguage != this.problem.spjLanguage ||
        this.spjRecord.spjCode != this.problem.spjCode;
      if (!this.problem.isRemote) {
        if (this.problem.judgeMode != "default") {
          if (!this.problem.spjCode) {
            this.error.spj =
              this.$i18n.t("m.Spj_Or_Interactive_Code") +
              " " +
              this.$i18n.t("m.is_required");
            mMessage.error(this.error.spj);
          } else if (!this.problem.spjCompileOk && isChangeModeCode) {
            this.error.spj = this.$i18n.t(
              "m.Spj_Or_Interactive_Code_not_Compile_Success"
            );
          }
          if (this.error.spj) {
            mMessage.error(this.error.spj);
            return;
          }
        }
      }
      if (!this.problemLanguages.length) {
        this.error.languages =
          this.$i18n.t("m.Language") + " " + this.$i18n.t("m.is_required");
        mMessage.error(this.error.languages);
        return;
      }
      if (!this.problem.id) {
        this.problem.gid = this.$route.params.groupID;
      }
      if (this.apiMethod === "updateGroupContestProblem") {
        this.problem.cid = this.contest.id;
      }
      if (
        this.apiMethod === "addGroupProblem" ||
        this.apiMethod === "addGroupContestProblem"
      ) {
        this.problem.author = this.userInfo.username;
      }
      var ojName = "ME";
      if (this.problem.isRemote) {
        ojName = this.problem.problemId.split("-")[0];
      }
      let problemTagList = [];
      if (this.problemTags.length > 0) {
        problemTagList = Object.assign([], this.problemTags);
        for (let i = 0; i < problemTagList.length; i++) {
          for (let tag2 of this.allTags) {
            if (problemTagList[i].name == tag2.name && tag2.oj == ojName) {
              problemTagList[i] = tag2;
              break;
            }
          }
        }
      }
      this.problemCodeTemplate = [];
      let problemLanguageList = Object.assign([], this.problemLanguages);
      for (let i = 0; i < problemLanguageList.length; i++) {
        problemLanguageList[i] = { name: problemLanguageList[i] };
        for (let lang of this.allLanguage) {
          if (problemLanguageList[i].name == lang.name) {
            problemLanguageList[i] = lang;
            if (this.codeTemplate[lang.name].status) {
              if(this.codeTemplate[lang.name].code == null 
                || this.codeTemplate[lang.name].code.length == 0){
                  mMessage.error(
                    lang.name +
                      "：" +
                      this.$i18n.t("m.Code_template_of_the_language_cannot_be_empty")
                  );
                  return;
              }
              this.problemCodeTemplate.push({
                id: this.codeTemplate[lang.name].id,
                pid: this.pid,
                code: this.codeTemplate[lang.name].code,
                lid: lang.id,
                status: this.codeTemplate[lang.name].status,
              });
            }
            break;
          }
        }
      }
      let problemDto = {};
      if (this.problem.judgeMode != "default") {
        if (isChangeModeCode) {
          problemDto["changeModeCode"] = true;
        }
      } else {
        if (!this.spjRecord.spjCode) {
          problemDto["changeModeCode"] = true;
          this.problem.spjCode = null;
          this.problem.spjLanguage = null;
        }
      }
      if (this.userExtraFile && Object.keys(this.userExtraFile).length != 0) {
        this.problem.userExtraFile = JSON.stringify(this.userExtraFile);
      } else {
        this.problem.userExtraFile = null;
      }
      if (this.judgeExtraFile && Object.keys(this.judgeExtraFile).length != 0) {
        this.problem.judgeExtraFile = JSON.stringify(this.judgeExtraFile);
      } else {
        this.problem.judgeExtraFile = null;
      }
      problemDto["problem"] = Object.assign({}, this.problem); // 深克隆
      problemDto.problem.examples = utils.examplesToString(
        this.problem.examples
      );
      problemDto["codeTemplates"] = this.problemCodeTemplate;
      problemDto["tags"] = problemTagList;
      problemDto["languages"] = problemLanguageList;
      problemDto["isUploadTestCase"] = this.problem.isUploadCase;
      problemDto["uploadTestcaseDir"] = this.problem.uploadTestcaseDir;
      problemDto["judgeMode"] = this.problem.judgeMode;
      if (this.problem.isUploadCase) {
        problemDto["samples"] = this.problem.testCaseScore;
      } else {
        problemDto["samples"] = this.problemSamples;
      }

      if (this.judgeCaseModeRecord != this.problem.judgeCaseModeRecord) {
        problemDto["changeJudgeCaseMode"] = true;
      } else {
        problemDto["changeJudgeCaseMode"] = false;
      }

      api[this.apiMethod](problemDto)
        .then((res) => {
          if (this.contestId) {
            if (res.data.data) {
              this.contestProblem["pid"] = res.data.data.pid;
              this.contestProblem["cid"] = this.contestId;
            }
            api
              .updateGroupContestProblem(this.contestProblem)
              .then((res) => {});
          }
          if (this.mode === "edit") {
            mMessage.success(this.$t("m.Update_Successfully"));
            this.$emit("handleEditPage");
          } else {
            mMessage.success(this.$t("m.Create_Successfully"));
            if (this.contestId) {
              this.$emit("handleCreateProblemPage");
            } else {
              this.$emit("handleCreatePage");
            }
          }
          this.$emit("currentChange", 1);
        })
        .catch(() => {});
    },
  },
  computed: {
    ...mapGetters(["userInfo", "group"]),
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
  background-color: #2d8cf0 !important;
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
  background-color: #19be6b !important;
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
