<template>
  <codemirror
    v-model="currentValue"
    :options="options"
    ref="editor"
  ></codemirror>
</template>
<script>
import { codemirror, CodeMirror } from 'vue-codemirror-lite';
import 'codemirror/mode/javascript/javascript';
import 'codemirror/mode/clike/clike.js';
import 'codemirror/mode/python/python.js';
import 'codemirror/theme/solarized.css';
// active-line.js
import 'codemirror/addon/selection/active-line.js';
// foldGutter
import 'codemirror/addon/fold/foldgutter.css';
import 'codemirror/addon/fold/foldgutter.js';
import 'codemirror/addon/fold/brace-fold.js';
import 'codemirror/addon/fold/indent-fold.js';
import 'codemirror/addon/edit/matchbrackets.js';
import 'codemirror/addon/edit/matchtags.js';
import 'codemirror/addon/edit/closetag.js';
import 'codemirror/addon/edit/closebrackets.js';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/hint/show-hint.js';
import 'codemirror/addon/hint/anyword-hint.js';

export default {
  name: 'CodeMirror',
  data() {
    return {
      currentValue: '',
      options: {
        mode: 'text/x-c++src',
        lineNumbers: true,
        lineWrapping: false,
        theme: 'solarized',
        tabSize: 4,
        line: true,
        foldGutter: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
        indentUnit: 4, //一个块（编辑语言中的含义）应缩进多少个空格
        styleActiveLine: true,
        autofocus: false,
        matchBrackets: true, //括号匹配
        styleActiveLine: true,
        autoCloseBrackets: true,
        autoCloseTags: true,
      },
    };
  },
  components: {
    codemirror,
  },
  props: {
    value: {
      type: String,
      default: '',
    },
    mode: {
      type: String,
      default: 'text/x-c++src',
    },
  },
  mounted() {
    this.currentValue = this.value;
    this.$refs.editor.editor.setOption('mode', this.mode);
    this.$refs.editor.editor.on('inputRead', (instance, changeObj) => {
      if (/\w|\./g.test(changeObj.text[0]) && changeObj.origin !== 'complete') {
        instance.showHint({
          hint: CodeMirror.hint.anyword,
          completeSingle: false,
          range: 1000, // 附近多少行代码匹配
        });
      }
    });
  },
  watch: {
    value(val) {
      if (this.currentValue !== val) {
        this.currentValue = val;
      }
    },
    currentValue(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.$emit('change', newVal);
        this.$emit('input', newVal);
      }
    },
    mode(newVal) {
      this.$refs.editor.editor.setOption('mode', newVal);
    },
  },
};
</script>

<style scoped>
.CodeMirror {
  height: auto !important;
}

.CodeMirror-scroll {
  min-height: 300px;
  max-height: 600px;
}
</style>
