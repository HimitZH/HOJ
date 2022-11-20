<template>
  <div>
    <el-form :model="registerForm" :rules="rules" ref="registerForm">
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          prefix-icon="el-icon-user-solid"
          :placeholder="$t('m.Register_Username')"
          @keyup.enter.native="handleRegister"
          width="100%"
        ></el-input>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.Register_Password')"
          @keyup.enter.native="handleRegister"
          type="password"
        ></el-input>
      </el-form-item>
      <el-form-item prop="passwordAgain">
        <el-input
          v-model="registerForm.passwordAgain"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.Register_Password_Again')"
          @keyup.enter.native="handleRegister"
          type="password"
        ></el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="registerForm.email"
          prefix-icon="el-icon-message"
          :placeholder="$t('m.Register_Email')"
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
          :placeholder="$t('m.Register_Email_Captcha')"
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
        {{ $t('m.Register_Btn') }}
      </el-button>
      <el-link type="primary" @click="switchMode('Login')">{{
        $t('m.Register_Already_Registed')
      }}</el-link>
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
            callback(new Error(this.$i18n.t('m.The_username_already_exists')));
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
            callback(new Error(this.$i18n.t('m.The_email_already_exists')));
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
        callback(new Error(this.$i18n.t('m.Password_does_not_match')));
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
          {
            required: true,
            message: this.$i18n.t('m.Username_Check_Required'),
            trigger: 'blur',
          },
          {
            validator: CheckUsernameNotExist,
            trigger: 'blur',
            message: this.$i18n.t('m.The_username_already_exists'),
          },
          {
            max: 20,
            message: this.$i18n.t('m.Username_Check_Max'),
            trigger: 'blur',
          },
        ],

        email: [
          {
            required: true,
            message: this.$i18n.t('m.Email_Check_Required'),
            trigger: 'blur',
          },
          {
            type: 'email',
            message: this.$i18n.t('m.Email_Check_Format'),
            trigger: 'blur',
          },
          {
            validator: CheckEmailNotExist,
            message: this.$i18n.t('m.The_email_already_exists'),
            trigger: 'blur',
          },
        ],
        password: [
          {
            required: true,
            message: this.$i18n.t('m.Password_Check_Required'),
            trigger: 'blur',
          },
          {
            min: 6,
            max: 20,
            message: this.$i18n.t('m.Password_Check_Between'),
            trigger: 'blur',
          },
          { validator: CheckPassword, trigger: 'blur' },
        ],
        passwordAgain: [
          {
            required: true,
            message: this.$i18n.t('m.Password_Again_Check_Required'),
            trigger: 'blur',
          },
          { validator: CheckAgainPassword, trigger: 'change' },
        ],
        code: [
          {
            required: true,
            message: this.$i18n.t('m.Code_Check_Required'),
            trigger: 'blur',
          },
          {
            min: 6,
            max: 6,
            message: this.$i18n.t('m.Code_Check_Length'),
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
      var emailReg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      if (!emailReg.test(this.registerForm.email)) {
        mMessage.error(this.$i18n.t('m.Email_Check_Format'));
        return;
      }
      this.btnEmailLoading = true;
      this.countdownNum = 'Waiting...';
      if (this.registerForm.email) {
        mMessage.info(this.$i18n.t('m.The_system_is_processing'));
        api.getRegisterEmail(this.registerForm.email).then(
          (res) => {
            if (res.data.msg != null) {
              mMessage.message(
                'success',
                this.$i18n.t('m.Register_Send_Email_Msg'),
                5000
              );
              this.$notify.success({
                title: this.$i18n.t('m.Success'),
                message: this.$i18n.t('m.Register_Send_Email_Msg'),
                duration: 5000,
                offset: 50
              });
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
              mMessage.success(this.$i18n.t('m.Thanks_for_registering'));
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
