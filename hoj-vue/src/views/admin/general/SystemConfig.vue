<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">Website Config</span>
      </div>
      <el-form
        label-position="left"
        label-width="110px"
        ref="form"
        :model="websiteConfig"
      >
        <el-row :gutter="20">
          <el-col :md="8" :xs="24">
            <el-form-item label="Base URL" required>
              <el-input
                v-model="websiteConfig.baseUrl"
                placeholder="Website Base URL"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Name" required>
              <el-input
                v-model="websiteConfig.name"
                placeholder="Website Name"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Short Name" required>
              <el-input
                v-model="websiteConfig.shortName"
                placeholder="Website Name Shortcut"
              ></el-input>
            </el-form-item>
          </el-col>

          <el-col :md="12" :xs="24">
            <el-form-item label="Record Name" required>
              <el-input
                v-model="websiteConfig.recordName"
                placeholder="Website Record Name"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Record URL" required>
              <el-input
                v-model="websiteConfig.recordUrl"
                placeholder="Website Record URL"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Project Name" required>
              <el-input
                v-model="websiteConfig.projectName"
                placeholder="Website Project Namet"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Project URL" required>
              <el-input
                v-model="websiteConfig.projectUrl"
                placeholder="Website Project URL"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="Website Desc" required>
              <el-input
                type="textarea"
                placeholder="Website Description"
                v-model="websiteConfig.description"
                maxlength="150"
                show-word-limit
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="Allow Register" label-width="120px">
              <el-switch
                v-model="websiteConfig.register"
                active-color="#13ce66"
                inactive-color="#ff4949"
              >
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-button type="primary" @click.native="saveWebsiteConfig" size="small"
        >Save</el-button
      >
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">SMTP Config</span>
      </div>
      <el-form label-position="left" label-width="80px" :model="smtp">
        <el-row :gutter="20">
          <el-col :md="24" :xs="24">
            <el-form-item label="Host" required label-width="80px">
              <el-input
                v-model="smtp.emailHost"
                placeholder="SMTP Host Address"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="Port" required label-width="80px">
              <el-input
                v-model="smtp.emailPort"
                placeholder="SMTP Port"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Username" required label-width="80px">
              <el-input
                v-model="smtp.emailUsername"
                placeholder="Account Username"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Password" label-width="80px" required>
              <el-input
                v-model="smtp.emailPassword"
                type="password"
                placeholder="SMTP Server Password"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="BG IMG" label-width="80px" required>
              <el-input
                v-model="smtp.emailBGImg"
                placeholder="SMTP Template Background IMG Address"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="SSL">
              <el-switch v-model="smtp.emailSsl"> </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-button type="primary" @click.native="saveSMTPConfig" size="small"
        >Save</el-button
      >
      <el-button
        type="warning"
        @click.native="testSMTPConfig"
        v-if="saved"
        :loading="loadingBtnTest"
        size="small"
        >Send Test Email</el-button
      >
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">DataBase Config</span>
      </div>
      <el-form label-position="top" :model="databaseConfig">
        <el-row :gutter="20">
          <el-col :md="12" :xs="24">
            <el-form-item label="MySQL Host" required label-width="80px">
              <el-input
                v-model="databaseConfig.dbHost"
                placeholder="MySQL Host Address"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="MySQL Port" required label-width="80px">
              <el-input
                type="number"
                v-model="databaseConfig.dbPost"
                placeholder="MySQL Port"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="MySQL Username" required label-width="80px">
              <el-input
                v-model="databaseConfig.dbUsername"
                placeholder="MySQL Username"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="MySQL Password" label-width="80px" required>
              <el-input
                v-model="databaseConfig.dbPassword"
                type="password"
                placeholder="MySQL Password"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Redis Host" label-width="80px" required>
              <el-input
                v-model="databaseConfig.redisHost"
                type="password"
                placeholder="Redis Host Address"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Redis Port" label-width="80px" required>
              <el-input
                v-model="databaseConfig.redisPort"
                type="password"
                placeholder="Redis Port"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Redis Password" label-width="80px" required>
              <el-input
                v-model="databaseConfig.redisPassword"
                type="password"
                placeholder="Redis Password"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-button type="primary" @click.native="saveDataBaseConfig" size="small"
        >Save</el-button
      >
    </el-card>
  </div>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';
export default {
  name: 'SystemConfig',
  data() {
    return {
      init: false,
      saved: false,
      loadingBtnTest: false,
      smtp: {
        emailHost: 'smtp.example.com',
        emailPassword: '',
        emailPort: 587,
        emailBGImg: '',
        emailUsername: 'email@example.com',
        emailSsl: true,
      },
      websiteConfig: {},
      databaseConfig: {},
    };
  },
  mounted() {
    api.admin_getSMTPConfig().then((res) => {
      if (res.data.data) {
        this.smtp = res.data.data;
      } else {
        this.init = true;
        myMessage.warning('请先添加STMP的配置！');
      }
    });
    api
      .admin_getWebsiteConfig()
      .then((res) => {
        this.websiteConfig = res.data.data;
      })
      .catch(() => {}),
      api
        .admin_getDataBaseConfig()
        .then((res) => {
          this.databaseConfig = res.data.data;
        })
        .catch(() => {});
  },
  methods: {
    saveSMTPConfig() {
      api.admin_editSMTPConfig(this.smtp).then(
        (res) => {
          myMessage.success(res.data.msg);
          this.saved = true;
        },
        () => {}
      );
    },
    testSMTPConfig() {
      this.$prompt('Please input your email', '', {
        inputPattern: /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?/,
        inputErrorMessage: 'Error email format',
      })
        .then(({ value }) => {
          this.loadingBtnTest = true;
          api.admin_testSMTPConfig(value).then(
            (res) => {
              myMessage.success(res.data.msg);
              this.loadingBtnTest = false;
            },
            () => {
              this.loadingBtnTest = false;
            }
          );
        })
        .catch(() => {});
    },
    saveWebsiteConfig() {
      api
        .admin_editWebsiteConfig(this.websiteConfig)
        .then((res) => {
          myMessage.success(res.data.msg);
        })
        .catch(() => {});
    },
    saveDataBaseConfig() {
      api
        .admin_editDataBaseConfig(this.databaseConfig)
        .then((res) => {
          myMessage.success(res.data.msg);
        })
        .catch(() => {});
    },
  },
};
</script>
