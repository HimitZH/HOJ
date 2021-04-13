<template>
  <pre
    v-highlight="code"
    :style="styleObject"
  ><code :class="language"></code></pre>
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
};
</script>

<style scoped>
pre {
  padding: 0;
  display: block;
}
code {
  padding: 20px;
  font-size: 1.1em;
}
</style>
