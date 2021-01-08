<template>
  <div class="setting-main">
    <div class="flex-container">
     <el-row :gutter="20">
        <el-col :sm="24" :md="10" :lg="10" >
            <div class="left">
                <p class="section-title">Change Password</p>
                <el-form class="setting-content" ref="formPassword" :model="formPassword" :rules="rulePassword">
                <el-form-item label="Old Password" prop="old_password">
                    <el-input v-model="formPassword.oldPassword" type="password"/>
                </el-form-item>
                <el-form-item label="New Password" prop="new_password">
                    <el-input v-model="formPassword.newPassword" type="password"/>
                </el-form-item>
                <el-form-item label="Confirm New Password" prop="again_password">
                    <el-input  v-model="formPassword.againPassword" type="password"/>
                </el-form-item>
                <el-form-item v-if="visible.passwordAlert">
                    <el-alert type="success">You will need to login again after 5 seconds..</el-alert>
                </el-form-item>
                <el-button type="primary" @click="changePassword">Update Password</el-button>
                </el-form>
            </div>
        </el-col>
        <el-col :sm="24" :md="4" :lg="4" >
            
            <div class="separator hidden-sm-and-down"></div>
            <p></p>
        </el-col>
        <el-col :sm="24" :md="10" :lg="10">
            <div class="right">
                <p class="section-title">Change Email</p>
                <el-form class="setting-content" ref="formEmail" :model="formEmail" :rules="ruleEmail">
                <el-form-item label="Current Password" prop="password">
                    <el-input v-model="formEmail.password" type="password"/>
                </el-form-item>
                <el-form-item label="Old Email">
                    <el-input v-model="formEmail.old_email" disabled/>
                </el-form-item>
                <el-form-item label="New Email" prop="new_email">
                    <el-input v-model="formEmail.new_email"/>
                </el-form-item>
                <el-form-item v-if="visible.tfaRequired" label="Two Factor Auth" prop="tfa_code">
                    <el-input v-model="formEmail.tfa_code"/>
                </el-form-item>
                <el-button type="primary" @click="changeEmail">Change Email</el-button>
                </el-form>
            </div>
        </el-col>
     </el-row>
    </div>
  </div>
</template>

<script>
  import api from '@/common/api';
  import 'element-ui/lib/theme-chalk/display.css';
  export default {
    data () {
      const oldPasswordCheck = [{required: true, trigger: 'blur', min: 6, max: 20}]
      const tfaCheck = [{required: true, trigger: 'change'}]
      const CheckAgainPassword = (rule, value, callback) => {
        if (value !== this.formPassword.new_password) {
          callback(new Error('password does not match'))
        }
        callback()
      }
      const CheckNewPassword = (rule, value, callback) => {
        if (this.formPassword.old_password !== '') {
          if (this.formPassword.old_password === this.formPassword.new_password) {
            callback(new Error('The new password doesn\'t change'))
          } else {
            // 对第二个密码框再次验证
            this.$refs.formPassword.validateField('again_password')
          }
        }
        callback()
      }
      return {
        loading: {
          btnPassword: false,
          btnEmail: false
        },
        visible: {
          passwordAlert: false,
          emailAlert: false,
          tfaRequired: false
        },
        formPassword: {
          oldPassword: '',
          newPassword: '',
          againPassword: ''
        },
        formEmail: {
          password: '',
          old_email: '',
          new_email: ''
        },
        rulePassword: {
          old_password: oldPasswordCheck,
          new_password: [
            {required: true, trigger: 'blur', min: 6, max: 20},
            {validator: CheckNewPassword, trigger: 'blur'}
          ],
          again_password: [
            {required: true, validator: CheckAgainPassword, trigger: 'change'}
          ],
          tfa_code: tfaCheck
        },
        ruleEmail: {
          password: oldPasswordCheck,
          new_email: [{required: true, type: 'email', trigger: 'change'}],
          tfa_code: tfaCheck
        }
      }
    },
    mounted () {
      this.formEmail.old_email = this.$store.getters.user.email || ''
    },
    methods: {
      changePassword () {
        this.validateForm('formPassword').then(valid => {
          this.loading.btnPassword = true
          let data = Object.assign({}, this.formPassword)
          delete data.again_password
          if (!this.visible.tfaRequired) {
            delete data.tfa_code
          }
          api.changePassword(data).then(res => {
            this.loading.btnPassword = false
            this.visible.passwordAlert = true
            this.$success('Update password successfully')
            setTimeout(() => {
              this.visible.passwordAlert = false
              this.$router.push({name: 'logout'})
            }, 5000)
          }, res => {
            if (res.data.data === 'tfa_required') {
              this.visible.tfaRequired = true
            }
            this.loading.btnPassword = false
          })
        })
      },
      changeEmail () {
        this.validateForm('formEmail').then(valid => {
          this.loading.btnEmail = true
          let data = Object.assign({}, this.formEmail)
          if (!this.visible.tfaRequired) {
            delete data.tfa_code
          }
          api.changeEmail(data).then(res => {
            this.loading.btnEmail = false
            this.visible.emailAlert = true
            this.$success('Change email successfully')
            this.$refs.formEmail.resetFields()
          }, res => {
            if (res.data.data === 'tfa_required') {
              this.visible.tfaRequired = true
            }
          })
        })
      }
    }
  }
</script>

<style scoped>

.flex-container {
 justify-content: flex-start;
}
.section-title{
    font-size: 21px;
    font-weight: 500;
    padding-top: 10px;
    padding-bottom: 20px;
    line-height: 30px;
}
.left {
    text-align: center;  

}
.right {
    text-align: center;  

}
/deep/ .el-input__inner{
    height: 32px;
}
/deep/ .el-form-item__label{
    font-size: 12px;
    line-height: 20px;
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

