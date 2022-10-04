<template>
  <el-row>
    <el-col :span="24">
      <el-card shadow="never">
        <div slot="header">
          <span class="panel-title home-title">{{ title }}</span>
        </div>
        <el-form label-position="top" :model="training">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item :label="$t('m.Training_rank')" required>
                <el-input-number
                  v-model="training.rank"
                  :min="0"
                  :max="2147483647"
                  :label="$t('m.Training_rank')"
                ></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="$t('m.Training_Title')" required>
                <el-input
                  v-model="training.title"
                  :placeholder="$t('m.Training_Title')"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="$t('m.Training_Description')" required>
                <Editor :value.sync="training.description"></Editor>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="$t('m.Category')" required>
                <el-select v-model="trainingCategoryId">
                  <el-option
                    :label="category.name"
                    :value="category.id"
                    v-for="(category, index) in trainingCategoryList"
                    :key="index"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :md="8" :xs="24">
              <el-form-item :label="$t('m.Training_Auth')" required>
                <el-select v-model="training.auth">
                  <el-option
                    :label="$t('m.Public_Training')"
                    value="Public"
                  ></el-option>
                  <el-option
                    :label="$t('m.Private_Training')"
                    value="Private"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :md="8" :xs="24">
              <el-form-item
                :label="$t('m.Training_Password')"
                v-show="training.auth != 'Public'"
                :required="training.auth != 'Public'"
              >
                <el-input
                  v-model="training.privatePwd"
                  :placeholder="$t('m.Training_Password')"
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-button type="primary" @click.native="submit">{{
          $t('m.Save')
        }}</el-button>
      </el-card>
    </el-col>
  </el-row>
</template>

<script>
import { mapGetters } from 'vuex';
import api from '@/common/api';
import mMessage from '@/common/message';
import Editor from '@/components/admin/Editor.vue';
import Accordion from '@/components/admin/Accordion.vue';
import AddExtraFile from '@/components/admin/AddExtraFile.vue';
import CodeMirror from '@/components/admin/CodeMirror.vue';
export default {
  name: 'GroupTraining',
  components: {
    Accordion,
    AddExtraFile,
    CodeMirror,
    Editor,
  },
  props: {
    mode: {
      type: String,
      default: 'edit'
    },
    title: {
      type: String,
      default: 'Edit Training'
    },
    apiMethod: {
      type: String,
      default: 'addGroupTraining'
    },
    tid: {
      type: Number,
      default: null
    },
  },
  data() {
    return {
      training: {
        rank: 1000,
        title: '',
        description: '',
        privatePwd: '',
        auth: 'Public',
      },
      trainingCategoryId: null,
      trainingCategoryList: [],
    };
  },
  mounted() {
    this.init();
  },
  watch: {
    $route() {
      this.training = {
        rank: 1000,
        title: '',
        description: '',
        privatePwd: '',
        auth: 'Public',
      };
      this.init();
    },
  },
  methods: {
    init() {
      api.getTrainingCategoryList().then((res) => {
        let data = res.data.data;
        if (!data || !data.length) {
          this.$alert(
            this.$i18n.t('m.Redirect_To_Category'),
            this.$i18n.t('m.Redirect'),
            {
              confirmButtonText: this.$i18n.t('m.OK'),
              showClose: false,
              callback: (action) => {
                this.$router.push({
                  path: '/admin/training/category',
                });
              },
            }
          );
        } else {
          this.trainingCategoryList = data;
          if (this.mode === 'edit') {
            this.getTraining();
          }
        }
      });
    },
    getTraining() {
      api.getGroupTraining(this.tid).then((res) => {
          let data = res.data.data;
          this.training = data.training || {};
          this.trainingCategoryId = data.trainingCategory.id || null;
        }).catch(() => {});
    },
    submit() {
      if (!this.training.rank && this.training.rank != 0) {
        mMessage.error(
          this.$i18n.t('m.Training_rank') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }

      if (!this.training.title) {
        mMessage.error(
          this.$i18n.t('m.Training_Title') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.training.description) {
        mMessage.error(
          this.$i18n.t('m.Training_Description') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }

      if (!this.trainingCategoryId) {
        mMessage.error(
          this.$i18n.t('m.Training_Category') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }

      if (this.training.auth != 'Public' && !this.training.privatePwd) {
        mMessage.error(
          this.$i18n.t('m.Training_Password') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }
      this.training.gid = this.$route.params.groupID;
      let data = Object.assign({}, this.training);
      if (this.mode === 'add') {
        data['author'] = this.userInfo.username;
      }
      let trainingDto = {
        training: data,
        trainingCategory: {
          id: this.trainingCategoryId,
        },
      };

      api[this.apiMethod](trainingDto)
        .then((res) => {
          mMessage.success(this.$t('m.Update_Successfully'));
          if (this.mode === 'edit') {
            this.$emit("handleEditPage");
          } else {
            this.$emit("handleCreatePage");
          }
          this.$emit("currentChange", 1);
        })
        .catch(() => {});
    },
  },
  computed: {
    ...mapGetters(['userInfo', 'group']),
  },
};
</script>

<style scoped>
/deep/.el-form-item__label {
  padding: 0 !important;
}
.el-form-item {
  margin-bottom: 10px !important;
}
.difficulty-select {
  width: 120px;
}
.input-new-tag {
  width: 120px;
}
.button-new-tag {
  height: 24px;
  line-height: 22px;
  padding-top: 0;
  padding-bottom: 0;
}

.accordion {
  margin-bottom: 10px;
}

.add-examples {
  width: 100%;
  background-color: #fff;
  border: 1px dashed #2d8cf0;
  outline: none;
  cursor: pointer;
  color: #2d8cf0;
  height: 35px;
  font-size: 14px;
}
.add-examples i {
  margin-right: 10px;
}
.add-examples:hover {
  border: 0px;
  background-color: #2d8cf0 !important;
  color: #fff;
}
.add-example-btn {
  margin-bottom: 10px;
}

.add-samples {
  width: 100%;
  background-color: #fff;
  border: 1px dashed #19be6b;
  outline: none;
  cursor: pointer;
  color: #19be6b;
  height: 35px;
  font-size: 14px;
}
.add-samples i {
  margin-right: 10px;
}
.add-samples:hover {
  border: 0px;
  background-color: #19be6b !important;
  color: #fff;
}
.add-sample-btn {
  margin-bottom: 10px;
}

.dialog-compile-error {
  width: auto;
  max-width: 80%;
  overflow-x: scroll;
}
</style>
