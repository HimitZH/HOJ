<template>
  <div class="setting-main">
    <div class="section-title">Avatar Setting</div>
    <template v-if="!avatarOption.imgSrc">
      <el-upload type="drag"
              class="mini-container"
              accept=".jpg,.jpeg,.png,.bmp,.gif"
              action=""
              :before-upload="handleSelectFile">
        <div style="padding: 30px 0">
          <el-icon type="ios-cloud-upload" size="52" style="color: #3399ff"></el-icon>
          <p>Drop here, or click to select manually</p>
        </div>
      </el-upload>
    </template>

    <template v-else>
      <div class="flex-container">
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
            @realTime="realTime">
          </vueCropper>
        </div>
        <ButtonGroup vertical class="cropper-btn">
          <Button @click="rotate('left')">
            <Icon type="arrow-return-left" size="20"></Icon>
          </Button>
          <Button @click="rotate('right')">
            <Icon type="arrow-return-right" size="20"></Icon>
          </Button>
          <Button @click="reselect">
            <Icon type="refresh" size="20"></Icon>
          </Button>
          <Button @click="finishCrop">
            <Icon type="checkmark-round" size="20"></Icon>
          </Button>
        </ButtonGroup>
        <div class="cropper-preview" :style="previewStyle">
          <div :style=" preview.div">
            <img :src="avatarOption.imgSrc" :style="preview.img">
          </div>
        </div>
      </div>
    </template>
    <el-alert v-model="uploadModalVisible"
           title="Upload the avatar">
      <div class="upload-modal">
        <p class="notice">Your avatar will be set to:</p>
        <img :src="uploadImgSrc"/>
      </div>
      <div slot="footer">
        <el-button @click="uploadAvatar" :loading="loadingUploadBtn">upload</el-button>
      </div>
    </el-alert>

    <div class="section-title">UserInfo Setting</div>
    <el-form ref="formProfile" :model="formProfile">
      <el-row type="flex" :gutter="30" justify="space-around">
        <el-col :span="11">
          <el-form-item label="Real Name">
            <el-input v-model="formProfile.real_name"/>
          </el-form-item>
          <el-form-item label="School">
            <el-input v-model="formProfile.school"/>
          </el-form-item>
          <el-form-item label="Major">
            <el-input v-model="formProfile.major"/>
          </el-form-item>
          <el-form-item label="Language">
            <el-select v-model="formProfile.language">
              <el-option v-for="lang in languages" :key="lang.value" :value="lang.value">{{lang.label}}</el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateProfile" :loading="loadingSaveBtn">Save All</el-button>
          </el-form-item>
        </el-col>

        <el-col :span="11">
          <el-form-item label="Mood">
            <el-input v-model="formProfile.mood"/>
          </el-form-item>
          <el-form-item label="Blog">
            <el-input v-model="formProfile.blog"/>
          </el-form-item>
          <el-form-item label="Github">
            <el-input v-model="formProfile.github"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
  import api from '@/common/api'
  import utils from '@/common/utils'
  import {VueCropper} from 'vue-cropper'

  export default {
    components: {
      VueCropper
    },
    data () {
      return {
        loadingSaveBtn: false,
        loadingUploadBtn: false,
        uploadModalVisible: false,
        preview: {},
        uploadImgSrc: '',
        avatarOption: {
          imgSrc: '',
          size: 0.8,
          outputType: 'png'
        },
        formProfile: {
          real_name: '',
          mood: '',
          major: '',
          blog: '',
          school: '',
          github: '',
          language: ''
        }
      }
    },
    mounted () {
      let profile = this.$store.state.userInfo
      Object.keys(this.formProfile).forEach(element => {
        if (profile[element] !== undefined) {
          this.formProfile[element] = profile[element]
        }
      })
    },
    methods: {
      checkFileType (file) {
        if (!/\.(gif|jpg|jpeg|png|bmp|GIF|JPG|PNG)$/.test(file.name)) {
          this.$Notice.warning({
            title: 'File type not support',
            desc: 'The format of ' + file.name + ' is incorrect ，please choose image only.'
          })
          return false
        }
        return true
      },
      checkFileSize (file) {
        // max size is 2MB
        if (file.size > 2 * 1024 * 1024) {
          this.$Notice.warning({
            title: 'Exceed max size limit',
            desc: 'File ' + file.name + ' is too big, you can upload a image up to 2MB in size'
          })
          return false
        }
        return true
      },
      handleSelectFile (file) {
        let isOk = this.checkFileType(file) && this.checkFileSize(file)
        if (!isOk) {
          return false
        }
        let reader = new window.FileReader()
        reader.onload = (e) => {
          this.avatarOption.imgSrc = e.target.result
        }
        reader.readAsDataURL(file)
        return false
      },
      realTime (data) {
        this.preview = data
      },
      rotate (direction) {
        if (direction === 'left') {
          this.$refs.cropper.rotateLeft()
        } else {
          this.$refs.cropper.rotateRight()
        }
      },
      reselect () {
        this.$Modal.confirm({
          content: 'Are you sure to disgard the changes?',
          onOk: () => {
            this.avatarOption.imgSrc = ''
          }
        })
      },
      finishCrop () {
        this.$refs.cropper.getCropData(data => {
          this.uploadImgSrc = data
          this.uploadModalVisible = true
        })
      },
      uploadAvatar () {
        this.$refs.cropper.getCropBlob(blob => {
          let form = new window.FormData()
          let file = new window.File([blob], 'avatar.' + this.avatarOption.outputType)
          form.append('image', file)
          this.loadingUploadBtn = true
          this.$http({
            method: 'post',
            url: 'upload_avatar',
            data: form,
            headers: {'content-type': 'multipart/form-data'}
          }).then(res => {
            this.loadingUploadBtn = false
            this.$success('Successfully set new avatar')
            this.uploadModalVisible = false
            this.avatarOption.imgSrc = ''
            this.$store.dispatch('getProfile')
          }, () => {
            this.loadingUploadBtn = false
          })
        })
      },
      updateProfile () {
        this.loadingSaveBtn = true
        let updateData = utils.filterEmptyValue(Object.assign({}, this.formProfile))
        api.updateProfile(updateData).then(res => {
          this.$success('Success')
          this.$store.commit(types.CHANGE_PROFILE, {profile: res.data.data})
          this.loadingSaveBtn = false
        }, _ => {
          this.loadingSaveBtn = false
        })
      }
    },
    computed: {
      previewStyle () {
        return {
          'width': this.preview.w + 'px',
          'height': this.preview.h + 'px',
          'overflow': 'hidden'
        }
      }
    }
  }
</script>

<style  scoped>
/deep/ .el-input__inner{
    height: 32px;
}
/deep/ .el-form-item__label{
    font-size: 12px;
    line-height: 20px;
}
.section-title{
    font-size: 21px;
    font-weight: 500;
    padding-top: 10px;
    padding-bottom: 20px;
    line-height: 30px;
}
  .inline {
    display: inline-block;
  }

  .copper-img {
    width: 400px;
    height: 300px;
  }

  .flex-container {
    flex-wrap: wrap;
    justify-content: flex-start;
    margin-bottom: 10px;
  }
.flex-container .cropper-main {
      flex: none;
    }
.flex-container    .cropper-btn {
      flex: none;
      vertical-align: top;
    }
.flex-container   .cropper-preview {
      flex: none;
      /*margin: 10px;*/
      margin-left: 20px;
      box-shadow: 0 0 1px 0;
 
}
.upload-modal .notice {
      font-size: 16px;
      display: inline-block;
      vertical-align: top;
      padding: 10px;
      padding-right: 15px;
}
.upload-modal img {
      box-shadow: 0 0 1px 0;
      border-radius: 50%;
  }
</style>
