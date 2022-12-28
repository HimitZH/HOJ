<template>
  <div class="setting-main">
    <el-row :gutter="20">
      <el-col :sm="24" :md="10" :lg="10">
        <div class="left">
          <p class="section-title">{{ $t('m.Change_Password') }}</p>
          <el-form
            class="setting-content"
            ref="formPassword"
            :model="formPassword"
            :rules="rulePassword"
          >
            <el-form-item :label="$t('m.Old_Password')" prop="oldPassword">
              <el-input v-model="formPassword.oldPassword" type="password" />
            </el-form-item>
            <el-form-item :label="$t('m.New_Password')" prop="newPassword">
              <el-input v-model="formPassword.newPassword" type="password" />
            </el-form-item>
            <el-form-item
              :label="$t('m.Confirm_New_Password')"
              prop="againPassword"
            >
              <el-input v-model="formPassword.againPassword" type="password" />
            </el-form-item>
          </el-form>
          <el-popover
            placement="top"
            width="350"
            v-model="visible.passwordSlideBlock"
            trigger="click"
          >
            <el-button
              type="primary"
              slot="reference"
              :loading="loading.btnPassword"
              :disabled="disabled.btnPassword"
              >{{ $t('m.Update_Password') }}</el-button
            >
            <slide-verify
              :l="42"
              :r="10"
              :w="325"
              :h="100"
              :accuracy="3"
              @success="changePassword"
              @again="onAgain('password')"
              :slider-text="$t('m.Slide_Verify')"
              ref="passwordSlideBlock"
              v-show="!verify.passwordSuccess"
            >
            </slide-verify>
            <el-alert
              :title="$t('m.Slide_Verify_Success')"
              type="success"
              :description="verify.passwordMsg"
              v-show="verify.passwordSuccess"
              :center="true"
              :closable="false"
              show-icon
            >
            </el-alert>
          </el-popover>
        </div>
        <el-alert
          v-show="visible.passwordAlert.show"
          :title="visible.passwordAlert.title"
          :type="visible.passwordAlert.type"
          :description="visible.passwordAlert.description"
          :closable="false"
          effect="dark"
          style="margin-top:15px"
          show-icon
        >
        </el-alert>
      </el-col>
      <el-col :md="4" :lg="4">
        <div class="separator hidden-md-and-down"></div>
        <p></p>
      </el-col>
      <el-col :sm="24" :md="10" :lg="10">
        <div class="right">
          <p class="section-title">{{ $t('m.Change_Email') }}</p>
          <el-form
            class="setting-content"
            ref="formEmail"
            :model="formEmail"
            :rules="ruleEmail"
          >
            <el-form-item :label="$t('m.Current_Password')" prop="password">
              <el-input v-model="formEmail.password" type="password" />
            </el-form-item>
            <el-form-item :label="$t('m.Old_Email')">
              <el-input v-model="formEmail.oldEmail" disabled />
            </el-form-item>
            <el-form-item :label="$t('m.New_Email')" prop="newEmail">
              <el-input v-model="formEmail.newEmail">
                <el-button slot="append"
                @click="getChangeEmailCode"
                :loading="loading.btnSendEmail"
                icon="el-icon-message">
                {{$t('m.Get_Captcha')}}
              </el-button>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('m.Captcha')" prop="code">
              <el-input v-model="formEmail.code" />
            </el-form-item>
          </el-form>
          <el-popover
            placement="top"
            width="350"
            v-model="visible.emailSlideBlock"
            trigger="click"
          >
            <el-button
              type="primary"
              slot="reference"
              :loading="loading.btnEmailLoading"
              :disabled="disabled.btnEmail"
              >{{ $t('m.Update_Email') }}</el-button
            >
            <slide-verify
              :l="42"
              :r="10"
              :w="325"
              :h="100"
              :accuracy="3"
              @success="changeEmail"
              @again="onAgain('email')"
              :slider-text="$t('m.Slide_Verify')"
              ref="emailSlideBlock"
              v-show="!verify.emailSuccess"
            >
            </slide-verify>
            <el-alert
              :title="$t('m.Slide_Verify_Success')"
              type="success"
              :description="verify.emailMsg"
              v-show="verify.emailSuccess"
              :center="true"
              :closable="false"
              show-icon
            >
            </el-alert>
          </el-popover>
        </div>
        <el-alert
          v-show="visible.emailAlert.show"
          :title="visible.emailAlert.title"
          :type="visible.emailAlert.type"
          :description="visible.emailAlert.description"
          :closable="false"
          effect="dark"
          style="margin-top:15px"
          show-icon
        >
        </el-alert>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';
import 'element-ui/lib/theme-chalk/display.css';
export default {
  data() {
    const oldPasswordCheck = [
      {
        required: true,
        trigger: 'blur',
        message: this.$i18n.t('m.The_current_password_cannot_be_empty'),
      },
      {
        trigger: 'blur',
        min: 6,
        max: 20,
        message: this.$i18n.t('m.Password_Check_Between'),
      },
    ];
    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.formPassword.newPassword) {
        callback(new Error(this.$i18n.t('m.Password_does_not_match')));
      }
      callback();
    };
    const CheckNewPassword = (rule, value, callback) => {
      if (this.formPassword.oldPassword !== '') {
        if (this.formPassword.oldPassword === this.formPassword.newPassword) {
          callback(
            new Error(this.$i18n.t('m.The_new_password_does_not_change'))
          );
        } else {
          // 对第二个密码框再次验证
          this.$refs.formPassword.validateField('again_password');
        }
      }
      callback();
    };
    const CheckEmail = (rule, value, callback) => {
      if (this.formEmail.oldEmail !== '') {
        if (this.formEmail.oldEmail === this.formEmail.newEmail) {
          callback(new Error(this.$i18n.t('m.The_new_email_does_not_change')));
        }
      }
      callback();
    };
    return {
      loading: {
        btnPassword: false,
        btnEmail: false,
        btnSendEmail: false,
      },
      disabled: {
        btnPassword: false,
        btnEmail: false,
      },
      verify: {
        passwordSuccess: false,
        passwordMsg: '',
        emailSuccess: false,
        emailMsg: '',
      },
      visible: {
        passwordAlert: {
          type: 'success',
          show: false,
          title: '',
          description: '',
        },
        emailAlert: {
          type: 'success',
          show: false,
          title: '',
          description: '',
        },
        passwordSlideBlock: false,
        emailSlideBlock: false,
      },
      formPassword: {
        oldPassword: '',
        newPassword: '',
        againPassword: '',
      },
      formEmail: {
        password: '',
        oldEmail: '',
        newEmail: '',
      },
      rulePassword: {
        oldPassword: oldPasswordCheck,
        newPassword: [
          {
            required: true,
            trigger: 'blur',
            message: this.$i18n.t('m.The_new_password_cannot_be_empty'),
          },
          {
            trigger: 'blur',
            min: 6,
            max: 20,
            message: this.$i18n.t('m.Password_Check_Between'),
          },
          { validator: CheckNewPassword, trigger: 'blur' },
        ],
        againPassword: [
          {
            required: true,
            trigger: 'blur',
            message: this.$i18n.t('m.Password_Again_Check_Required'),
          },
          { validator: CheckAgainPassword, trigger: 'blur' },
        ],
      },
      ruleEmail: {
        password: oldPasswordCheck,
        newEmail: [
          {
            required: true,
            message: this.$i18n.t('m.Email_Check_Required'),
            trigger: 'blur',
          },
          {
            type: 'email',
            trigger: 'change',
            message: this.$i18n.t('m.Email_Check_Format'),
          },
          { validator: CheckEmail, trigger: 'blur' },
        ],
        code:[
          {
              required: true,
              message: this.$i18n.t('m.Code_Check_Required'),
              trigger: 'blur',
          },
        ]
      },
    };
  },
  mounted() {
    this.formEmail.oldEmail = this.$store.getters.userInfo.email || '';
  },
  methods: {
    changePassword(times) {
      this.verify.passwordSuccess = true;
      let time = (times / 1000).toFixed(1);
      this.verify.passwordMsg = 'Total time ' + time + 's';
      setTimeout(() => {
        this.visible.passwordSlideBlock = false;
        this.verify.passwordSuccess = false;
        // 无论后续成不成功，验证码滑动都要刷新
        this.$refs.passwordSlideBlock.reset();
      }, 1000);

      this.$refs['formPassword'].validate((valid) => {
        if (valid) {
          this.loading.btnPassword = true;
          let data = Object.assign({}, this.formPassword);
          delete data.againPassword;
          api.changePassword(data).then(
            (res) => {
              this.loading.btnPassword = false;
              if (res.data.data.code == 200) {
                myMessage.success(this.$i18n.t('m.Update_Successfully'));
                this.visible.passwordAlert = {
                  show: true,
                  title: this.$i18n.t('m.Update_Successfully'),
                  type: 'success',
                  description: res.data.data.msg,
                };
                setTimeout(() => {
                  this.visible.passwordAlert = false;
                  this.$router.push({ name: 'Logout' });
                }, 5000);
              } else {
                myMessage.error(res.data.data.msg);
                this.visible.passwordAlert = {
                  show: true,
                  title: this.$i18n.t('m.Update_Failed'),
                  type: 'warning',
                  description: res.data.data.msg,
                };
                if (res.data.data.code == 403) {
                  this.visible.passwordAlert.type = 'error';
                  this.disabled.btnPassword = true;
                }
              }
            },
            (err) => {
              this.loading.btnPassword = false;
            }
          );
        }
      });
    },
    getChangeEmailCode(){
      if(!this.formEmail.newEmail){
        myMessage.error(this.$i18n.t('m.The_new_email_cannot_be_empty'));
      }
      var emailReg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      if (!emailReg.test(this.formEmail.newEmail)) {
        mMessage.error(this.$i18n.t('m.Email_Check_Format'));
        return;
      }
      if (this.formEmail.oldEmail === this.formEmail.newEmail) {
        myMessage.error(this.$i18n.t('m.The_new_email_does_not_change'));
      }
      this.loading.btnSendEmail = true;
      api.getChangeEmailCode(this.formEmail.newEmail).then((res)=>{
        myMessage.success(this.$i18n.t('m.Change_Send_Email_Msg'));
        this.$notify.success({
          title: this.$i18n.t('m.Success'),
          message: this.$i18n.t('m.Change_Send_Email_Msg'),
          duration: 5000,
          offset: 50
        });
        this.loading.btnSendEmail = false;
      },(_)=>{
        this.loading.btnSendEmail = false;
      })
    },
    changeEmail(times) {
      this.verify.emailSuccess = true;
      let time = (times / 1000).toFixed(1);
      this.verify.emailMsg = 'Total time ' + time + 's';
      setTimeout(() => {
        this.visible.emailSlideBlock = false;
        this.verify.emailSuccess = false;
        // 无论后续成不成功，验证码滑动都要刷新
        this.$refs.emailSlideBlock.reset();
      }, 1000);
      this.$refs['formEmail'].validate((valid) => {
        if (valid) {
          this.loading.btnEmail = true;
          let data = Object.assign({}, this.formEmail);
          api.changeEmail(data).then(
            (res) => {
              this.loading.btnEmail = false;
              if (res.data.data.code == 200) {
                myMessage.success(this.$i18n.t('m.Update_Successfully'));
                this.visible.emailAlert = {
                  show: true,
                  title: this.$i18n.t('m.Update_Successfully'),
                  type: 'success',
                  description: res.data.data.msg,
                };
                // 更新本地缓存
                this.$store.dispatch('setUserInfo', res.data.data.userInfo);
                this.$refs['formEmail'].resetFields();
                this.formEmail.oldEmail = res.data.data.userInfo.email;
              } else {
                myMessage.error(res.data.data.msg);
                this.visible.emailAlert = {
                  show: true,
                  title: this.$i18n.t('m.Update_Failed'),
                  type: 'warning',
                  description: res.data.data.msg,
                };
                if (res.data.data.code == 403) {
                  this.visible.emailAlert.type = 'error';
                  this.disabled.btnEmail = true;
                }
              }
            },
            (err) => {
              this.loading.btnEmail = false;
            }
          );
        }
      });
    },
    onAgain(type) {
      if ((type = 'password')) {
        this.$refs.passwordSlideBlock.reset();
      } else {
        this.$refs.emailSlideBlock.reset();
      }
      myMessage.warning(this.$i18n.t('m.Guess_robot'));
    },
  },
};
</script>

<style scoped>
.section-title {
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
/deep/ .el-input__inner {
  height: 32px;
}
/deep/ .el-form-item__label {
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
