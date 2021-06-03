<template>
  <div class="mavonEditor">
    <mavon-editor
      ref="md"
      @imgAdd="$imgAdd"
      @imgDel="$imgDel"
      :ishljs="true"
      :autofocus="false"
      v-model="currentValue"
      codeStyle="arduino-light"
    >
      <template v-slot:left-toolbar-after v-if="isAdminRole">
        <button
          type="button"
          title="文件上传"
          class="op-icon fa markdown-upload"
          aria-hidden="true"
          @click="uploadFile"
        >
          <!-- 这里用的是element-ui给出的图标 -->
          <i class="el-icon-upload" />
        </button>
      </template>
    </mavon-editor>
    <!-- 在这里放一个隐藏的input，用来选择文件 -->
    <input
      ref="uploadInput"
      style="display: none"
      type="file"
      @change="uploadFileChange"
    />
  </div>
</template>
<script>
import { mapGetters } from 'vuex';
import { addCodeBtn } from '@/common/codeblock';
export default {
  name: 'Editor',
  props: {
    value: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      currentValue: this.value,
      img_file: {},
    };
  },
  methods: {
    // 将图片上传到服务器，返回地址替换到md中
    $imgAdd(pos, $file) {
      var formdata = new FormData();
      formdata.append('image', $file);
      //将下面上传接口替换为你自己的服务器接口
      this.$http({
        url: '/api/file/upload-md-img',
        method: 'post',
        data: formdata,
        headers: { 'Content-Type': 'multipart/form-data' },
      }).then((res) => {
        this.$refs.md.$img2Url(pos, res.data.data.link);
        this.img_file[res.data.data.link] = res.data.data.filePath;
      });
    },
    $imgDel(pos) {
      // 删除文件
      this.$http({
        url: '/api/file/delete-md-img',
        method: 'get',
        params: {
          filePath: this.img_file[pos[0]],
        },
      });
    },
    uploadFile() {
      // 通过ref找到隐藏的input标签，触发它的点击方法
      this.$refs.uploadInput.click();
    },
    // 监听input获取文件的状态
    uploadFileChange(e) {
      // 获取到input选取的文件
      const file = e.target.files[0];
      // 创建form格式的数据，将文件放入form中
      const formdata = new FormData();
      formdata.append('file', file);

      this.$http({
        url: '/api/file/upload-md-file',
        method: 'post',
        data: formdata,
        headers: { 'Content-Type': 'multipart/form-data' },
      }).then((res) => {
        // 这里获取到的是mavon编辑器实例，上面挂载着很多方法
        const $vm = this.$refs.md;
        // 将文件名与文件路径插入当前光标位置，这是mavon-editor 内置的方法
        $vm.insertText($vm.getTextareaDom(), {
          prefix: `[${file.name}](${res.data.data.link})`,
          subfix: '',
          str: '',
        });
      });
    },
  },
  watch: {
    value(val) {
      if (this.currentValue !== val) {
        this.currentValue = val;
      }
    },
    currentValue(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.$emit('update:value', newVal);
        this.$nextTick((_) => {
          addCodeBtn();
        });
      }
    },
  },
  computed: {
    ...mapGetters(['isAdminRole']),
  },
};
</script>
<style>
.markdown-body pre {
  display: block;
  border-radius: 3px !important;
  border: 1px solid #c3ccd0;
  padding: 0 16px 0 55px !important;
  position: relative !important;
  overflow-y: hidden !important;
  font-size: 1rem !important;
  background: #f8f8f9 !important;
  white-space: pre !important;
}
.auto-textarea-wrapper .auto-textarea-block {
  white-space: pre-wrap !important;
}
.markdown-body pre code {
  line-height: 26px !important;
}
.markdown-body pre ol.pre-numbering {
  position: absolute;
  top: 0;
  left: 0;
  line-height: 26px;
  margin: 0;
  padding: 0;
  list-style-type: none;
  counter-reset: sectioncounter;
  background: #f1f1f1;
  color: #777;
  font-size: 12px;
}
.markdown-body pre ol.pre-numbering li {
  margin-top: 0 !important;
}
.markdown-body pre ol.pre-numbering li:before {
  content: counter(sectioncounter) '';
  counter-increment: sectioncounter;
  display: inline-block;
  width: 40px;
  text-align: center;
}
.markdown-body pre i.code-copy {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #2196f3;
  display: none;
  padding: 5px;
  margin: 5px 5px 0 0;
  font-size: 11px;
  border-radius: inherit;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease-in-out;
}

.markdown-body pre:hover i.code-copy {
  display: block;
}
.markdown-body pre i.code-copy:hover i.code-copy {
  display: block;
}

.markdown-body blockquote {
  color: #666;
  border-left: 4px solid #8bc34a;
  padding: 10px;
  margin-left: 0;
  font-size: 14px;
  background: #f8f8f8;
}
.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  position: relative;
  margin-top: 1em;
  margin-bottom: 16px;
  font-weight: bold;
  line-height: 1.4;
}
.markdown-body h1 {
  padding-bottom: 0.3em;
  font-size: 2.25em;
  line-height: 1.2;
  border-bottom: 1px solid #eee;
}
.markdown-body h2 {
  font-size: 1.45em;
  line-height: 1.425;
  border-bottom: 1px solid #eee;
  background: #cce5ff;
  padding: 8px 10px;
  color: #545857;
  border-radius: 3px;
}
.markdown-body h3 {
  font-size: 1.3em;
  line-height: 1.43;
}
.markdown-body h3:before {
  content: '';
  border-left: 4px solid #03a9f4;
  padding-left: 6px;
}
.markdown-body h4 {
  font-size: 1.12em;
}
.markdown-body h4:before {
  content: '';
  border-left: 4px solid #bbb;
  padding-left: 6px;
}
.markdown-body img {
  border: 0;
  background: #ffffff;
  padding: 15px;
  margin: 5px 0;
  box-shadow: inset 0 0 12px rgb(219 219 219);
}
</style>
