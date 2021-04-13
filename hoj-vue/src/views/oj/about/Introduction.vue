<template>
  <div>
    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <el-card class="container">
          <div slot="header">
            <span class="panel-title home-title">Compiler & Example</span>
          </div>
          <div class="content markdown-body">
            <ul>
              <li v-for="lang in languages" :key="lang.name">
                {{ lang.name }} ( {{ lang.description }} )
                <p style="color: #409EFF;font-size:16px">Compiler</p>
                <pre>{{ lang.compileCommand }}</pre>
                <p style="color: #409EFF;font-size:16px">A+B Problem</p>
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
            <span class="panel-title home-title">Result Explanation</span>
          </div>
          <ul class="result">
            <li>
              <span :class="getStatusColor(5)">Pending</span>
              ：您的解答正在排队等待测评中，请等待结果...
            </li>
            <li>
              <span :class="getStatusColor(10)">Submitted Failed</span>
              ：您的此次提交失败，请点击按钮重新提交...
            </li>
            <li>
              <span :class="getStatusColor(6)">Compiling</span>
              ：正在对您的源代码进行编译中，请等待结果...
            </li>
            <li>
              <span :class="getStatusColor(7)">Judging</span>
              ：正在使用测试数据运行您的程序中，请等待结果...
            </li>
            <li>
              <span :class="getStatusColor(-2)">Compile Error</span> :
              无法编译您的源代码，点击链接查看编译器的输出。
            </li>
            <li>
              <span :class="getStatusColor(-3)">Presentation Error</span> :
              您提交的代码已经很接近正确答案，请检查代码格式输出是否有多余空格，换行等空白符。
            </li>
            <li>
              <span :class="getStatusColor(0)">Accepted</span> :
              您的解题方法是正确的。
            </li>
            <li>
              <span :class="getStatusColor(-1)">Wrong Answer</span> :
              您的程序输出结果与判题程序的答案不符。
            </li>
            <li>
              <span :class="getStatusColor(3)">Runtime Error</span> :
              您的程序异常终止，可能的原因是：段错误，被零除或用非0的代码退出程序。
            </li>
            <li>
              <span :class="getStatusColor(1)">Time Limit Exceeded</span> :
              您的程序使用的 CPU 时间已超出题目限制。
            </li>
            <li>
              <span :class="getStatusColor(2)">Memory Limit Exceeded</span> :
              您的程序实际使用的内存已超出题目限制。
            </li>
            <li>
              <span :class="getStatusColor(4)">System Error</span> :
              糟糕，判题机系统出了问题。请报告给管理员。
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import utils from '@/common/utils';
import Highlight from '@/components/oj/common/Highlight';
import { JUDGE_STATUS } from '@/common/constants';
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
  font-weight: 500;
}
.result li {
  list-style-type: none;
  margin-top: 8px;
}
</style>
