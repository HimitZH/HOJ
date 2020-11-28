<template>
  <el-form>
    <el-form-item label="Input">
      <el-input type="textarea" v-model="input" @change="changeInput" @keyup.enter.native="changeInput"></el-input>
    </el-form-item>

    <el-form-item label="Output">
    </el-form-item>
    <div v-html="text"></div>
  </el-form>
</template>

<script>
  import katex from 'katex'
  export default {
    name: '',
    data () {
      return {
        input: 'c = \\pm\\sqrt{a^2 + b^2}',
        text: ''
      }
    },
    mounted () {
      this.text = this.renderTex(this.input)
    },
    methods: {
      renderTex (data) {
        return katex.renderToString(data, {
          displayMode: true,
          throwOnError: false
        })
      },
      changeInput () {
        try {
          this.text = this.renderTex(this.input)
        } catch (e) {
          this.text = '<p style="text-align: center"><span style="color:red">Error Input</span></p>'
        }
      }
    }
  }
</script>

<style scoped>
</style>
