<template>
  <div>
    <el-card class="card-top">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.System_Switch') }}</span>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :md="12">
            <el-card>
                <div slot="header">
                    <span class="switch-item-title">{{ $t('m.Judge_Config') }}</span>
                </div>
                <el-form
                    :label-position="labelPosition"
                    label-width="320px"
                    ref="form"
                    :model="switchConfig"
                >
                    <el-form-item :label="$t('m.Open_Public_Judge')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.openPublicJudge"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Open_Group_Judge')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.openGroupJudge"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Open_Contest_Judge')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.openContestJudge"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Hide_Non_Contest_Submission_Code')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.hideNonContestSubmissionCode"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Non_Contest_Submission_Frequency')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultSubmitInterval" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                </el-form>
                <el-button
                  type="primary"
                  :loading="loading"
                  style="margin-top:15px"
                  @click.native="saveSwitchConfig"
                  size="small"
                  ><i class="fa fa-save"> {{ $t('m.Save') }}</i></el-button
                >
            </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
            <el-card class="card-top">
                <div slot="header">
                    <span class="switch-item-title">{{ $t('m.Discussion_Config') }}</span>
                </div>
                <el-form
                    :label-position="labelPosition"
                    label-width="320px"
                    ref="form"
                    :model="switchConfig"
                >
                     <el-form-item :label="$t('m.Open_Public_Discussion')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.openPublicDiscussion"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Open_Group_Discussion')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.openGroupDiscussion"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Open_Contest_Comment')" label-width="320px">
                        <el-switch
                            v-model="switchConfig.openContestComment"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                        >
                        </el-switch>
                    </el-form-item>
                    <el-form-item :label="$t('m.Number_of_AC_required_for_ordinary_users_to_post')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultCreateDiscussionACInitValue" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                    <el-form-item :label="$t('m.Number_of_posts_that_users_can_create_per_day')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultCreateDiscussionDailyLimit" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                    <el-form-item :label="$t('m.Number_of_AC_required_for_Comment_of_ordinary_users')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultCreateCommentACInitValue" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                </el-form>
                <el-button
                  type="primary"
                  :loading="loading"
                  style="margin-top:15px"
                  @click.native="saveSwitchConfig"
                  size="small"
                  ><i class="fa fa-save"> {{ $t('m.Save') }}</i></el-button
                >
            </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
            <el-card class="card-top">
                <div slot="header">
                    <span class="switch-item-title">{{ $t('m.Group_Config') }}</span>
                </div>
                <el-form
                    :label-position="labelPosition"
                    label-width="320px"
                    ref="form"
                    :model="switchConfig"
                >
                    <el-form-item :label="$t('m.Number_of_Groups_that_users_can_create_per_day')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultCreateGroupDailyLimit" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                    <el-form-item :label="$t('m.Total_number_of_groups_that_ordinary_users_can_create')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultCreateGroupLimit" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                    <el-form-item :label="$t('m.Number_of_AC_required_for_ordinary_users_to_create_group')" label-width="320px">
                        <el-input-number
                         v-model="switchConfig.defaultCreateGroupACInitValue" 
                         :min="0">
                         </el-input-number>
                    </el-form-item>
                </el-form>
                <el-button
                  type="primary"
                  :loading="loading"
                  style="margin-top:15px"
                  @click.native="saveSwitchConfig"
                  size="small"
                  ><i class="fa fa-save"> {{ $t('m.Save') }}</i></el-button
                >
            </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="15">
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount 
            :usernameList.sync="switchConfig.hduUsernameList"
            :passwordList.sync="switchConfig.hduPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="HDU">
          </RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount 
            :usernameList.sync="switchConfig.cfUsernameList"
            :passwordList.sync="switchConfig.cfPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="Codeforces">
          </RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount 
            :usernameList.sync="switchConfig.pojUsernameList"
            :passwordList.sync="switchConfig.pojPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="POJ">
          </RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount 
            :usernameList.sync="switchConfig.atcoderUsernameList"
            :passwordList.sync="switchConfig.atcoderPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="Atcoder">
          </RemoteJudgeAccount>
        </el-col>
        <el-col :xs="24" :md="12" style="margin-top: 15px;">
          <RemoteJudgeAccount 
            :usernameList.sync="switchConfig.spojUsernameList"
            :passwordList.sync="switchConfig.spojPasswordList"
            :loading.sync="loading"
            @saveSwitchConfig="saveSwitchConfig"
            OJ="SPOJ">
          </RemoteJudgeAccount>
        </el-col>
      </el-row>
    </el-card>
</div>
</template>
<script>
import api from '@/common/api';
import myMessage from '@/common/message';
const RemoteJudgeAccount = () => import('@/components/admin/RemoteJudgeAccount.vue');
export default {
  name: 'SystemConfig',
  components:{
    RemoteJudgeAccount
  },
  data() {
    return {
      loading: false,
      switchConfig: {},
      labelPosition:'left'
    };
  },
  created(){
    let screenWidth = window.screen.width;
    if (screenWidth < 500) {
      this.labelPosition = 'top';
    } 
  },
  mounted() {
    api.admin_getSwitchConfig().then((res) => {
        this.switchConfig = res.data.data;
    });
  },
  methods: {
    saveSwitchConfig() {
      this.loading = true;
      api.admin_saveSwitchConfig(this.switchConfig).then(
        (res) => {
          myMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.loading = false;
        },
        () => {
          this.loading = false;
        }
      );
    },
  },
};
</script>
<style scoped>
.switch-item-title{
  font-size: 18px;
  font-weight: bolder;
}
@media screen and (max-width: 992px) {
  .card-top{
    margin-top: 15px;
  }
}
</style>
