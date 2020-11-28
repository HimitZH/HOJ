<template>
  <textarea ref="editor"></textarea>
</template>

<script>
  import Simditor from 'tar-simditor' // 富文本编辑器
  import 'tar-simditor/styles/simditor.css'
  import 'tar-simditor-markdown'
  import 'tar-simditor-markdown/styles/simditor-markdown.css'
  import './simditor-file-upload'

  export default {
    name: 'Simditor',
    props: {
      toolbar: {
        type: Array,
        default: () => ['title', 'bold', 'italic', 'underline', 'fontScale', 'color', 'ol', 'ul', '|', 'link', 'image', 'uploadfile', 'hr', '|', 'indent', 'outdent', 'alignment', '|', 'markdown']
      },
      value: {
        type: String,
        default: ''
      }
    },
    data () {
      return {
        editor: null,
        currentValue: this.value
      }
    },
    mounted () {
      this.editor = new Simditor({
        textarea: this.$refs.editor,
        toolbar: this.toolbar,
        pasteImage: true,
        markdown: false,
        upload: {
          url: '/api/admin/upload_image/',
          params: null,
          fileKey: 'image',//服务器端获取文件数据的参数名
          connectionCount: 3,
          leaveConfirm: '正在上传文件'
        },
        allowedStyles: {
          span: ['color']
        }
      })
      this.editor.on('valuechanged', (e, src) => {
        this.currentValue = this.editor.getValue()
      })
      this.editor.on('decorate', (e, src) => {
        this.currentValue = this.editor.getValue()
      })

      this.editor.setValue(this.value)
    },
    watch: {
      'value' (val) {
        if (this.currentValue !== val) {
          this.currentValue = val
          this.editor.setValue(val)
        }
      },
      'currentValue' (newVal, oldVal) {
        if (newVal !== oldVal) {
          this.$emit('change', newVal)
          this.$emit('input', newVal)
        }
      }
    }
  }
</script>

<style scoped>
</style>
