<template>
  <div>
    <el-form :model="registerForm" :rules="rules" ref="registerForm">
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          prefix-icon="el-icon-user-solid"
          placeholder="Please Enter Username"
          @keyup.enter.native="handleRegister"
          width="100%"
        ></el-input>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          prefix-icon="el-icon-lock"
          placeholder="Please Enter Password"
          @keyup.enter.native="handleRegister"
          type="password"
        ></el-input>
      </el-form-item>
      <el-form-item prop="passwordAgain">
        <el-input
          v-model="registerForm.passwordAgain"
          prefix-icon="el-icon-lock"
          placeholder="Please Enter Password Again"
          @keyup.enter.native="handleRegister"
          type="password"
        ></el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="registerForm.email"
          prefix-icon="el-icon-message"
          placeholder="Please Enter Email"
          @keyup.enter.native="handleRegister"
        >
          <el-button
            slot="append"
            icon="el-icon-message"
            type="primary"
            @click.native="sendRegisterEmail"
            :loading="btnEmailLoading"
          >
            <span v-show="btnEmailLoading">{{ countdownNum }}</span>
          </el-button>
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input
          v-model="registerForm.code"
          prefix-icon="el-icon-s-check"
          placeholder="Please enter the captcha from the email"
          @keyup.enter.native="handleRegister"
        ></el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button
        type="primary"
        @click="handleRegister()"
        :loading="btnRegisterLoading"
      >
        注册
      </el-button>
      <el-link type="primary" @click="switchMode('Login')"
        >已有账户? 返回登录!</el-link
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
          if (res.data.data.username === true) {
            callback(new Error('The username already exists'));
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
          if (res.data.data.email === true) {
            callback(new Error('The email already exists'));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const CheckPassword = (rule, value, callback) => {
      if (this.registerForm.password !== '') {
        // 对第二个密码框再次验证
        this.$refs.registerForm.validateField('passwordAgain');
      }
      callback();
    };

    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('Password does not match'));
      }
      callback();
    };
    return {
      btnRegisterLoading: false,
      btnEmailLoading: false,
      countdownNum: null,
      registerForm: {
        username: '',
        password: '',
        passwordAgain: '',
        email: '',
        code: '',
      },
      sendEmailError: false,
      rules: {
        username: [
          { required: true, message: 'Username is required', trigger: 'blur' },
          {
            validator: CheckUsernameNotExist,
            trigger: 'blur',
            message: 'The username already exists',
          },
          {
            max: 255,
            message: 'The longest length of a username is 255',
            trigger: 'blur',
          },
        ],

        email: [
          {
            required: true,
            message: 'The email is required',
            trigger: 'blur',
          },
          {
            type: 'email',
            message: 'The email format is incorrect',
            trigger: 'blur',
          },
          {
            validator: CheckEmailNotExist,
            message: 'The email already exists',
            trigger: 'blur',
          },
        ],
        password: [
          {
            required: true,
            message: 'The password is required',
            trigger: 'blur',
          },
          {
            min: 6,
            max: 20,
            message: 'The length of the password is between 6 and 20',
            trigger: 'blur',
          },
          { validator: CheckPassword, trigger: 'blur' },
        ],
        passwordAgain: [
          {
            required: true,
            message: 'The password again is required',
            trigger: 'blur',
          },
          { validator: CheckAgainPassword, trigger: 'change' },
        ],
        code: [
          {
            required: true,
            message: 'The captcha must be six digits',
            trigger: 'blur',
          },
          {
            min: 6,
            max: 6,
            message: 'The captcha must be six digits',
            trigger: 'blur',
          },
        ],
      },
    };
  },
  methods: {
    ...mapActions([
      'changeModalStatus',
      'startTimeOut',
      'changeRegisterTimeOut',
    ]),
    switchMode(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    countDown() {
      let i = this.time;
      if (i == 0) {
        this.btnEmailLoading = false;
        return;
      }
      this.countdownNum = i;
      setTimeout(() => {
        this.countDown();
      }, 1000);
    },
    sendRegisterEmail() {
      var emailReg = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
      if (!emailReg.test(this.registerForm.email)) {
        mMessage.error('请检查邮箱格式！');
        return;
      }
      this.btnEmailLoading = true;
      this.countdownNum = '正在处理...';
      if (this.registerForm.email) {
        mMessage.info('请稍后...系统正在处理中...');
        api.getRegisterEmail(this.registerForm.email).then(
          (res) => {
            if (res.data.msg != null) {
              mMessage.success(res.data.msg);
              this.countDown();
              this.startTimeOut({ name: 'registerTimeOut' });
            }
          },
          (res) => {
            this.btnEmailLoading = false;
            this.countdownNum = null;
          }
        );
      }
    },
    handleRegister() {
      this.$refs['registerForm'].validate((valid) => {
        if (valid) {
          const _this = this;
          let formData = Object.assign({}, this.registerForm);
          delete formData['passwordAgain'];
          this.btnRegisterLoading = true;
          api.register(formData).then(
            (res) => {
              mMessage.success(res.data.msg);
              this.switchMode('Login');
              this.btnRegisterLoading = false;
            },
            (res) => {
              this.registerForm.code = '';
              this.btnRegisterLoading = false;
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters(['registerTimeOut', 'modalStatus']),
    time: {
      get() {
        return this.registerTimeOut;
      },
      set(value) {
        this.changeRegisterTimeOut({ time: value });
      },
    },
  },
  created() {
    if (this.time != 60 && this.time != 0) {
      this.btnEmailLoading = true;
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
/deep/ .el-input-group__append {
  color: #fff;
  background: #25bb9b;
}
/deep/.footer .el-button--primary {
  margin: 0 0 15px 0;
  width: 100%;
}

/deep/ .el-form-item__content {
  margin-left: 0px !important;
}
</style>
