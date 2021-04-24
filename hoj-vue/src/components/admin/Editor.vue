<template>
  <div class="mavonEditor">
    <mavon-editor
      ref="md"
      @imgAdd="$imgAdd"
      @imgDel="$imgDel"
      v-model="currentValue"
      codeStyle="arduino-light"
    ></mavon-editor>
  </div>
</template>
<script>
import myMessage from '@/common/message';
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
        url: '/file/upload-md-img',
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
        url: '/file/delete-md-img',
        method: 'get',
        params: {
          filePath: this.img_file[pos[0]],
        },
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
      }
    },
  },
};
</script>
