<template>
  <div>
    <vue-particles
      color="#dedede"
      :particleOpacity="0.7"
      :particlesNumber="80"
      shapeType="circle"
      :particleSize="4"
      linesColor="#dedede"
      :linesWidth="1"
      :lineLinked="true"
      :lineOpacity="0.4"
      :linesDistance="150"
      :moveSpeed="3"
      :hoverEffect="true"
      hoverMode="grab"
      :clickEffect="true"
      clickMode="push"
    >
    </vue-particles>
    <div class="form">
      <el-form
        :model="ruleForm2"
        :rules="rules2"
        ref="ruleForm2"
        label-position="left"
        label-width="0px"
        class="demo-ruleForm login-container"
      >
        <h1 class="title">{{ $t('m.Welcome_to_Login_Admin') }}</h1>
        <el-form-item prop="username">
          <el-input
            type="text"
            v-model="ruleForm2.username"
            auto-complete="off"
            :placeholder="$t('m.Please_enter_username')"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            type="password"
            v-model="ruleForm2.password"
            auto-complete="off"
            :placeholder="$t('m.Please_enter_password')"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item style="width: 100%">
          <el-button
            type="primary"
            style="width: 100%"
            @click.native.prevent="handleLogin"
            :loading="logining"
            >{{ $t('m.Login') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import mMessage from '@/common/message';
export default {
  data() {
    return {
      logining: false,
      ruleForm2: {
        username: '',
        password: '',
      },
      rules2: {
        username: [
          {
            required: true,
            trigger: 'blur',
            message: this.$i18n.t('m.Username_Check_Required'),
          },
        ],
        password: [
          {
            required: true,
            trigger: 'blur',
            message: this.$i18n.t('m.Password_Check_Required'),
          },
        ],
      },
      checked: true,
    };
  },
  methods: {
    handleLogin(ev) {
      this.$refs.ruleForm2.validate((valid) => {
        if (valid) {
          this.logining = true;
          api
            .admin_login(this.ruleForm2.username, this.ruleForm2.password)
            .then(
              (res) => {
                this.logining = false;
                const jwt = res.headers['authorization'];
                this.$store.commit('changeUserToken', jwt);
                this.$store.dispatch('setUserInfo', res.data.data);
                mMessage.success(this.$i18n.t('m.Admin_Login_Success'));
                this.$router.push({ name: 'admin-dashboard' });
              },
              () => {
                this.logining = false;
              }
            );
        } else {
          mMessage.error(
            this.$i18n.t('m.Please_check_your_username_or_password')
          );
        }
      });
    },
  },
};
</script>

<style scoped>
.login-container {
  -webkit-border-radius: 5px;
  border-radius: 5px;
  -moz-border-radius: 5px;
  background-clip: padding-box;
  margin: 180px auto;
  width: 350px;
  padding: 35px 35px 15px 35px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
}
.login-container .title {
  margin: 0px auto 40px auto;
  text-align: center;
  color: #1e9fff;
  font-size: 25px;
  font-weight: bold;
}
.login-container .remember {
  margin: 0px 0px 35px 0px;
}
.form {
  position: relative;
  z-index: 9999;
}
</style>
