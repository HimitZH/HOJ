<template>
  <codemirror
    v-model="currentValue"
    :options="options"
    ref="editor"
  ></codemirror>
</template>
<script>
import { codemirror } from 'vue-codemirror-lite';
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

export default {
  name: 'CodeMirror',
  data() {
    return {
      currentValue: '',
      options: {
        mode: 'text/x-csrc',
        lineNumbers: true,
        lineWrapping: false,
        theme: 'solarized',
        tabSize: 4,
        line: true,
        foldGutter: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
        indentUnit: 4, //一个块（编辑语言中的含义）应缩进多少个空格
        styleActiveLine: true,
        autofocus: true,
      },
    };
  },
  components: {
    codemirror,
  },
  props: {
    value: {
      type: String,
      default: 'C',
    },
    mode: {
      type: String,
      default: 'text/x-csrc',
    },
  },
  mounted() {
    this.currentValue = this.value;
    this.$refs.editor.editor.setOption('mode', this.mode);
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

<style>
.CodeMirror {
  height: auto !important;
}

.CodeMirror-scroll {
  min-height: 300px;
  max-height: 1000px;
}
</style>
