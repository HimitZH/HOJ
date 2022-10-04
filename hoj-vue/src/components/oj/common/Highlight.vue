<template>
  <div class="markdown-body submission-detail">
    <pre
      v-highlight="code"
      :style="styleObject"
    ><code :class="language"></code></pre>
  </div>
</template>

<script>
export default {
  name: 'highlight',
  data() {
    return {
      styleObject: {
        'border-left': '3px solid ' + this.borderColor,
      },
    };
  },
  props: {
    language: {
      type: String,
    },
    code: {
      required: true,
      type: String,
    },
    borderColor: {
      type: String,
      default: '#19be6b',
    },
  },
  watch: {
    borderColor(newval, oldval) {
      if (newval != oldval) {
        this.styleObject['border-left'] = '3px solid ' + newval;
      }
    },
  },
};
</script>

<style scoped>
.hljs {
  padding: 0 !important;
}
.submission-detail pre {
  padding-left: 50px !important;
}
</style>
