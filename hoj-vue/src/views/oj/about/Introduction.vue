<template>
  <div>
    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <el-card class="container">
          <div slot="header">
            <span class="panel-title home-title">{{
              $t('m.Compiler') + ' & ' + $t('m.Example')
            }}</span>
          </div>
          <div class="content">
            <ul>
              <li v-for="lang in languages" :key="lang.name">
                {{ lang.name }} ( {{ lang.description }} )
                <p style="color: #409EFF;font-size:16px">
                  {{ $t('m.Compiler') }}
                </p>
                <pre>{{ lang.compileCommand }}</pre>
                <p style="color: #409EFF;font-size:16px">
                  A+B {{ $t('m.Problem') }}
                </p>
                <Highlight
                  :code="lang.template"
                  :language="lang.name"
                ></Highlight>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card class="container">
          <div slot="header">
            <span class="panel-title home-title">{{
              $t('m.Result_Explanation')
            }}</span>
          </div>
          <ul class="result">
            <li>
              <span :class="getStatusColor(5)">Pending</span>
              ：{{ $t('m.Pending_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(10)">Submitted Failed</span>
              ：{{ $t('m.Submitted_Faild_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(6)">Compiling</span>
              ：{{ $t('m.Compiling_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(7)">Judging</span>
              ：{{ $t('m.Judging_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(-2)">Compile Error</span> :
              {{ $t('m.Compile_Error_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(-3)">Presentation Error</span> :
              {{ $t('m.Persentation_Error_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(8)">Partial Accepted</span> :
              {{ $t('m.Partial_Accepted_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(0)">Accepted</span> :
              {{ $t('m.Accepted_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(-1)">Wrong Answer</span> :
              {{ $t('m.Wrong_Answer_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(3)">Runtime Error</span> :
              {{ $t('m.Runtime_Error_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(1)">Time Limit Exceeded</span> :
              {{ $t('m.Time_Limit_Exceeded_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(2)">Memory Limit Exceeded</span> :
              {{ $t('m.Memory_Limit_Exceeded_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(4)">System Error</span> :
              {{ $t('m.System_Error_Description') }}
            </li>
            <li>
              <span :class="getStatusColor(-4)">Cancelled</span> :
              {{ $t('m.Cancelled_Description') }}
            </li>
          </ul>
        </el-card>
        <el-card class="container">
          <div slot="header">
            <span class="panel-title home-title">{{
              $t('m.Compile_Explanation')
            }}</span>
          </div>
          <ul class="result">
            <li>1. {{ $t('m.Compile_Tips1') }}</li>
            <li>2. {{ $t('m.Compile_Tips2') }}</li>
            <li>3. {{ $t('m.Compile_Tips3') }}</li>
            <li>4. {{ $t('m.Compile_Tips4') }}</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import utils from '@/common/utils';
import { JUDGE_STATUS } from '@/common/constants';
import { addCodeBtn } from '@/common/codeblock';
const Highlight = () => import('@/components/oj/common/Highlight');
export default {
  components: {
    Highlight,
  },
  data() {
    return {
      languages: [],
      JUDGE_STATUS: JUDGE_STATUS,
    };
  },
  mounted() {
    setTimeout(() => {
      this.$nextTick((_) => {
        addCodeBtn();
      });
    }, 200);
  },
  methods: {
    getStatusColor(status) {
      return 'el-tag el-tag--medium status-' + JUDGE_STATUS[status].color;
    },
  },
  beforeRouteEnter(to, from, next) {
    utils.getLanguages(true).then((languages) => {
      next((vm) => {
        vm.languages = languages.filter(function(element, index, array) {
          return element.oj == 'ME';
        });
      });
    });
  },
  watch: {
    languages(newVal, oldVal) {
      if (newVal.length > 0) {
        this.$nextTick((_) => {
          addCodeBtn();
        });
      }
    },
  },
};
</script>

<style scoped>
.container {
  margin-bottom: 20px;
}
.container .content {
  font-size: 16px;
  margin: 0 50px 20px 50px;
}
.container .content pre {
  padding: 5px 10px;
  white-space: pre-wrap;
  margin-top: 15px;
  margin-bottom: 15px;
  background: #f8f8f9;
  border: 1px dashed #e9eaec;
}
@media screen and (max-width: 768px) {
  .container .content {
    font-size: 1rem;
    margin: 0 5px;
  }
}
ul {
  list-style: disc;
  padding-inline-start: 0px;
}
li {
  line-height: 2;
}
li .title {
  font-weight: 600;
  font-size: 1rem;
}
.result li {
  list-style-type: none;
  margin-top: 8px;
}
</style>
