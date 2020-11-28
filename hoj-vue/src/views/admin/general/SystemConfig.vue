<template>
  <div>
    <el-card>
      <div slot="header"><span class="panel-title home-title">SMTP Config</span></div>
      <el-form label-position="left" label-width="80px" :model="smtp">
        <el-row :gutter="20">
          <el-col :md="12" :xs="24">
            <el-form-item label="Server" required label-width="80px">
              <el-input v-model="smtp.server" placeholder="SMTP Server Address"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Port" required label-width="80px">
              <el-input type="number" v-model="smtp.port" placeholder="SMTP Server Port"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Email" required label-width="80px">
              <el-input v-model="smtp.email" placeholder="Account Used To Send Email"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="Password" label-width="80px" required>
              <el-input v-model="smtp.password" type="password" placeholder="SMTP Server Password"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="SSL">
              <el-switch
                v-model="smtp.tls">
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-button type="primary" @click="saveSMTPConfig" size="small">Save</el-button>
      <el-button type="warning" @click="testSMTPConfig"
                 v-if="saved" :loading="loadingBtnTest" size="small">Send Test Email</el-button>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header"><span class="panel-title home-title">Website Config</span></div>
      <el-form label-position="left" label-width="100px" ref="form" :model="websiteConfig">
        <el-row :gutter="20">
          <el-col :md="8" :xs="24">
            <el-form-item label="Base Url" required>
              <el-input v-model="websiteConfig.website_base_url" placeholder="Website Base Url"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Name" required>
              <el-input v-model="websiteConfig.website_name" placeholder="Website Name"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Short Name" required>
              <el-input v-model="websiteConfig.website_name_shortcut" placeholder="Website Name Shortcut"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
            <el-form-item label="Footer" required>
              <el-input type="textarea" :autosize="{ minRows: 2, maxRows: 4}" v-model="websiteConfig.website_footer"
                        placeholder="Website Footer HTML"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="24" :xs="24">
              <el-form-item label="Allow Register" label-width="120px">
                <el-switch
                  v-model="websiteConfig.allow_register"
                  active-color="#13ce66"
                  inactive-color="#ff4949">
                </el-switch>
              </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-button type="primary"  @click.native="saveWebsiteConfig" size="small">Save</el-button>
    </el-card>
  </div>
</template>

<script>
  import api from '@/common/api'
  import myMessage from '@/common/message'
  export default {
    name: 'SystemConfig',
    data () {
      return {
        init: false,
        saved: false,
        loadingBtnTest: false,
        smtp: {
          server: 'smtp.example.com',
          port: 25,
          password: '',
          email: 'email@example.com',
          tls: true
        },
        websiteConfig: {}
      }
    },
    mounted () {
      // api.getSMTPConfig().then(res => {
      //   if (res.data.data) {
      //     this.smtp = res.data.data
      //   } else {
      //     this.init = true
      //     myMessage.warning('请先添加STMP的配置！')
      //   }
      // })
      // api.getWebsiteConfig().then(res => {
      //   this.websiteConfig = res.data.data
      // }).catch(() => {
      // })
    },
    methods: {
      saveSMTPConfig () {
        if (!this.init) {
          api.editSMTPConfig(this.smtp).then(() => {
            this.saved = true
          }, () => {
          })
        } else {
          api.createSMTPConfig(this.smtp).then(() => {
            this.saved = true
          }, () => {
          })
        }
      },
      testSMTPConfig () {
        this.$prompt('Please input your email', '', {
          inputPattern: /[\w!#$%&'*+/=?^_`{|}~-]+(?:\.[\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\w](?:[\w-]*[\w])?\.)+[\w](?:[\w-]*[\w])?/,
          inputErrorMessage: 'Error email format'
        }).then(({value}) => {
          this.loadingBtnTest = true
          api.testSMTPConfig(value).then(() => {
            this.loadingBtnTest = false
          }, () => {
            this.loadingBtnTest = false
          })
        }).catch(() => {
        })
      },
      saveWebsiteConfig () {
        api.editWebsiteConfig(this.websiteConfig).then(() => {
        }).catch(() => {
        })
      }
    }
  }
</script>
