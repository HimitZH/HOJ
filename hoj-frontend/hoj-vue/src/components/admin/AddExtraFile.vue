<template>
  <div class="accordion">
    <el-tag
      :key="index"
      v-for="(value, key, index) in files"
      closable
      class="file"
      :disable-transitions="false"
      @close="deleteFile(key)"
      @click="openFileDialog(key, value)"
    >
      <i class="fa fa-file-code-o"> {{ key }}</i>
    </el-tag>

    <el-button
      class="button-new-file"
      size="small"
      @click="openFileDialog('', '')"
      >+ New File</el-button
    >

    <el-dialog
      :width="dialogWith"
      :visible.sync="upsertFileDialogVisible"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.File_Name')" required>
          <el-input
            v-model="fileName"
            size="small"
            placeholder="******.h"
          ></el-input>
        </el-form-item>

        <el-form-item :label="$t('m.File_Content')" required>
          <code-mirror v-model="fileContent"></code-mirror>
        </el-form-item>

        <el-form-item style="text-align:center;margin-top:10px">
          <el-button type="primary" @click="upsertFile"
            >{{ $t('m.Save') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>
import CodeMirror from '@/components/admin/CodeMirror.vue';
import myMessage from '@/common/message';
export default {
  name: 'Accordion',
  components: {
    CodeMirror,
  },
  props: {
    files: {
      type: Array,
      required: true,
    },
    type: {
      type: String,
      required: true,
    },
  },
  mounted() {
    let screenWidth = window.screen.width;
    if (screenWidth < 768) {
      this.dialogWith = '100%';
    } else {
      this.dialogWith = '70%';
    }
  },
  data() {
    return {
      upsertFileDialogVisible: false,
      upsertTagLoading: false,
      fileName: '',
      fileOldName: '',
      fileContent: '',
      dialogWith: '70%',
    };
  },
  methods: {
    deleteFile(fileName) {
      this.$confirm(this.$i18n.t('m.Delete_Extra_File_Tips'), 'Tips', {
        type: 'warning',
      }).then(
        () => {
          this.$emit('deleteFile', this.type, fileName);
        },
        () => {}
      );
    },
    openFileDialog(name, content) {
      this.fileName = name;
      this.fileContent = content;
      this.fileOldName = name;
      this.upsertFileDialogVisible = true;
    },
    upsertFile() {
      if (!this.fileName) {
        myMessage.error(
          this.$i18n.t('m.File_Name') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.fileContent) {
        myMessage.error(
          this.$i18n.t('m.File_Content') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }
      this.$emit(
        'upsertFile',
        this.type,
        this.fileName,
        this.fileOldName,
        this.fileContent
      );
      this.upsertFileDialogVisible = false;
    },
  },
};
</script>

<style scoped>
.accordion {
  border: 1px solid #eaeefb;
}
.file {
  margin: 10px;
  cursor: pointer;
}
.button-new-file {
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
  margin: 10px;
}
/deep/.CodeMirror-scroll {
  max-height: 300px;
}
</style>
