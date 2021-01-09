<template>
  <div class="setting-main">
    <el-row :gutter="20">
      <el-col :sm="24" :md="10" :lg="10">
        <div class="left">
          <p class="section-title">Change Password</p>
          <el-form
            class="setting-content"
            ref="formPassword"
            :model="formPassword"
            :rules="rulePassword"
          >
            <el-form-item label="Old Password" prop="oldPassword">
              <el-input v-model="formPassword.oldPassword" type="password" />
            </el-form-item>
            <el-form-item label="New Password" prop="newPassword">
              <el-input v-model="formPassword.newPassword" type="password" />
            </el-form-item>
            <el-form-item label="Confirm New Password" prop="againPassword">
              <el-input v-model="formPassword.againPassword" type="password" />
            </el-form-item>
          </el-form>
          <el-popover
            placement="top"
            width="350"
            v-model="visible.slideBlock"
            trigger="click"
          >
            <el-button
              type="primary"
              slot="reference"
              :loading="loading.btnPassword"
              >Update Password</el-button
            >
            <slide-verify
              :l="42"
              :r="10"
              :w="325"
              :h="100"
              :accuracy="3"
              @success="changePassword"
              @again="onAgain"
              slider-text="请向右滑动验证"
              ref="slideblock"
              v-show="!verify.passwordSuccess"
            >
            </slide-verify>
            <el-alert
              title="验证成功"
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
          <p class="section-title">Change Email</p>
          <el-form
            class="setting-content"
            ref="formEmail"
            :model="formEmail"
            :rules="ruleEmail"
          >
            <el-form-item label="Current Password" prop="password">
              <el-input v-model="formEmail.password" type="password" />
            </el-form-item>
            <el-form-item label="Old Email">
              <el-input v-model="formEmail.old_email" disabled />
            </el-form-item>
            <el-form-item label="New Email" prop="new_email">
              <el-input v-model="formEmail.new_email" />
            </el-form-item>
            <el-form-item
              v-if="visible.tfaRequired"
              label="Two Factor Auth"
              prop="tfa_code"
            >
              <el-input v-model="formEmail.tfa_code" />
            </el-form-item>
            <el-button type="primary" @click="changeEmail"
              >Change Email</el-button
            >
          </el-form>
        </div>
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
        message: 'The old password is required',
      },
      {
        trigger: 'blur',
        min: 6,
        max: 20,
        message: 'The length of the password is between 6 and 20',
      },
    ];
    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.formPassword.newPassword) {
        callback(new Error('Password does not match'));
      }
      callback();
    };
    const CheckNewPassword = (rule, value, callback) => {
      if (this.formPassword.oldPassword !== '') {
        if (this.formPassword.oldPassword === this.formPassword.newPassword) {
          callback(new Error("The new password doesn't change"));
        } else {
          // 对第二个密码框再次验证
          this.$refs.formPassword.validateField('again_password');
        }
      }
      callback();
    };
    return {
      loading: {
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
        slideBlock: false,
      },
      formPassword: {
        oldPassword: '',
        newPassword: '',
        againPassword: '',
      },
      formEmail: {
        password: '',
        old_email: '',
        new_email: '',
      },
      rulePassword: {
        oldPassword: oldPasswordCheck,
        newPassword: [
          {
            required: true,
            trigger: 'blur',
            message: 'The new password is required',
          },
          {
            trigger: 'blur',
            min: 6,
            max: 20,
            message: 'The length of the password is between 6 and 20',
          },
          { validator: CheckNewPassword, trigger: 'blur' },
        ],
        againPassword: [
          {
            required: true,
            trigger: 'blur',
            message: 'The again password is required',
          },
          { validator: CheckAgainPassword, trigger: 'blur' },
        ],
      },
      ruleEmail: {
        password: oldPasswordCheck,
        new_email: [{ required: true, type: 'email', trigger: 'change' }],
      },
    };
  },
  mounted() {
    this.formEmail.old_email = this.$store.getters.userInfo.email || '';
  },
  methods: {
    changePassword(times) {
      this.verify.passwordSuccess = true;
      let time = (times / 1000).toFixed(1);
      this.verify.passwordMsg = '本次耗时' + time + 's';
      setTimeout(() => {
        this.visible.slideBlock = false;
        this.verify.passwordSuccess = false;
        // 无论后续成不成功，验证码滑动都要刷新
        this.$refs.slideblock.reset();
      }, 1000);

      this.$refs['formPassword'].validate((valid) => {
        if (valid) {
          this.loading.btnPassword = true;
          let data = Object.assign({}, this.formPassword);
          delete data.again_password;
          api.changePassword(data).then(
            (res) => {
              this.loading.btnPassword = false;
              this.visible.passwordAlert = true;
              if (res.data.data.code == 200) {
                myMessage.success(res.data.msg);
                this.visible.passwordAlert = {
                  show: true,
                  title: '修改成功',
                  type: 'success',
                  description: res.data.data.msg,
                };
                setTimeout(() => {
                  this.visible.passwordAlert = false;
                  this.$router.push({ name: 'Logout' });
                }, 5000);
              } else {
                myMessage.error(res.data.msg);
                this.visible.passwordAlert = {
                  show: true,
                  title: '修改失败',
                  type: 'warning',
                  description: res.data.data.msg,
                };
                if (res.data.data.code == 403) {
                  this.visible.passwordAlert.type = 'error';
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
    changeEmail() {
      this.validateForm('formEmail').then((valid) => {
        this.loading.btnEmail = true;
        let data = Object.assign({}, this.formEmail);
        api.changeEmail(data).then(
          (res) => {
            this.loading.btnEmail = false;
            this.visible.emailAlert = true;
            this.$success('Change email successfully');
            this.$refs.formEmail.resetFields();
          },
          (err) => {
            if (res.data.data === 'tfa_required') {
              this.visible.tfaRequired = true;
            }
          }
        );
      });
    },
    onAgain() {
      this.$refs.slideblock.reset();
      myMessage.warning('速度过快，可能为机器操作！请重新验证！');
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
