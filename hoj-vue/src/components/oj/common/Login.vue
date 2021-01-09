<template>
  <div>
    <el-form
      :model="formLogin"
      :rules="rules"
      ref="formLogin"
      label-width="100px"
    >
      <el-form-item prop="username">
        <el-input
          v-model="formLogin.username"
          prefix-icon="el-icon-user-solid"
          placeholder="Username"
          width="100%"
          @keyup.enter.native="handleLogin"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="formLogin.password"
          prefix-icon="el-icon-lock"
          placeholder="Password"
          type="password"
          @keyup.enter.native="handleLogin"
        ></el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button type="primary" @click="handleLogin" :loading="btnLoginLoading">
        登录
      </el-button>
      <el-link
        v-if="allow_register"
        type="primary"
        @click="switchMode('Register')"
        >没有账户? 现在注册!</el-link
      >
      <el-link
        type="danger"
        @click="switchMode('ResetPwd')"
        style="float: right"
        >忘记密码</el-link
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
    return {
      allow_register: true, //是否允许注册
      btnLoginLoading: false,
      formLogin: {
        username: '',
        password: '',
      },
      rules: {
        username: [
          { required: true, message: 'Username is required', trigger: 'blur' },
          {
            max: 255,
            message: 'The longest length of a username is 255',
            trigger: 'blur',
          },
        ],
        password: [
          { required: true, message: 'Password is required', trigger: 'blur' },
          {
            min: 6,
            max: 20,
            message: 'The length of the password is between 6 and 20',
            trigger: 'blur',
          },
        ],
      },
    };
  },
  methods: {
    ...mapActions(['changeModalStatus']),
    switchMode(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    handleLogin() {
      this.$refs['formLogin'].validate((valid) => {
        if (valid) {
          this.btnLoginLoading = true;
          let formData = Object.assign({}, this.formLogin);
          api.login(formData).then(
            (res) => {
              this.btnLoginLoading = false;
              this.changeModalStatus({ visible: false });

              const jwt = res.headers['authorization'];
              this.$store.commit('changeUserToken', jwt);
              this.$store.dispatch('setUserInfo', res.data.data);
              mMessage.success('欢迎回来~');
            },
            (_) => {
              this.btnLoginLoading = false;
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters(['modalStatus']),
    visible: {
      get() {
        return this.modalStatus.visible;
      },
      set(value) {
        this.changeModalStatus({ visible: value });
      },
    },
  },
};
</script>
<style scoped>
.footer {
  overflow: auto;
  margin-top: 20px;
  margin-bottom: -15px;
  text-align: left;
}
/deep/.el-button {
  margin: 0 0 15px 0;
  width: 100%;
}

/deep/ .el-form-item__content {
  margin-left: 0px !important;
}
</style>
