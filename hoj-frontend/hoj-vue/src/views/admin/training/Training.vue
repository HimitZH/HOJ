<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">
          {{ title }}
        </span>
      </div>
      <el-form label-position="top">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item :label="$t('m.Training_rank')" required>
              <el-input-number
                v-model="training.rank"
                @change="handleChange"
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
      <el-button type="primary" @click.native="saveTraining">{{
        $t('m.Save')
      }}</el-button>
    </el-card>
  </div>
</template>

<script>
import api from '@/common/api';
import { mapGetters } from 'vuex';
import myMessage from '@/common/message';
const Editor = () => import('@/components/admin/Editor.vue');
export default {
  name: 'CreateTraining',
  components: {
    Editor,
  },
  data() {
    return {
      title: 'Create Training',
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
      if (this.$route.name === 'admin-edit-training') {
        this.title = this.$i18n.t('m.Edit_Training');
        this.getTraining();
      } else {
        this.title = this.$i18n.t('m.Create_Training');
        this.training = {
          rank: 1000,
          title: '',
          description: '',
          privatePwd: '',
          auth: 'Public',
        };
      }
    },
  },
  computed: {
    ...mapGetters(['userInfo']),
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
          if (this.$route.name === 'admin-edit-training') {
            this.title = this.$i18n.t('m.Edit_Training');
            this.getTraining();
          } else {
            this.title = this.$i18n.t('m.Create_Training');
          }
        }
      });
    },

    getTraining() {
      api
        .admin_getTraining(this.$route.params.trainingId)
        .then((res) => {
          let data = res.data.data;
          this.training = data.training || {};
          this.trainingCategoryId = data.trainingCategory.id || null;
        })
        .catch(() => {});
    },

    saveTraining() {
      if (!this.training.rank && this.training.rank != 0) {
        myMessage.error(
          this.$i18n.t('m.Training_rank') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }

      if (!this.training.title) {
        myMessage.error(
          this.$i18n.t('m.Training_Title') + ' ' + this.$i18n.t('m.is_required')
        );
        return;
      }
      if (!this.training.description) {
        myMessage.error(
          this.$i18n.t('m.Training_Description') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }

      if (!this.trainingCategoryId) {
        myMessage.error(
          this.$i18n.t('m.Training_Category') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }

      if (this.training.auth != 'Public' && !this.training.privatePwd) {
        myMessage.error(
          this.$i18n.t('m.Training_Password') +
            ' ' +
            this.$i18n.t('m.is_required')
        );
        return;
      }

      let funcName =
        this.$route.name === 'admin-edit-training'
          ? 'admin_editTraining'
          : 'admin_createTraining';

      let data = Object.assign({}, this.training);
      if (funcName === 'admin_createTraining') {
        data['author'] = this.userInfo.username;
      }
      let trainingDto = {
        training: data,
        trainingCategory: {
          id: this.trainingCategoryId,
        },
      };

      api[funcName](trainingDto)
        .then((res) => {
          myMessage.success('success');
          this.$router.push({
            name: 'admin-training-list',
            query: { refresh: 'true' },
          });
        })
        .catch(() => {});
    },
  },
};
</script>
<style scoped>
.userPreview {
  padding-left: 10px;
  padding-top: 20px;
  padding-bottom: 20px;
  color: red;
  font-size: 16px;
  margin-bottom: 10px;
}
</style>
