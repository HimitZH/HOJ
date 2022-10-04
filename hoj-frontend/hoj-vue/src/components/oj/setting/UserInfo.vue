<template>
  <div>
    <div class="section-title">{{ $t('m.Avatar_Setting') }}</div>
    <div class="section-main">
      <avatar
        :username="formProfile.username"
        :inline="true"
        :size="130"
        color="#FFF"
        style="margin-bottom:15px"
        :src="avatar"
      ></avatar>
      <template v-if="!avatarOption.imgSrc">
        <el-upload
          class="upload-container"
          action=""
          drag
          :before-upload="handleSelectFile"
        >
          <div style="padding: 20px 0">
            <i class="el-icon-upload" style="color: #3399ff;font-size:52px"></i>
            <p>{{ $t('m.Upload_avatar_hint') }}</p>
          </div>
        </el-upload>
      </template>

      <template v-else>
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <div class="cropper-main inline">
              <vueCropper
                ref="cropper"
                autoCrop
                fixed
                :autoCropWidth="200"
                :autoCropHeight="200"
                :img="avatarOption.imgSrc"
                :outputSize="avatarOption.size"
                :outputType="avatarOption.outputType"
                :info="true"
                @realTime="realTime"
              >
              </vueCropper>
            </div>
            <div class="cropper-btn">
              <el-tooltip
                class="item"
                effect="dark"
                content="向左旋转90°"
                trigger="hover"
                placement="bottom"
              >
                <el-button
                  @click="rotate('left')"
                  icon="el-icon-refresh-left"
                  size="mini"
                ></el-button>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                content="向右旋转90°"
                trigger="hover"
                placement="bottom"
              >
                <el-button
                  @click="rotate('right')"
                  icon="el-icon-refresh-right"
                  size="mini"
                ></el-button>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                content="关闭图像截取"
                trigger="hover"
                placement="bottom"
              >
                <el-button
                  @click="reselect"
                  icon="el-icon-refresh"
                  size="mini"
                ></el-button>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                trigger="hover"
                content="确定图像截取"
                placement="bottom"
              >
                <el-button
                  @click="finishCrop"
                  icon="el-icon-check"
                  size="mini"
                ></el-button>
              </el-tooltip>
            </div>
          </el-col>
          <el-col :xs="24" :md="12">
            <div class="cropper-preview" :style="previewStyle">
              <div :style="preview.div">
                <img :src="avatarOption.imgSrc" :style="preview.img" />
              </div>
            </div>
          </el-col>
        </el-row>
      </template>
      <el-dialog
        :visible.sync="uploadModalVisible"
        :title="$t('m.Upload')"
        width="350px"
      >
        <div class="upload-modal">
          <p class="notice">{{ $t('m.Your_new_avatar') + ':' }}</p>
          <img :src="uploadImgSrc" />
        </div>
        <div slot="footer">
          <el-button
            @click="uploadAvatar"
            :loading="loadingUploadBtn"
            type="primary"
            >{{ $t('m.Upload') }}</el-button
          >
        </div>
      </el-dialog>
    </div>

    <div class="section-title">{{ $t('m.UserInfo_Setting') }}</div>
    <el-form ref="formProfile" :model="formProfile">
      <el-row :gutter="30" justify="space-around">
        <el-col :md="10" :xs="24">
          <el-form-item :label="$t('m.RealName')">
            <el-input v-model="formProfile.realname" :maxlength="50" />
          </el-form-item>
          <el-form-item :label="$t('m.Nickname')">
            <el-input v-model="formProfile.nickname" :maxlength="20" />
          </el-form-item>
          <el-form-item :label="$t('m.School')">
            <el-input v-model="formProfile.school" :maxlength="50" />
          </el-form-item>
          <el-form-item :label="$t('m.Student_Number')">
            <el-input v-model="formProfile.number" :maxlength="20" />
          </el-form-item>
        </el-col>
        <el-col :md="4" :lg="4">
          <div class="separator hidden-md-and-down"></div>
          <p></p>
        </el-col>
        <el-col :md="10" :xs="24">
          <el-form-item :label="$t('m.CF_Username')">
            <el-input v-model="formProfile.cfUsername" :maxlength="50" />
          </el-form-item>
          <el-form-item :label="$t('m.Blog')">
            <el-input v-model="formProfile.blog" :maxlength="255" />
          </el-form-item>
          <el-form-item :label="$t('m.Github')">
            <el-input v-model="formProfile.github" :maxlength="255" />
          </el-form-item>
          <el-form-item :label="$t('m.Gender')">
            <el-radio-group v-model="formProfile.gender">
              <el-radio label="male" border size="small">{{
                $t('m.Male')
              }}</el-radio>
              <el-radio label="female" border size="small">{{
                $t('m.Female')
              }}</el-radio>
              <el-radio label="secrecy" border size="small">{{
                $t('m.Secrecy')
              }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <label class="el-form-item__label" style="float: none;">{{
            $t('m.Signature')
          }}</label>
          <Editor
            :value.sync="formProfile.signature"
            style="padding: 5px;"
          ></Editor>
        </el-col>
      </el-row>
    </el-form>
    <div style="text-align:center;margin-top:10px">
      <el-button
        type="primary"
        @click="updateUserInfo"
        :loading="loadingSaveBtn"
        >{{ $t('m.Save') }}</el-button
      >
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import myMessage from '@/common/message';
import { VueCropper } from 'vue-cropper';
import Avatar from 'vue-avatar';
import 'element-ui/lib/theme-chalk/display.css';
const Editor = () => import('@/components/admin/Editor.vue');
export default {
  components: {
    Avatar,
    VueCropper,
    Editor,
  },
  data() {
    return {
      loadingSaveBtn: false,
      loadingUploadBtn: false,
      uploadModalVisible: false,
      preview: {},
      uploadImgSrc: '',
      avatarOption: {
        imgSrc: '',
        size: 0.8,
        outputType: 'png',
      },

      formProfile: {
        realname: '',
        username: '',
        cfUsername: '',
        gender: '',
        nickname: '',
        signature: '',
        number: '',
        blog: '',
        school: '',
        github: '',
      },
    };
  },
  mounted() {
    let profile = this.$store.getters.userInfo;
    Object.keys(this.formProfile).forEach((element) => {
      if (profile[element] !== undefined) {
        this.formProfile[element] = profile[element];
      }
    });
  },
  methods: {
    checkFileType(file) {
      if (!/\.(gif|jpg|jpeg|png|bmp|webp|GIF|JPG|PNG|WEBP)$/.test(file.name)) {
        this.$notify.warning({
          title: this.$i18n.t('m.File_type_not_support'),
          message: file.name + this.$i18n.t('m.is_incorrect_format_file'),
        });
        return false;
      }
      return true;
    },
    checkFileSize(file) {
      // max size is 2MB
      if (file.size > 2 * 1024 * 1024) {
        this.$notify.warning({
          title: this.$i18n.t('m.Exceed_max_size_limit'),
          message: file.name + this.$i18n.t('m.File_Exceed_Tips'),
        });
        return false;
      }
      return true;
    },
    handleSelectFile(file) {
      let isOk = this.checkFileType(file) && this.checkFileSize(file);
      if (!isOk) {
        return false;
      }
      let reader = new window.FileReader();
      reader.onload = (e) => {
        this.avatarOption.imgSrc = e.target.result;
      };
      reader.readAsDataURL(file);
      return false;
    },
    realTime(data) {
      this.preview = data;
    },
    rotate(direction) {
      if (direction === 'left') {
        this.$refs.cropper.rotateLeft();
      } else {
        this.$refs.cropper.rotateRight();
      }
    },
    reselect() {
      this.$confirm(this.$i18n.t('m.Cancel_Avater_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      }).then(() => {
        this.avatarOption.imgSrc = '';
      });
    },
    finishCrop() {
      this.$refs.cropper.getCropData((data) => {
        this.uploadImgSrc = data;
        this.uploadModalVisible = true;
      });
    },
    uploadAvatar() {
      this.$refs.cropper.getCropBlob((blob) => {
        let form = new window.FormData();
        let file = new window.File(
          [blob],
          'avatar.' + this.avatarOption.outputType
        );
        form.append('image', file);
        this.loadingUploadBtn = true;
        this.$http({
          method: 'post',
          url: '/api/file/upload-avatar',
          data: form,
          headers: { 'content-type': 'multipart/form-data' },
        }).then(
          (res) => {
            this.loadingUploadBtn = false;
            myMessage.success(this.$i18n.t('m.Upload_Avatar_Successfully'));
            this.uploadModalVisible = false;
            this.avatarOption.imgSrc = '';
            this.$store.dispatch('setUserInfo', res.data.data);
          },
          () => {
            this.loadingUploadBtn = false;
          }
        );
      });
    },
    updateUserInfo() {
      this.loadingSaveBtn = true;
      let updateData = utils.filterEmptyValue(
        Object.assign({}, this.formProfile)
      );
      if(!updateData.gender){
        updateData.gender = 'secrecy';
      }
      api.changeUserInfo(updateData).then(
        (res) => {
          myMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.$store.dispatch('setUserInfo', res.data.data);
          this.loadingSaveBtn = false;
        },
        (_) => {
          this.loadingSaveBtn = false;
        }
      );
    },
  },
  computed: {
    avatar() {
      return this.$store.getters.userInfo.avatar;
    },
    previewStyle() {
      return {
        width: this.preview.w + 'px',
        height: this.preview.h + 'px',
        overflow: 'hidden',
      };
    },
  },
};
</script>

<style scoped>
/deep/ .el-input__inner {
  height: 32px;
}
/deep/ .el-form-item__label {
  font-size: 12px;
  line-height: 20px;
}
.section-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
  text-align: center;
}
.section-main {
  text-align: center;
  margin-bottom: 20px;
}

/deep/.upload-container .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 320px;
}
/deep/.upload-container .el-upload:hover {
  border-color: #409eff;
}
.inline {
  display: inline-block;
}
.cropper-btn {
  margin: 10px 0;
}
.copper-img {
  width: 400px;
  height: 300px;
}

.cropper-main {
  flex: none;
  width: 400px;
  height: 300px;
}
.section-main .cropper-preview {
  flex: none;
  text-align: center;
  box-shadow: 0 0 1px 0;
}
@media screen and (max-width: 1080px) {
  .section-main .cropper-preview {
    margin: 0 auto;
  }
}
.upload-modal .notice {
  font-size: 16px;
  display: inline-block;
  vertical-align: top;
  padding: 10px;
}
/deep/ .el-dialog__body {
  padding: 0;
}
/deep/ .el-upload-dragger {
  width: 100%;
  height: 100%;
}
.upload-modal img {
  box-shadow: 0 0 1px 0;
  border-radius: 50%;
  width: 250px;
  height: 250px;
}
.separator {
  display: block;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  border: 1px dashed #eee;
}
</style>
