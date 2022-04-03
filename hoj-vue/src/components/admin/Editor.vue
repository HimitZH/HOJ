<template>
  <div class="mavonEditor">
    <mavon-editor
      ref="md"
      @imgAdd="$imgAdd"
      @imgDel="$imgDel"
      :ishljs="true"
      :html="openHtml"
      :autofocus="false"
      :toolbars="toolbars"
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
    openHtml: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      currentValue: this.value,
      img_file: {},
      toolbars: {
        bold: true, // 粗体
        italic: true, // 斜体
        header: true, // 标题
        underline: true, // 下划线
        strikethrough: true, // 中划线
        mark: true, // 标记
        superscript: true, // 上角标
        subscript: true, // 下角标
        quote: true, // 引用
        ol: true, // 有序列表
        ul: true, // 无序列表
        link: true, // 链接
        imagelink: false, // 图片链接
        code: true, // code
        table: true, // 表格
        fullscreen: true, // 全屏编辑
        readmodel: true, // 沉浸式阅读
        htmlcode: true, // 展示html源码
        help: true, // 帮助
        /* 1.3.5 */
        undo: true, // 上一步
        redo: true, // 下一步
        trash: true, // 清空
        save: true, // 保存（触发events中的save事件）
        /* 1.4.2 */
        navigation: true, // 导航目录
        /* 2.1.8 */
        alignleft: true, // 左对齐
        aligncenter: true, // 居中
        alignright: true, // 右对齐
        /* 2.2.1 */
        subfield: true, // 单双栏模式
        preview: true, // 预览
      },
    };
  },
  created() {
    if (this.isAdminRole || this.isGroupAdmin) {
      this.toolbars.imagelink = true;
    }
  },
  methods: {
    // 将图片上传到服务器，返回地址替换到md中
    $imgAdd(pos, $file) {
      if (!this.isAdminRole && !this.isGroupAdmin) {
        return;
      }
      var formdata = new FormData();
      formdata.append('image', $file);
      let gid = this.$route.params.groupID;
      if (gid != null && gid != undefined) {
        formdata.append('gid', gid);
      }
      //将下面上传接口替换为你自己的服务器接口
      this.$http({
        url: '/api/file/upload-md-img',
        method: 'post',
        data: formdata,
        headers: { 'Content-Type': 'multipart/form-data' },
      }).then((res) => {
        this.$refs.md.$img2Url(pos, res.data.data.link);
        this.img_file[res.data.data.link] = res.data.data.fileId;
      });
    },
    $imgDel(pos) {
      // 删除文件
      this.$http({
        url: '/api/file/delete-md-img',
        method: 'get',
        params: {
          fileId: this.img_file[pos[0]],
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
      let gid = this.$route.params.groupID;
      if (gid != null && gid != undefined) {
        formdata.append('gid', gid);
      }
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
  computed: {
    ...mapGetters(['isAdminRole', 'isGroupAdmin']),
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
    isAdminRole(val) {
      if (!val) {
        this.toolbars.imagelink = false;
      } else {
        this.toolbars.imagelink = true;
      }
    },
  },
};
</script>
<style>
.auto-textarea-wrapper .auto-textarea-block {
  white-space: pre-wrap !important;
}
</style>
