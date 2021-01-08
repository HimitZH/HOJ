<template>
  <div>
    <div class="container">
      <el-card shadow="always" body-style="{backgroud-color:gray}">
        <h2 style="text-align: center;">Set New Password - HOJ</h2>
        <el-form :model="formResetPassword" :rules="rules" ref="formResetPassword">
          <el-form-item prop="username">
            <el-input
              v-model="formResetPassword.username"
              prefix-icon="el-icon-user-solid"
              :disabled="true"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="formResetPassword.password"
              prefix-icon="el-icon-lock"
              type="password"
              placeholder="Please Enter New Password"
            ></el-input>
          </el-form-item>
          <el-form-item prop="passwordAgain">
            <el-input
              v-model="formResetPassword.passwordAgain"
              prefix-icon="el-icon-lock"
              type="password"
              placeholder="Please Enter New Password Again"
            ></el-input>
          </el-form-item>
        </el-form>
        <div class="footer">
          <el-button
            type="primary"
            @click="handleResetPwd"
            :loading="btnLoading"
          >
            重置密码
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script>
import api from "@/common/api";
import { mapGetters, mapActions } from "vuex";
import mMessage from "@/common/message";
export default {
  data() {
    const CheckUsernameNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(value, undefined).then(
        (res) => {
          if (res.data.data.username === false) {
            callback(new Error("The username does not exists"));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const CheckPassword = (rule, value, callback) => {
      if (this.formResetPassword.password !== "") {
        // 对第二个密码框再次验证
        this.$refs.formResetPassword.validateField("passwordAgain");
      }
      callback();
    };

    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.formResetPassword.password) {
        callback(new Error("Password does not match"));
      }
      callback();
    };
    return {
      btnLoading: false,
      formResetPassword: {
        username: "",
        password: "",
        passwordAgain: "",
        code: "",
      },
      rules: {
        username: [
          { required: true, message: "Username is required", trigger: "blur" },
          { validator: CheckUsernameNotExist, trigger: "blur" },
        ],
        password: [
          {
            required: true,
            message: "Password is required",
            trigger: "blur",
          },
          {
            min: 6,
            max: 20,
            trigger: "blur",
            message: "The length of the password is between 6 and 20",
          },
          { validator: CheckPassword, trigger: "blur" },
        ],
        passwordAgain: [
          {
            required: true,
            message: "Password again is required",
            trigger: "blur",
          },
          { validator: CheckAgainPassword, trigger: "change" },
        ],
      },
    };
  },
  created() {
    let username = this.$route.query.username;
    let code = this.$route.query.code;
    if (username) {
      this.formResetPassword.username = username;
    }
    if (code) {
      this.formResetPassword.code = code;
    }
  },
  methods: {
    ...mapActions(["changeModalStatus"]),
    handleResetPwd() {
      this.$refs['formResetPassword'].validate((valid) => {
        this.btnLoading = true;
        let data = Object.assign({}, this.formResetPassword);
        delete data.passwordAgain;
        api.resetPassword(data).then(
          (res) => {
            this.btnLoading = false;
            mMessage.success('重置密码成功')
            this.$router.replace({
              path: '/'
            })
            this.changeModalStatus({
              mode:'Login',
              visible: true,
            });
          },
          (_) => {
            this.btnLoading = false;
          }
        );
      });
    },
  },
};
</script>
<style scoped>
.container {
  width: 450px;
  margin: 0 auto;
}
/deep/ .el-card {
  border: 1px solid skyblue;
  border-radius: 8px;
}
h2 {
  font-size: 22px;
  font-weight: 600;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1em;
  color: #4e4e4e;
}
</style>