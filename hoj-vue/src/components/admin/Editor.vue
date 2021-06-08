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
          :title="$t('m.Upload_file')"
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
.auto-textarea-wrapper .auto-textarea-block {
  white-space: pre-wrap !important;
}
</style>
