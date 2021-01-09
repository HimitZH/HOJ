<template>
  <div>
    <el-form :model="formResetPassword" :rules="rules" ref="formResetPassword">
      <el-form-item prop="username">
        <el-input
          v-model="formResetPassword.username"
          prefix-icon="el-icon-user-solid"
          placeholder="Please Enter Your Username"
          width="100%"
        ></el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="formResetPassword.email"
          prefix-icon="el-icon-s-promotion"
          placeholder="Please Enter Your Email"
        >
        </el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button
        type="primary"
        @click="handleResetPwd"
        :loading="btnResetPwdLoading"
      >
        {{ resetText }}
      </el-button>
      <el-link type="primary" @click="switchMode('Login')"
        >想起密码? 返回登录!</el-link
      >
    </div>
  </div>
</template>
<script>
import { mapGetters, mapActions } from 'vuex';
import api from '@/common/api';
import mMessage from '@/common/message';
export default {
  data() {
    const CheckUsernameNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(value, undefined).then(
        (res) => {
          if (res.data.data.username === false) {
            callback(new Error('The username does not exist'));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const CheckEmailNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(undefined, value).then(
        (res) => {
          if (res.data.data.email === false) {
            callback(new Error('The email does not exist'));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    return {
      resetText: '发送重置密码的邮件',
      btnResetPwdLoading: false,
      formResetPassword: {
        username: '',
        email: '',
      },
      rules: {
        username: [
          {
            required: true,
            message: 'The username is required',
            trigger: 'blur',
          },
          { validator: CheckUsernameNotExist, trigger: 'blur' },
        ],
        email: [
          {
            required: true,
            message: 'The email is required',
            type: 'email',
            trigger: 'blur',
          },
          { validator: CheckEmailNotExist, trigger: 'blur' },
        ],
      },
    };
  },
  methods: {
    ...mapActions(['changeModalStatus', 'changeResetTimeOut', 'startTimeOut']),
    switchMode(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    countDown() {
      let i = this.time;
      this.resetText = i + '秒后，可重新发送重置密码的邮件...';
      if (i == 0) {
        this.btnResetPwdLoading = false;
        this.resetText = '发送重置密码的邮件';
        return;
      }
      setTimeout(() => {
        this.countDown();
      }, 1000);
    },
    handleResetPwd() {
      this.$refs['formResetPassword'].validate((valid) => {
        if (valid) {
          this.resetText = '正在发送...';
          mMessage.info('请稍后...系统正在向您的邮箱发送重置确认邮件');
          this.btnResetPwdLoading = true;
          api.applyResetPassword(this.formResetPassword).then(
            (res) => {
              mMessage.success(res.data.msg);
              this.successApply = true;
              this.countDown();
              this.startTimeOut({ name: 'resetTimeOut' });
            },
            (_) => {
              this.btnResetPwdLoading = false;
              this.resetText = '重新发送';
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters(['resetTimeOut', 'modalStatus']),
    time: {
      get() {
        return this.resetTimeOut;
      },
      set(value) {
        this.changeResetTimeOut({ time: value });
      },
    },
  },
  created() {
    if (this.time != 90 && this.time != 0) {
      this.btnResetPwdLoading = true;
      this.countDown();
    }
  },
};
</script>
<style scoped>
.footer {
  overflow: auto;
  margin-top: 20px;
  margin-bottom: -15px;
  text-align: center;
}
/deep/.el-button--primary {
  margin: 0 0 15px 0;
  width: 100%;
}

/deep/ .el-form-item__content {
  margin-left: 0px !important;
}
</style>
